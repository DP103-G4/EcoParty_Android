package tw.dp103g4.tablayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.AfterImageTask;
import tw.dp103g4.task.CommonTask;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class MyPieceDetailFragment extends Fragment {
    private RecyclerView rvMyParty;
    private FragmentActivity activity;
    private List<Party> myPieces;
    private int imageSize;
    private CommonTask pieceGetAllTask;
    private AfterImageTask pieceImageTask;

    public MyPieceDetailFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment MyPieceDetailFragment.
     */
    public static MyPieceDetailFragment newInstance() {
        return new MyPieceDetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvMyParty = view.findViewById(R.id.rvMyParty);
        rvMyParty.setLayoutManager(new LinearLayoutManager(activity));
        myPieces = getMyPieces();
        showMyParties(myPieces);
    }

    private void showMyParties(List<Party> myPieces) {
        if (myPieces == null || myPieces.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartiesFound);
        }
        MyPieceAdapter myPieceAdapter = (MyPieceAdapter) rvMyParty.getAdapter();
        if (myPieceAdapter == null) {
            rvMyParty.setAdapter(new MyPieceAdapter(activity, myPieces));
        } else {
            myPieceAdapter.setMyPieces(myPieces);
            myPieceAdapter.notifyDataSetChanged();
        }
    }

    private List<Party> getMyPieces() {
        List<Party> myPieces = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getCurrentParty");
            jsonObject.addProperty("state", 4);
            jsonObject.addProperty("participantId", 2);
            String jsonOut = jsonObject.toString();
            pieceGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = pieceGetAllTask.execute().get();
                Type listType = new TypeToken<List<Party>>() {
                }.getType();
                myPieces = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return myPieces;
    }

    private class MyPieceAdapter extends RecyclerView.Adapter<MyPieceAdapter.MyPieceViewHolder> {
        private List<Party> myPieces;
        private LayoutInflater layoutInflater;

        MyPieceAdapter(Context context, List<Party> myPieces) {
            layoutInflater = LayoutInflater.from(context);
            this.myPieces = myPieces;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        void setMyPieces(List<Party> myPieces) {
            this.myPieces = myPieces;
        }

        @Override
        public int getItemCount() {
            return myPieces.size();
        }

        private class MyPieceViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMyParty;

            MyPieceViewHolder(View itemView) {
                super(itemView);
                ivMyParty = itemView.findViewById(R.id.ivMyParty);
            }
        }

        @NonNull
        @Override
        public MyPieceAdapter.MyPieceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_my_party, parent, false);
            return new MyPieceAdapter.MyPieceViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyPieceAdapter.MyPieceViewHolder holder, int position) {
            Party myPiece = myPieces.get(position);
            String url = Common.URL_SERVER + "PartyServlet";
            int id = myPiece.getId();
            pieceImageTask = new AfterImageTask(url, id, imageSize, holder.ivMyParty);
            pieceImageTask.execute();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (pieceGetAllTask != null) {
            pieceGetAllTask.cancel(true);
            pieceGetAllTask = null;
        }
        if (pieceImageTask != null) {
            pieceImageTask.cancel(true);
            pieceImageTask = null;
        }
    }
}
