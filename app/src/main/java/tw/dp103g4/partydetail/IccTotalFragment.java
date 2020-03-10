package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;


public class IccTotalFragment extends Fragment {
    private static final String TAG = "IccTotalFragment";
    private Activity activity;
    private Bundle bundle;
    private int partyId;
    private List<IccTableInfo> iccTableInfos;
    private TextView btIccDetailOK, btIccDetailRe;

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


        btIccDetailOK = view.findViewById(R.id.btIccTotalOK);
        btIccDetailRe = view.findViewById(R.id.btIccTotalRe);


        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            navController.popBackStack();
            return;
        }
        partyId = bundle.getInt("partyId");

        iccTableInfos = getIccTableInfos(partyId);


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
