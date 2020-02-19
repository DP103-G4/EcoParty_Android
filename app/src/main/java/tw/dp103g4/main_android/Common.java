package tw.dp103g4.main_android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;

import tw.dp103g4.friend.ChatWebSocketClient;

public class Common {
    private final static String TAG = "CommonSocket";
    public static String URL_SERVER = "http://10.0.2.2:8080/EcoParty/";
//    public static String URL_SERVER = "http://192.168.196.196:8080/EcoParty/";
    //偏好設定檔叫做member
    public static final String PREFERENCE_MEMBER = "member";

    public static int getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_MEMBER, Context.MODE_PRIVATE);
        return pref.getInt("id", 0);
    }
    public static String URI_SERVER = "ws://10.0.2.2:8080/EcoParty/SocketServer/";
//    public static String URI_SERVER = "ws://192.168.196.196:8080/EcoParty/SocketServer/";
    public static ChatWebSocketClient chatWebSocketClient;

    //建立webSocket連線
    public static void connectServer(Context context, int userId) {
       URI uri = null;
       try{
           Log.d(TAG, URI_SERVER + userId);
           uri = new URI(URI_SERVER + userId);
       }catch (URISyntaxException e){
           Log.e(TAG, e.toString());
       }
        if (chatWebSocketClient == null) {
            chatWebSocketClient = new ChatWebSocketClient(uri, context);
            chatWebSocketClient.connect();
        }
    }
    // 中斷WebSocket連線
    public static void disconnectServer() {
        if (chatWebSocketClient != null) {
            chatWebSocketClient.close();
            chatWebSocketClient = null;
        }
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
}
