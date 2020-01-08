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

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.CoverImageTask;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class MyPartyDetailFragment extends Fragment {
    private int imageSize;
    private RecyclerView rvMyParty;
    private List<Party> myParties;
    private FragmentActivity activity;
    private CommonTask partyGetAllTask;
    private CoverImageTask partyImageTask;

    public MyPartyDetailFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment MyPartyDetailFragment.
     */
    public static MyPartyDetailFragment newInstance() {
        return new MyPartyDetailFragment();
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
        myParties = getMyParties();
        showMyParties(myParties);
    }

    private void showMyParties(List<Party> myParties) {
        if (myParties == null || myParties.isEmpty()) {
            Common.showToast(activity, R.string.textNoPartiesFound);
        }
        MyPartyAdapter myPartyAdapter = (MyPartyAdapter) rvMyParty.getAdapter();
        if (myPartyAdapter == null) {
            rvMyParty.setAdapter(new MyPartyAdapter(activity, myParties));
        } else {
            myPartyAdapter.setMyParties(myParties);
            myPartyAdapter.notifyDataSetChanged();
        }
    }

    private List<Party> getMyParties() {
        List<Party> myParties = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllParty");
            jsonObject.addProperty("state", 1);
            String jsonOut = jsonObject.toString();
            partyGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = partyGetAllTask.execute().get();
                Type listType = new TypeToken<List<Party>>() {
                }.getType();
                myParties = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return myParties;
    }

    private class MyPartyAdapter extends RecyclerView.Adapter<MyPartyAdapter.MyPartyViewHolder> {
        private List<Party> myParties;
        private LayoutInflater layoutInflater;

        MyPartyAdapter(Context context, List<Party> myParties) {
            layoutInflater = LayoutInflater.from(context);
            this.myParties = myParties;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        void setMyParties(List<Party> myParties) {
            this.myParties = myParties;
        }

        @Override
        public int getItemCount() {
            return myParties.size();
        }

        private class MyPartyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivMyParty;

            MyPartyViewHolder(View itemView) {
                super(itemView);
                ivMyParty = itemView.findViewById(R.id.ivMyParty);
            }
        }

        @NonNull
        @Override
        public MyPartyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_my_party, parent, false);
            return new MyPartyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyPartyViewHolder holder, int position) {
            Party party = myParties.get(position);
            String url = Common.URL_SERVER + "PartyServlet";
            int id = party.getId();
            partyImageTask = new CoverImageTask(url, id, imageSize, holder.ivMyParty);
            partyImageTask.execute();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (partyGetAllTask != null) {
            partyGetAllTask.cancel(true);
            partyGetAllTask = null;
        }
        if (partyImageTask != null) {
            partyImageTask.cancel(true);
            partyImageTask = null;
        }
    }
}
