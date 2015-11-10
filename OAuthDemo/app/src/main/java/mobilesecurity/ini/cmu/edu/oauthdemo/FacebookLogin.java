package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FacebookLogin extends AppCompatActivity {
    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private void setAccessToken(AccessToken token) {
        Resources res = getResources();
        info.setText(String.format(
                res.getString(R.string.loginSuccess),
                token.getUserId(),
                token.getToken()
        ));

        new AuthWithDoorTask().execute(token);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_facebook_login);
        info = (TextView) findViewById(R.id.info);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            setAccessToken(accessToken);
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                setAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                info.setText(R.string.loginCanceled);
            }

            @Override
            public void onError(FacebookException e) {
                info.setText(R.string.loginFailed);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
