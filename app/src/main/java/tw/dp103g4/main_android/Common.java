package tw.dp103g4.main_android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.MotionEvent;
import android.widget.Toast;

public class Common {
    public static String URL_SERVER = "http://10.0.2.2:8080/EcoParty/";
    //偏好設定檔叫做member
    public static final String PREFERENCE_MEMBER = "member";

    public static int getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_MEMBER, Context.MODE_PRIVATE);
        return pref.getInt("id", 0);
    }

    // check if the device connect to the network
    public static boolean networkConnected(Activity activity) {
        ConnectivityManager conManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager != null ? conManager.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void showToast(Context context, int messageResId) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


//點擊空白處收合鍵盤
//    public boolean onTouchEvent(MotionEvent event){
//        return super.onTouchEvent(event);
//    }
}
