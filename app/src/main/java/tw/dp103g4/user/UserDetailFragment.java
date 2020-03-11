package tw.dp103g4.user;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class UserDetailFragment extends Fragment {
    private final static String TAG = "TAG_DetailFragment";
    private static final int REQ_TAKE_PICTURE = 0;
    private static final int REQ_PICK_IMAGE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private byte[] image;

    private FragmentActivity activity;
    private String account;
    private User user;
    private int userId;

    private ImageView ivUser;
    private TextView tvAccountShow;
    private EditText etEmailShow, etNameShow;
    private Button btTakePic, btLoadPic, btSave, btCancel;
    private SharedPreferences pref;
    private Uri contentUri, croppedImageUri;

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
        activity.setTitle("編輯會員資訊");
        return inflater.inflate(R.layout.fragment_user_detail, container, false);

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


        pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        userId = pref.getInt("id", 0);
        account = pref.getString("account", "");

        String url = Common.URL_SERVER + "UserServlet";

        tvAccountShow = view.findViewById(R.id.tvAccountShow);
        etEmailShow = view.findViewById(R.id.etEmailShow);
        etNameShow = view.findViewById(R.id.etNameShow);

        btTakePic = view.findViewById(R.id.btTakePic);
        btLoadPic = view.findViewById(R.id.btLoadPic);
        btSave = view.findViewById(R.id.btSave);
        btCancel = view.findViewById(R.id.btCancel);
        ivUser = view.findViewById(R.id.ivUser);


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "findById");
        jsonObject.addProperty("id", userId);
        CommonTask userDetailTask = new CommonTask(url, jsonObject.toString());
        try {
            String result = userDetailTask.execute().get();
            //把取出的資料放到對應位置上
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            user = gson.fromJson(result, User.class);
            //result轉型成boolean回傳到isValid
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        showUser();

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
                startActivityForResult(intent, REQ_PICK_IMAGE);
            }
        });

//從此
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmailShow.getText().toString();
                String name = etNameShow.getText().toString();

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "UserServlet";
                    user.setFields(email, name);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "update");
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                    jsonObject.addProperty("user", gson.toJson(user));
                    if (image != null) {
                        jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
                    }
                    int count = 0;

                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    if (count == 0) {

                        Common.showToast(activity, "儲存失敗");
                    } else {
                        SharedPreferences pref = activity
                                .getSharedPreferences(Common.PREFERENCE_MEMBER, Context.MODE_PRIVATE);
                        pref.edit().putString("name", name).apply();
                        Common.showToast(activity, "儲存成功");
                    }
                } else {
                    Common.showToast(activity, "連線失敗");
                }
                navController.popBackStack();
            }

        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
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
            bitmap = new ImageTask(url, userId, imageSize).execute().get();
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


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_TAKE_PICTURE:
                    crop(contentUri);
                    break;
                case REQ_PICK_IMAGE:
                    Uri uri = intent.getData();
                    crop(uri);
                    break;
                case REQ_CROP_PICTURE:
                    Log.d(TAG, "REQ_CROP_PICTURE:" + croppedImageUri.toString());

                    try {
                        Bitmap picture = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(croppedImageUri));

                        ivUser.setImageBitmap(picture);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;

            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        croppedImageUri = Uri.fromFile(file);

        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.setDataAndType(sourceImageUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 0);
            cropIntent.putExtra("aspectY", 0);
            cropIntent.putExtra("outputX", 0);
            cropIntent.putExtra("outputY", 0);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, croppedImageUri);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, REQ_CROP_PICTURE);
        } catch (ActivityNotFoundException anfe) {
            Common.showToast(activity, "This device doesn't support the crop action!");
        }


    }
}
