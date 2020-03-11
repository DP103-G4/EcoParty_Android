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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

import static android.content.Context.MODE_PRIVATE;


public class IccTotalFragment extends Fragment {
    private static final String TAG = "IccTotalFragment";
    private Activity activity;
    private Bundle bundle;
    private int partyId, userId;
    private PartyInfo partyInfo;
    private List<IccTableInfo> iccTableInfos;
    private TextView tvLocation, tvDate, tvDistance, tvCount, tvWeight, tvOwnerName,
            tvPlastic01, tvPlastic02, tvPlastic03, tvPlastic04,
            tvWashLess01, tvWashLess02, tvWashLess03, tvWashLess04,
            tvOthers01, tvOthers02, tvOthers03,
            tvPersonal01, tvPersonal02,
            tvSmoke01, tvSmoke02,
            tvFish01, tvFish02, tvFish03,
            tvCare01, tvCare02, tvCare03, tvCare04;



    private CommonTask getAllTask;
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
        return inflater.inflate(R.layout.fragment_icc_total, container, false);
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




        tvLocation = view.findViewById(R.id.tvIccLocation);
        tvDate = view.findViewById(R.id.tvIccDate);
        tvDistance = view.findViewById(R.id.tvIccDistance);
        tvCount = view.findViewById(R.id.tvIccUserCount);
        tvWeight = view.findViewById(R.id.tvIccTotalWeight);
        tvOwnerName = view.findViewById(R.id.tvOwnerName);
        tvPlastic01 = view.findViewById(R.id.tvIccPlastic01);
        tvPlastic02 = view.findViewById(R.id.tvIccPlastic02);
        tvPlastic03 = view.findViewById(R.id.tvIccPlastic03);
        tvPlastic04 = view.findViewById(R.id.tvIccPlastic04);
        tvOthers01 = view.findViewById(R.id.tvIccOthers01);
        tvOthers02 = view.findViewById(R.id.tvIccOthers02);
        tvOthers03 = view.findViewById(R.id.tvIccOthers03);
        tvPersonal01 = view.findViewById(R.id.tvIccPersonal01);
        tvPersonal02 = view.findViewById(R.id.tvIccPersonal02);
        tvSmoke01 = view.findViewById(R.id.tvIccSmoke01);
        tvSmoke02 = view.findViewById(R.id.tvIccSmoke02);
        tvFish01 = view.findViewById(R.id.tvIccfish01);
        tvFish02 = view.findViewById(R.id.tvIccfish02);
        tvFish03 = view.findViewById(R.id.tvIccfish03);
        tvCare01 = view.findViewById(R.id.tvIccCare01);
        tvCare02 = view.findViewById(R.id.tvIccCare02);
        tvCare03 = view.findViewById(R.id.tvIccCare03);
        tvCare04 = view.findViewById(R.id.tvIccCare04);



        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);

        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }
        partyId = bundle.getInt("partyId");

        partyInfo = getPartyInfo(partyId, userId);
        iccTableInfos = getIccTableInfos(partyId);

        tvLocation.setText(partyInfo.getParty().getLocation());
        tvDate.setText(new SimpleDateFormat("M/d").format(partyInfo.getParty().getStartTime()));
        tvDistance.setText(String.valueOf(partyInfo.getParty().getDistance()));
        tvCount.setText(String.valueOf(partyInfo.getParty().getCountCurrent()));
        tvOwnerName.setText(partyInfo.getOwnerName());

        double weight = 0.0;
        int plastic01 = 0, plastic02 = 0, plastic03 = 0, plastic04 = 0,
                others01 = 0, others02 = 0, others03 = 0,
                personal01 = 0, personal02 = 0,
                smoke01 = 0, smoke02 = 0,
                fish01 = 0, fish02 = 0, fish03 = 0,
                care01 = 0, care02 = 0, care03 = 0, care04 = 0;

        for (IccTableInfo iccTableInfo: iccTableInfos) {
            weight += iccTableInfo.getIccTable().getWeight();
            plastic01 += iccTableInfo.getIccTable().getPlastic01();
            plastic02 += iccTableInfo.getIccTable().getPlastic02();
            plastic03 += iccTableInfo.getIccTable().getPlastic03();
            plastic04 += iccTableInfo.getIccTable().getPlastic04();
            others01 += iccTableInfo.getIccTable().getOthers01();
            others02 += iccTableInfo.getIccTable().getOthers02();
            others03 += iccTableInfo.getIccTable().getOthers03();
            personal01 += iccTableInfo.getIccTable().getPersonal01();
            personal02 += iccTableInfo.getIccTable().getPersonal02();
            smoke01 += iccTableInfo.getIccTable().getSmoke01();
            smoke02 += iccTableInfo.getIccTable().getSmoke02();
            fish01 += iccTableInfo.getIccTable().getFishery01();
            fish02 += iccTableInfo.getIccTable().getFishery02();
            fish03 += iccTableInfo.getIccTable().getFishery03();
            care01 += iccTableInfo.getIccTable().getCare01();
            care02 += iccTableInfo.getIccTable().getCare02();
            care03 += iccTableInfo.getIccTable().getCare03();
            care04 += iccTableInfo.getIccTable().getCare04();
        }

        tvWeight.setText(String.format("%.2f", weight));
        tvPlastic01.setText(String.valueOf(plastic01));
        tvPlastic02.setText(String.valueOf(plastic02));
        tvPlastic03.setText(String.valueOf(plastic03));
        tvPlastic04.setText(String.valueOf(plastic04));
        tvOthers01.setText(String.valueOf(others01));
        tvOthers02.setText(String.valueOf(others02));
        tvOthers03.setText(String.valueOf(others03));
        tvPersonal01.setText(String.valueOf(personal01));
        tvPersonal02.setText(String.valueOf(personal02));
        tvSmoke01.setText(String.valueOf(smoke01));
        tvSmoke02.setText(String.valueOf(smoke02));
        tvFish01.setText(String.valueOf(fish01));
        tvFish02.setText(String.valueOf(fish02));
        tvFish03.setText(String.valueOf(fish03));
        tvCare01.setText(String.valueOf(care01));
        tvCare02.setText(String.valueOf(care02));
        tvCare03.setText(String.valueOf(care03));
        tvCare04.setText(String.valueOf(care04));

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
//                System.out.println(jsonOut);
//                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return partyInfo;
    }

    private List<IccTableInfo> getIccTableInfos ( int partyId){
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

}
