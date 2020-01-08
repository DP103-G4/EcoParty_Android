package tw.dp103g4.inform;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

public class InformFragment extends Fragment {
    private static final String TAG = "TAG_Inform";
    private FragmentActivity activity;
    private RecyclerView rvInform;
    private List<Inform> informs;
    private CommonTask informGetAllTask;
    private Button btInformIsRead;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inform, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvInform = view.findViewById(R.id.rvInform);
        btInformIsRead = view.findViewById(R.id.btInformIsRead);
        rvInform.setLayoutManager(new LinearLayoutManager(activity));
        informs = getInforms();
        showInform(informs);
    }

    private void showInform(List<Inform> informs) {
        if (informs == null || informs.isEmpty()) {
            Common.showToast(activity, R.string.textNoInform);
        }
        InfromAdapter informAdapter = (InfromAdapter) rvInform.getAdapter();
        if (informAdapter == null) {
            rvInform.setAdapter(new InfromAdapter(activity, informs));
        } else {
            informAdapter.setInforms(informs);
            informAdapter.notifyDataSetChanged();
        }
    }

    private List<Inform> getInforms() {
        List<Inform> informs = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "InformServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllInform");
            jsonObject.addProperty("receiverId", 2);
            String jsonOut = jsonObject.toString();
            informGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = informGetAllTask.execute().get();
                Type listType = new TypeToken<List<Inform>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                informs = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return informs;
    }

    private class InfromAdapter extends RecyclerView.Adapter<InfromAdapter.InformViewHolder> {
        private List<Inform> informs;
        private LayoutInflater layoutInflater;
        public InfromAdapter(Context context, List<Inform> informs) {
            layoutInflater = LayoutInflater.from(context);
            this.informs = informs;
        }

        void setInforms(List<Inform> informs) {
            this.informs = informs;
        }

        @Override
        public int getItemCount() {
            return informs.size();
        }

        private class InformViewHolder extends RecyclerView.ViewHolder {
            TextView tvInformContent;
            public InformViewHolder(View itemView) {
                super(itemView);
                tvInformContent = itemView.findViewById(R.id.tvInformContent);
            }
        }

        @NonNull
        @Override
        public InformViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_inform, parent, false);
            return new InformViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull InformViewHolder holder, int position) {
            Inform inform = informs.get(position);
            holder.tvInformContent.setText(inform.getContent());
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (informGetAllTask != null) {
            informGetAllTask.cancel(true);
            informGetAllTask = null;
        }
    }
}
