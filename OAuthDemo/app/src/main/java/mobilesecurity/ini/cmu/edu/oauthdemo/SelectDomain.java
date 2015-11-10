package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by agadgil on 11/9/15.
 */
public class SelectDomain extends Activity {
    Button selectFacebook;
    Button selectGoogle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_domain);
        selectFacebook = (Button)findViewById(R.id.button_domain_facebook);
        selectGoogle = (Button)findViewById(R.id.button_domain_google);
        selectFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectDomain.this, FacebookLogin.class));
                finish();
            }
        });
    }
}
