package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;

import static android.app.Activity.RESULT_OK;


public class PieceCreateFragment extends Fragment {
    private Activity activity;
    private static final String TAG = "TAG_PieceCreate";
    private static final int REQ_PICK_PICTURE = 1;
    private ImageView ivAfter;
    private Button btPieceOK, btPieceRe, btPieceUpload;
    private byte[] image;
    private Party party;
    private int partyId;
    private final int review = 0, post = 1, close = 2, start = 3, end = 4, delete = 5;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public PieceCreateFragment() {
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
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_piece_create, container, false);
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

        final Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("party") == null) {
            navController.popBackStack();
            return;
        }
        party = (Party) bundle.getSerializable("party");
        partyId = party.getId();

        ivAfter = view.findViewById(R.id.ivAfter);
        btPieceUpload = view.findViewById(R.id.btPieceUpload);
        btPieceOK = view.findViewById(R.id.btPieceOK);
        btPieceRe = view.findViewById(R.id.btPieceRe);

        btPieceUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 上傳圖片
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_PICTURE);
            }
        });

        btPieceOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "PartyServlet";

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "setAfterImg");
                    jsonObject.addProperty("id", partyId);
                    // 有圖才上傳
                    if (image != null) {
                        jsonObject.addProperty("imageBase64", Base64.encodeToString(image, Base64.DEFAULT));
                    }

                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();
                        count = Integer.valueOf(result.trim());
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    if (count == 0) {
                        Common.showToast(getActivity(), R.string.textInsertFail);
                    } else {
//                        Common.showToast(getActivity(), R.string.textInsertSuccess);

                        if (changePartyState(partyId, end)) {
                            party.setState(end);
                        }

                        navController.popBackStack();
                    }

                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

        btPieceRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivAfter.setImageResource(R.drawable.upload_img);
                image = null;
            }
        });

    }

    private boolean changePartyState(int partyId, int state) {
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "changePartyState");
            jsonObject.addProperty("id", gson.toJson(partyId));
            jsonObject.addProperty("state", gson.toJson(state));
            String jsonOut = jsonObject.toString();

            int count = 0;
            try {
                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                System.out.println(jsonOut);
                count = Integer.valueOf(result.trim());

                if (count == 0) {
                    Common.showToast(getActivity(), R.string.textChageStateFail);
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
            return false;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_PICK_PICTURE:
                    Uri uri = intent.getData();
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                    activity.getContentResolver().openInputStream(uri));
                            ivAfter.setImageBitmap(bitmap);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            image = out.toByteArray();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    if (bitmap != null) {
                        ivAfter.setImageBitmap(bitmap);
                    } else {
                        ivAfter.setImageResource(R.drawable.no_image);
                    }
                    break;

            }
        }
    }
}
