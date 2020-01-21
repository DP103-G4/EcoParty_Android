package tw.dp103g4.partydetail;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bozin.partylist_android.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import tw.dp103g4.main_android.Common;
import tw.dp103g4.main_android.MainActivity;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;

import static android.app.Activity.RESULT_OK;


public class PartyInsertFragment extends Fragment {
    private MainActivity activity;
    private ConstraintLayout layoutCover;
    private ImageView ivCover;
    private EditText etName, etLoction, etAddress,etContent;
    private TextView tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvPostEndDate, tvPostEndTime,
            tvUpper, tvLower, tvDistance;
    private SeekBar sbUpper, sbLower, sbDistance;
    private Button btOk, btCancel;
    private byte[] image;
    private Party party;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private Calendar calendar = Calendar.getInstance();
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final String TAG = "TAG_PartyInsert";


    // bundle
    final int userId = 2;

    public PartyInsertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_party_insert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getBottomNavigationView().setVisibility(View.GONE);
        final NavController navController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });
        ivCover = view.findViewById(R.id.ivCover);
        layoutCover = view.findViewById(R.id.layoutCover);
        etName = view.findViewById(R.id.etName);
        etLoction = view.findViewById(R.id.etLoction);
        etAddress = view.findViewById(R.id.etAddress);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvStartTime = view.findViewById(R.id.tvStartTime);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvEndTime = view.findViewById(R.id.tvEndTime);
        tvPostEndDate = view.findViewById(R.id.tvPostEndDate);
        tvPostEndTime = view.findViewById(R.id.tvPostEndTime);
        etContent = view.findViewById(R.id.etContent);
        tvUpper = view.findViewById(R.id.tvUpper);
        tvLower = view.findViewById(R.id.tvLower);
        tvDistance = view.findViewById(R.id.tvDistance);
        btOk = view.findViewById(R.id.btOk);
        btCancel = view.findViewById(R.id.btCancel);
        sbUpper = view.findViewById(R.id.sbUpper);
        sbLower = view.findViewById(R.id.sbLower);
        sbDistance = view.findViewById(R.id.sbDistance);

        final SimpleDateFormat sdfDate = new SimpleDateFormat("YYYY/MM/dd");
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        String nowDateString = sdfDate.format(new Date());
        String nowTimeString = sdfTime.format(new Date());

        tvStartDate.setText(nowDateString);
        tvStartTime.setText(nowTimeString);
        tvEndDate.setText(nowDateString);
        tvEndTime.setText(nowTimeString);
        tvPostEndDate.setText(nowDateString);
        tvPostEndTime.setText(nowTimeString);

        layoutCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 上傳圖片
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_PICTURE);
            }
        });

        final DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String text = String.format("%d/%02d/%02d", year, month+1, dayOfMonth);
                tvStartDate.setText(text);
            }
        };

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity, startDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TimePickerDialog.OnTimeSetListener startTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String text = String.format("%02d:%02d", hourOfDay, minute);
                tvStartTime.setText(text);
            }
        };

        tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(activity, startTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        final DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String text = String.format("%d/%d/%d", year, month+1, dayOfMonth);
                tvEndDate.setText(text);
            }
        };

        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity, endDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String text = String.format("%02d:%02d", hourOfDay, minute);
                tvEndTime.setText(text);
            }
        };

        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(activity, endTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        final DatePickerDialog.OnDateSetListener postEndDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String text = String.format("%d/%d/%d", year, month+1, dayOfMonth);
                tvPostEndDate.setText(text);
            }
        };

        tvPostEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity, postEndDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final TimePickerDialog.OnTimeSetListener postEndTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String text = String.format("%02d:%02d", hourOfDay, minute);
                tvPostEndTime.setText(text);
            }
        };

        tvPostEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(activity, postEndTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }
        });

        sbUpper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvUpper.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbLower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvLower.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDistance.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 送出
                if (etName.getText().toString().isEmpty()
                        || etLoction.getText().toString().isEmpty()
                        || etAddress.getText().toString().isEmpty()
                        || etContent.getText().toString().isEmpty()) {
                    Common.showToast(activity, "輸入不可為空!");
                    return;
                }

                if (sbUpper.getProgress() < sbLower.getProgress()) {
                    Common.showToast(activity, "人數上限不可低於下限!");
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d H:mm");
                    String dateString;
                    dateString = tvStartDate.getText().toString() + " " + tvStartTime.getText().toString();
                    Date startTime = sdf.parse(dateString);
                    dateString = tvEndDate.getText().toString() + " " + tvEndTime.getText().toString();
                    Date endTime = sdf.parse(dateString);
                    dateString = tvPostEndDate.getText().toString() + " " + tvPostEndTime.getText().toString();
                    Date postEndTime = sdf.parse(dateString);

                    if (endTime.before(startTime)) {
                        Common.showToast(activity, "結束時間不可晚於開始時間!");
                        return;
                    }

                    party = new Party(userId, etName.getText().toString(), startTime, endTime, new Date(), postEndTime,
                            etLoction.getText().toString(), etAddress.getText().toString(), -181, -181, etContent.getText().toString(),
                            sbUpper.getProgress(), sbLower.getProgress(), 0, 1, sbDistance.getProgress());


                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "PartyServlet";

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "partyInsert");
                    jsonObject.addProperty("party", gson.toJson(party));
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
                        Common.showToast(getActivity(), R.string.textInsertSuccess);
                        navController.popBackStack();
                    }

                } else {
                    Common.showToast(getActivity(), R.string.textNoNetwork);
                }
            }
        });




        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重置
                ivCover.setImageResource(R.drawable.no_image);
                etName.setText("");
                etLoction.setText("");
                etAddress.setText("");
                etContent.setText("");

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_PICK_PICTURE:
                    crop(intent.getData());
                    break;
                case REQ_CROP_PICTURE:
                    Uri uri = intent.getData();
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = BitmapFactory.decodeStream(
                                    activity.getContentResolver().openInputStream(uri));
                            ivCover.setImageBitmap(bitmap);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            image = out.toByteArray();
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    if (bitmap != null) {
                        ivCover.setImageBitmap(bitmap);
                    } else {
                        ivCover.setImageResource(R.drawable.no_image);
                    }
                    break;
            }
        }
    }

    private void crop(Uri sourceImageUri) {
        File file = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture_cropped.jpg");
        Uri uri = Uri.fromFile(file);
        // 開啟截圖功能
        Intent intent = new Intent("com.android.camera.action.CROP");
        // 授權讓截圖程式可以讀取資料
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 設定圖片來源與類型
        intent.setDataAndType(sourceImageUri, "image/*");
        // 設定要截圖
        intent.putExtra("crop", "true");
        // 設定截圖框大小，0代表user任意調整大小
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        // 設定圖片輸出寬高，0代表維持原尺寸
        intent.putExtra("outputX", 0);
        intent.putExtra("outputY", 0);
        // 是否保持原圖比例
        intent.putExtra("scale", true);
        // 設定截圖後圖片位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        // 設定是否要回傳值
        intent.putExtra("return-data", true);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            // 開啟截圖activity
            startActivityForResult(intent, REQ_CROP_PICTURE);
        } else {
            Toast.makeText(activity, R.string.textNoImageCropAppFound,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activity.getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}
