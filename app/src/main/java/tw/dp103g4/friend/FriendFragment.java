package tw.dp103g4.friend;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;


public class FriendFragment extends Fragment {
    private static final String TAG = "TAG_FriendShipFragment";
    private RecyclerView rvFriends;
    private RecyclerView rvFriendMsg;
    private Activity activity;
    private CommonTask friendShipGetAllTask;
    private CommonTask friendShipDeleteTask;
    private CommonTask talkGetAllTask;
    private CommonTask isReadTask;
    private ImageTask friendImageTask;
    private List<FriendShip> friendShips;
    private List<NewestTalk> newestTalks;
    private Button btInsert;
    private int userId = 3;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        activity.setTitle("訊息");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = view.findViewById(R.id.svFriends);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        rvFriends = view.findViewById(R.id.rvFriends);
        rvFriendMsg = view.findViewById(R.id.rvFriendMsg);
        btInsert = view.findViewById(R.id.btInsert);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFriends.setLayoutManager(linearLayoutManager);
        friendShips = getFriendShip();
        showFriendShip(friendShips);

        rvFriendMsg.setLayoutManager(new LinearLayoutManager(activity));
        newestTalks = getNewestTalk();
        showNewestTalk(newestTalks);

        btInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_friendFragment_to_friendMsgFragment);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showFriendShip(friendShips);
                    showNewestTalk(newestTalks);
                } else {
                    List<FriendShip> searchFriendShip = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (FriendShip friendShip : friendShips) {
                        if (friendShip.getAccount().toUpperCase().contains(newText.toUpperCase())) {
                            searchFriendShip.add(friendShip);
                        }
                    }
                    List<NewestTalk> searchNewestTalks = new ArrayList<>();
                    for (NewestTalk newestTalk : newestTalks) {
                        if (newestTalk.getAccount().toUpperCase().contains(newText.toUpperCase())) {
                            searchNewestTalks.add(newestTalk);
                        }
                    }
                    showFriendShip(searchFriendShip);
                    showNewestTalk(searchNewestTalks);
                }
                return true;
            }
        });
    }

    private void isRead(int friendId) {
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/TalkServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "updateIsRead");
            jsonObject.addProperty("senderId", friendId);
            jsonObject.addProperty("receiverId", userId);
            int count = 0;
            try {
                isReadTask = new CommonTask(url, jsonObject.toString());
                String result = isReadTask.execute().get();
                count = Integer.valueOf(result.trim());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
//                Common.showToast(activity, R.string.txtIsReadFail);
            } else {
//                Common.showToast(activity, R.string.txtIsReadSuccess);
            }
        } else {
            Common.showToast(activity, R.string.txtNoNetwork);
        }
    }


    private List<FriendShip> getFriendShip() {

        List<FriendShip> friendShips = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/FriendShipServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllFriend");
            jsonObject.addProperty("userId", userId);
            String jsonOut = jsonObject.toString();
            friendShipGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = friendShipGetAllTask.execute().get();
                Type listType = new TypeToken<List<FriendShip>>() {
                }.getType();
                friendShips = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.txtNoNetwork);
        }
        return friendShips;
    }

    private void showFriendShip(List<FriendShip> friendShips) {
        if (friendShips == null || friendShips.isEmpty()) {
            Common.showToast(activity, R.string.txtNoFriendShipFound);
            return;
        }
        FriendShipAdapter friendShipAdapter = (FriendShipAdapter) rvFriends.getAdapter();
        if (friendShipAdapter == null) {
            rvFriends.setAdapter(new FriendShipAdapter(activity, friendShips));
        } else {
            friendShipAdapter.setFriendShip(friendShips);
            friendShipAdapter.notifyDataSetChanged();
        }
    }

    private List<NewestTalk> getNewestTalk() {

        List<NewestTalk> talks = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/TalkServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getNewestTalk");
            jsonObject.addProperty("userId", userId);
            String jsonOut = jsonObject.toString();
            talkGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = talkGetAllTask.execute().get();
                Type listType = new TypeToken<List<NewestTalk>>() {
                }.getType();
                talks = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.txtNoNetwork);
        }
        return talks;
    }

    private void showNewestTalk(List<NewestTalk> newestTalks) {
        if (newestTalks == null || newestTalks.isEmpty()) {
            Common.showToast(activity, R.string.txtNoTalkFound);
            return;
        }
        NewestTalkAdapter newestTalkAdapter = (NewestTalkAdapter) rvFriendMsg.getAdapter();
        if (newestTalkAdapter == null) {
            rvFriendMsg.setAdapter(new NewestTalkAdapter(activity, newestTalks));
        } else {
            newestTalkAdapter.setTalks(newestTalks);
            newestTalkAdapter.notifyDataSetChanged();
        }
    }

    private class FriendShipAdapter extends RecyclerView.Adapter<FriendShipAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<FriendShip> friendShips;
        private int imageSize;

        FriendShipAdapter(Context context, List<FriendShip> friendShips) {
            layoutInflater = LayoutInflater.from(context);
            this.friendShips = friendShips;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setFriendShip(List<FriendShip> friendShips) {
            this.friendShips = friendShips;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView tvUserName;


            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivFriend);
                tvUserName = itemView.findViewById(R.id.tvFriend);

            }
        }

        @Override
        public int getItemCount() {
            return friendShips.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_friend_s, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final FriendShip friendShip = friendShips.get(position);
            String url = Common.URL_SERVER + "UserServlet";
            int id = friendShip.getFriendId();
            friendImageTask = new ImageTask(url, id, imageSize, holder.imageView);
            friendImageTask.execute();
            holder.tvUserName.setText(friendShip.getAccount());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRead(friendShip.getFriendId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("friendId", friendShip.getFriendId());
                    bundle.putString("account", friendShip.getAccount());

                    Navigation.findNavController(v).navigate(R.id.action_friendFragment_to_friendMsgFragment, bundle);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    PopupMenu menu = new PopupMenu(activity, view, Gravity.END);
                    menu.inflate(R.menu.friendship_menu);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.delete:
                                    if (Common.networkConnected(activity)) {
                                        String url = Common.URL_SERVER + "/FriendShipServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "friendShipDelete");
                                        jsonObject.addProperty("idOne", friendShip.getIdOne());
                                        jsonObject.addProperty("idTwo", friendShip.getIdTwo());
                                        int count = 0;
                                        try {
                                            friendShipDeleteTask = new CommonTask(url, jsonObject.toString());
                                            String result = friendShipDeleteTask.execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.showToast(activity, R.string.txtDeleteFail);
                                        } else {
                                            friendShips.remove(friendShip);
                                            FriendShipAdapter.this.notifyDataSetChanged();
                                            // 外面spots也必須移除選取的spot
                                            FriendFragment.this.friendShips.remove(friendShip);
                                            Common.showToast(activity, R.string.txtDeleteSuccess);
                                        }
                                    } else {
                                        Common.showToast(activity, R.string.txtNoNetwork);
                                    }
                            }
                            return true;
                        }
                    });
                    menu.show();
                    return true;
                }
            });
        }
    }

    private class NewestTalkAdapter extends RecyclerView.Adapter<NewestTalkAdapter.MsgViewHolder> {
        private LayoutInflater layoutInflater;
        private List<NewestTalk> newestTalks;
        private int imageSize;

        NewestTalkAdapter(Context context, List<NewestTalk> talks) {
            layoutInflater = LayoutInflater.from(context);
            this.newestTalks = talks;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setTalks(List<NewestTalk> newestTalks) {
            this.newestTalks = newestTalks;
        }


        class MsgViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFriend;
            TextView tvFriendName;
            TextView tvContent;

            MsgViewHolder(View itemView) {
                super(itemView);
                ivFriend = itemView.findViewById(R.id.ivFriend);
                tvFriendName = itemView.findViewById(R.id.tvFriend);
                tvContent = itemView.findViewById(R.id.tvMsg);

            }
        }

        @Override
        public int getItemCount() {
            return newestTalks.size();
        }

        @NonNull
        @Override
        public NewestTalkAdapter.MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_friend, parent, false);
            return new NewestTalkAdapter.MsgViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull NewestTalkAdapter.MsgViewHolder holder, int position) {
            final NewestTalk newestTalk = newestTalks.get(position);
            String url = Common.URL_SERVER + "UserServlet";
            int id = newestTalk.getSenderId();
            friendImageTask = new ImageTask(url, id, imageSize, holder.ivFriend);
            friendImageTask.execute();
            holder.tvFriendName.setText(newestTalk.getAccount());
            holder.tvContent.setText(newestTalk.getContent());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isRead(newestTalk.getSenderId());
                    Bundle bundle = new Bundle();
                    bundle.putInt("friendId", newestTalk.getSenderId());
                    bundle.putString("account", newestTalk.getAccount());

                    Navigation.findNavController(v).navigate(R.id.action_friendFragment_to_friendMsgFragment, bundle);
                }
            });

        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (friendShipGetAllTask != null) {
            friendShipGetAllTask.cancel(true);
            friendShipGetAllTask = null;
        }

        if (friendImageTask != null) {
            friendImageTask.cancel(true);
            friendImageTask = null;
        }

        if (friendShipDeleteTask != null) {
            friendShipDeleteTask.cancel(true);
            friendShipDeleteTask = null;
        }
    }


}