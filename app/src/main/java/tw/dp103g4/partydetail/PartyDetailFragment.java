package tw.dp103g4.partydetail;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
    private Button btLike, btIn, btShare, btStart, btSet, btQR, btRollCall, btMap, btICC;
    private List<PartyMessage> msgList;
    private CommonTask getMsgListTask;
    private CoverImageTask coverImageTask;
    private ScrollView scrollView;
    private ConstraintLayout participantLayout;
    private final int review = 0, post = 1, close = 2, start = 3, end = 4, delete = 5;

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
        ivParticipant = view.findViewById(R.id.ivMsg);
        ivLocation = view.findViewById(R.id.ivLocation);
        btQR = view.findViewById(R.id.btQR);
        btRollCall = view.findViewById(R.id.btRollCall);
        btMap = view.findViewById(R.id.btMap);
        btICC = view.findViewById(R.id.btICC);
        btSet = view.findViewById(R.id.btSet);
        btLike = view.findViewById(R.id.btLike);
        btStart = view.findViewById(R.id.btStart);
        btIn = view.findViewById(R.id.btIn);
        btShare = view.findViewById(R.id.btShare);
        ibSend = view.findViewById(R.id.ibSend);
        etInput = view.findViewById(R.id.etInput);
        rvMessage = view.findViewById(R.id.rvMessage);
        participantLayout = view.findViewById(R.id.participantLayout);



        Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            Common.showToast(activity, R.string.textNoParticipantFound);
            navController.popBackStack();
            return;
        }
        final int partyId = bundle.getInt("partyId");

        final PartyInfo partyInfo = getPartyInfo(partyId, userId);
        final Party party = partyInfo.getParty();

        if (party != null) {
            int imageSize = (getResources().getDisplayMetrics().widthPixels > 200) ? getResources().getDisplayMetrics().widthPixels : 200;
            String url = Common.URL_SERVER + "/PartyServlet";
            coverImageTask = new CoverImageTask(url, partyId, imageSize, ivCover);
            coverImageTask.execute();

            tvName.setText(party.getName());
            String text = new SimpleDateFormat("E M月d日 H:mm").format(party.getStartTime());
            String startDateString = new SimpleDateFormat("YYYYMMdd").format(party.getStartTime());
            String endDateString = new SimpleDateFormat("YYYYMMdd").format(party.getEndTime());
            if (startDateString.compareTo(endDateString) == 0)
                text += new SimpleDateFormat(" ~ H:mm").format(party.getEndTime());
            else
                text += new SimpleDateFormat(" ~ E M月d日 H:mm").format(party.getEndTime());
            tvTime.setText(text);
            tvPostEndTime.setText(new SimpleDateFormat("E M月d日 H:mm").format(party.getPostEndTime()));

            if (party.getOwnerId() == userId) {
                btIn.setVisibility(View.GONE);
                btQR.setVisibility(View.GONE);
            } else {
                btStart.setVisibility(View.GONE);
                btSet.setVisibility(View.GONE);
                btRollCall.setVisibility(View.GONE);
                btICC.setVisibility(View.GONE);
            }

            if (party.getState() == post) {
                btStart.setText("發布中");
            } else if (party.getState() == close) {
                btStart.setText("已截止");
            } else if (party.getState() == start) {
                btStart.setText("進行中");
            }

            if (partyInfo.getIsIn()) {
                btIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.out, 0, 0, 0);
                btIn.setText("已參加");
            } else {
                btIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.in, 0, 0, 0);
                btIn.setText("參加");
            }
            if (partyInfo.getIsLike()) {
                btLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unlike, 0, 0, 0);
                btLike.setText("已收藏");
            } else {
                btLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
                btLike.setText("收藏");
            }

            tvOwner.setText(String.valueOf(party.getOwnerId()));
            tvParticipant.setText(String.valueOf(party.getCountCurrent()));
            tvLocation.setText(party.getLocation());
            tvAddress.setText(party.getAddress());
            tvContent.setText(party.getContent());

            rvMessage.setLayoutManager(new LinearLayoutManager(activity));
            msgList = getMsgList(party.getId());
            showMsgList(msgList);

        }

        btMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_partyDetailFragment_to_locationFragment);
            }
        });

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
                popupMenu.inflate(R.menu.start_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String text = "";
                        int state = 0;

                        switch (item.getItemId()) {
                            case R.id.post:
                                text = "發布中";
                                state = post;
                                break;
                            case R.id.close:
                                text = "已截止";
                                state = close;
                                break;
                            case R.id.start:
                                text = "進行中";
                                state = start;
                                break;
                            case R.id.end:
                                state = end;
                                break;
                            case R.id.delete:
                                state = delete;
                                break;
                        }

                        if (Common.networkConnected(activity)) {
                            String url = Common.URL_SERVER + "PartyServlet";
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("action", "chagePartyState");
                            jsonObject.addProperty("id", gson.toJson(party.getId()));
                            jsonObject.addProperty("state", gson.toJson(state));
                            String jsonOut = jsonObject.toString();

                            int count = 0;
                            try {
                                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                System.out.println(jsonOut);
                                count = Integer.valueOf(result.trim());

                                if (count == 0) {
                                    Common.showToast(getActivity(), R.string.textChageStateFail);
                                } else {
                                    party.setState(state);
                                    btStart.setText(text);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        } else {
                            Common.showToast(getActivity(), R.string.textNoNetwork);
                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });


        btLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!partyInfo.getIsLike()) {
                    if (Common.networkConnected(activity)) {
                        PartyLike partyLike = new PartyLike(userId, partyId);

                        String url = Common.URL_SERVER + "PartyLikeServlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "partyLikeInsert");
                        jsonObject.addProperty("partyLike", gson.toJson(partyLike));
                        String jsonOut = jsonObject.toString();

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            System.out.println(jsonOut);
                            count = Integer.valueOf(result.trim());

                            if (count == 0) {
//                                Common.showToast(getActivity(), R.string.textInsertFail);
                            } else {
//                                Common.showToast(getActivity(), R.string.textInsertSuccess);

                                btLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unlike, 0, 0, 0);
                                btLike.setText("已收藏");
                                partyInfo.setIsLike(true);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }
                } else {
                    if (Common.networkConnected(activity)) {
                        PartyLike partyLike = new PartyLike(userId, partyId);

                        String url = Common.URL_SERVER + "PartyLikeServlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "partyLikeDelete");
                        jsonObject.addProperty("partyLike", gson.toJson(partyLike));
                        String jsonOut = jsonObject.toString();

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            System.out.println(jsonOut);
                            count = Integer.valueOf(result.trim());

                            if (count == 0) {
//                                Common.showToast(getActivity(), R.string.textInsertFail);
                            } else {
//                                Common.showToast(getActivity(), R.string.textInsertSuccess);

                                btLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
                                btLike.setText("收藏");
                                partyInfo.setIsLike(false);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }
                }
            }

        });

        btIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!partyInfo.getIsIn()) {
                    // 參加
                    if (Common.networkConnected(activity)) {
                        // 先不團報
                        Participant participant = new Participant(userId, partyId, 1);

                        String url = Common.URL_SERVER + "ParticipantServlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "participantInsert");
                        jsonObject.addProperty("participant", gson.toJson(participant));
                        String jsonOut = jsonObject.toString();

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            System.out.println(jsonOut);
                            count = Integer.valueOf(result.trim());

                            if (count == 0) {
//                                Common.showToast(getActivity(), R.string.textInsertFail);
                            } else {
//                                Common.showToast(getActivity(), R.string.textInsertSuccess);

                                party.setCountCurrent(party.getCountCurrent() + 1);
                                tvParticipant.setText(String.valueOf(party.getCountCurrent()));
                                btIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.out, 0, 0, 0);
                                btIn.setText("已參加");
                                partyInfo.setIsIn(true);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }

                } else {
                    // 取消參加
                    if (Common.networkConnected(activity)) {
                        // 先不團報
                        Participant participant = new Participant(userId, partyId, 1);

                        String url = Common.URL_SERVER + "ParticipantServlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "participantDelete");
                        jsonObject.addProperty("participant", gson.toJson(participant));
                        String jsonOut = jsonObject.toString();

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            System.out.println(jsonOut);
                            count = Integer.valueOf(result.trim());

                            if (count == 0) {
//                                Common.showToast(getActivity(), R.string.textDeleteFail);
                            } else {
//                                Common.showToast(getActivity(), R.string.textDeleteSuccess);

                                party.setCountCurrent(party.getCountCurrent() - 1);
                                tvParticipant.setText(String.valueOf(party.getCountCurrent()));
                                btIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.in, 0, 0, 0);
                                btIn.setText("參加");
                                partyInfo.setIsIn(false);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }
                }
            }
        });


        participantLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("partyId", partyId);

                Navigation.findNavController(v).navigate(R.id.action_partyDetailFragment_to_participantListFragment, bundle);

            }
        });


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

    private PartyInfo getPartyInfo(int id, int userId) {
        PartyInfo partyInfo = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getParty");
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("userId", userId);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                partyInfo = gson.fromJson(jsonIn, PartyInfo.class);
                System.out.println(jsonOut);
                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return partyInfo;
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
            String text = new SimpleDateFormat("M/d H:mm").format(message.getTime());
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
