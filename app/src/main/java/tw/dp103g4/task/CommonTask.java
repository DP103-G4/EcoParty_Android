package tw.dp103g4.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommonTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "TAG_CommonTask";
    private String url, outStr;

    public CommonTask(String url, String outStr) {
        this.url = url;
        this.outStr = outStr;
    }

    @Override
    protected String doInBackground(String... strings) {
        return getRemoteData();
    }

    private String getRemoteData() {
        HttpURLConnection connection = null;
        StringBuilder inStr = new StringBuilder();
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestMethod("POST");
            connection.setChunkedStreamingMode(0);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(outStr);
            Log.d(TAG, "output: " + outStr);
            bw.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    inStr.append(line);
                }
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d(TAG, "input: " + inStr);
        return inStr.toString();
    }
}
