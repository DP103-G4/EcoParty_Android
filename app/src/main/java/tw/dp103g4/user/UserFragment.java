package tw.dp103g4.user;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;

import static android.content.Context.MODE_PRIVATE;

public class UserFragment extends Fragment {
//    private TextView tvTest, tvLogin;

    private final static String TAG = "TAG_UserFragment";
//    private String account;

    private FragmentActivity activity;
    private TextView tvLogin, tvUserName, tvEdit, tvChangePassword, tvMyParty, tvSignOut;
    //    private ImageView ivUser;
    private User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String url = Common.URL_SERVER + "UserServlet";
        SharedPreferences preferences =
                activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);

        int userId = 0;

        tvLogin = view.findViewById(R.id.tvLogin);
        tvChangePassword = view.findViewById(R.id.tvChangePassword);
        tvMyParty = view.findViewById(R.id.tvMyParty);
        tvSignOut = view.findViewById(R.id.tvSignOut);
        tvEdit = view.findViewById(R.id.tvEdit);


//        if (preferences.getInt("userId", userId) != 0) {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", "findById");
//            jsonObject.addProperty("userId", userId);
//            CommonTask userDetailTask = new CommonTask(url, jsonObject.toString());
//            try {
//                String result = userDetailTask.execute().get();
//                //把取出的資料放到對應位置上
//                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
//                user = gson.fromJson(result, User.class);
//                showUser();
//                //result轉型成boolean回傳到isValid
//            } catch (Exception e) {
//                Log.e(TAG, e.toString());
//            }
//        } else {
//
//        }


        //去 登入頁
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_userFragment_to_loginFragment);
            }
        });


        //去 編輯會員資料
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_userFragment_to_userDetailFragment);
            }
        });


        //去 修改密碼
        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_userFragment_to_userPasswordFragment);
            }
        });

        //去 我的活動
        tvMyParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_userFragment2_to_myPartyFragment);

            }
        });
    }
//
//    private void showUser() {
//        String url = Common.URL_SERVER + "UserServlet";
//        String email = user.getEmail();
//        String name = user.getName();
//        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
//        Bitmap bitmap = null;
//        try {
////            bitmap = new ImageTask(url, user.getId(), imageSize).execute().get();
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
//        if (bitmap != null) {
//            ivUser.setImageBitmap(bitmap);
//        } else {//若無圖，show 無圖的圖片
//            ivUser.setImageResource(R.drawable.no_image);
//        }
//        tvAccountShow.setText(account);
//        tvEmailShow.setText(email);
//        tvNameShow.setText(name);
//
//    }


}
