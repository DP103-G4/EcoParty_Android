package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
    private ImageView ivOwner;
    private TextView tvName, tvOwner, tvContent;
    private CommonTask getAllPartyImgsTask, getAllPieceInfoTask;
    private ImageTask ownerImagetask, partyImgTask;
    private RecyclerView rvPartyImg, rvPiece;
    private List<PartyImg> partyImgs;
    private List<PieceInfo> pieceInfoList;
    private int imageSize;
    private String url;
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
        final NavController navController = Navigation.findNavController(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        ivOwner = view.findViewById(R.id.ivOwner);
        tvName = view.findViewById(R.id.tvName);
        tvOwner = view.findViewById(R.id.tvOwner);
        tvContent = view.findViewById(R.id.tvContent);

        rvPartyImg = view.findViewById(R.id.rvPartyImg);
        rvPartyImg.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));

        rvPiece = view.findViewById(R.id.rvPiece);

        final Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }
        final int partyId = bundle.getInt("partyId");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        final int userId = pref.getInt("id", 0);

        final PartyInfo partyInfo = getPartyInfo(partyId, userId);
        final Party party = partyInfo.getParty();

        if (party != null) {

            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
            url = Common.URL_SERVER + "/UserServlet";
            ownerImagetask = new ImageTask(url, party.getOwnerId(), imageSize, ivOwner);
            ownerImagetask.execute();

            tvName.setText(party.getName());
            tvOwner.setText(partyInfo.getOwnerName());
            tvContent.setText(partyInfo.getParty().getContent());

            partyImgs = getPartyImgs(partyId);
            showPartyImgs(partyImgs);

            PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
            pagerSnapHelper.attachToRecyclerView(rvPartyImg);

            rvPiece.setLayoutManager(new LinearLayoutManager(activity));
            pieceInfoList = getPieceInfoList(party.getId());
            showPieceInfoList(pieceInfoList);

        }
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

    private List<PartyImg> getPartyImgs(int partyId) {
        List<PartyImg> partyImgs = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyImgServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getPartyImgs");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getAllPartyImgsTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllPartyImgsTask.execute().get();
                Type listType = new TypeToken<List<PartyImg>>() {
                }.getType();
                partyImgs = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return partyImgs;
    }

    private void showPartyImgs(List<PartyImg> partyImgs) {
        if (partyImgs == null || partyImgs.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartyImgs);
        }
        PartyImgAdapter partyImgAdapter = (PartyImgAdapter) rvPartyImg.getAdapter();
        if (partyImgAdapter == null) {
            rvPartyImg.setAdapter(new PartyImgAdapter(activity, partyImgs));
        } else {
            partyImgAdapter.setPartyImgs(partyImgs);
            partyImgAdapter.notifyDataSetChanged();
        }
    }

    private class PartyImgAdapter extends RecyclerView.Adapter<PieceDetailFragment.PartyImgAdapter.PartyImgViewHolder> {
        private List<PartyImg> partyImgs;
        private LayoutInflater layoutInflater;

        PartyImgAdapter(Context context, List<PartyImg> partyImgs) {
            layoutInflater = LayoutInflater.from(context);
            this.partyImgs = partyImgs;
            imageSize = getResources().getDisplayMetrics().widthPixels;
        }

        void setPartyImgs(List<PartyImg> partyImgs) {
            this.partyImgs = partyImgs;
        }

        @Override
        public int getItemCount() {
            return partyImgs.size();
        }

        private class PartyImgViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPartyImg;

            public PartyImgViewHolder(View itemView) {
                super(itemView);
                ivPartyImg = itemView.findViewById(R.id.PartyImg);
            }
        }

        @NonNull
        @Override
        public PartyImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_party_img, parent, false);
            return new PartyImgViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PartyImgViewHolder holder, int position) {
            PartyImg partyImg = partyImgs.get(position);
            String url = Common.URL_SERVER + "PartyImgServlet";
            final int id = partyImg.getId();
            imageSize = getResources().getDisplayMetrics().widthPixels;
            partyImgTask = new ImageTask(url, id, imageSize, holder.ivPartyImg);
            partyImgTask.execute();
        }
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
            TextView tvPieceName, tvPieceTime, tvPieceContent;

            public PieceInfoViewHolder(View itemView) {
                super(itemView);
                tvPieceName = itemView.findViewById(R.id.tvPieceName);
                tvPieceTime = itemView.findViewById(R.id.tvPieceTime);
                tvPieceContent = itemView.findViewById(R.id.tvPieceContent);
            }
        }

        @NonNull
        @Override
        public PieceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_party_piece, parent, false);
            return new PieceInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PieceInfoViewHolder holder, int position) {
            PieceInfo pieceInfo = pieceInfoList.get(position);

            holder.tvPieceName.setText(pieceInfo.getOwnerName());
            holder.tvPieceContent.setText(pieceInfo.getPartyPiece().getContent());
            String text = new SimpleDateFormat("M/d H:mm").format(pieceInfo.getPartyPiece().getTime());
            holder.tvPieceTime.setText(text);
            holder.setIsRecyclable(false);
        }

        @Override
        public int getItemCount() {
            return pieceInfoList.size();
        }
    }

}
