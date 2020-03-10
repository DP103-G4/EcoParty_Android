package tw.dp103g4.partylist_android;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import tw.dp103g4.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.AfterImageTask;
import tw.dp103g4.task.CommonTask;

public class PieceListFragment extends Fragment {
    private static final String TAG = "TAG_PieceFragment";
    private FragmentActivity activity;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView rvPiece;
    private List<Party> parties;
    private CommonTask pieceGetAllTask;
    private int imageSize;
    private AfterImageTask afterImageTask;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_piece, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView= activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvPiece = view.findViewById(R.id.rvPiece);
        rvPiece.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        parties = getParties();
        showParties(parties);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                parties = getParties();
                swipeRefreshLayout.setRefreshing(true);
                showParties(parties);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showParties(List<Party> parties) {
        if (parties == null || parties.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartiesFound);
        }
        PieceAdapter pieceAdapter = (PieceAdapter) rvPiece.getAdapter();
        if (pieceAdapter == null) {
            rvPiece.setAdapter(new PieceAdapter(activity, parties));
        } else {
            pieceAdapter.setParties(parties);
            pieceAdapter.notifyDataSetChanged();
        }
    }

    private List<Party> getParties() {
        List<Party> parties = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getPieceList");
            jsonObject.addProperty("state", 4);
            String jsonOut = jsonObject.toString();
            pieceGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = pieceGetAllTask.execute().get();
                Type listType = new TypeToken<List<Party>>() {
                }.getType();
                parties = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return parties;
    }

    private class PieceAdapter extends RecyclerView.Adapter<PieceAdapter.PieceViewHolder> {
        private List<Party> parties;
        private LayoutInflater layoutInflater;

        public PieceAdapter(Context context, List<Party> parties) {
            layoutInflater = LayoutInflater.from(context);
            this.parties = parties;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        void setParties(List<Party> parties) {
            this.parties = parties;
        }


        @Override
        public int getItemCount() {
            return parties.size();
        }

        private class PieceViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPiece1, ivPiece2, ivPiece3, ivPiece4, ivPiece5;

            public PieceViewHolder(View itemView) {
                super(itemView);
                ivPiece1 = itemView.findViewById(R.id.ivPiece1);
            }
        }

        @NonNull
        @Override
        public PieceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_piece, parent, false);
            return new PieceViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PieceViewHolder holder, int position) {
            Party party = parties.get(position);
            String url = Common.URL_SERVER + "PartyServlet";
            final int id = party.getId();
            afterImageTask = new AfterImageTask(url, id, imageSize, holder.ivPiece1);
            afterImageTask.execute();
//            bundle花絮
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("partyId", id);
                    Navigation.findNavController(v).navigate(R.id.action_pieceFragment_to_pieceDetailFragment, bundle);
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (pieceGetAllTask != null) {
            pieceGetAllTask.cancel(true);
            pieceGetAllTask = null;
        }
        if (afterImageTask != null) {
            afterImageTask.cancel(true);
            afterImageTask = null;
        }

    }
}
