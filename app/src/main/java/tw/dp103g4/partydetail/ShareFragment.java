package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.friend.ChatMsg;
import tw.dp103g4.friend.FriendFragment;
import tw.dp103g4.friend.FriendMsgFragment;
import tw.dp103g4.friend.FriendShip;
import tw.dp103g4.friend.NewestTalk;
import tw.dp103g4.friend.Talk;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.content.Context.MODE_PRIVATE;
import static tw.dp103g4.main_android.Common.chatWebSocketClient;

public class ShareFragment extends Fragment {
    private static final String TAG = "ShareFragment";

    private Activity activity;
    private SearchView searchView;
    private RecyclerView rvFriends;
    private SwipeRefreshLayout rlFriends;
    private List<FriendShip> friendShips;
    private List<FriendShip> checked;
    private CommonTask friendShipGetAllTask;
    private ImageTask friendImageTask;
    private int userId, partyId;
    private Party party;
    private Bundle bundle;
    private Button btShareOK, btShareRe;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        checked = new ArrayList<FriendShip>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("party") == null) {
            navController.popBackStack();
            return;
        }
        party = (Party) bundle.getSerializable("party");
        partyId = party.getId();

        searchView = view.findViewById(R.id.svShare);
        rvFriends = view.findViewById(R.id.rvShare);
        rlFriends = view.findViewById(R.id.rlShare);
        btShareOK = view.findViewById(R.id.btShareOK);
        btShareRe = view.findViewById(R.id.btShareRe);

        rvFriends.setLayoutManager(new LinearLayoutManager(activity));

        friendShips = getFriendShip();
        showFriendShip(friendShips);

        btShareOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked.size() == 0)
                    return;

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "TalkServlet";
                    List<Talk> talks = new ArrayList<Talk>();
                    for (FriendShip friendShip:checked) {
                        Talk talk = new Talk(friendShip.getFriendId(), userId, partyId, "邀請您參加"+party.getName()+"活動");
                        talks.add(talk);
                    }
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "talksInsert");
                    jsonObject.addProperty("talks", new Gson().toJson(talks));
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result.trim());
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        Common.showToast(getActivity(), R.string.textInsertFail);
                    } else {
                        navController.popBackStack();
                    }

                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

        btShareRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked.clear();
                friendShips = getFriendShip();

                if (friendShips == null || friendShips.isEmpty()) {
                    return;
                }
                rvFriends.setAdapter(new FriendShipAdapter(activity, friendShips));
            }
        });

        rlFriends.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                friendShips = getFriendShip();
                showFriendShip(friendShips);
                rlFriends.setRefreshing(false);
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
                } else {
                    List<FriendShip> searchFriendShip = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (FriendShip friendShip : friendShips) {
                        if (friendShip.getAccount().toUpperCase().contains(newText.toUpperCase())) {
                            searchFriendShip.add(friendShip);
                        }
                    }
                    showFriendShip(searchFriendShip);
                }
                return true;
            }
        });
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
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return friendShips;
    }

    private void showFriendShip(List<FriendShip> friendShips) {
        if (friendShips == null || friendShips.isEmpty()) {
//            Common.showToast(activity, R.string.textNoFriendShipFound);
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
            CheckBox checkBox;


            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivFriend);
                tvUserName = itemView.findViewById(R.id.tvFriend);
                checkBox = itemView.findViewById(R.id.checkBox);

            }
        }

        @Override
        public int getItemCount() {
            return friendShips.size();
        }

        @NonNull
        @Override
        public FriendShipAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_share, parent, false);
            return new FriendShipAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final FriendShipAdapter.MyViewHolder holder, int position) {
            final FriendShip friendShip = friendShips.get(position);
            String url = Common.URL_SERVER + "UserServlet";
            int id = friendShip.getFriendId();
            friendImageTask = new ImageTask(url, id, imageSize, holder.imageView);
            friendImageTask.execute();
            holder.tvUserName.setText(friendShip.getAccount());
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checked.add(friendShip);
                    } else
                        checked.remove(friendShip);
                }
            });
        }
    }


}
