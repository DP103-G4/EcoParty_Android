package tw.dp103g4.user;

import android.app.Activity;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import tw.dp103g4.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;


public class LoginFragment extends Fragment {

    private static final String TAG = "TAG_LoginFragment";

    //    private Gson gson;
    private Activity activity;
    private Button btRegister, btLogin;
    private TextView tvMsg, tvForgot;
    private EditText etAccount, etPassword;
    private CommonTask userLoginTask, userGetAllTask;
    private List<User> users;
    private String account, password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
//        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle("Login");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvMsg = view.findViewById(R.id.tvMsg);
        tvForgot = view.findViewById(R.id.tvForgot);
        etAccount = view.findViewById(R.id.etAccount);
        etPassword = view.findViewById(R.id.etPassword);
        users= getUsers();


        btRegister = view.findViewById(R.id.btRegister);
        btLogin = view.findViewById(R.id.btLogin);

        //去註冊頁面
        btRegister.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_userInsertFragment);
            }
        });


        //登入判斷
        //先導到detail
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
                boolean isValid = false;

                //Client輸入後用CommonTask去Server
                userLoginTask = new CommonTask(url, jsonObject.toString());
                try {
                    String result = userLoginTask.execute().get();
                    //result轉型成boolean回傳到isValid
                    isValid = Boolean.parseBoolean(result.trim());
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (isValid) {      //成功
                    Common.showToast(getActivity(), "Login Success");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("account", account);
                    Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_userDetailFragment,bundle);

                } else {            //失敗
                    Common.showToast(getActivity(), "Login Fail");
                }

//
//                Bundle bundle = new Bundle();
//
//                bundle.putString("account", account);
//                bundle.putString("password", password);


                //帳密空白
                if (account.isEmpty() || password.isEmpty()) {
                    tvMsg.setText("Account or Password is empty");
                    return;
                }
                tvMsg.setText("");


            }
        });

        //忘記密碼



    }

    private List<User> getUsers() {
        //確認網路連線
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "UserServlet";
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            userGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = userGetAllTask.execute().get();
                Type listType = new TypeToken<List<User>>() {
                }.getType();
//
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                users = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return users;

    }


}


// client -> Server -> DB
// client <- Server <- 帶資料給Server判斷<-DB
// client <- true <- Server有資料
// client <- false <- Server無資料