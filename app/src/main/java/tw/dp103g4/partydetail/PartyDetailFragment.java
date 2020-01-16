package tw.dp103g4.partydetail;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.CoverImageTask;


public class PartyDetailFragment extends Fragment {
    private MainActivity activity;
    private TextView tvName, tvTime, tvPostEndTime, tvOwner, tvParticipant, tvLocation, tvAddress, tvContent;
    private RecyclerView rvMessage;
    private ImageView ivCover, ivOwner, ivParticipant, ivLocation;
    private ImageButton ibSend;
    private EditText etInput;
    private Button btLike, btIn, btShare, bt;
    private List<PartyMessage> msgList;
    private CommonTask getMsgListTask;
    private CoverImageTask coverImageTask;
    private ScrollView scrollView;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final String TAG = "TAG_PartyDetail";

    // bundle
//    final int partyId = 138;
    final int userId = 2;

    public PartyDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_party_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getBottomNavigationView().setVisibility(View.GONE);
        final NavController navController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });
        scrollView = view.findViewById(R.id.scrollView);
        tvName = view.findViewById(R.id.tvName);
        tvTime = view.findViewById(R.id.tvTime);
        tvPostEndTime = view.findViewById(R.id.tvPostEndTime);
        tvOwner = view.findViewById(R.id.tvOwner);
        tvParticipant = view.findViewById(R.id.tvParticipant);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvContent = view.findViewById(R.id.tvContent);
        rvMessage = view.findViewById(R.id.rvMessage);
        ivCover = view.findViewById(R.id.ivCover);
        ivOwner = view.findViewById(R.id.ivOwner);
        ivParticipant = view.findViewById(R.id.ivParticipant);
        ivLocation = view.findViewById(R.id.ivLocation);
        btLike = view.findViewById(R.id.btLike);
        btIn = view.findViewById(R.id.btIn);
        btShare = view.findViewById(R.id.btShare);
        ibSend = view.findViewById(R.id.ibSend);
        etInput = view.findViewById(R.id.etInput);
        rvMessage = view.findViewById(R.id.rvMessage);

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            Common.showToast(activity, R.string.textNoPartiesFound);
            navController.popBackStack();
            return;
        }
        final int partyId = bundle.getInt("partyId");

        Party party = getParty(partyId);

        if (party != null) {
            int imageSize = (getResources().getDisplayMetrics().widthPixels > 200)?getResources().getDisplayMetrics().widthPixels:200;
            String url = Common.URL_SERVER + "/PartyServlet";
            coverImageTask = new CoverImageTask(url, partyId, imageSize, ivCover);
            coverImageTask.execute();

            tvName.setText(party.getName());
            String text = new SimpleDateFormat("E M月d日 H:m").format(party.getStartTime());
            String startDateString = new SimpleDateFormat("YYYYMMdd").format(party.getStartTime());
            String endDateString = new SimpleDateFormat("YYYYMMdd").format(party.getEndTime());
            if (startDateString.compareTo(endDateString) == 0)
                text += new SimpleDateFormat(" ~ H:m").format(party.getEndTime());
            else
                text += new SimpleDateFormat(" ~ E M月d日 H:m").format(party.getEndTime());
            tvTime.setText(text);
            tvPostEndTime.setText(new SimpleDateFormat("E M月d日 H:m").format(party.getPostEndTime()));
            // join user table to get name
            tvOwner.setText(String.valueOf(party.getOwnerId()));
            // 點擊事件
            tvParticipant.setText(String.valueOf(party.getCountCurrent()));
            tvLocation.setText(party.getLocation());
            tvAddress.setText(party.getAddress());
            tvContent.setText(party.getContent());

            rvMessage.setLayoutManager(new LinearLayoutManager(activity));
            msgList = getMsgList(party.getId());
            showMsgList(msgList);

        }

//        String userId =
//        int imageSize = 40;
//        userImageTask = new CoverImageTask(url, userId, imageSize, ivOwner);
//        userImageTask.execute();



        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartyMessage message = null;
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create();

                if (Common.networkConnected(activity)) {
                    message = new PartyMessage(userId, partyId, etInput.getText().toString(), new Date());

                    String url = Common.URL_SERVER + "PartyMessageServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "msgInsert");
                    jsonObject.addProperty("message", gson.toJson(message));
                    String jsonOut = jsonObject.toString();
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        System.out.println(jsonOut);
                        System.out.println(result);
                        msgList.add(message);
                        showMsgList(msgList);
                        etInput.clearFocus();
                        etInput.setText("");


                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

    }

    private void showMsgList(List<PartyMessage> msgList) {
        MyAdapter myAdapter = (MyAdapter) rvMessage.getAdapter();
        if (myAdapter == null) {
            rvMessage.setAdapter(new MyAdapter(activity, msgList));
        } else {
            myAdapter.setMsgList(msgList);
            myAdapter.notifyDataSetChanged();
        }
    }

    private List<PartyMessage> getMsgList(int partyId) {
        List<PartyMessage> msgList = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyMessageServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getMsgList");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getMsgListTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMsgListTask.execute().get();
                Type listType = new TypeToken<List<PartyMessage>>() {
                }.getType();
                msgList = gson.fromJson(jsonIn, listType);
                System.out.println(jsonOut);
                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return msgList;
    }

    private Party getParty(int id) {
        Party party = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getParty");
            jsonObject.addProperty("id", id);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                party = gson.fromJson(jsonIn, Party.class);
                System.out.println(jsonOut);
                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return party;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<PartyMessage> msgList;
        private LayoutInflater layoutInflater;

        MyAdapter(Context context, List<PartyMessage> msgList) {
            layoutInflater = LayoutInflater.from(context);
            this.msgList = msgList;
        }

        void setMsgList(List<PartyMessage> msgList) {
            this.msgList = msgList;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvMsgName, tvMsg, tvMsgTime;
            ImageView ivMsg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMsgName = itemView.findViewById(R.id.tvMsgName);
                tvMsg = itemView.findViewById(R.id.tvMessage);
                tvMsgTime = itemView.findViewById(R.id.tvMsgTime);
            }
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_message, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyAdapter.MyViewHolder holder, int position) {
            PartyMessage message = msgList.get(position);

            holder.tvMsgName.setText(String.valueOf(message.getUserId()));
            holder.tvMsg.setText(message.getContent());
            String text = new SimpleDateFormat("M/d HH:mm").format(message.getTime());
            holder.tvMsgTime.setText(text);
            // 抓user頭像
//            String url = Common.URL_SERVER + "";
//            int id = party.getId();
//            partyImageTask = new CoverImageTask(url, id, imageSize, holder.ivMyParty);
//            partyImageTask.execute();

        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (getMsgListTask != null) {
            getMsgListTask.cancel(true);
            getMsgListTask = null;
        }
        activity.getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}
