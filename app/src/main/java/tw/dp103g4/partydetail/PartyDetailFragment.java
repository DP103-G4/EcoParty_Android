package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import qrcode.Contents;
import qrcode.QRCodeEncoder;
import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.CoverImageTask;
import tw.dp103g4.task.ImageTask;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class PartyDetailFragment extends Fragment {
    private Activity activity;
    private BottomNavigationView bottomNavigationView;
    private InputMethodManager imm;
    private TextView tvName, tvTime, tvPostEndTime, tvOwner, tvParticipant, tvLocation, tvAddress, tvContent;
    private RecyclerView rvMessage;
    private ImageView ivCover, ivOwner, ivParticipant, ivLocation;
    private ImageButton ibSend;
    private EditText etInput;
    private Button btLike, btIn, btShare, btStart, btQR, btRollCall, btMap, btICC;
    private List<PartyMsgInfo> msgList;
    private CommonTask getMsgListTask;
    private ImageTask ownerImagetask, msgImagetask;
    private CoverImageTask coverImageTask;
    private int imageSize;
    private String url;
    private ScrollView scrollView;
    private ConstraintLayout participantLayout, directLayout, msgLayout;
    private byte[] image;
    private static final int REQ_PICK_PICTURE = 1;
    private final int review = 0, post = 1, close = 2, start = 3, end = 4, delete = 5;
    private Bundle bundle;
    private int partyId, userId;
    private PartyInfo partyInfo;
    private TextView tvLeftCount;
    private LinearLayout leftCount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Switch postSwitch;

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final String TAG = "TAG_PartyDetail";

    public PartyDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
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
        final NavController navController = Navigation.findNavController(view);

        bottomNavigationView = activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.GONE);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

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
        ivCover = view.findViewById(R.id.PartyImg);
        ivOwner = view.findViewById(R.id.ivOwner);
        ivParticipant = view.findViewById(R.id.ivParticipant);
        ivLocation = view.findViewById(R.id.ivLocation);
        btQR = view.findViewById(R.id.btQR);
        btRollCall = view.findViewById(R.id.btRollCall);
        btMap = view.findViewById(R.id.btMap);
        btICC = view.findViewById(R.id.btICC);
        btLike = view.findViewById(R.id.btLike);
        btStart = view.findViewById(R.id.btStart);
        btIn = view.findViewById(R.id.btIn);
        btShare = view.findViewById(R.id.btShare);
        ibSend = view.findViewById(R.id.ibSend);
        etInput = view.findViewById(R.id.etInput);
        rvMessage = view.findViewById(R.id.rvMessage);
        participantLayout = view.findViewById(R.id.participantLayout);
        directLayout = view.findViewById(R.id.directLayout);
        msgLayout = view.findViewById(R.id.msgLaout);
        leftCount = view.findViewById(R.id.leftCount);
        tvLeftCount = view.findViewById(R.id.tvLeftCount);
        postSwitch = view.findViewById(R.id.switch1);



        rvMessage.setLayoutManager(new LinearLayoutManager(activity));


        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }
        partyId = bundle.getInt("partyId");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);

        partyInfo = getPartyInfo(partyId, userId);
        showPartyDetail(partyInfo);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                partyInfo = getPartyInfo(partyId, userId);
                swipeRefreshLayout.setRefreshing(true);
                showPartyDetail(partyInfo);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (changePartyState(partyId, post)) {
                        partyInfo.getParty().setState(post);
                        showPartyDetail(partyInfo);
                    }
                } else {
                    if (changePartyState(partyId, close)) {
                        partyInfo.getParty().setState(close);
                        showPartyDetail(partyInfo);
                    }
                }
            }
        });


        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle = new Bundle();
                bundle.putSerializable("party", partyInfo.getParty());
                navController.navigate(R.id.action_partyDetailFragment_to_shareFragment, bundle);
            }
        });


        btQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = new ImageView(activity);

                int dimension = getResources().getDisplayMetrics().widthPixels / 2;
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(String.valueOf(userId), null,
                        Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
                        dimension);

                try {
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    imageView.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    Log.e(TAG, e.toString());
                }

                new AlertDialog.Builder(activity)
                        .setTitle("請掃描QRcode")
                        .setView(imageView)
                        .setNegativeButton("關閉", null).create()
                        .show();

            }
        });


        btICC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId == partyInfo.getParty().getOwnerId()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("partyId", partyInfo.getParty().getId());
                    navController.navigate(R.id.action_partyDetailFragment_to_iccListFragment, bundle);

                } else if (partyInfo.getIsStaff()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("userId", userId);
                    bundle.putInt("partyId", partyInfo.getParty().getId());
                    navController.navigate(R.id.action_partyDetailFragment_to_iccDetailFragment, bundle);

                } else {
                    Common.showToast(getActivity(), "會員權限不足");
                }

            }
        });

        btMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("partyId", partyInfo.getParty().getId());
                bundle.putInt("ownerId", partyInfo.getParty().getOwnerId());

                navController.navigate(R.id.action_partyDetailFragment_to_locationFragment, bundle);
            }
        });


        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
                popupMenu.inflate(R.menu.party_start_menu);
                if (partyInfo.getParty().getState() < start) {
                    popupMenu.getMenu().findItem(R.id.start).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.end).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(true);
                }
                else {
                    popupMenu.getMenu().findItem(R.id.start).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.end).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.start:
                                if (changePartyState(partyId, start)) {
                                    partyInfo.getParty().setState(start);
                                    showPartyDetail(partyInfo);
                                }
                                break;
                            case R.id.end:
                                bundle.putSerializable("party", partyInfo.getParty());
                                navController.navigate(R.id.action_partyDetailFragment_to_pieceCreateFragment, bundle);
                                break;
                            case R.id.delete:
                                if (changePartyState(partyId, delete)) {
                                    partyInfo.getParty().setState(delete);
                                    navController.popBackStack();
                                }
                                break;
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
                        if (userId == 0) {
                            Common.showToast(getActivity(), R.string.textNoLogin);
                            return;
                        }

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

                                partyInfo.setIsLike(true);
                                showPartyDetail(partyInfo);
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

                                partyInfo.setIsLike(false);
                                showPartyDetail(partyInfo);
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
                        if (userId == 0) {
                            Common.showToast(getActivity(), R.string.textNoLogin);
                            return;
                        }

                        int left = partyInfo.getParty().getCountUpperLimit() - partyInfo.getParty().getCountCurrent();
                        final NumberPicker numberPicker = new NumberPicker(activity);

                        if (left <= 0)
                            return;

                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(left);
                        numberPicker.setWrapSelectorWheel(false);

                        new AlertDialog.Builder(activity)
                                .setTitle("報名活動")
                                .setMessage("請選擇參加人數")
                                .setView(numberPicker)
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Participant participant = new Participant(userId, partyId, numberPicker.getValue());

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

                                            if (count == -1) {
//                                Common.showToast(getActivity(), R.string.textInsertFail);
                                            } else {
//                                Common.showToast(getActivity(), R.string.textInsertSuccess);

                                                partyInfo.getParty().setCountCurrent(count);
                                                partyInfo.setIsIn(true);
                                                showPartyDetail(partyInfo);
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                    }
                                }).setNegativeButton("取消", null).create()
                                .show();

                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }

                } else {
                    // 取消參加
                    if (Common.networkConnected(activity)) {

                        Participant participant = new Participant(userId, partyId, 0);

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

                            if (count == -1) {
//                                Common.showToast(getActivity(), R.string.textDeleteFail);
                            } else {
//                                Common.showToast(getActivity(), R.string.textDeleteSuccess);

                                partyInfo.getParty().setCountCurrent(count);
                                partyInfo.setIsIn(false);
                                showPartyDetail(partyInfo);
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

        btRollCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putSerializable("party", partyInfo.getParty());
                bundle.putBoolean("isRollCall", true);
                navController.navigate(R.id.action_partyDetailFragment_to_participantListFragment, bundle);

            }
        });

        participantLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putSerializable("party", partyInfo.getParty());
                bundle.putBoolean("isRollCall", false);
                navController.navigate(R.id.action_partyDetailFragment_to_participantListFragment, bundle);

            }
        });

        directLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (partyInfo.getParty().getLongitude() != -181 &&
                    partyInfo.getParty().getLatitude() != -181) {

                    double toLat = partyInfo.getParty().getLatitude();
                    double toLng = partyInfo.getParty().getLongitude();

                    direct(toLat, toLng);
                }
            }
        });

        msgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msgList = getMsgList(partyId);
                showMsgList(msgList);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartyMessage message = null;

                if (Common.networkConnected(activity)) {
                    if (userId == 0) {
                        Common.showToast(getActivity(), R.string.textNoLogin);
                        return;
                    }
                    String text = etInput.getText().toString();
                    if (text.trim().isEmpty())
                        return;
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


                        msgList = getMsgList(partyId);
                        showMsgList(msgList);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });

                        imm =  (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(imm != null) {
                            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
                        }

                        etInput.setText("");


                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

    }

    private boolean changePartyState(int partyId, int state) {
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "changePartyState");
            jsonObject.addProperty("id", gson.toJson(partyId));
            jsonObject.addProperty("state", gson.toJson(state));
            String jsonOut = jsonObject.toString();

            int count = 0;
            try {
                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                System.out.println(jsonOut);
                count = Integer.valueOf(result.trim());

                if (count == 0) {
                    Common.showToast(getActivity(), R.string.textChageStateFail);
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
            return false;
        }
        return false;
    }

    private void showMsgList(List<PartyMsgInfo> msgList) {
        MyAdapter myAdapter = (MyAdapter) rvMessage.getAdapter();
        if (myAdapter == null) {
            rvMessage.setAdapter(new MyAdapter(activity, msgList));
        } else {
            myAdapter.setMsgList(msgList);
            myAdapter.notifyDataSetChanged();
        }
    }

    private List<PartyMsgInfo> getMsgList(int partyId) {
        List<PartyMsgInfo> msgList = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyMessageServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getMsgList");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getMsgListTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getMsgListTask.execute().get();
                Type listType = new TypeToken<List<PartyMsgInfo>>() {
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
        private List<PartyMsgInfo> msgList;
        private LayoutInflater layoutInflater;

        MyAdapter(Context context, List<PartyMsgInfo> msgList) {
            layoutInflater = LayoutInflater.from(context);
            this.msgList = msgList;
        }

        void setMsgList(List<PartyMsgInfo> msgList) {
            this.msgList = msgList;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvMsgName, tvMsg, tvMsgTime;
            ImageView ivMsg;
            ImageButton ibMsgMenu;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivMsg = itemView.findViewById(R.id.ivMsg);
                tvMsgName = itemView.findViewById(R.id.tvMsgName);
                tvMsg = itemView.findViewById(R.id.tvMessage);
                tvMsgTime = itemView.findViewById(R.id.tvMsgTime);
                ibMsgMenu = itemView.findViewById(R.id.ibMsgMenu);
            }
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_message, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyAdapter.MyViewHolder holder, final int position) {
            final PartyMsgInfo msgInfo = msgList.get(position);

            holder.tvMsgName.setText(msgInfo.getMsgName());
            holder.tvMsg.setText(msgInfo.getPartyMessage().getContent());
            String text = new SimpleDateFormat("M/d H:mm").format(msgInfo.getPartyMessage().getTime());
            holder.tvMsgTime.setText(text);

            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
            url = Common.URL_SERVER + "/UserServlet";
            msgImagetask = new ImageTask(url, msgInfo.getPartyMessage().getUserId(), imageSize, holder.ivMsg);
            msgImagetask.execute();

            if (userId == 0) {
                holder.ibMsgMenu.setVisibility(View.GONE);
            } else {
                holder.ibMsgMenu.setVisibility(View.VISIBLE);
            }

            holder.ibMsgMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                    popupMenu.inflate(R.menu.msg_menu);

                    if (userId == msgInfo.getPartyMessage().getUserId()) {
                        popupMenu.getMenu().findItem(R.id.msgWarn).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.msgDelete).setVisible(true);
                    } else {
                        popupMenu.getMenu().findItem(R.id.msgWarn).setVisible(true);
                        popupMenu.getMenu().findItem(R.id.msgDelete).setVisible(false);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.msgWarn:
                                    final EditText input = new EditText(activity);
                                    new AlertDialog.Builder(activity)
                                            .setTitle("檢舉留言")
                                            .setMessage("請輸入檢舉原因")
                                            .setView(input)
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    MsgWarn msgWarn = new MsgWarn(msgInfo.getPartyMessage().getId(), userId, input.getText().toString());

                                                    String url = Common.URL_SERVER + "MsgWarnServlet";
                                                    JsonObject jsonObject = new JsonObject();
                                                    jsonObject.addProperty("action", "msgWarnInsert");
                                                    jsonObject.addProperty("msgWarn", gson.toJson(msgWarn));
                                                    String jsonOut = jsonObject.toString();

                                                    int count = 0;
                                                    try {
                                                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                                        System.out.println(jsonOut);
                                                        count = Integer.valueOf(result.trim());

                                                        if (count == 0) {
                                                            Common.showToast(getActivity(), "檢舉失敗");
                                                        } else {
                                                            Common.showToast(getActivity(), "檢舉成功");
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e(TAG, e.toString());
                                                    }
                                                }
                                            }).setNegativeButton("取消", null).create()
                                            .show();
                                    break;


                                case R.id.msgDelete:
                                    new AlertDialog.Builder(activity)
                                            .setTitle("刪除留言")
                                            .setMessage("是否要刪除此留言")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String url = Common.URL_SERVER + "PartyMessageServlet";
                                                    JsonObject jsonObject = new JsonObject();
                                                    jsonObject.addProperty("action", "msgDelete");
                                                    jsonObject.addProperty("id", msgInfo.getPartyMessage().getId());
                                                    String jsonOut = jsonObject.toString();

                                                    int count = 0;
                                                    try {
                                                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                                        System.out.println(jsonOut);
                                                        count = Integer.valueOf(result.trim());

                                                        if (count == 0) {
                                                            Common.showToast(getActivity(), "刪除失敗");
                                                        } else {
                                                            msgList = getMsgList(partyId);
                                                            showMsgList(msgList);
//                                                            Common.showToast(getActivity(), "刪除成功");
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e(TAG, e.toString());
                                                    }
                                                }
                                            }).setNegativeButton("取消", null).create()
                                            .show();
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }

    }


    public void showPartyDetail(PartyInfo partyInfo)
    {
        if (partyInfo.getParty() != null) {
            postSwitch.setVisibility(View.GONE);
            btLike.setVisibility(View.GONE);
            btStart.setVisibility(View.GONE);
            btShare.setVisibility(View.GONE);
            btIn.setVisibility(View.GONE);
            btRollCall.setVisibility(View.GONE);
            btMap.setVisibility(View.GONE);
            btICC.setVisibility(View.GONE);
            btQR.setVisibility(View.GONE);
            leftCount.setVisibility(View.GONE);


            if (partyInfo.getParty().getOwnerId() == userId) {
                if (partyInfo.getParty().getState() < start)
                    postSwitch.setVisibility(View.VISIBLE);
                btLike.setVisibility(View.VISIBLE);
                btStart.setVisibility(View.VISIBLE);
                btShare.setVisibility(View.VISIBLE);
            } else {
                btLike.setVisibility(View.VISIBLE);
                btIn.setVisibility(View.VISIBLE);
                btShare.setVisibility(View.VISIBLE);
            }

            if (partyInfo.getParty().getState() == post) {
                btStart.setText("發布中");
                btStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.start, 0, 0, 0);
                int left = partyInfo.getParty().getCountUpperLimit() - partyInfo.getParty().getCountCurrent();
                tvLeftCount.setText(String.valueOf(left));
                leftCount.setVisibility(View.VISIBLE);

            } else if (partyInfo.getParty().getState() == close) {
                btStart.setText("已截止");
                btStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.start, 0, 0, 0);

                btIn.setClickable(false);
            } else if (partyInfo.getParty().getState() == start) {
                btStart.setText("進行中");
                btStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ing, 0, 0, 0);

                btIn.setClickable(false);
                if (partyInfo.getParty().getOwnerId() == userId) {
                    btRollCall.setVisibility(View.VISIBLE);
                    btMap.setVisibility(View.VISIBLE);
                    btICC.setVisibility(View.VISIBLE);
                } else if (partyInfo.getIsStaff()){
                    btQR.setVisibility(View.VISIBLE);
                    btMap.setVisibility(View.VISIBLE);
                    btICC.setVisibility(View.VISIBLE);
                } else {
                    btQR.setVisibility(View.VISIBLE);
                    btMap.setVisibility(View.VISIBLE);
                    btICC.setVisibility(View.GONE);
                }
            } else if (partyInfo.getParty().getState() == end) {
                btStart.setText("已結束");
                btStart.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ing, 0, 0, 0);
                btIn.setClickable(false);
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

        }

        imageSize = (getResources().getDisplayMetrics().widthPixels > 200) ? getResources().getDisplayMetrics().widthPixels : 200;
        url = Common.URL_SERVER + "/PartyServlet";
        coverImageTask = new CoverImageTask(url, partyId, imageSize, ivCover);
        coverImageTask.execute();

        imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        url = Common.URL_SERVER + "/UserServlet";
        ownerImagetask = new ImageTask(url, partyInfo.getParty().getOwnerId(), imageSize, ivOwner);
        ownerImagetask.execute();

        tvName.setText(partyInfo.getParty().getName());
        String text = new SimpleDateFormat("E M月d日 H:mm").format(partyInfo.getParty().getStartTime());
        String startDateString = new SimpleDateFormat("yyyyMMdd").format(partyInfo.getParty().getStartTime());
        String endDateString = new SimpleDateFormat("yyyyMMdd").format(partyInfo.getParty().getEndTime());
        if (startDateString.compareTo(endDateString) == 0)
            text += new SimpleDateFormat(" ~ H:mm").format(partyInfo.getParty().getEndTime());
        else
            text += new SimpleDateFormat(" ~ E M月d日 H:mm").format(partyInfo.getParty().getEndTime());
        tvTime.setText(text);
        tvPostEndTime.setText(new SimpleDateFormat("E M月d日 H:mm").format(partyInfo.getParty().getPostEndTime()));

        tvOwner.setText(partyInfo.getOwnerName());
        tvParticipant.setText(String.valueOf(partyInfo.getParty().getCountCurrent()));
        tvLocation.setText(partyInfo.getParty().getLocation());
        tvAddress.setText(partyInfo.getParty().getAddress());
        tvContent.setText(partyInfo.getParty().getContent());
        msgList = getMsgList(partyInfo.getParty().getId());
        showMsgList(msgList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_PICK_PICTURE:
                    Uri uri = intent.getData();
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                    activity.getContentResolver().openInputStream(uri));
                            ivCover.setImageBitmap(bitmap);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            image = out.toByteArray();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    if (bitmap != null) {
                        ivCover.setImageBitmap(bitmap);
                    } else {
                        ivCover.setImageResource(R.drawable.no_image);
                    }
                    break;

            }
        }
    }

    private void direct(double toLat, double toLng) {
        String uriStr = String.format(Locale.US,
                "https://www.google.com/maps/dir/?api=1" +
                        "&destination=%f,%f&travelmode=driving", toLat, toLng);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriStr));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        partyInfo = getPartyInfo(partyId, userId);
        showPartyDetail(partyInfo);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getMsgListTask != null) {
            getMsgListTask.cancel(true);
            getMsgListTask = null;
        }
        if (coverImageTask != null) {
            coverImageTask.cancel(true);
            coverImageTask = null;
        }
        if (ownerImagetask != null) {
            ownerImagetask.cancel(true);
            ownerImagetask = null;
        }
        if (msgImagetask != null) {
            msgImagetask.cancel(true);
            msgImagetask = null;
        }
    }

}
