package tw.dp103g4.partydetail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.ImageTask;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class PieceUpdateFragment extends Fragment {
    private Activity activity;
    private static final String TAG = "TAG_PieceInsert";

    private static final int REQ_PICK_PICTURE = 1;
    private CommonTask getAllPieceImgTask, pieceTask;
    private ImageTask pieceImgTask;
    private RecyclerView rvInsertImg;
    private TextView tvPartyName, tvTitle;
    private Button btPieceInsertOK, btPieceInsertRe, btUploadImg;
    private EditText etContent;
    private CardView countImg;
    private TextView tvCountImg;
    private final int REQ_PICK_IMAGES = 101;
    private Bundle bundle;
    private int partyId, pieceId;
    private PartyInfo partyInfo;
    private List<String> imagesBase64;
    private PagerSnapHelper pagerSnapHelper;
    private LinearLayoutManager linearLayoutManager;
    private TextView tvPieceContent;
    private PartyPiece partyPiece;
    private List<PieceImg> pieceImgs;


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
        return inflater.inflate(R.layout.fragment_piece_insert, container, false);
    }

    @SuppressLint("WrongThread")
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

        tvTitle = view.findViewById(R.id.title);

        countImg = view.findViewById(R.id.countImg);
        tvCountImg = view.findViewById(R.id.tvCountImg);

        tvPartyName = view.findViewById(R.id.tvPartyName);
        rvInsertImg = view.findViewById(R.id.rvInsertImg);
        btPieceInsertOK = view.findViewById(R.id.btPieceInsertOK);
        btPieceInsertRe = view.findViewById(R.id.btPieceInsertRe);
        etContent = view.findViewById(R.id.etPieceContent);
        btUploadImg = view.findViewById(R.id.btUploadImg);

        tvPieceContent = view.findViewById(R.id.textView17);

        tvTitle.setText("修改花絮");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        final int userId = pref.getInt("id", 0);

        bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0 || bundle.getInt("pieceId") == 0) {
            navController.popBackStack();
            return;
        }

        partyId = bundle.getInt("partyId");
        pieceId = bundle.getInt("pieceId");

        partyInfo = getPartyInfo(partyId, userId);
        tvPartyName.setText(partyInfo.getParty().getName());

        linearLayoutManager  = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
        rvInsertImg.setLayoutManager(linearLayoutManager);

        if (pagerSnapHelper == null)
            pagerSnapHelper = new PagerSnapHelper();
        if (rvInsertImg.getOnFlingListener() == null)
            pagerSnapHelper.attachToRecyclerView(rvInsertImg);


        pieceImgs = getPieceImgs(pieceId);

        for (PieceImg pieceImg: pieceImgs) {
            String url = Common.URL_SERVER + "PieceImgServlet";
            final int id = pieceImg.getId();
            int imageSize = getResources().getDisplayMetrics().widthPixels;
            pieceImgTask = new ImageTask(url, id, imageSize);
            try {
                Bitmap bitmap = pieceImgTask.execute().get();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byte[] image = out.toByteArray();
                String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                imagesBase64.add(imageBase64);

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        showImgs(imagesBase64);

        partyPiece = getPartyPiece(pieceId);
        etContent.setText(partyPiece.getContent());




        // for demo
        tvPieceContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etContent.setText("我剛剛傳錯了啦, 淨灘成功ya");
            }
        });

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


                if (etContent.getText().toString().isEmpty()) {
                    Common.showToast(activity, "輸入不可為空!");
                    return;
                }

                int maxContent = 500;
                if (etContent.getText().toString().length() > maxContent) {
                    Common.showToast(activity, "輸入超過上限");
                    return;
                }


                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "PartyPieceServlet";

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "pieceUpdate");
                    PartyPiece partyPiece = new PartyPiece(pieceId, userId, partyId, etContent.getText().toString());
                    jsonObject.addProperty("piece", gson.toJson(partyPiece));
                    jsonObject.addProperty("imagesBase64", gson.toJson(imagesBase64));

                    int count = 0;
                    try {
                        String result = new CommonTask(url, jsonObject.toString()).execute().get();

//                        System.out.println(jsonObject.toString());

                        count = Integer.valueOf(result.trim());

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    if (count == 0) {
                        Common.showToast(getActivity(), R.string.textInsertFail);
                    } else {
//                        Common.showToast(getActivity(), R.string.textInsertSuccess);

                        navController.popBackStack();
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
                pieceImgs = getPieceImgs(pieceId);

                for (PieceImg pieceImg: pieceImgs) {
                    String url = Common.URL_SERVER + "PieceImgServlet";
                    final int id = pieceImg.getId();
                    int imageSize = getResources().getDisplayMetrics().widthPixels;
                    pieceImgTask = new ImageTask(url, id, imageSize);
                    try {
                        Bitmap bitmap = pieceImgTask.execute().get();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        byte[] image = out.toByteArray();
                        String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                        imagesBase64.add(imageBase64);

                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }

                showImgs(imagesBase64);

                partyPiece = getPartyPiece(pieceId);
                etContent.setText(partyPiece.getContent());
            }
        });

    }

    private PartyPiece getPartyPiece(int pieceId) {
        PartyPiece partyPiece = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyPieceServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getOneById");
            jsonObject.addProperty("id", pieceId);
            String jsonOut = jsonObject.toString();
            pieceTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = pieceTask.execute().get();
                partyPiece = gson.fromJson(jsonIn, PartyPiece.class);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return partyPiece;
    }

    private List<PieceImg> getPieceImgs(int pieceId) {
        List<PieceImg> pieceImgs = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PieceImgServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getPieceImgs");
            jsonObject.addProperty("pieceId", pieceId);
            String jsonOut = jsonObject.toString();
            getAllPieceImgTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = getAllPieceImgTask.execute().get();
                Type listType = new TypeToken<List<PieceImg>>() {
                }.getType();
                pieceImgs = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return pieceImgs;
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
                ivImg = itemView.findViewById(R.id.ivPieceImg);
            }
        }

        @NonNull
        @Override
        public ImgAdapter.ImgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_piece_img, parent, false);
            return new ImgAdapter.ImgViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(@NonNull ImgAdapter.ImgViewHolder holder, final int position) {
            if (imgs.size() == 0) {
                holder.ivImg.setImageResource(R.drawable.upload_img);
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

    private void showImgs(final List<String> imgs) {
        if (imgs.size() > 1) {
            countImg.setVisibility(View.VISIBLE);
            tvCountImg.setVisibility(View.VISIBLE);
            tvCountImg.setText(String.valueOf(1) + "/" + imgs.size());

            rvInsertImg.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        //Dragging
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int position = linearLayoutManager.findFirstVisibleItemPosition();
                        tvCountImg.setText(String.valueOf(position+1) + "/" + imgs.size());
                    }
                }

            });

        } else {
            countImg.setVisibility(View.GONE);
            tvCountImg.setVisibility(View.GONE);
        }

        ImgAdapter imgAdapter = (ImgAdapter) rvInsertImg.getAdapter();
        if (imgAdapter == null) {
            rvInsertImg.setAdapter(new ImgAdapter(activity, imgs));
        } else {
            imgAdapter.setImgs(imgs);
            imgAdapter.notifyDataSetChanged();
        }

    }

    private PartyInfo getPartyInfo(int id, int userId) {
        PartyInfo partyInfo = null;

        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "PartyServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getParty");
            jsonObject.addProperty("id", id);
            jsonObject.addProperty("userId", userId);
            String jsonOut = jsonObject.toString();
            try {
                String jsonIn = new CommonTask(url, jsonOut).execute().get();
                partyInfo = gson.fromJson(jsonIn, PartyInfo.class);
//                System.out.println(jsonOut);
//                System.out.println(jsonIn);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(getActivity(), R.string.textNoNetwork);
        }
        return partyInfo;
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

    @Override
    public void onStop() {
        super.onStop();
        if (getAllPieceImgTask != null) {
            getAllPieceImgTask.cancel(true);
            getAllPieceImgTask = null;
        }
    }
}
