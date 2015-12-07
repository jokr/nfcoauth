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

public class AuthWithDoorTask extends AsyncTask<LoginToken, Void, Boolean> {
    private static final String TAG = "AuthWithDoorTask";
    private final Activity activity;

    public AuthWithDoorTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(LoginToken... params) {
        if (params.length != 1) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        try {
            //URL url = new URL("http://10.0.0.212:8090/auth");
            URL url = new URL("http://172.29.93.2:8090/auth");
            Log.v(TAG, "Prepare connection: " + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            JSONObject user = new JSONObject();
            // ToDO Update the userId Field
            user.put("userId", "");
            user.put("token", params[0].getToken());
            user.put("logintype", params[0].getLoginType());

            Log.v(TAG, "Prepare post: " + user.toString());

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(user.toString());
            wr.flush();
            int retCode = urlConnection.getResponseCode();
            Log.v(TAG, "Sent post. Status Code: " + retCode);
            wr.close();
            return  retCode == 200;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
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
