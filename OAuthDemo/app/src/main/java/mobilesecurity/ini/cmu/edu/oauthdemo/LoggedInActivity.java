package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoggedInActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in);
        TextView loggedIn = (TextView) findViewById(R.id.textView_logged_in);
//        TextView authorized = (TextView) findViewById(R.id.textView_authorized);
//        authorized.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
