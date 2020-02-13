package tw.dp103g4.friend;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import tw.dp103g4.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;


public class FriendMsgFragment extends Fragment {
    private static final String TAG = "TAG_FriendMsgFragment";
    private RecyclerView rvMsg;
    private MainActivity activity;
    private CommonTask msgGetAllTask;
    private ImageTask talkImageTask;
    private List<Talk> talks;
    private EditText etMsg;
    private ImageButton ibtMsg;
    private int friendId;
    private String account;
    private int userId = 2;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_msg, container, false);

        //點擊空白處鍵盤消失
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                getActivity().onTouchEvent(motionEvent);
                return false;
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getBottomNavigationView().setVisibility(View.GONE);
        rvMsg = view.findViewById(R.id.rvMsg);
        etMsg = view.findViewById(R.id.etMsg);
        ibtMsg = view.findViewById(R.id.ibtMsg);
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        //拿bundle key="friend"
        final NavController navController = Navigation.findNavController(view);
        Bundle bundle = getArguments();
        if (bundle == null) {
            Common.showToast(activity, R.string.textNoFriendBundle);
            navController.popBackStack();
            return;
        }
        account = bundle.getString("account");
        toolbar.setTitle(account);

        friendId = bundle.getInt("friendId");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setStackFromEnd(true);
        rvMsg.setLayoutManager(linearLayoutManager);

        talks = getTalks();
        showTalks(talks);

        ibtMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etMsg.getText().toString().trim();
                int partyId = -1;
                if (content.equals("")) {
                    return;
                }
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "TalkServlet";
                    Talk talk = new Talk(friendId, userId, partyId, content);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "talkInsert");
                    jsonObject.addProperty("talk", new Gson().toJson(talk));

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
                        talks = getTalks();
                        showTalks(talks);
                        etMsg.setText("");
//                        Common.showToast(getActivity(), R.string.textInsertSuccess);
                    }
                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

    }


    private class TalkAdapter extends RecyclerView.Adapter<FriendMsgFragment.TalkAdapter.TalkViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Talk> talks;
        private int imageSize;

        TalkAdapter(Context context, List<Talk> talks) {
            layoutInflater = LayoutInflater.from(context);
            this.talks = talks;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setTalks(List<Talk> talks) {
            this.talks = talks;
        }


        class TalkViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFriend;
            TextView tvMsg, tvMsgSend, tvTimeSend, tvRead, tvTime, tvOldTime;
            ConstraintLayout receiveLayout, sendLayout;

            TalkViewHolder(View itemView) {
                super(itemView);
                ivFriend = itemView.findViewById(R.id.ivFriend);
                tvMsg = itemView.findViewById(R.id.tvMsg);
                tvMsgSend = itemView.findViewById(R.id.tvMsgSend);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvTimeSend = itemView.findViewById(R.id.tvTimeSend);
                tvOldTime = itemView.findViewById(R.id.tvOldTime);
                tvRead = itemView.findViewById(R.id.tvRead);
                receiveLayout = itemView.findViewById(R.id.receiveLayout);
                sendLayout = itemView.findViewById(R.id.sendLayout);

            }
        }

        @Override
        public int getItemCount() {
            return talks.size();
        }

        @NonNull
        @Override
        public TalkAdapter.TalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_msg_receive, parent, false);
            return new FriendMsgFragment.TalkAdapter.TalkViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull TalkViewHolder holder, int position) {
            final Talk talk = talks.get(position);
            String url = Common.URL_SERVER + "UserServlet";
            int id = talk.getSenderId();
            talkImageTask = new ImageTask(url, id, imageSize, holder.ivFriend);
            talkImageTask.execute();
            int senderId = talk.getSenderId();
            //如果日期不相同就顯示日期
            Calendar msgTime = Calendar.getInstance();
            msgTime.setTime(talk.getTime());
            Calendar checkTime = Calendar.getInstance();
            checkTime.setTime(talks.get(position == 0 ? 0 : position - 1).getTime());
            Calendar nowTime = Calendar.getInstance();
            String time = new SimpleDateFormat("HH:mm").format(talk.getTime());
            if (position == 0 || checkTime.getTime().getTime() / 86400000 <
                    msgTime.getTime().getTime() / 86400000) {
                Log.d(TAG, String.valueOf(position));
                if (msgTime.get(Calendar.YEAR) < nowTime.get(Calendar.YEAR)) {
                    String strOldTime = new SimpleDateFormat("yyyy-MM-dd").format(msgTime.getTime());
                    holder.tvOldTime.setText(strOldTime);
                } else {
                    Calendar diffDay = Calendar.getInstance();
                    diffDay.add(Calendar.DAY_OF_MONTH,-1);
                    if(msgTime.get(Calendar.DAY_OF_MONTH) == nowTime.get(Calendar.DAY_OF_MONTH)){
                        holder.tvOldTime.setText("今天");
                    }else if(msgTime.get(Calendar.DAY_OF_MONTH) == diffDay.get(Calendar.DAY_OF_MONTH)){
                        holder.tvOldTime.setText("昨天");
                    }else {
                        String strOldTime = new SimpleDateFormat("MM-dd E").format(talk.getTime());
                        holder.tvOldTime.setText(strOldTime);
                    }
                }
            } else {
                holder.tvOldTime.setText("");
            }
            if (senderId == userId) {
                holder.receiveLayout.setVisibility(View.GONE);
                holder.sendLayout.setVisibility(View.VISIBLE);
                holder.tvMsgSend.setText(talk.getContent());
                holder.tvTimeSend.setText(time);
                if (talk.getIsRead()) {
                    holder.tvRead.setText(R.string.textIsRead);
                }else{
                    holder.tvRead.setText("");
                }
            } else {
                holder.sendLayout.setVisibility(View.GONE);
                holder.receiveLayout.setVisibility(View.VISIBLE);
                holder.tvMsg.setText(talk.getContent());
                holder.tvTime.setText(time);
            }

        }

    }


    private List<Talk> getTalks() {
        List<Talk> talks = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/TalkServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            jsonObject.addProperty("userId", userId);
            jsonObject.addProperty("friendId", friendId);
            String jsonOut = jsonObject.toString();
            msgGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = msgGetAllTask.execute().get();
                Type listType = new TypeToken<List<Talk>>() {
                }.getType();
                talks = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return talks;
    }

    private void showTalks(List<Talk> talks) {
        if (talks == null || talks.isEmpty()) {
            Common.showToast(activity, R.string.textNoTalkFound);
            return;
        }
        TalkAdapter talkAdapter = (TalkAdapter) rvMsg.getAdapter();

        if (talkAdapter == null) {
            rvMsg.setAdapter(new TalkAdapter(activity, talks));
        } else {
            talkAdapter.setTalks(talks);
            rvMsg.scrollToPosition(talkAdapter.getItemCount()-1);
            talkAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}
