package tw.dp103g4.user;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

public class UserDetailFragment extends Fragment {
    private final static String TAG = "TAG_DetailFragment";
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private byte[] image;

    private Uri contentUri;
    private FragmentActivity activity;
    private String account;
    private User user;
    private int userId;

    private ImageView ivUser;
    private TextView tvAccountShow;
    private EditText etEmailShow, etNameShow;
    private Button btTakePic, btLoadPic, btSave, btCancel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        activity.setTitle("會員資訊");
        return inflater.inflate(R.layout.fragment_user_detail, container, false);

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

        tvAccountShow = view.findViewById(R.id.tvAccountShow);
        etEmailShow = view.findViewById(R.id.etEmailShow);
        etNameShow = view.findViewById(R.id.etNameShow);
//        tvChangePassword = view.findViewById(R.id.tvChangePassword);
//        tvMyParty = view.findViewById(R.id.tvMyParty);
//        tvSignOut = view.findViewById(R.id.tvSignOut);

        btTakePic = view.findViewById(R.id.btTakePic);
        btLoadPic = view.findViewById(R.id.btLoadPic);
        btSave = view.findViewById(R.id.btSave);
        btCancel = view.findViewById(R.id.btCancel);
        ivUser = view.findViewById(R.id.ivUser);


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "findById");
        jsonObject.addProperty("userId", userId);
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


        //拍照
        btTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定存檔路徑
                File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                file = new File(file, "picture.jpg");
                contentUri = FileProvider.getUriForFile(
                        activity, activity.getPackageName() + ".provider", file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivityForResult(intent, REQ_TAKE_PICTURE);
                } else {
                    Common.showToast(getActivity(), R.string.textNoCameraApp);
                }
            }
        });

        //存取圖庫
        btLoadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_PICTURE);
            }
        });


        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.showToast(getActivity(), "儲存成功");
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });

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
        tvAccountShow.setText(account);
        etEmailShow.setText(email);
        etNameShow.setText(name);

    }

}
