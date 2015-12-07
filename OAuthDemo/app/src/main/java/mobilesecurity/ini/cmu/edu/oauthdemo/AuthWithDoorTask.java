package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthWithDoorTask extends AsyncTask<DoorTaskParameter, Void, Boolean> {
    private static final String TAG = "AuthWithDoorTask";
    private final Activity activity;

    public AuthWithDoorTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(DoorTaskParameter... params) {
        if (params.length != 1) {
            return null;
        }
        String server = params[0].url;
        LoginToken token = params[0].token;

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://" + server);
            Log.v(TAG, "Prepare connection: " + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            JSONObject user = new JSONObject();
            user.put("userId", "");
            user.put("token", token.getToken());
            user.put("logintype", token.getLoginType());
            Log.v(TAG, "Prepare payload: " + user.toString());

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(user.toString());
            wr.flush();
            int retCode = urlConnection.getResponseCode();
            Log.v(TAG, "Sent post. Status Code: " + retCode);
            wr.close();
            return  retCode == 200;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error when sending post to server.", e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.v(TAG, "Finished task, start new activity: " + result);
        activity.finish();
        Intent intent = new Intent(activity, LoggedInActivity.class);
        intent.putExtra("authSuccess", result);
        activity.startActivity(intent);
    }
}
