package tw.dp103g4.user;



import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import tw.dp103g4.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

public class UserDetailFragment extends Fragment {
    private final static String TAG = "TAG_DetailFragment";
    private String account;
    private FragmentActivity activity;
    private TextView tvEmailShow, tvNameShow;
    //    private Button btEdit, btCancel;
    private ImageView ivUser;
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
        activity.setTitle("User Detail");
        return inflater.inflate(R.layout.fragment_detail, container,false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        final NavController navController = Navigation.findNavController(view);
        if (bundle == null || bundle.getSerializable("account") == null) {
            Common.showToast(activity, "No User Found");
            navController.popBackStack();
            return;
        }
        //取出bundle資料
        account = (String) bundle.getSerializable("account");
        String url = Common.URL_SERVER + "UserServlet";
        tvEmailShow = view.findViewById(R.id.tvEmailShow);
        tvNameShow = view.findViewById(R.id.tvNameShow);
//        btCancel = view.findViewById(R.id.btCancel);
//        btEdit = view.findViewById(R.id.btEdit);
        ivUser = view.findViewById(R.id.ivUser);



        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "findById");
        jsonObject.addProperty("account", account);
        CommonTask userDetailTask = new CommonTask(url, jsonObject.toString());
        try {
            String result = userDetailTask.execute().get();
            //把取出的資料放到對應位置上
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            user = gson.fromJson(result, User.class);
            showUser();
            //result轉型成boolean回傳到isValid
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
//Detail = 用Bundle裝資料



//        if (bundle != null) {
//            String isbnNum = bundle.getString("isbn");
//            String bookName = bundle.getString("bookName");
////
//            double price = bundle.getDouble("price");
//            String author = bundle.getString("author");
//
//            Bitmap bitmap = null;
//            ivBook.setImageBitmap(bitmap);
//
//            tvISBNnum.append("isbn");
//            etBookName.append("bookName");
//            etAuthor.append("author");
//            etPrice.append("price");
//        }
    }

    private void showUser() {
        String url = Common.URL_SERVER + "UserServlet";
        String email = user.getEmail();
        String name = user.getName();
        int imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        Bitmap bitmap = null;
        try {
//            bitmap = new ImageTask(url, user.getId(), imageSize).execute().get();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if (bitmap != null) {
            ivUser.setImageBitmap(bitmap);
        } else {//若無圖，show 無圖的圖片
            ivUser.setImageResource(R.drawable.no_image);
        }
        tvEmailShow.setText(email);
        tvNameShow.setText(name);


    }

}
