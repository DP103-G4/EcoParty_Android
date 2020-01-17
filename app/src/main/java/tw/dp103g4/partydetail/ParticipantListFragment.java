package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;


public class ParticipantListFragment extends Fragment {
    private static final String TAG = "ParticipantListFragment";

    private Activity activity;
    private RecyclerView rvParticipant;
    private List<Participant> participantList;
    private CommonTask getAllTask;


    public ParticipantListFragment() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_participant_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvParticipant = view.findViewById(R.id.rvParticipant);
        rvParticipant.setLayoutManager(new LinearLayoutManager(activity));
        participantList = getParticipantList();
        showParticipantList(participantList);

    }

    private void showParticipantList(List<Participant> participantList) {
        if (participantList == null || participantList.isEmpty()) {
            Common.showToast(activity, R.string.textNoParticipantFound);
        }
        ParticipantListFragment.ParticipantListAdapter ParticipantAdapter = (ParticipantListFragment.ParticipantListAdapter) rvParticipant.getAdapter();
        if (ParticipantAdapter == null) {
            rvParticipant.setAdapter(new ParticipantListFragment.ParticipantListAdapter(activity, participantList));
        } else {
            ParticipantAdapter.setParticipantList(participantList);
            ParticipantAdapter.notifyDataSetChanged();
        }
    }

    private List<Participant> getParticipantList() {
        List<Participant> participantList = null;
        if (Common.networkConnected(activity)) {
            int partyId = 19;


            String url = Common.URL_SERVER + "ParticipantServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getParticipantList");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllTask.execute().get();
                Type listType = new TypeToken<List<Participant>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                participantList = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return participantList;
    }

    private class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListAdapter.ParticipantListViewHolder> {
        private List<Participant> participantList;
        private LayoutInflater layoutInflater;
        public ParticipantListAdapter(Context context, List<Participant> participantList) {
            layoutInflater = LayoutInflater.from(context);
            this.participantList = participantList;
        }

        void setParticipantList(List<Participant> participantList) {
            this.participantList = participantList;
        }

        @Override
        public int getItemCount() {
            return participantList.size();
        }

        private class ParticipantListViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivParticipant;
            private TextView tvParticipantName;
            private TextView tvCount;

            public ParticipantListViewHolder(View itemView) {
                super(itemView);
                ivParticipant = itemView.findViewById(R.id.ivParticipant);
                tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
                tvCount = itemView.findViewById(R.id.tvCount);
            }
        }

        @NonNull
        @Override
        public ParticipantListFragment.ParticipantListAdapter.ParticipantListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_participant, parent, false);
            return new ParticipantListFragment.ParticipantListAdapter.ParticipantListViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ParticipantListFragment.ParticipantListAdapter.ParticipantListViewHolder holder, int position) {
            Participant participant = participantList.get(position);
            holder.tvParticipantName.setText(participant.getId());
            holder.tvCount.setText(participant.getCount());
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (getAllTask != null) {
            getAllTask.cancel(true);
            getAllTask = null;
        }
    }
}


