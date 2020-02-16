package tw.dp103g4.inform;


import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

import static android.content.Context.MODE_PRIVATE;

public class InformFragment extends Fragment {
    private static final String TAG = "TAG_Inform";
    private FragmentActivity activity;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView rvInform;
    private List<Inform> informs;
    private CommonTask informGetAllTask;
    private Button btInformIsRead;
    private SharedPreferences pref;
    private int receiverId;

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
        bottomNavigationView = activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
        pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        receiverId = pref.getInt("id", 0);
        rvInform = view.findViewById(R.id.rvInform);
        btInformIsRead = view.findViewById(R.id.btInformIsRead);
        rvInform.setLayoutManager(new LinearLayoutManager(activity));
        informs = getInforms(receiverId);
        showInform(informs);
        btInformIsRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "InformServlet";
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "setRead");
                    jsonObject.addProperty("receiverId", receiverId);
                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                    if (count == 0) {
                        Common.showToast(activity, R.string.textIsReadFail);
                    } else {
                        for (int i = 0; i<informs.size();i++ ) {
                            informs.get(i).setRead(true);

                        }
                        showInform(informs);
                    }
                }
            }
        });

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

    private List<Inform> getInforms(int receiverId) {
        List<Inform> informs = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "InformServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAllInform");
            jsonObject.addProperty("receiverId", receiverId);
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
        public void onBindViewHolder(@NonNull final InformViewHolder holder, int position) {
            Inform inform = informs.get(position);
            holder.tvInformContent.setText(inform.getContent());

            if(inform.isRead()){
                holder.itemView.setBackgroundColor(0xFFAED581);
            }


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
