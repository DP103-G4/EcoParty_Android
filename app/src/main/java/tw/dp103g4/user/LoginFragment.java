package tw.dp103g4.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;


public class LoginFragment extends Fragment {

    private static final String TAG = "TAG_LoginFragment";

    private Activity activity;
    private Button btRegister, btLogin;
    private TextView tvMsg, tvForgot, tvWelcome;
    private EditText etAccount, etPassword;
    private CommonTask userLoginTask, userGetIdTask;
    private List<User> users;
    private String account, password;

    private TextView tvTest1, tvTest3, tvTest5;

    private BottomNavigationView bottomNavigationView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle("登入");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomNavigationView = activity.findViewById(R.id.navigation);
        bottomNavigationView.setVisibility(View.GONE);

        final NavController navController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });



//        tvForgot = view.findViewById(R.id.tvForgot);
        etAccount = view.findViewById(R.id.etAccount);
        etPassword = view.findViewById(R.id.etPassword);
        tvMsg = view.findViewById(R.id.tvLoginMsg);

        btRegister = view.findViewById(R.id.btRegister);
        btLogin = view.findViewById(R.id.btLogin);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTest1 = view.findViewById(R.id.tvTest1);
        tvTest3 = view.findViewById(R.id.tvTest3);
        tvTest5 = view.findViewById(R.id.tvTest5);
        //去註冊頁面
        btRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_userInsertFragment);
            }
        });

        tvWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAccount.setText("tintin");
                etPassword.setText("111");
            }
        });

        tvTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAccount.setText("kyoko01");
                etPassword.setText("111");
            }
        });

        tvTest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAccount.setText("hawk03");
                etPassword.setText("111");
            }
        });

        tvTest5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAccount.setText("zhan05");
                etPassword.setText("111");
            }
        });



        //登入判斷
        //成功後導到 會員
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String msg = tvMsg.getText().toString().trim();
                String url = Common.URL_SERVER + "UserServlet";
                JsonObject jsonObject = new JsonObject();

                //對應Server的isLogin
                jsonObject.addProperty("action", "isLogin");
                jsonObject.addProperty("account", account);
                jsonObject.addProperty("password", password);
                int isValid = 0;

                //Client輸入後用CommonTask去Server
                userLoginTask = new CommonTask(url, jsonObject.toString());
                try {
                    String result = userLoginTask.execute().get();
                    //result轉型成boolean回傳到isValid
                    isValid = Integer.parseInt(result.trim());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (isValid == 1) {      //登入成功
                    Common.showToast(getActivity(), "登入成功");
                    //  偏好設定檔
                    SharedPreferences pref = activity
                            .getSharedPreferences(Common.PREFERENCE_MEMBER, Context.MODE_PRIVATE);
                    User user = getUserByAccount(account);
                    pref.edit().putString("account", account)
                            .putString("password", password)
                            .putInt("id", user.getId()).putString("name", user.getName()).apply();
                    Navigation.findNavController(v).popBackStack(R.id.partyFragment, false);


                } else  if (isValid == 2) {            //登入失敗
                    tvMsg.setText("該帳號已被停權");
                    return;

                } else {
                    tvMsg.setText("帳號或密碼有誤");
                    return;
                }

                //帳密空白
                if (account.isEmpty() || password.isEmpty()) {
                    tvMsg.setText("帳號和密碼不可空白");
                    return;
                }
                tvMsg.setText("");

            }
        });

//        //忘記密碼
//        tvForgot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_userForgetFragment);
//            }
//        });

    }

    private User getUserByAccount(String account) {
        //確認網路連線
        User user = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "UserServlet";
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("action", "getUserByAccount");
            jsonObject.addProperty("account", account);
            String jsonOut = jsonObject.toString();
            userGetIdTask = new CommonTask(url, jsonOut);
            try {
                //傳入String 回傳String 轉型int(id)
                String userJson = userGetIdTask.execute().get();
                user = new Gson().fromJson(userJson, User.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return user;

    }


}