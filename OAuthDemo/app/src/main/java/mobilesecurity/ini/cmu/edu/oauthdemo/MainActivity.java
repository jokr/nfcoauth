package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button selectDomain;
    TextView tagDetected;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectDomain = (Button) findViewById(R.id.button_main_select_domain);
        tagDetected = (TextView) findViewById(R.id.textView_main_tag);
        selectDomain.setVisibility(View.INVISIBLE);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "NfcIntent detected!", Toast.LENGTH_SHORT).show();

            NdefMessage ndefMessage = this.getNdefMessageFromIntent(intent);

            if (ndefMessage.getRecords().length > 0) {
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];
                String payload = new String(ndefRecord.getPayload());

                tagDetected.setText("Valid tag found: " + payload);
                selectDomain.setVisibility(View.VISIBLE);

                selectDomain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, SelectDomain.class));
                        finish();
                    }
                });
            }
        }
    }

    public NdefMessage getNdefMessageFromIntent(Intent intent) {
        NdefMessage ndefMessage = null;
        Parcelable[] extra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (extra != null && extra.length > 0) {
            ndefMessage = (NdefMessage) extra[0];
        }
        return ndefMessage;
    }

    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilters = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
}
