package tw.dp103g4.partydetail;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.partylist_android.Party;
import tw.dp103g4.task.CommonTask;
import tw.dp103g4.task.CoverImageTask;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class PartyUpdateFragment extends Fragment {
    private Activity activity;
    private ImageView ivCover;
    private EditText etName, etLoction, etAddress,etContent;
    private TextView tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvPostEndDate, tvPostEndTime,
            tvUpper, tvLower, tvDistance;
    private Button btPartyOK, btPartyRe, btUploadCover;
    private ImageView geoSuccess;
    private ImageButton btGeocode;
    private double longitude, latitude;
    private ScrollView scrollView;
    private SeekBar sbUpper, sbLower, sbDistance;
    private byte[] image;
    private CoverImageTask partyImageTask;
    private Party party;
    private static final int REQ_PICK_PICTURE = 1;
    private static final int REQ_CROP_PICTURE = 2;
    private Calendar calendar = Calendar.getInstance();
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final String TAG = "TAG_PartyUpdate";


    public PartyUpdateFragment() {
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
        return inflater.inflate(R.layout.fragment_party_insert, container, false);
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

        TextView tvTitle = view.findViewById(R.id.title);
        tvTitle.setText("修改活動");

        ivCover = view.findViewById(R.id.PartyImg);
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
        btPartyOK = view.findViewById(R.id.btPartyOK);
        btPartyRe = view.findViewById(R.id.btPartyRe);
        sbUpper = view.findViewById(R.id.sbUpper);
        sbLower = view.findViewById(R.id.sbLower);
        sbDistance = view.findViewById(R.id.sbDistance);
        btUploadCover = view.findViewById(R.id.btUploadCover);
        btGeocode = view.findViewById(R.id.btGeocode);
        geoSuccess = view.findViewById(R.id.geoSuccess);

        scrollView = view.findViewById(R.id.scrollview);


        btPartyOK.setText("完成");
        btPartyOK.getLayoutParams().width = 120;

        final Bundle bundle = getArguments();
        if (bundle == null || bundle.getSerializable("party") == null) {
            navController.popBackStack();
            return;
        }
        party = (Party) bundle.getSerializable("party");

        SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        final int userId = pref.getInt("id", 0);
        final String url = Common.URL_SERVER + "PartyServlet";

        final int id = party.getId();
        final int imageSize = getResources().getDisplayMetrics().widthPixels;
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        partyImageTask = new CoverImageTask(url, id, imageSize, ivCover);
        partyImageTask.execute();

        etName.setText(party.getName());
        etLoction.setText(party.getLocation());
        etAddress.setText(party.getAddress());
        etContent.setText(party.getContent());
        tvUpper.setText(String.valueOf(party.getCountUpperLimit()));
        tvLower.setText(String.valueOf(party.getCountLowerLimit()));
        tvDistance.setText(String.valueOf(party.getDistance()));
        sbUpper.setProgress(party.getCountUpperLimit());
        sbLower.setProgress(party.getCountLowerLimit());
        sbDistance.setProgress(party.getDistance());

        tvStartDate.setText(sdfDate.format(party.getStartTime()));
        tvStartTime.setText(sdfTime.format(party.getStartTime()));
        tvEndDate.setText(sdfDate.format(party.getEndTime()));
        tvEndTime.setText(sdfTime.format(party.getEndTime()));
        tvPostEndDate.setText(sdfDate.format(party.getPostEndTime()));
        tvPostEndTime.setText(sdfTime.format(party.getPostEndTime()));

        longitude = party.getLongitude();
        latitude = party.getLatitude();

        if (longitude != -181) {
            geoSuccess.setVisibility(View.VISIBLE);
        } else {
            geoSuccess.setVisibility(View.GONE);
        }

        btGeocode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = etAddress.getText().toString().trim();

                if (locationName.isEmpty()) {
                    locationName = etLoction.getText().toString().trim();
                    if (!locationName.isEmpty()) {
                        // geocode
                        Address address = geocode(locationName);
                        if (address == null) {
                            longitude = -181;
                            latitude = -181;
                            geoSuccess.setVisibility(View.GONE);

                        } else {
                            longitude = address.getLongitude();
                            latitude = address.getLatitude();
                            geoSuccess.setVisibility(View.VISIBLE);

                            Address addressReverse = reverseGeocode(latitude, longitude);
                            StringBuilder sb = new StringBuilder();
                            if (addressReverse != null) {
                                for (int i = 0; i <= addressReverse.getMaxAddressLineIndex(); i++) {
                                    sb.append(addressReverse.getAddressLine(i)).append("\n");
                                }
                            }
                            etAddress.setText(sb);

                        }
                    }
                } else {
                    // geocode
                    Address address = geocode(locationName);
                    if (address == null) {
                        longitude = -181;
                        latitude = -181;
                        geoSuccess.setVisibility(View.GONE);
                    } else {
                        longitude = address.getLongitude();
                        latitude = address.getLatitude();
                        geoSuccess.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        btUploadCover.setOnClickListener(new View.OnClickListener() {
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

        btPartyOK.setOnClickListener(new View.OnClickListener() {
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

                int maxContent = 500;
                if (etContent.getText().toString().length() > maxContent) {
                    Common.showToast(activity, "輸入超過上限");
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

                    party = new Party(party.getId(), userId, etName.getText().toString(), startTime, endTime, new Date(), postEndTime,
                            etLoction.getText().toString(), etAddress.getText().toString(), longitude, latitude, etContent.getText().toString(),
                            sbUpper.getProgress(), sbLower.getProgress(), party.getCountCurrent(), party.getState(), sbDistance.getProgress());


                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Common.networkConnected(activity)) {
                    String url = Common.URL_SERVER + "PartyServlet";

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "partyUpdate");
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




        btPartyRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重置
                partyImageTask = new CoverImageTask(url, id, imageSize, ivCover);
                partyImageTask.execute();

                etName.setText(party.getName());
                etLoction.setText(party.getLocation());
                etAddress.setText(party.getAddress());
                etContent.setText(party.getContent());
                tvUpper.setText(String.valueOf(party.getCountUpperLimit()));
                tvLower.setText(String.valueOf(party.getCountLowerLimit()));
                tvDistance.setText(String.valueOf(party.getDistance()));
                sbUpper.setProgress(party.getCountUpperLimit());
                sbLower.setProgress(party.getCountLowerLimit());
                sbDistance.setProgress(party.getDistance());

                tvStartDate.setText(sdfDate.format(party.getStartTime()));
                tvStartTime.setText(sdfTime.format(party.getStartTime()));
                tvEndDate.setText(sdfDate.format(party.getEndTime()));
                tvEndTime.setText(sdfTime.format(party.getEndTime()));
                tvPostEndDate.setText(sdfDate.format(party.getPostEndTime()));
                tvPostEndTime.setText(sdfTime.format(party.getPostEndTime()));

                longitude = party.getLongitude();
                latitude = party.getLatitude();

                if (longitude != -181) {
                    geoSuccess.setVisibility(View.VISIBLE);
                } else {
                    geoSuccess.setVisibility(View.GONE);
                }

                scrollView.fullScroll(scrollView.FOCUS_UP);

            }
        });
    }


    private Address geocode(String locationName) {
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocationName(locationName, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
    }

    private Address reverseGeocode(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(activity);
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        if (addressList == null || addressList.isEmpty()) {
            return null;
        } else {
            return addressList.get(0);
        }
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
                            bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
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


    @Override
    public void onStop() {
        super.onStop();
    }
}
