package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.content.Context.MODE_PRIVATE;


public class IccListFragment extends Fragment {
    private static final String TAG = "IccListFragment";
    private Activity activity;
    private RecyclerView rvIcc;
    private SwipeRefreshLayout rlIcc;
    private SearchView svIcc;
    private int partyId, userId;
    private List<IccTableInfo> iccTableInfos;
    private TextView tvTotalCount, tvTotalWeight;
    private CommonTask getAllTask;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public IccListFragment() {
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
        return inflater.inflate(R.layout.fragment_icc_list, container, false);
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

        rvIcc = view.findViewById(R.id.rvIcc);
        rlIcc = view.findViewById(R.id.rlIcc);
        svIcc = view.findViewById(R.id.svIcc);
        tvTotalCount = view.findViewById(R.id.tvTotalCount);
        tvTotalWeight = view.findViewById(R.id.tvTotalWeight);

        rvIcc.setLayoutManager(new LinearLayoutManager(activity));


        Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }

        partyId = bundle.getInt("partyId");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);

        iccTableInfos = getIccTableInfos(partyId);
        showIccTableInfos(iccTableInfos);

        rlIcc.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                iccTableInfos = getIccTableInfos(partyId);
                showIccTableInfos(iccTableInfos);
                rlIcc.setRefreshing(false);
            }
        });

        svIcc.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // 如果搜尋條件為空字串，就顯示原始資料；否則就顯示搜尋後結果
                if (newText.isEmpty()) {
                    showIccTableInfos(iccTableInfos);
                } else {
                    List<IccTableInfo> searchIccTableInfos = new ArrayList<>();
                    // 搜尋原始資料內有無包含關鍵字(不區別大小寫)
                    for (IccTableInfo iccTableInfo : iccTableInfos) {
                        if (iccTableInfo.getUserName().toUpperCase().contains(newText.toUpperCase())) {
                            searchIccTableInfos.add(iccTableInfo);
                        }
                    }
                    showIccTableInfos(searchIccTableInfos);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

    }

    private void showIccTableInfos(List<IccTableInfo> iccTableInfos) {
        if (iccTableInfos == null || iccTableInfos.isEmpty()) {
            Common.showToast(activity, "沒有ICC表格");
        } else {
            int totalCount = 0;
            Double totalWeight = 0.0;

            for (IccTableInfo iccTableInfo : iccTableInfos) {
                totalCount += iccTableInfo.getCount();
                totalWeight += iccTableInfo.getIccTable().getWeight();
            }

            tvTotalCount.setText(String.valueOf(totalCount));

            String text = String.format("%.2f", totalWeight);
            tvTotalWeight.setText(text);

        }

        IccTableInfoAdapter iccTableInfoAdapter = (IccTableInfoAdapter) rvIcc.getAdapter();
        if (iccTableInfoAdapter == null) {
            rvIcc.setAdapter(new IccTableInfoAdapter(activity, iccTableInfos));
        } else {
            iccTableInfoAdapter.setIccTableInfos(iccTableInfos);
            iccTableInfoAdapter.notifyDataSetChanged();
        }
    }

    private List<IccTableInfo> getIccTableInfos(int partyId) {
            List<IccTableInfo> iccTableInfos = null;
            if (Common.networkConnected(activity)) {
                String url = Common.URL_SERVER + "IccTableServlet";
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "getIccTableInfos");
                jsonObject.addProperty("partyId", partyId);
                String jsonOut = jsonObject.toString();
                getAllTask = new CommonTask(url, jsonOut);
                try {
                    String jsonIn = getAllTask.execute().get();
                    Type listType = new TypeToken<List<IccTableInfo>>() {
                    }.getType();
                    iccTableInfos = gson.fromJson(jsonIn, listType);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Common.showToast(activity, R.string.textNoNetwork);
            }
            return iccTableInfos;
    }

    private class IccTableInfoAdapter extends RecyclerView.Adapter<IccTableInfoAdapter.IccTableInfoViewHolder> {
        private List<IccTableInfo> iccTableInfos;
        private LayoutInflater layoutInflater;

        public IccTableInfoAdapter(Context context, List<IccTableInfo> iccTableInfos) {
            layoutInflater = LayoutInflater.from(context);
            this.iccTableInfos = iccTableInfos;
        }

        void setIccTableInfos(List<IccTableInfo> iccTableInfos) {
            this.iccTableInfos = iccTableInfos;
        }

        @Override
        public int getItemCount() {
            return iccTableInfos.size();
        }

        private class IccTableInfoViewHolder extends RecyclerView.ViewHolder {
            private ImageView ivIccUser;
            private TextView tvIccName, tvIccCount, tvIccWeight;


            public IccTableInfoViewHolder(View itemView) {
                super(itemView);
                ivIccUser = itemView.findViewById(R.id.ivIccUser);
                tvIccName = itemView.findViewById(R.id.tvIccName);
                tvIccCount = itemView.findViewById(R.id.tvIccCount);
                tvIccWeight = itemView.findViewById(R.id.tvIccWeight);

            }
        }

        @NonNull
        @Override
        public IccTableInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_icc, parent, false);
            return new IccTableInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final IccTableInfoViewHolder holder, final int position) {
            final IccTableInfo iccTableInfo = iccTableInfos.get(position);

            String url = Common.URL_SERVER + "UserServlet";
            final int id = iccTableInfo.getIccTable().getUserId();
            int imageSize = getResources().getDisplayMetrics().widthPixels / 4;
            ImageTask getUserImageTask = new ImageTask(url, id, imageSize, holder.ivIccUser);
            getUserImageTask.execute();

            holder.tvIccName.setText(iccTableInfo.getUserName());
            holder.tvIccCount.setText(String.valueOf(iccTableInfo.getCount()));

            String text = String.format("%.2f", iccTableInfo.getIccTable().getWeight());
            holder.tvIccWeight.setText(text);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("userId", iccTableInfo.getIccTable().getUserId());
                    bundle.putInt("partyId", iccTableInfo.getIccTable().getPartyId());
                    Navigation.findNavController(v).navigate(R.id.action_iccListFragment_to_iccDetailFragment, bundle);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                    popupMenu.inflate(R.menu.icc_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.removeStaff:
                                    if (Common.networkConnected(activity)) {
                                        String url = Common.URL_SERVER + "/ParticipantServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "setStaff");
                                        jsonObject.addProperty("userId", iccTableInfo.getIccTable().getUserId());
                                        jsonObject.addProperty("partyId", iccTableInfo.getIccTable().getPartyId());
                                        jsonObject.addProperty("isStaff", false);

                                        String jsonOut = jsonObject.toString();

                                        int count = 0;
                                        try {
                                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                                            System.out.println(jsonOut);
                                            count = Integer.valueOf(result.trim());

                                            if (count == 0) {
                                                Common.showToast(getActivity(), "設定工作人員失敗");
                                            } else {
                                                iccTableInfos.remove(position);
                                                showIccTableInfos(iccTableInfos);
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

    }


    @Override
    public void onStop() {
        super.onStop();if (getAllTask != null) {
            getAllTask.cancel(true);
            getAllTask = null;
        }
    }
    
}
