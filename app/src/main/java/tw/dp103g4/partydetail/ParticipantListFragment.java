package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;


public class ParticipantListFragment extends Fragment {
    private static final String TAG = "ParticipantListFragment";

    private Activity activity;
    private SearchView searchView;
    private RecyclerView rvParticipant;
    private SwipeRefreshLayout rlParticipant;
    private Button btQRcode;
    private List<ParticipantInfo> participantInfos;
    private CommonTask getAllTask;
    private Party party;
    private int partyId;
    private boolean isRollCall;


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();


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
        final NavController navController = Navigation.findNavController(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        rlParticipant = view.findViewById(R.id.rlParticipant);
        btQRcode = view.findViewById(R.id.btQRcode);
        searchView = view.findViewById(R.id.svParticipant);
        rvParticipant = view.findViewById(R.id.rvParticipant);
        rvParticipant.setLayoutManager(new LinearLayoutManager(activity));
        rlParticipant = view.findViewById(R.id.rlParticipant);

        Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("party") == null) {
            navController.popBackStack();
            return;
        }

        isRollCall = bundle.getBoolean("isRollCall", false);

        party = (Party) bundle.getSerializable("party");
        partyId = party.getId();

        if (isRollCall) {
            btQRcode.setVisibility(View.VISIBLE);
        } else {
            btQRcode.setVisibility(View.GONE);
        }


        participantInfos = getParticipantInfos(partyId);
        showParticipantInfos(participantInfos);

        rlParticipant.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                participantInfos = getParticipantInfos(partyId);
                showParticipantInfos(participantInfos);
                rlParticipant.setRefreshing(false);
            }
        });

        btQRcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 若在Activity內需要呼叫IntentIntegrator(Activity)建構式建立IntentIntegrator物件；
                 * 而在Fragment內需要呼叫IntentIntegrator.forSupportFragment(Fragment)建立物件，
                 * 掃瞄完畢時，Fragment.onActivityResult()才會被呼叫 */
                // IntentIntegrator integrator = new IntentIntegrator(this);
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(ParticipantListFragment.this);
                // Set to true to enable saving the barcode image and sending its path in the result Intent.
                integrator.setBarcodeImageEnabled(true);
                // Set to false to disable beep on scan.
                integrator.setBeepEnabled(false);
                // Use the specified camera ID.
                integrator.setCameraId(0);
                // By default, the orientation is locked. Set to false to not lock.
                integrator.setOrientationLocked(false);
                // Set a prompt to display on the capture screen.
                integrator.setPrompt("Scan a QR Code");
                // Initiates a scan
                integrator.initiateScan();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showParticipantInfos(participantInfos);
                } else {
                    List<ParticipantInfo> searchParticipantInfos = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (ParticipantInfo participantInfo : participantInfos) {
                        if (participantInfo.getParticipantName().toUpperCase().contains(newText.toUpperCase())) {
                            searchParticipantInfos.add(participantInfo);
                        }
                    }
                    showParticipantInfos(searchParticipantInfos);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });


    }

    private void showParticipantInfos(List<ParticipantInfo> participantInfos) {
        if (participantInfos == null || participantInfos.isEmpty()) {
            Common.showToast(activity, R.string.textNoParticipantFound);
        }
        ParticipantInfoAdapter ParticipantAdapter = (ParticipantInfoAdapter) rvParticipant.getAdapter();
        if (ParticipantAdapter == null) {
            rvParticipant.setAdapter(new ParticipantInfoAdapter(activity, participantInfos));
        } else {
            ParticipantAdapter.setParticipantInfos(participantInfos);
            ParticipantAdapter.notifyDataSetChanged();
        }
    }

    private List<ParticipantInfo> getParticipantInfos(int partyId) {
        List<ParticipantInfo> participantInfos = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "ParticipantServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getParticipantInfos");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            getAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllTask.execute().get();
                Type listType = new TypeToken<List<ParticipantInfo>>() {
                }.getType();
                participantInfos = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return participantInfos;
    }

    private class ParticipantInfoAdapter extends RecyclerView.Adapter<ParticipantInfoAdapter.ParticipantInfoViewHolder> {
        private List<ParticipantInfo> participantInfos;
        private LayoutInflater layoutInflater;
        public ParticipantInfoAdapter(Context context, List<ParticipantInfo> participantInfos) {
            layoutInflater = LayoutInflater.from(context);
            this.participantInfos = participantInfos;
        }

        void setParticipantInfos(List<ParticipantInfo> participantInfos) {
            this.participantInfos = participantInfos;
        }

        @Override
        public int getItemCount() {
            return participantInfos.size();
        }

        private class ParticipantInfoViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivParticipant, ivStaff;
            private TextView tvParticipantName;
            private TextView tvCount;
            private CheckBox cbArrival;

            public ParticipantInfoViewHolder(View itemView) {
                super(itemView);
                ivParticipant = itemView.findViewById(R.id.ivParticipant);
                tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
                tvCount = itemView.findViewById(R.id.tvCount);
                cbArrival = itemView.findViewById(R.id.cbArrival);
                ivStaff = itemView.findViewById(R.id.ivStaff);

            }
        }

        @NonNull
        @Override
        public ParticipantInfoAdapter.ParticipantInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_participant, parent, false);
            return new ParticipantInfoAdapter.ParticipantInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ParticipantInfoAdapter.ParticipantInfoViewHolder holder, final int position) {
            final ParticipantInfo participantInfo = participantInfos.get(position);

            String url = Common.URL_SERVER + "UserServlet";
            final int id = participantInfo.getParticipant().getId();
            int imageSize = getResources().getDisplayMetrics().widthPixels / 4;
            ImageTask getUserImageTask = new ImageTask(url, id, imageSize, holder.ivParticipant);
            getUserImageTask.execute();

            if (isRollCall) {
                holder.cbArrival.setVisibility(View.VISIBLE);

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                        popupMenu.inflate(R.menu.participant_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.setStaff:
                                        if (Common.networkConnected(activity)) {
                                            String url = Common.URL_SERVER + "/ParticipantServlet";
                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.addProperty("action", "setStaff");
                                            jsonObject.addProperty("userId", participantInfo.getParticipant().getId());
                                            jsonObject.addProperty("partyId", participantInfo.getParticipant().getPartyId());
                                            jsonObject.addProperty("isStaff", !participantInfo.getParticipant().isStaff());

                                            String jsonOut = jsonObject.toString();

                                            int count = 0;
                                            try {
                                                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                                System.out.println(jsonOut);
                                                count = Integer.valueOf(result.trim());

                                                if (count == 0) {
                                                    Common.showToast(getActivity(), "設定工作人員失敗");
                                                } else {
                                                    participantInfo.getParticipant().setStaff(!participantInfo.getParticipant().isStaff());
//                                                    showParticipantInfos(participantInfos);
                                                    if (participantInfo.getParticipant().isStaff()) {
                                                        holder.ivStaff.setVisibility(View.VISIBLE);
                                                    } else {
                                                        holder.ivStaff.setVisibility(View.GONE);

                                                    }
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                            }
                                        } else {
                                            Common.showToast(getActivity(), R.string.textNoNetwork);
                                        }

                                        break;
                                    case R.id.kickOut:
                                        if (Common.networkConnected(activity)) {

                                            Participant participant = new Participant(participantInfo.getParticipant().getId(), participantInfo.getParticipant().getPartyId(), 0);

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

                                                if (count == 0) {
                                                    Common.showToast(getActivity(), R.string.textDeleteFail);
                                                } else {
                                                    participantInfos.remove(position);
                                                    showParticipantInfos(participantInfos);
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, e.toString());
                                            }
                                        } else {
                                            Common.showToast(getActivity(), R.string.textNoNetwork);
                                        }

                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                        return true;
                    }
                });



            }
            else {
                holder.cbArrival.setVisibility(View.GONE);
            }

            holder.tvParticipantName.setText(participantInfo.getParticipantName());
            holder.tvCount.setText(String.valueOf(participantInfo.getParticipant().getCount()));
            if (participantInfo.getParticipant().isArrival()) {
                holder.cbArrival.setChecked(true);
                holder.cbArrival.setText("已報到");
            } else {
                holder.cbArrival.setChecked(false);
                holder.cbArrival.setText("未報到");
            }

            if (participantInfo.getParticipant().isStaff())
                holder.ivStaff.setVisibility(View.VISIBLE);
            else
                holder.ivStaff.setVisibility(View.GONE);

            holder.cbArrival.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Common.networkConnected(activity)) {
                        String url = Common.URL_SERVER + "ParticipantServlet";
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "setArrival");
                        jsonObject.addProperty("userId", participantInfo.getParticipant().getId());
                        jsonObject.addProperty("partyId", participantInfo.getParticipant().getPartyId());
                        jsonObject.addProperty("isArrival", !participantInfo.getParticipant().isArrival());

                        String jsonOut = jsonObject.toString();

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            System.out.println(jsonOut);
                            count = Integer.valueOf(result.trim());

                            if (count == 0) {
                                Common.showToast(getActivity(), R.string.textRollCallFailed);
                            } else {
                                participantInfo.getParticipant().setArrival(!participantInfo.getParticipant().isArrival());
//                                showParticipantInfos(participantInfos);
                                if (participantInfo.getParticipant().isArrival()) {
                                    holder.cbArrival.setChecked(true);
                                    holder.cbArrival.setText("已報到");
                                } else {
                                    holder.cbArrival.setChecked(false);
                                    holder.cbArrival.setText("未報到");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null && intentResult.getContents() != null) {
//            intentResult.getContents()
//                Common.showToast(getActivity(), intentResult.getContents());
            if (Common.networkConnected(activity)) {
                String url = Common.URL_SERVER + "ParticipantServlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "setArrival");
                jsonObject.addProperty("userId", Integer.valueOf(intentResult.getContents()));
                jsonObject.addProperty("partyId", partyId);
                jsonObject.addProperty("isArrival", true);

                String jsonOut = jsonObject.toString();

                int count = 0;
                try {
                    String result = new CommonTask(url, jsonObject.toString()).execute().get();
                    System.out.println(jsonOut);
                    count = Integer.valueOf(result.trim());

                    if (count == 0) {
                        Common.showToast(getActivity(), "未參加活動");
                    } else {
                        participantInfos = getParticipantInfos(partyId);
                        showParticipantInfos(participantInfos);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Common.showToast(getActivity(), R.string.textNoNetwork);
            }
        } else {
            Common.showToast(getActivity(), "掃描失敗");
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


