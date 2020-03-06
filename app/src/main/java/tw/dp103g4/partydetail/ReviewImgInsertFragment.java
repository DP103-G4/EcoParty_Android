package tw.dp103g4.partydetail;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ReviewImgInsertFragment extends Fragment {
    private Activity activity;
    private static final String TAG = "TAG_PieceInsert";

    private static final int REQ_PICK_PICTURE = 1;
    private RecyclerView rvInsertImg;
    private Button btPieceInsertOK, btPieceInsertRe, btUploadImg;
    private final int REQ_PICK_IMAGES = 101;
    private Bundle bundle;
    private Party party;
    private byte[] cover;
    private List<String> imagesBase64;
    private PagerSnapHelper pagerSnapHelper;
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        imagesBase64 = new ArrayList<String>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_review_img_insert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        rvInsertImg = view.findViewById(R.id.rvInsertImg);
        btPieceInsertOK = view.findViewById(R.id.btPieceInsertOK);
        btPieceInsertRe = view.findViewById(R.id.btPieceInsertRe);
        btUploadImg = view.findViewById(R.id.btUploadImg);

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        final int userId = pref.getInt("id", 0);


        bundle = getArguments();
        if (bundle == null || bundle.getSerializable("party") == null) {
            navController.popBackStack();
            return;
        }
        party = (Party) bundle.getSerializable("party");
        cover = bundle.getByteArray("cover");

        rvInsertImg.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        if (pagerSnapHelper == null)
            pagerSnapHelper = new PagerSnapHelper();
        if (rvInsertImg.getOnFlingListener() == null)
            pagerSnapHelper.attachToRecyclerView(rvInsertImg);

        showImgs(imagesBase64);

        btUploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_PICK_IMAGES);
            }
        });

        btPieceInsertOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagesBase64.size() == 0) {
                    Common.showToast(activity, "請上傳圖片");
                    return;
                }

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "PartyServlet";

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "partyInsert");
                    jsonObject.addProperty("party", gson.toJson(party));
                    jsonObject.addProperty("imagesBase64", gson.toJson(imagesBase64));
                    // 有圖才上傳
                    if (cover != null) {
                        jsonObject.addProperty("imageBase64", Base64.encodeToString(cover, Base64.DEFAULT));
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
                        Common.showToast(getActivity(), R.string.textInsertSuccess);
                        navController.popBackStack(R.id.partyFragment, true);
                    }

                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });

        btPieceInsertRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagesBase64.clear();
                showImgs(imagesBase64);
            }
        });

    }

    private class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.ImgViewHolder> {
        private List<String> imgs;
        private LayoutInflater layoutInflater;


        ImgAdapter(Context context, List<String> imgs) {
            layoutInflater = LayoutInflater.from(context);
            this.imgs = imgs;
        }

        void setImgs(List<String> imgs) {
            this.imgs = imgs;
        }


        private class ImgViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImg;

            public ImgViewHolder(View itemView) {
                super(itemView);
                ivImg = itemView.findViewById(R.id.ivReviewImg);
            }
        }

        @NonNull
        @Override
        public ImgAdapter.ImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_review_img, parent, false);
            return new ImgAdapter.ImgViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(@NonNull ImgAdapter.ImgViewHolder holder, final int position) {
            if (imgs.size() == 0) {
                holder.ivImg.setImageResource(R.drawable.upload_review);
            } else {
                String img = imgs.get(position);
                //decode base64 string to image
                byte[] imageBytes = Base64.decode(img, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.ivImg.setImageBitmap(decodedImage);
                holder.ivImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(activity, v, Gravity.END);
                        popupMenu.inflate(R.menu.img_menu);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.imgRemove:
                                        if (imgs.size() != 0) {
                                            imgs.remove(position);
                                            showImgs(imgs);
                                        }
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (imgs.size() == 0)
                return 1;
            else
                return imgs.size();
        }

    }

    private void showImgs(List<String> imgs) {
        ImgAdapter imgAdapter = (ImgAdapter) rvInsertImg.getAdapter();
        if (imgAdapter == null) {
            rvInsertImg.setAdapter(new ImgAdapter(activity, imgs));
        } else {
            imgAdapter.setImgs(imgs);
            imgAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == REQ_PICK_IMAGES && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Bitmap bitmap = null;
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    byte[] image;
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        bitmap = BitmapFactory.decodeStream(
                                activity.getContentResolver().openInputStream(uri));
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        image = out.toByteArray();
                        String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                        imagesBase64.add(imageBase64);
                    }

                    showImgs(imagesBase64);
                }
            } else {
                Toast.makeText(activity, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

}
