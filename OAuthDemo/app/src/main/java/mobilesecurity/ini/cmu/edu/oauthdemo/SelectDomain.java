package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

public class SelectDomain extends AppCompatActivity implements
        View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "SelectDomainActivity";
    private CallbackManager callbackManager;

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 1;

    /* RequestCode for resolutions to get GET_ACCOUNTS permission on M */
    private static final int RC_PERM_GET_ACCOUNTS = 2;

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private LoginToken loginToken = new LoginToken();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.select_domain);
        callbackManager = CallbackManager.Factory.create();

        findViewById(R.id.button_unlock).setOnClickListener(this);
        findViewById(R.id.button_domain_google).setOnClickListener(this);

        ((SignInButton) findViewById(R.id.button_domain_google)).setSize(SignInButton.SIZE_WIDE);

        // Set up Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Code borrowed from google login sample
        if(loginToken.isLoggedIn()) {
            switch (loginToken.getLoginType()) {
                case GOOGLE:
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Code borrowed from google login sample
        if(loginToken.isLoggedIn()) {
            switch (loginToken.getLoginType()) {
                case GOOGLE:
                    mGoogleApiClient.disconnect();
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Both Google and Facebook Login will call this function. Use login type
        // to distiguish each other
        switch (loginToken.getLoginType()) {
            case FACEBOOK:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                // Set the login status to true only if we can get a valid token
                if(accessToken.getToken() != null) {
                    loginToken.setToken(accessToken.getToken());
                    loginToken.setLoginStatus(true);
                }
                // User can switch account next time when he wants to login
                LoginManager.getInstance().logOut();
                // Update the UI after login
                updateUI();
                break;
            case GOOGLE:
                // Code borrowed from Google's login sample
                if (requestCode == RC_SIGN_IN) {
                    // If the error resolution was not successful we should not resolve further.
                    if (resultCode != RESULT_OK) {
                        mShouldResolve = false;
                    }
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    /**
     * Code borrowed from Google's login sample
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERM_GET_ACCOUNTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateUI();
            } else {
                Log.d(TAG, "GET_ACCOUNTS Permission Denied.");
            }
        }
    }

    /**
     * Code borrowed from Google's Login sample with customization.
     */
    @Override
    public void onConnected(Bundle bundle) {
        mShouldResolve = false;

        // Once the connection to Google's API is successful, launch a new task to retrieve the
        // access token from Google. Currently, the scope only covers the email in order to minimize
        // the privilege
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                String scope = "oauth2:" + Scopes.EMAIL;
                try {
                    String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                    loginToken.setToken(GoogleAuthUtil.getToken(SelectDomain.this, email, scope));
                    loginToken.setLoginStatus(true);

                    // User can switch account next time when he wants to login
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            /**
             * After login, we need to update the main UI
             */
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                updateUI();
            }
        };
        task.execute((Void)null);
    }

    /**
     * Code borrowed from Google's login sample
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    /**
     * Code borrowed from Google's login sample
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                Log.e(TAG, "Could not resolve ConnectionResult.");
            }
        }
    }

    /**
     * Listener function used to handle button click event
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_domain_google:
                onGoogleSignInClicked();
                break;
            case R.id.button_unlock:
                unlock();
                break;
        }
    }

    /**
     * Code borrowed from Google's Login sample with customization.
     */
    private void onGoogleSignInClicked() {
        mShouldResolve = true;
        // Update the login type
        loginToken.setLoginType(LoginToken.LoginType.GOOGLE);
        // For Android M, we need to get the runtime permission
        checkAccountsPermission();
        mGoogleApiClient.connect();
    }

    /**
     * Request permission at runtime
     */
    private void checkAccountsPermission() {
        final String perm = Manifest.permission.GET_ACCOUNTS;
        int permissionCheck = ContextCompat.checkSelfPermission(this, perm);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // We have the permission
            return;
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{perm},
                    RC_PERM_GET_ACCOUNTS);
        }
    }

    /**
     * If the user successfully logined, hide the login button, and show the unlock button
     */
    void updateUI() {
        if(loginToken.isLoggedIn()) {
            findViewById(R.id.button_domain_facebook).setVisibility(View.GONE);
            findViewById(R.id.button_domain_google).setVisibility(View.GONE);
            findViewById(R.id.button_unlock).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.textView_domain_instruction)).setText("Click to unlock the door");
        }
    }

    /**
     * Handler for the unlock button
     */
    private void unlock() {
        new AuthWithDoorTask(this).execute(loginToken);
    }
}
