package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthWithDoorTask extends AsyncTask<AccessToken, Void, Boolean> {
    private static final String TAG = "AuthWithDoorTask";

    @Override
    protected Boolean doInBackground(AccessToken... params) {
        if (params.length != 1) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://10.0.13.179:8090/auth");
            Log.v(TAG, "Prepare connection: " + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            JSONObject user = new JSONObject();
            user.put("userId", params[0].getUserId());
            user.put("token", params[0].getToken());

            Log.v(TAG, "Prepare post: " + user.toString());

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(user.toString());
            wr.flush();
            Log.v(TAG, "Sent post. Status Code: " + urlConnection.getResponseCode());
            wr.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return true;
    }
}
