package tw.dp103g4.location;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import tw.dp103g4.R;
import tw.dp103g4.main_android.Common;
import tw.dp103g4.task.CommonTask;

import static android.content.Context.MODE_PRIVATE;

public class InfoLocationFragment extends Fragment {
    private static final int REQ_CHECK_SETTINGS = 101;
    private static final int PER_ACCESS_LOCATION = 202;
    private static final String TAG = "TAG_LocationFragment";
    private Activity activity;
    private MapView mapLocation;
    private GoogleMap map;
    private Location lastLocation;
    private List<InfoLocation> infoLocations;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CommonTask locationDeleteTask;
    private CommonTask locationGetAllTask;
    private SharedPreferences pref;
    private int memId;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();


        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                每10秒取一次,若user不用常常看位置可以設定久一點降低耗電
                .setInterval(10000)
//                移動1km才做更動, 假設設定10m就刷新會太耗電
                .setSmallestDisplacement(1000);
//        監聽器 位置有改變的話會呼叫
        locationCallback = new LocationCallback() {
            @Override
//            locationCallback檢查位置若沒有改變則不會呼叫onLocationResult,比較不耗電
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
        memId = pref.getInt("id", 0);
        mapLocation = view.findViewById(R.id.mapLocation);
        mapLocation.onCreate(savedInstanceState);
        mapLocation.onStart();
        final NavController navController = Navigation.findNavController(view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.location_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.popBackStack();
            }
        });

        final Bundle bundle = getArguments();
        if (bundle == null || bundle.getInt("partyId") == 0) {
            Common.showToast(activity, R.string.textNoParticipantFound);
            navController.popBackStack();
            return;
        }
        final int partyId = bundle.getInt("partyId");
        final int ownerId = bundle.getInt("ownerId");

            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    infoLocations = getInfoLocation(partyId);
//                    先清空Marker
                    map.clear();
                    for (InfoLocation infoLocation : infoLocations) {
                        showMarker(infoLocation, infoLocation.getId());
                    }

                    return false;
                }
            });

        infoLocations = getInfoLocation(partyId);
        mapLocation.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                moveMap(new LatLng(24.9677449899, 121.191698313));
                for (InfoLocation infoLocation : infoLocations) {
                    showMarker(infoLocation, infoLocation.getId());
                }
                map.setInfoWindowAdapter(new MyInfoWindowAdapter(activity));

                if (ownerId == memId) {
                    map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            SharedPreferences pref = activity.getSharedPreferences(Common.PREFERENCE_MEMBER, MODE_PRIVATE);
                            LayoutInflater layoutInflater = LayoutInflater.from(activity);
//                        text_entry is an Layout XML file containing two text field to display in alert dialog
                            final View textEntryView = layoutInflater.inflate(R.layout.alert_custom, null);
                            TextView tvInfoTitle = textEntryView.findViewById(R.id.tvInfoTitle);
                            final EditText edTitle = textEntryView.findViewById(R.id.edTitle);
                            final EditText edContent = textEntryView.findViewById(R.id.edContent);
                            tvInfoTitle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    edTitle.setText("這邊垃圾超多！！");
                                    edContent.setText("快來幫幫忙");
                                }
                            });
                            final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                            int userId = memId;
                            int id = getId();
                            double latitude = latLng.latitude;
                            double longitude = latLng.longitude;
                            String name = "";
                            String content = "";
                            final InfoLocation infoLocation = new InfoLocation(
                                    id, partyId, userId, latitude, longitude, name, content);

                            alert.setView(textEntryView)
                                    .setPositiveButton("確定",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    infoLocation.setName(edTitle.getText().toString());
                                                    infoLocation.setContent(edContent.getText().toString());
                                                    infoLocation.setId(addMarker(infoLocation));
                                                    moveMap(infoLocation.getLatLng());
                                                    infoLocations.add(infoLocation);
                                                    /* User clicked OK so do some stuff */
                                                }
                                            })
                                    .setNegativeButton("取消",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    dialog.dismiss();
                                                }
                                            });
                            alert.show();

                        }
                    });
                }
                // 長按訊息視窗就移除該標記
                if (ownerId == memId) {
                    map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                        @Override
                        public void onInfoWindowLongClick(Marker marker) {
                            int count = deleteMarker((int) marker.getTag());
                            Log.d(TAG, "123a " + String.valueOf(count));
                            if (count != 0) {
                                marker.remove();
                            }
                        }
                    });
                }
            }
        });
        checkLocationSettings();
    }


    private List<InfoLocation> getInfoLocation(int partyId) {
        List<InfoLocation> infoLocations = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "InfoLocationServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            jsonObject.addProperty("partyId", partyId);
            String jsonOut = jsonObject.toString();
            locationGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = locationGetAllTask.execute().get();
                Type listType = new TypeToken<List<InfoLocation>>() {
                }.getType();
                infoLocations = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return infoLocations;
    }

    private void checkLocationSettings() {
        // 必須將LocationRequest設定加入檢查,先造好請求的物件
        LocationSettingsRequest.Builder builder =
//                先造好內部類別Builder裡面的物件
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
//        開啟新執行緒物件
        Task<LocationSettingsResponse> task =
//                先取得Client端物件去檢查定位服務有無開啟 造好checkLocationSettings物件;
                LocationServices.getSettingsClient(activity)
                        .checkLocationSettings(builder.build());
//        啟動執行緒,檢查有沒有同意,成功會呼叫onSuccess
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                再檢查user開啟手機定位設定後, app是否有允許定位服務
                if (ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // 取得並顯示最新位置
                    showMyLocation();
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    Log.e(TAG, e.getMessage());
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        // 跳出Location設定的對話視窗
                        resolvable.startResolutionForResult(activity, REQ_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    private int addMarker(InfoLocation infoLocation) {
        int count = 0;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "InfoLocationServlet";
            JsonObject jsonObject = new JsonObject();
//                    告訴server端 要做新增動作
            jsonObject.addProperty("action", "locationInsert");
            jsonObject.addProperty("location", new Gson().toJson(infoLocation));
            try {
                String result = new CommonTask(url, jsonObject.toString()).execute().get();
                count = Integer.valueOf(result);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(getActivity(), R.string.textInsertFail);
            } else {
                Common.showToast(getActivity(), R.string.textInsertSuccess);
            }
        }
        Log.d("TAG_makerId", String.valueOf(count));
        showMarker(infoLocation, count);
        return count;
    }

    private int deleteMarker(int id) {
        int count = 0;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "InfoLocationServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "locationDelete");
            jsonObject.addProperty("id", id);
            count = 0;
            try {
                locationDeleteTask = new CommonTask(url, jsonObject.toString());
                String result = locationDeleteTask.execute().get();
                count = Integer.valueOf(result.trim());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (count == 0) {
                Common.showToast(activity, R.string.textDeleteFail);
            } else {
                Common.showToast(activity, R.string.textDeleteSuccess);
            }
        }
        return count;
    }

    private void showMarker(InfoLocation infoLocation, int id) {

        Marker marker = map.addMarker(new MarkerOptions()
                .position(infoLocation.getLatLng())
                .title(infoLocation.getName())
                .snippet(infoLocation.getContent()));
        marker.setTag(id);
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        Context context;
         MyInfoWindowAdapter(Context context) {
            this.context = context;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            int id = (int) marker.getTag();
            View view = View.inflate(context, R.layout.info_window, null);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvContent = view.findViewById(R.id.tvContent);
            for (InfoLocation infoLocation : infoLocations) {
                if (infoLocation.getId() == id) {
                    tvTitle.setText(infoLocation.getName());
                    tvContent.setText(infoLocation.getContent());
                    break;
                }

            }
            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private void moveMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
//        target  傳進來的經緯度放這邊
                .target(latLng)
//                縮放
                .zoom(16)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

    private void showLastLocation() {

        if (fusedLocationProviderClient == null) {
//            取得Client端物件
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
//            取得最後的位置
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
//                        Location的物件
                        lastLocation = task.getResult();
                    }
                }
            });

            // 持續性取得最新位置。looper設為null代表以現行執行緒呼叫callback方法，而非使用其他執行緒
//           locationCallback監控user位置是否有更新
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
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
    public void onStart() {
        super.onStart();
        askAccessLocationPermission();
    }

    private void askAccessLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        int result = ContextCompat.checkSelfPermission(activity, permissions[0]);
        if (result == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, PER_ACCESS_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        showMyLocation();
    }

    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationGetAllTask != null) {
            locationGetAllTask.cancel(true);
            locationGetAllTask = null;
        }
        if (locationDeleteTask != null) {
            locationDeleteTask.cancel(true);
            locationDeleteTask = null;
        }
    }
}
