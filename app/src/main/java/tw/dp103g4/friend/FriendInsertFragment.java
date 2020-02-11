package tw.dp103g4.friend;


import android.accounts.Account;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;


public class FriendInsertFragment extends Fragment {
    private static final String TAG = "TAG_FInsertFragment";
    private MainActivity activity;
    private RecyclerView rvAddFriend;
    private CommonTask insertFriendTask,updateIsInviteTask;
    private ImageTask insertFriendImageTask;
    private Button btQRCode;
    private ImageButton ibtSearch;
    private EditText etSearch;
    private List<FriendShip> friendShips;
    private User user = null;
    private int userId = 2;
    private int count = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_insert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getBottomNavigationView().setVisibility(View.GONE);
        rvAddFriend = view.findViewById(R.id.rvAddFriend);
        etSearch = view.findViewById(R.id.etSearch);
        btQRCode = view.findViewById(R.id.btQR);
        ibtSearch = view.findViewById(R.id.ibtSearch);

        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("加入好友");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        rvAddFriend.setLayoutManager(new LinearLayoutManager(activity));

        friendShips = getFriendShips();
        showFriendShips(friendShips);

        ibtSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "UserServlet";
                    String userAccound = etSearch.getText().toString();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "searchUser");
                    jsonObject.addProperty("account", userAccound);
                    String jsonOut = jsonObject.toString();
                    insertFriendTask = new CommonTask(url, jsonOut);
                    try {
                        String jsonIn = insertFriendTask.execute().get();
                        Type listType = new TypeToken<User>(){}.getType();
                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                        user = gson.fromJson(jsonIn, listType);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (user==null) {
                        Common.showToast(getActivity(), R.string.textSearchUserFail);
                    } else {
                            if(getIsInvite(userId,user.getUserId())){
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(user.getAccount() + "已經是好友囉！")
                                        .setNegativeButton("確定",null).create()
                                        .show();
                            }else{
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("是否要邀請 " + user.getAccount() + " 成為好友？")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Common.networkConnected(activity)) {
                                            String url = Common.URL_SERVER + "/FriendShipServlet";
                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.addProperty("action", "friendShipInsert");
                                            jsonObject.addProperty("idOne", userId);
                                            jsonObject.addProperty("idTwo", user.getUserId());
                                            int count = 0;
                                            try {
                                                insertFriendTask = new CommonTask(url, jsonObject.toString());
                                                String result = insertFriendTask.execute().get();
                                                count = Integer.valueOf(result.trim());
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                            }
                                            if (count == 0) {
                                                Common.showToast(activity, R.string.textFriendShipInsertFail);
                                            } else {
                                                Common.showToast(activity, R.string.textFriendShipInsertSuccess);
                                            }
                                        } else {
                                            Common.showToast(activity, R.string.textNoNetwork);
                                        }
                                    }
                                }).setNegativeButton("取消",null).create()
                                .show();}
                        Common.showToast(getActivity(), R.string.textSearchUserSuccess);
                    }
                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }

            }
        });

        btQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private class InsertFriendAdapter extends RecyclerView.Adapter<FriendInsertFragment.InsertFriendAdapter.InsertFriendViewHolder>{
        private LayoutInflater layoutInflater;
        private List<FriendShip> friendShips;
        private int imageSize;

        InsertFriendAdapter(Context context, List<FriendShip> FriendShips) {
            layoutInflater = LayoutInflater.from(context);
            this.friendShips= FriendShips;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setFriendShips(List<FriendShip> friendShips) {
            this.friendShips = friendShips;
        }

        class InsertFriendViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFriend;
            TextView tvFriend;
            Button btAgree, btDelete;

            public InsertFriendViewHolder(@NonNull View itemView) {
                super(itemView);
                ivFriend = itemView.findViewById(R.id.ivFriend);
                tvFriend = itemView.findViewById(R.id.tvFriend);
                btAgree = itemView.findViewById(R.id.btAgree);
                btDelete = itemView.findViewById(R.id.btDelete);
            }
        }
        @Override
        public int getItemCount() {
            return friendShips.size();
        }

        @NonNull
        @Override
        public InsertFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_friend_insert, parent, false);
            return new InsertFriendViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull InsertFriendViewHolder holder, int position) {
            final FriendShip friendShip = friendShips.get(position);
            String url = Common.URL_SERVER + "UserServlet";
            int id = friendShip.getFriendId();
            insertFriendImageTask = new ImageTask(url, id, imageSize, holder.ivFriend);
            insertFriendImageTask.execute();
            holder.tvFriend.setText(friendShip.getAccount());
            holder.btAgree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertFriend(friendShip.getFriendId(),userId);
                    if(count!= 0){
                    friendShips.remove(friendShip);
                    InsertFriendAdapter.this.notifyDataSetChanged();
                    // 外面List也必須移除選取的List
                    FriendInsertFragment.this.friendShips.remove(friendShip);}
                }
            });
            holder.btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFriendShip(friendShip.getFriendId(),userId);
                    if(count!= 0){
                        friendShips.remove(friendShip);
                        InsertFriendAdapter.this.notifyDataSetChanged();
                        // 外面List也必須移除選取的List
                        FriendInsertFragment.this.friendShips.remove(friendShip);}
                }
            });

        }
    }
    private boolean getIsInvite(int userId, int friendId){
        boolean isInvite = false;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/FriendShipServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "isInviteById");
            jsonObject.addProperty("idOne", userId);
            jsonObject.addProperty("idTwo", friendId);
            String jsonOut = jsonObject.toString();
            insertFriendTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = insertFriendTask.execute().get();
                Type listType = new TypeToken<Boolean>() {
                }.getType();
                isInvite = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return isInvite;
    }


    private List<FriendShip> getFriendShips(){
        List<FriendShip> friendShips = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/FriendShipServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllInvite");
            jsonObject.addProperty("userId", userId);
            String jsonOut = jsonObject.toString();
            insertFriendTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = insertFriendTask.execute().get();
                Type listType = new TypeToken<List<FriendShip>>() {
                }.getType();
                friendShips = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return friendShips;
    }

    private void showFriendShips(List<FriendShip> friendShips) {
        if (friendShips== null || friendShips.isEmpty()) {
            Common.showToast(activity, R.string.textNoFriendShipFound);
            return;
        }
        InsertFriendAdapter insertFriendAdapter = (InsertFriendAdapter) rvAddFriend.getAdapter();

        if (insertFriendAdapter == null) {
            rvAddFriend.setAdapter(new FriendInsertFragment.InsertFriendAdapter(activity, friendShips));
        } else {
            insertFriendAdapter.setFriendShips(friendShips);
            rvAddFriend.scrollToPosition(insertFriendAdapter.getItemCount()-1);
            insertFriendAdapter.notifyDataSetChanged();
        }
    }

    private int insertFriend(int userOne, int userTwo){
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/FriendShipServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "updateIsInvite");
            jsonObject.addProperty("idOne", userOne);
            jsonObject.addProperty("idTwo", userTwo);
            count = 0;
            try {
                updateIsInviteTask = new CommonTask(url, jsonObject.toString());
                String result = updateIsInviteTask.execute().get();
                count = Integer.valueOf(result.trim());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            //是否有更改已讀欄位
            if (count == 0) {
                Common.showToast(activity, R.string.txtUpdateIsInviteFail);
            } else {
                Common.showToast(activity, R.string.txtUpdateIsInviteSuccess);
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return count;
    }

    private int deleteFriendShip(int userOne, int userTwo){
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/FriendShipServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "friendShipDelete");
            jsonObject.addProperty("idOne", userOne);
            jsonObject.addProperty("idTwo", userTwo);
            count = 0;
            try {
                updateIsInviteTask = new CommonTask(url, jsonObject.toString());
                String result = updateIsInviteTask.execute().get();
                count = Integer.valueOf(result.trim());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            //是否有更改已讀欄位
            if (count == 0) {
                Common.showToast(activity, R.string.txtDeleteFriendShipFail);
            } else {
                Common.showToast(activity, R.string.txtDeleteFriendShipSuccess);
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return count;
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}
