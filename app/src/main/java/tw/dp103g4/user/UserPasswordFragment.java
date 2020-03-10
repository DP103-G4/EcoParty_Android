package tw.dp103g4.user;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutionException;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

public class UserPasswordFragment extends Fragment {
    private static final String TAG = "TAG_PasswordFragment";
    private FragmentActivity activity;
    private EditText etOlder, etNew, etNewAgain;
    private Button btCancel, btOK;
    private CommonTask editPasswordTask;
    private int userId;

    private BottomNavigationView bottomNavigationView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity.setTitle("修改密碼");
        return inflater.inflate(R.layout.fragment_user_password, container, false);
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

        bottomNavigationView = activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.GONE);

        etOlder = view.findViewById(R.id.etOlder);
        etNew = view.findViewById(R.id.etNew);
        etNewAgain = view.findViewById(R.id.etNewAgain);
        btCancel = view.findViewById(R.id.btCancel);
        btOK = view.findViewById(R.id.btOK);

        userId = Common.getUserId(activity);
        if (userId == 0) {
            navController.popBackStack();
        }

        //都有輸入就送出，且修改

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pwOlder = etOlder.getText().toString().trim();
                String pwNew = etNew.getText().toString().trim();
                String pwNewAgain = etNewAgain.getText().toString().trim();

                //任一editText空白時
                if (etOlder.length() == 0 || etNew.length() == 0 || etNewAgain.length() == 0) {
//                    etOlder.setError("");
                    Common.showToast(getActivity(), "不可空白");
                    return;
                } else if (pwOlder.equals(pwNew)) {
                    //舊密碼和新密碼相同時
                    Common.showToast(getActivity(), "舊密碼不可與新密碼相同");
                    return;
                } else if (!(pwNew.equals(pwNewAgain))) {
                    //新密碼和確認新密碼不相同時
                    Common.showToast(getActivity(), "新密碼有誤");
                    return;

                }

                String url = Common.URL_SERVER + "UserServlet";
                JsonObject jsonObject = new JsonObject();

                //對應Server的update
                jsonObject.addProperty("action", "changePassword");
                jsonObject.addProperty("id", userId);
                jsonObject.addProperty("oldPassword", pwOlder);
                jsonObject.addProperty("newPassword", pwNew);
                editPasswordTask = new CommonTask(url, jsonObject.toString());
                int count = 0;
                try {
                    String result = editPasswordTask.execute().get();
                    count = Integer.parseInt(result);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (count == 0) {
                    //舊密碼輸入有誤時
                    Common.showToast(getActivity(), "修改失敗");
                } else {
                    Common.showToast(getActivity(), "修改成功");
                    navController.popBackStack();
                }


            }
        });


        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });

    }

//    private int update(String password,byte[] userImg) {
//        //確認網路連線
//        int id = 0;
//        if (Common.networkConnected(activity)) {
//            String url = Common.URL_SERVER + "UserServlet";
//            JsonObject jsonObject = new JsonObject();
//
//            jsonObject.addProperty("action", "update");
//            jsonObject.addProperty("password", password);
//
//            String jsonOut = jsonObject.toString();
//            userGetIdTask = new CommonTask(url, jsonOut);
//            try {
//                //傳入String 回傳String 轉型int(id)
//                String result = userGetIdTask.execute().get();
//                Log.d(TAG, result);
//                id = Integer.parseInt(result);
//            } catch (Exception e) {
//                Log.e(TAG, e.toString());
//            }
//        } else {
//            Common.showToast(activity, R.string.textNoNetwork);
//        }
//        return id;
//
//    }
}
