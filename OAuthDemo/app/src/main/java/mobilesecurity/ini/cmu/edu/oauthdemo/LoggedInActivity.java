package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class LoggedInActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);

        TextView loggedIn = (TextView) findViewById(R.id.textView_logged_in);

        Bundle bundle = getIntent().getExtras();
        boolean authSuccess = bundle.getBoolean("authSuccess");
        if(!authSuccess) {
            loggedIn.setBackgroundColor(getResources().getColor(R.color.red));
            loggedIn.setText("Login Failed. Please retry.");
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent mainActivity = new Intent(LoggedInActivity.this, MainActivity.class);
                startActivity(mainActivity);
            }
        }, 5000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
