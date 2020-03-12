package tw.dp103g4.friend;


import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import qrcode.Contents;
import qrcode.QRCodeEncoder;
import tw.dp103g4.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;
import tw.dp103g4.user.User;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static tw.dp103g4.main_android.Common.chatWebSocketClient;


public class FriendInsertFragment extends Fragment {
    private static final String TAG = "TAG_FInsertFragment";
    private MainActivity activity;
    private RecyclerView rvAddFriend;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CommonTask insertFriendTask,updateIsInviteTask;
    private ImageTask insertFriendImageTask;
    private Button btQRCode;
    private ImageButton ibtSearch;
    private EditText etSearch;
    private List<FriendShip> friendShips;
    private User user = null;
    private int count = 0;
    //Alert
    private Dialog myDialog,friendDialog,isFriendDialog;
    private ImageView ivAlert,ivFriendDialog;
    private Button btQr,btEnter,btFriendDialogClose,btFriendDialogAdd,btIsFriend;
    private TextView tvFriendDialogTitle,tvIsFriend;

    private SharedPreferences pref;
    private int userId;

    //socket
    private LocalBroadcastManager broadcastManager;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);
        //註冊socket
        broadcastManager = LocalBroadcastManager.getInstance(activity);
        registerMsg();
        Common.connectServer(activity, userId);
//        userId = 2;

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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                friendShips = getFriendShips();
                swipeRefreshLayout.setRefreshing(true);
                showFriendShips(friendShips);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ibtSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (etSearch.getText().toString().equals("")){
                }else {
                    if (Common.networkConnected(activity)) {
                        String url = Common.URL_SERVER + "UserServlet";
                        String userAccount = etSearch.getText().toString();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "searchUser");
                        jsonObject.addProperty("account", userAccount);
                        String jsonOut = jsonObject.toString();
                        insertFriendTask = new CommonTask(url, jsonOut);
                        try {
                            String jsonIn = insertFriendTask.execute().get();
                            Type listType = new TypeToken<User>() {
                            }.getType();
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                            user = gson.fromJson(jsonIn, listType);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        if (user == null || userId == user.getId()) {
                            String type = "";
                            if (user == null) {
                                type = "無法找到該用戶!";
                            } else {
                                type = "無法將自己的帳號加入好友歐！";
                            }
                            IsFriendDialog(type);
//                        Common.showToast(getActivity(), R.string.textSearchUserFail);
                        } else {
                            FriendShip isInvite = getIsInvite(userId, user.getId());
                            if (!isInvite.getNoInsert()) {
                                 String type = "";
                                if (!isInvite.getIsInvite()) {
                                    if(userId == isInvite.getIdTwo()){
                                        String titleStr = "是否要同意『" + user.getAccount() + "』的好友邀請？";
                                        FriendAlertDialog(titleStr,user.getId(),true);
                                    }else{
                                    type = "已經對『"+user.getAccount()+"』發出邀請囉！";}

                                } else {
                                    type = "『"+user.getAccount()+"』已經是好友囉！";

                                }
                                if (!type.equals("")){
                                IsFriendDialog(type);
                                }
                            } else {
                                String titleStr = "是否要邀請『" + user.getAccount() + "』成為好友？";
                                FriendAlertDialog(titleStr,user.getId(),false);
                            }
//                            Common.showToast(getActivity(), R.string.textSearchUserSuccess);
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }
                }
            }
        });

        btQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCustomAlertDialog();
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
                    int count = 0;
                    count = insertFriend(friendShip.getFriendId(),userId);
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
                    int count = 0;
                    count =  deleteFriendShip(friendShip.getFriendId(),userId);
                    if(count!= 0){
                        friendShips.remove(friendShip);
                        InsertFriendAdapter.this.notifyDataSetChanged();
                        // 外面List也必須移除選取的List
                        FriendInsertFragment.this.friendShips.remove(friendShip);}
                }
            });

        }
    }
    private FriendShip getIsInvite(int userId, int friendId){
        FriendShip isInvite = null;
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
                isInvite = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().fromJson(jsonIn, FriendShip.class);
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
//            Common.showToast(activity, R.string.textNoFriendShipFound);
            friendShips = new ArrayList<>();
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
//                Common.showToast(activity, R.string.txtUpdateIsInviteSuccess);
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
//                Common.showToast(activity, R.string.txtDeleteFriendShipSuccess);
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return count;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //wedSocket
    //接訊息 key: "newFriend"
    private void registerMsg(){
        IntentFilter newFriendFilter = new IntentFilter("newFriend");
        broadcastManager.registerReceiver(newMsgReceiver, newFriendFilter);
    }
    //處理訊息
    private BroadcastReceiver newMsgReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            ChatMsg chatMsg = new Gson().fromJson(message, ChatMsg.class);
            System.out.println(TAG+" : "+chatMsg.getMessage());
            if(userId == chatMsg.getReceiver()){
                FriendShip friendShip = new FriendShip(chatMsg.getSender(),chatMsg.getMessage());
                friendShips.add(0,friendShip);
                showFriendShips(friendShips);
            }
            Log.d(TAG, message);
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fragment頁面切換時解除註冊，但不需要關閉WebSocket，
        // 否則回到前頁好友列表，會因為斷線而無法顯示好友
        broadcastManager.unregisterReceiver(newMsgReceiver);
    }
    //QRcode Alert
    public void MyCustomAlertDialog(){
        myDialog = new Dialog(activity);
        myDialog.setContentView(R.layout.customdialog);
        myDialog.setTitle("MyAlert");

        btQr = (Button)myDialog.findViewById(R.id.btQr);
        btEnter = (Button)myDialog.findViewById(R.id.btEnter);
        ivAlert = (ImageView)myDialog.findViewById(R.id.ivAlert);

        btEnter.setEnabled(true);
        btQr.setEnabled(true);

        // QR code image's length is the same as the width of the window,
        int dimension = getResources().getDisplayMetrics().widthPixels;

        // Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(String.valueOf(userId), null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
                dimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ivAlert.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Log.e(TAG, e.toString());
        }

        btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.cancel();
            }
        });

        btQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 若在Activity內需要呼叫IntentIntegrator(Activity)建構式建立IntentIntegrator物件；
                 * 而在Fragment內需要呼叫IntentIntegrator.forSupportFragment(Fragment)建立物件，
                 * 掃瞄完畢時，Fragment.onActivityResult()才會被呼叫 */
                // IntentIntegrator integrator = new IntentIntegrator(this);
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(FriendInsertFragment.this);
                // Set to true to enable saving the barcode image and sending its path in the result Intent.
                integrator.setBarcodeImageEnabled(true);
                // Set to false to disable beep on scan.
                integrator.setBeepEnabled(false);
                // Use the specified camera ID.
                integrator.setCameraId(0);
                // By default, the orientation is locked. Set to false to not lock.
                integrator.setOrientationLocked(false);
                // Set a prompt to display on the capture screen.
                integrator.setPrompt("掃描QRCode");
                // Initiates a scan
                integrator.initiateScan();
                myDialog.cancel();
            }
        });

        myDialog.show();
    }
    //掃到QRcode
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null && intentResult.getContents() != null) {
            String url = Common.URL_SERVER + "UserServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "searchUserById");
            jsonObject.addProperty("id", intentResult.getContents());
            String jsonOut = jsonObject.toString();
            insertFriendTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = insertFriendTask.execute().get();
                Type listType = new TypeToken<User>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                user = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (user == null || userId == user.getId()) {
                String type = "";
                if (user == null) {
                    type = "無法找到該用戶!";
                } else {
                    type = "無法將自己的帳號加入好友歐！";
                }
                IsFriendDialog(type);
//                Common.showToast(getActivity(), R.string.textSearchUserFail);
            } else {
                FriendShip isInvite = getIsInvite(userId, user.getId());
                if (!isInvite.getNoInsert()) {
                    String type = "";
                    if (!isInvite.getIsInvite()) {
                        if (userId == isInvite.getIdTwo()) {
                            String titleStr = "是否要同意『" + user.getName() + "』的好友邀請？";
                            FriendAlertDialog(titleStr, user.getId(), true);
                        } else {
                            type = "已經對『"+user.getName()+"』發出邀請囉！";
                        }

                    } else {
                        type = "『"+user.getName() + "』已經是好友囉！";

                    }
                    if (!type.equals("")) {
                        IsFriendDialog(type);
                    }
                } else {
                    String titleStr = "是否要邀請『" + user.getName() + "』成為好友？";
                    FriendAlertDialog(titleStr, user.getId(), false);
                }
            }
        } else {
            Log.d(TAG,"SQL掃描失敗");
        }
    }

    public void FriendAlertDialog(String titleStr, final int dialogId, final Boolean update){
        friendDialog = new Dialog(activity);
        friendDialog.setContentView(R.layout.insert_friend_dialog);
        friendDialog.setTitle("FriendAlert");

        btFriendDialogAdd = friendDialog.findViewById(R.id.btFriendDailogAdd);
        btFriendDialogClose = friendDialog.findViewById(R.id.btFriendDailogClose);
        ivFriendDialog = friendDialog.findViewById(R.id.ivFriendDailog);
        tvFriendDialogTitle = friendDialog.findViewById(R.id.tvFriendDialogTitle);

        btFriendDialogClose.setEnabled(true);
        btFriendDialogAdd.setEnabled(true);

        tvFriendDialogTitle.setText(titleStr);
        String url = Common.URL_SERVER + "UserServlet";
        int imageSize = getResources().getDisplayMetrics().widthPixels / 2;
        insertFriendImageTask = new ImageTask(url, dialogId, imageSize, ivFriendDialog);
        insertFriendImageTask.execute();
        btFriendDialogAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(update){
                    insertFriend(dialogId,userId);
                    friendShips = getFriendShips();
                    showFriendShips(friendShips);
                }else{
                    String url = Common.URL_SERVER + "/FriendShipServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "friendShipInsert");
                    jsonObject.addProperty("idOne", userId);
                    jsonObject.addProperty("idTwo", user.getId());
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
                        Common.showToast(activity, "已經送出好友邀請！");
                        //socket
                        ChatMsg chatMsg = new ChatMsg("newFriend", userId, user.getId(), user.getAccount());
                        String newFriendJson = new Gson().toJson(chatMsg);
                        chatWebSocketClient.send(newFriendJson);
                    }
                }
                friendDialog.cancel();
            }
        });

        btFriendDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendDialog.cancel();
            }
        });
        friendDialog.show();
    }

    public void IsFriendDialog(String title){
        isFriendDialog = new Dialog(activity);
        isFriendDialog.setContentView(R.layout.isfriend_alert);
        isFriendDialog.setTitle("isFriendAlert");

        btIsFriend = isFriendDialog.findViewById(R.id.btIsFriend);
        tvIsFriend = isFriendDialog.findViewById(R.id.tvIsFriend);
        tvIsFriend.setText(title);
        btIsFriend.setEnabled(true);

        btIsFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFriendDialog.cancel();
            }
        });
        isFriendDialog.show();
    }
}
