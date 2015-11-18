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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivtiy";

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button simulateScan = (Button) findViewById(R.id.textView_main_tap);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            simulateScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SelectDomain.class));
                    finish();
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "Received intent: " + intent.toString());

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Toast.makeText(this, "NfcIntent detected!", Toast.LENGTH_SHORT).show();

            NdefMessage ndefMessage = this.getNdefMessageFromIntent(intent);
            Log.v(TAG, "NdefMessage: " + ndefMessage.toString());

            if (ndefMessage.getRecords().length > 0) {
                Log.v(TAG, "Records: " + Arrays.toString(ndefMessage.getRecords()));
                NdefRecord ndefRecord = ndefMessage.getRecords()[0];
                String payload = new String(ndefRecord.getPayload());

                Toast.makeText(this, "Valid tag found: " + payload, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SelectDomain.class));
                finish();
            }
        }
    }

    public NdefMessage getNdefMessageFromIntent(Intent intent) {
        NdefMessage ndefMessage = null;
        Parcelable[] extra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        Log.v(TAG, "Extra: " + Arrays.toString(extra));

        if (extra != null && extra.length > 0) {
            ndefMessage = (NdefMessage) extra[0];
        }
        return ndefMessage;
    }

    protected void onResume() {
        super.onResume();
        if (nfcAdapter == null) {
            Toast.makeText(this, "No nfc adapter on this phone.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
}
