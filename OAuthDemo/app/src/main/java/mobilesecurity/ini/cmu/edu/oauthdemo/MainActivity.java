package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivtiy";
    private static final int MIFARE_ULTRALIGHT_SIZE_LIMIT = 48; // bytes, 12 pages a 4 bytes

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

    private void handleTag(Tag tag) {
        Log.v(TAG, tag.toString());
        List<String> tech = Arrays.asList(tag.getTechList());

        if (tech.contains(Ndef.class.getName())) {
            Log.v(TAG, "Read formatted tag.");
            try {
                NdefMessage message = Ndef.get(tag).getNdefMessage();
                if (message.getRecords().length == 0) {
                    Toast.makeText(this, "Empty tag found.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String payload = new String(message.getRecords()[0].getPayload());
                login(payload);
            } catch (FormatException | IOException e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(this, "Error while reading ndef tag.", Toast.LENGTH_SHORT).show();
            }
        } else if (tech.contains(MifareUltralight.class.getName())) {
            Log.v(TAG, "Read Mifare ultralight tag.");
            MifareUltralight mifareUltralight = MifareUltralight.get(tag);
            byte[] payload = new byte[MIFARE_ULTRALIGHT_SIZE_LIMIT];
            try {
                mifareUltralight.connect();
                for (int i = 4; i < 16; i++) {
                    System.arraycopy(
                            mifareUltralight.readPages(i),
                            0,
                            payload,
                            (i - 4) * 4,
                            4
                    );
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error while reading mifare ultralight tag.", Toast.LENGTH_SHORT).show();
                return;
            } finally {
                try {
                    mifareUltralight.close();
                } catch (IOException e) {
                    Toast.makeText(this, "Error while reading mifare ultralight tag.", Toast.LENGTH_SHORT).show();
                }
            }
            for(int i = 0; i < payload.length; i++) {
                if (payload[i] == (byte) 0x0A) {
                    login(new String(Arrays.copyOfRange(payload, 0, i), StandardCharsets.UTF_8));
                    return;
                }
            }
            Toast.makeText(this, "No newline character found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String serverUrl) {
        Log.v(TAG, "Login with server " + serverUrl + ".");
        Intent intent = new Intent(MainActivity.this, SelectDomain.class);
        intent.putExtra("server", serverUrl);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "Received intent: " + intent.toString());
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            handleTag(tag);
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

    @Override
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
