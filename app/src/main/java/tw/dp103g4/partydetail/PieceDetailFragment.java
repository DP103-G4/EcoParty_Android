package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.content.Context.MODE_PRIVATE;


public class PieceDetailFragment extends Fragment {
    private Activity activity;
    private BottomNavigationView bottomNavigationView;

    private CommonTask getAllPieceInfoTask, getAllPieceImgTask;
    private ImageTask ownerImageTask, pieceImgTask;
    private RecyclerView rvPiece;
    private TextView tvPiece;
    private FloatingActionButton btAddPiece;
    private List<PieceInfo> pieceInfoList;
    private int imageSize;
    private PartyInfo partyInfo;
    private String url;
    private int userId, partyId;
    private Bundle bundle;
    private NavController navController;
    private final int review = 0, post = 1, close = 2, start = 3, end = 4, delete = 5;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final String TAG = "TAG_PieceDetail";


    public PieceDetailFragment() {
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
        return inflater.inflate(R.layout.fragment_piece_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        bottomNavigationView = activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.GONE);


        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        rvPiece = view.findViewById(R.id.rvPiece);
        btAddPiece = view.findViewById(R.id.btAddPiece);
        tvPiece = view.findViewById(R.id.tvPiece);

        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }
        partyId = bundle.getInt("partyId");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);

        partyInfo = getPartyInfo(partyId, userId);

        if (partyInfo != null) {
            tvPiece.setText(partyInfo.getParty().getName());
        }


        if (partyInfo.getIsIn() || partyInfo.getParty().getOwnerId() == userId) {
            btAddPiece.setVisibility(View.VISIBLE);
        } else {
            btAddPiece.setVisibility(View.GONE);
        }


        final PartyInfo partyInfo = getPartyInfo(partyId, userId);
        final Party party = partyInfo.getParty();

        rvPiece.setLayoutManager(new LinearLayoutManager(activity));
        pieceInfoList = getPieceInfoList(party.getId());
        showPieceInfoList(pieceInfoList);


        btAddPiece.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putInt("partyId", partyId);
                navController.navigate(R.id.action_pieceDetailFragment_to_pieceInsertFragment, bundle);
            }
        });

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

    // piece

    private List<PieceInfo> getPieceInfoList(int partyId) {
        List<PieceInfo> pieceInfoList = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyPieceServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getPieceInfoList");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getAllPieceInfoTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllPieceInfoTask.execute().get();
                Type listType = new TypeToken<List<PieceInfo>>() {
                }.getType();
                pieceInfoList = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return pieceInfoList;
    }

    private void showPieceInfoList(List<PieceInfo> pieceInfoList) {
        PieceInfoAdapter pieceInfoAdapter = (PieceInfoAdapter) rvPiece.getAdapter();
        if (pieceInfoAdapter == null) {
            rvPiece.setAdapter(new PieceInfoAdapter(activity, pieceInfoList));
        } else {
            pieceInfoAdapter.setPieceInfoList(pieceInfoList);
            pieceInfoAdapter.notifyDataSetChanged();
        }
    }

    private class PieceImgAdapter extends RecyclerView.Adapter<PieceDetailFragment.PieceImgAdapter.PieceImgViewHolder> {
        private List<PieceImg> pieceImgs;
        private LayoutInflater layoutInflater;

        PieceImgAdapter(Context context, List<PieceImg> pieceImgs) {
            layoutInflater = LayoutInflater.from(context);
            this.pieceImgs = pieceImgs;
        }

        void setpieceImgs(List<PieceImg> pieceImgs) {
            this.pieceImgs = pieceImgs;
        }

        @Override
        public int getItemCount() {
            return pieceImgs.size();
        }

        private class PieceImgViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPieceImg;

            public PieceImgViewHolder(View itemView) {
                super(itemView);
                ivPieceImg = itemView.findViewById(R.id.ivPieceImg);
            }
        }

        @NonNull
        @Override
        public PieceImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_piece_img, parent, false);
            return new PieceImgViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PieceImgViewHolder holder, int position) {
            PieceImg pieceImg = pieceImgs.get(position);
            String url = Common.URL_SERVER + "PieceImgServlet";
            final int id = pieceImg.getId();
            imageSize = getResources().getDisplayMetrics().widthPixels;
            pieceImgTask = new ImageTask(url, id, imageSize, holder.ivPieceImg);
            pieceImgTask.execute();
        }
    }

    private class PieceInfoAdapter extends RecyclerView.Adapter<PieceDetailFragment.PieceInfoAdapter.PieceInfoViewHolder> {
        private List<PieceInfo> pieceInfoList;
        private LayoutInflater layoutInflater;

        PieceInfoAdapter(Context context, List<PieceInfo> pieceInfoList) {
            layoutInflater = LayoutInflater.from(context);
            this.pieceInfoList = pieceInfoList;
        }

        void setPieceInfoList(List<PieceInfo> pieceInfoList) {
            this.pieceInfoList = pieceInfoList;
        }

        private class PieceInfoViewHolder extends RecyclerView.ViewHolder {
            TextView tvPieceName, tvPieceName2, tvPieceTime, tvPieceContent;
            ImageView ivPieceUser;
            ImageButton ibPieceMenu;
            RecyclerView rvPieceImg;
            CardView countImg;
            TextView tvCountImg;

            public PieceInfoViewHolder(View itemView) {
                super(itemView);
                tvPieceName = itemView.findViewById(R.id.tvPieceName);
                tvPieceName2 = itemView.findViewById(R.id.tvPieceName2);
                tvPieceTime = itemView.findViewById(R.id.tvPieceTime);
                tvPieceContent = itemView.findViewById(R.id.tvPieceContent);
                ivPieceUser = itemView.findViewById(R.id.ivPieceUser);
                rvPieceImg = itemView.findViewById(R.id.rvPieceImg);
                ibPieceMenu = itemView.findViewById(R.id.ibPieceMenu);
                countImg = itemView.findViewById(R.id.countImg);
                tvCountImg = itemView.findViewById(R.id.tvCountImg);

            }
        }

        @NonNull
        @Override
        public PieceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_party_piece, parent, false);
            return new PieceInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final PieceInfoViewHolder holder, int position) {
            final List<PieceImg> pieceImgs;

            final PieceInfo pieceInfo = pieceInfoList.get(position);

            holder.tvPieceName.setText(pieceInfo.getOwnerName());
            holder.tvPieceName2.setText(pieceInfo.getOwnerName());
            holder.tvPieceContent.setText(pieceInfo.getPartyPiece().getContent());
            String text = new SimpleDateFormat("M/d H:mm").format(pieceInfo.getPartyPiece().getTime());
            holder.tvPieceTime.setText(text);

            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
            url = Common.URL_SERVER + "/UserServlet";
            ownerImageTask = new ImageTask(url, pieceInfo.getPartyPiece().getUserId(), imageSize, holder.ivPieceUser);
            ownerImageTask.execute();

            pieceImgs = getPieceImgs(pieceInfo.getPartyPiece().getId());

            if (pieceImgs == null || pieceImgs.isEmpty()) {
                Common.showToast(activity, "沒有花絮圖片");
            }

            if (pieceImgs.size() > 1) {
                holder.countImg.setVisibility(View.VISIBLE);
                holder.tvCountImg.setVisibility(View.VISIBLE);
                holder.tvCountImg.setText(String.valueOf(1) + "/" + pieceImgs.size());

                final LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
                holder.rvPieceImg.setLayoutManager(linearLayoutManager);

                holder.rvPieceImg.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            //Dragging
                        } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            int index = linearLayoutManager.findFirstVisibleItemPosition();
                            holder.tvCountImg.setText(String.valueOf(index+1) + "/" + pieceImgs.size());
                        }
                    }

                });

            } else {
                holder.countImg.setVisibility(View.GONE);
                holder.tvCountImg.setVisibility(View.GONE);
            }


            PieceImgAdapter pieceImgAdapter = (PieceImgAdapter) holder.rvPieceImg.getAdapter();
            if (pieceImgAdapter == null) {
                holder.rvPieceImg.setAdapter(new PieceImgAdapter(activity, pieceImgs));
            } else {
                pieceImgAdapter.setpieceImgs(pieceImgs);
                pieceImgAdapter.notifyDataSetChanged();
            }

            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();

            if (holder.rvPieceImg.getOnFlingListener() == null)
                pagerSnapHelper.attachToRecyclerView(holder.rvPieceImg);

            holder.ibPieceMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                    popupMenu.inflate(R.menu.piece_menu);

                    if (userId == pieceInfo.getPartyPiece().getUserId()) {
                        popupMenu.getMenu().findItem(R.id.pieceWarn).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.pieceDelete).setVisible(true);
                        popupMenu.getMenu().findItem(R.id.pieceUpdate).setVisible(true);
                    } else {
                        popupMenu.getMenu().findItem(R.id.pieceWarn).setVisible(true);
                        popupMenu.getMenu().findItem(R.id.pieceDelete).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.pieceUpdate).setVisible(false);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.pieceWarn:
                                    final EditText input = new EditText(activity);
                                    new AlertDialog.Builder(activity)
                                            .setTitle("檢舉花絮")
                                            .setMessage("請輸入檢舉原因")
                                            .setView(input)
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    PieceWarn pieceWarn = new PieceWarn(pieceInfo.getPartyPiece().getId(), userId, input.getText().toString());

                                                    String url = Common.URL_SERVER + "PieceWarnServlet";
                                                    JsonObject jsonObject = new JsonObject();
                                                    jsonObject.addProperty("action", "pieceWarnInsert");
                                                    jsonObject.addProperty("pieceWarn", gson.toJson(pieceWarn));
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

                                case R.id.pieceUpdate:
                                    bundle.putInt("partyId", partyId);
                                    bundle.putInt("pieceId", pieceInfo.getPartyPiece().getId());
                                    navController.navigate(R.id.action_pieceDetailFragment_to_pieceUpdateFragment, bundle);
                                    break;

                                case R.id.pieceDelete:
                                    new AlertDialog.Builder(activity)
                                            .setTitle("刪除花絮")
                                            .setMessage("是否要刪除此花絮")
                                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String url = Common.URL_SERVER + "PartyPieceServlet";
                                                    JsonObject jsonObject = new JsonObject();
                                                    jsonObject.addProperty("action", "pieceDelete");
                                                    jsonObject.addProperty("id", pieceInfo.getPartyPiece().getId());
                                                    String jsonOut = jsonObject.toString();

                                                    int count = 0;
                                                    try {
                                                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                                        System.out.println(jsonOut);
                                                        count = Integer.valueOf(result.trim());

                                                        if (count == 0) {
                                                            Common.showToast(getActivity(), "刪除失敗");
                                                        } else {
                                                            pieceInfoList = getPieceInfoList(partyId);
                                                            showPieceInfoList(pieceInfoList);
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
            return pieceInfoList.size();
        }

        private List<PieceImg> getPieceImgs(int pieceId) {
            List<PieceImg> pieceImgs = null;
            if (Common.networkConnected(activity)) {
                String url = Common.URL_SERVER + "PieceImgServlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "getPieceImgs");
                jsonObject.addProperty("pieceId", pieceId);
                String jsonOut = jsonObject.toString();
                getAllPieceImgTask = new CommonTask(url, jsonOut);
                try {
                    String jsonIn = getAllPieceImgTask.execute().get();
                    Type listType = new TypeToken<List<PieceImg>>() {
                    }.getType();
                    pieceImgs = gson.fromJson(jsonIn, listType);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }
            return pieceImgs;
        }

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


    @Override
    public void onStop() {
        super.onStop();

        if (getAllPieceInfoTask != null) {
            getAllPieceInfoTask.cancel(true);
            getAllPieceInfoTask = null;
        }
        if (getAllPieceImgTask != null) {
            getAllPieceImgTask.cancel(true);
            getAllPieceImgTask = null;
        }
        if (ownerImageTask != null) {
            ownerImageTask.cancel(true);
            ownerImageTask = null;
        }
        if (pieceImgTask != null) {
            pieceImgTask.cancel(true);
            pieceImgTask = null;
        }
    }
}
