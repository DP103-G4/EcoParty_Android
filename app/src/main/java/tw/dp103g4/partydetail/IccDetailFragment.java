package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.content.Context.MODE_PRIVATE;


public class IccDetailFragment extends Fragment {
    private static final String TAG = "IccDetailFragment";
    private Activity activity;
    private Bundle bundle;
    private int partyId, userId;
    private IccTableInfo iccTableInfo;
    private RecyclerView rvIccDetail;
    private TextView btIccDetailOK, btIccDetailRe;


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();



    public IccDetailFragment() {
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
        return inflater.inflate(R.layout.fragment_icc_detail, container, false);
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


        rvIccDetail = view.findViewById(R.id.rvIccDetail);
        btIccDetailOK = view.findViewById(R.id.btIccDetailOK);
        btIccDetailRe = view.findViewById(R.id.btIccDetailRe);

        rvIccDetail.setLayoutManager(new GridLayoutManager(activity, 3));


        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0 || bundle.getInt("userId") == 0 ){
            navController.popBackStack();
            return;
        }
        userId = bundle.getInt("userId");
        partyId = bundle.getInt("partyId");

        iccTableInfo = getIccTableInfo(userId, partyId);
        if (iccTableInfo != null) {
            toolbar.setTitle(iccTableInfo.getUserName() + "的ICC表格");
            showIccTableDetail(iccTableInfo);
        }

        btIccDetailOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IccTableInfoAdapter iccTableInfoAdapter = (IccTableInfoAdapter) rvIccDetail.getAdapter();
                if (iccTableInfoAdapter != null) {
                    iccTableInfo = iccTableInfoAdapter.getIccTableInfo();


                    if (Common.networkConnected(activity)) {
                        String url = Common.URL_SERVER + "IccTableServlet";

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "iccUpdate");
                        jsonObject.addProperty("iccTable", gson.toJson(iccTableInfo.getIccTable()));

                        int count = 0;
                        try {
                            String result = new CommonTask(url, jsonObject.toString()).execute().get();
                            count = Integer.valueOf(result.trim());
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }

                        if (count == 0) {
                            Common.showToast(getActivity(), "ICC上傳失敗");
                        } else {
                            navController.popBackStack();
                        }

                    } else {
                        Common.showToast(getActivity(), R.string.textNoNetwork);
                    }


                }
            }
        });

        btIccDetailRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iccTableInfo = getIccTableInfo(userId, partyId);
                showIccTableDetail(iccTableInfo);
            }
        });



    }

    private class IccTableInfoAdapter extends RecyclerView.Adapter<IccTableInfoAdapter.IccTableInfoViewHolder> {
        private IccTableInfo iccTableInfo;
        private LayoutInflater layoutInflater;

        public IccTableInfoAdapter(Context context, IccTableInfo iccTableInfo) {
            layoutInflater = LayoutInflater.from(context);
            this.iccTableInfo = iccTableInfo;
        }

        void setIccTableInfo(IccTableInfo iccTableInfo) {
            this.iccTableInfo = iccTableInfo;
        }
        public IccTableInfo getIccTableInfo() { return this.iccTableInfo; }

        @Override
        public int getItemCount() {
            return 24;
        }

        private class IccTableInfoViewHolder extends RecyclerView.ViewHolder {
            private ImageButton ibPlus, ibMinus;
            private TextView tvPlastic, textPlastic;
            private ImageView ivPlastic;


            public IccTableInfoViewHolder(View itemView) {
                super(itemView);
                ibPlus = itemView.findViewById(R.id.ibPlus);
                ibMinus = itemView.findViewById(R.id.ibMinus);
                tvPlastic = itemView.findViewById(R.id.tvPlastic);
                textPlastic = itemView.findViewById(R.id.textPlastic);
                ivPlastic = itemView.findViewById(R.id.ivPlastic);

            }
        }

        @NonNull
        @Override
        public IccTableInfoAdapter.IccTableInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_icc_detail, parent, false);
            return new IccTableInfoAdapter.IccTableInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final IccTableInfoAdapter.IccTableInfoViewHolder holder, final int position) {
            int drawables[] = {R.drawable.plastic01, R.drawable.plastic02, R.drawable.plastic03, R.drawable.plastic04,
                    R.drawable.plastic_bag01, R.drawable.plastic_bag02, R.drawable.washless01, R.drawable.washless02, R.drawable.washless03,
                    R.drawable.other01, R.drawable.other02, R.drawable.other03, R.drawable.fish01, R.drawable.fish02, R.drawable.fish03,
                    R.drawable.person01, R.drawable.person02, R.drawable.smoke01, R.drawable.smoke02,
                    R.drawable.trash, R.drawable.trash, R.drawable.trash, R.drawable.trash, R.drawable.weight};

            holder.ivPlastic.setImageResource(drawables[position]);

            if (position >= 0 && position <= 23) {
                holder.textPlastic.setText(iccTableInfo.getIccTable().getIccName(position));
                if (position != 23) {
                    holder.tvPlastic.setText(iccTableInfo.getIccTable().getIcc(position).toString());
                } else {
                    double weight = (Double)iccTableInfo.getIccTable().getIcc(position);
                    holder.tvPlastic.setText(String.format("%.2f", weight));
                }

                holder.ibPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position != 23) {
                            int count = Integer.valueOf(holder.tvPlastic.getText().toString()) + 1;
                            iccTableInfo.getIccTable().setIcc(position, count);
                            holder.tvPlastic.setText(String.valueOf(count));
                        } else {
                            double weight = Double.valueOf(holder.tvPlastic.getText().toString()) + 0.1;
                            iccTableInfo.getIccTable().setIcc(position, weight);
                            holder.tvPlastic.setText(String.format("%.2f", weight));
                        }
                    }
                });

                holder.ibMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position != 23) {
                            int count = Integer.valueOf(holder.tvPlastic.getText().toString()) - 1;
                            if (count >= 0) {
                                iccTableInfo.getIccTable().setIcc(position, count);
                                holder.tvPlastic.setText(String.valueOf(count));
                            } else {
                                iccTableInfo.getIccTable().setIcc(position, 0);
                                holder.tvPlastic.setText(String.valueOf(0));
                            }
                        } else {
                            double weight = Double.valueOf(holder.tvPlastic.getText().toString()) - 0.1;
                            if (weight >= 0.0) {
                                iccTableInfo.getIccTable().setIcc(position, weight);
                                holder.tvPlastic.setText(String.format("%.2f", weight));
                            }
                            else {
                                iccTableInfo.getIccTable().setIcc(position, 0.0);
                                holder.tvPlastic.setText(String.format("%.2f", 0.0));
                            }
                        }
                    }
                });

                holder.tvPlastic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText input = new EditText(activity);
                        if (position != 23)
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        else
                            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        new AlertDialog.Builder(activity)
                                .setTitle(holder.textPlastic.getText().toString())
                                .setMessage("輸入數量")
                                .setView(input)
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!input.getText().toString().isEmpty()) {
                                            if (position != 23) {
                                                int count = Integer.valueOf(input.getText().toString());
                                                iccTableInfo.getIccTable().setIcc(position, count);
                                                holder.tvPlastic.setText(String.valueOf(count));
                                            } else {
                                                double weight = Double.valueOf(input.getText().toString());
                                                iccTableInfo.getIccTable().setIcc(position, weight);
                                                holder.tvPlastic.setText(String.format("%.2f", weight));
                                            }
                                        }
                                    }
                                }).setNegativeButton("取消", null).create()
                                .show();
                    }
                });

            }

        }

    }

    private void showIccTableDetail(IccTableInfo iccTableInfo) {
        if (iccTableInfo == null) {
            Common.showToast(activity, "沒有ICC表格");
            return;
        }

        IccTableInfoAdapter iccTableInfoAdapter = (IccTableInfoAdapter) rvIccDetail.getAdapter();
        if (iccTableInfoAdapter == null) {
            rvIccDetail.setAdapter(new IccTableInfoAdapter(activity, iccTableInfo));
        } else {
            iccTableInfoAdapter.setIccTableInfo(iccTableInfo);
            iccTableInfoAdapter.notifyDataSetChanged();
        }
    }

    private IccTableInfo getIccTableInfo(int userId, int partyId) {
        IccTableInfo iccTableInfo = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "IccTableServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getIccTableInfo");
            jsonObject.addProperty("userId", userId);
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                iccTableInfo = gson.fromJson(jsonIn, IccTableInfo.class);
//                System.out.println(jsonOut);
//                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return iccTableInfo;
    }
}
