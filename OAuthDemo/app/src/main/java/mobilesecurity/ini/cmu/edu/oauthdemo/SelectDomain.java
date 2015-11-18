package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class SelectDomain extends Activity {
    private static final String TAG = "SelectDomainActivity";
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;

    private void setAccessToken(AccessToken token) {
        if (token == null) {
            return;
        }
        new AuthWithDoorTask(this).execute(token);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.select_domain);

        callbackManager = CallbackManager.Factory.create();

        Button selectGoogle = (Button) findViewById(R.id.button_domain_google);
        // TODO implement google

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                setAccessToken(newToken);
            }
        };

        accessTokenTracker.startTracking();
        setAccessToken(AccessToken.getCurrentAccessToken());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}
