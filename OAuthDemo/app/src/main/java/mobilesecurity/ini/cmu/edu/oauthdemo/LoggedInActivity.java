package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

public class LoggedInActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);

        TextView loggedIn = (TextView) findViewById(R.id.textView_logged_in);
        TextView welcome = (TextView) findViewById(R.id.textView_welcome);

        Bundle bundle = getIntent().getExtras();
        boolean authSuccess = bundle.getBoolean("authSuccess");
        if (!authSuccess) {
            loggedIn.setBackgroundColor(getResources().getColor(R.color.red));
            loggedIn.setText(R.string.failed);
        }

        String name = bundle.getString("name", "Tatrick Pague");
        welcome.setText(String.format(getString(R.string.welcomeName), name));

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent mainActivity = new Intent(LoggedInActivity.this, MainActivity.class);
                startActivity(mainActivity);
            }
        }, 5000);

    }
}
