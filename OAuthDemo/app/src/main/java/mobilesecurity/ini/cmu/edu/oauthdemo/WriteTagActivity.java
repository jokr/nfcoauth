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
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class WriteTagActivity extends Activity {
    private static final String TAG = "WriteTagActivity";
    private static final int MIFARE_ULTRALIGHT_SIZE_LIMIT = 48; // bytes, 12 pages a 4 bytes

    private NfcAdapter nfcAdapter;
    private EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate called");
        Log.v(TAG, getIntent().toString());

        setContentView(R.layout.activity_write_tag);
        url = (EditText) findViewById(R.id.url);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "No nfc adapter available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            handleTag(getIntent());
            return;
        }

        Toast.makeText(this, "Ready to read tag.", Toast.LENGTH_SHORT).show();
    }

    private void handleTag(Intent intent) {
        Log.v(TAG, "handleTag called");

        String urlMsg = url.getText().toString();
        if (urlMsg.length() == 0) {
            Toast.makeText(this, "Please enter a url first.", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] payload = (urlMsg + "\n").getBytes(StandardCharsets.UTF_8);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.v(TAG, tag.toString());
        List<String> tech = Arrays.asList(tag.getTechList());

//        if (tech.contains(NdefFormatable.class.getName())) {
//            Toast.makeText(this, "Write NDEF.", Toast.LENGTH_SHORT).show();
//            write(NdefFormatable.get(tag), payload);
//            return;
//        }

        if (tech.contains(MifareUltralight.class.getName())) {
            Toast.makeText(this, "Write to Mifare Ultralight.", Toast.LENGTH_SHORT).show();
            clearTag(MifareUltralight.get(tag));
            write(MifareUltralight.get(tag), payload);
            return;
        }

        if (tech.contains(MifareUltralight.class.getName())) {
            Toast.makeText(this, "Write to Mifare Classic.", Toast.LENGTH_SHORT).show();
            write(MifareClassic.get(tag), payload);
            return;
        }

        Toast.makeText(this, "Not a supported tag.", Toast.LENGTH_SHORT).show();
    }

    private void write(NdefFormatable tag, byte[] payload) {
        NdefRecord record = NdefRecord.createExternal("cmu", "server", payload);
        NdefMessage message = new NdefMessage(record);
        Log.v(TAG, String.format("Write message with %d bytes.", message.getByteArrayLength()));

        try {
            tag.connect();
            Log.v(TAG, "Connected to tag.");
            tag.format(message);
            Toast.makeText(this, "Successfully formatted tag.", Toast.LENGTH_SHORT).show();
        } catch (FormatException | IOException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, "Could not format tag.", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                tag.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close tag.", e);
            }
        }
    }

    private void write(MifareClassic tag, byte[] payload) {
        // TODO implement
    }

    private void clearTag(MifareUltralight tag) {
        byte[] payload = new byte[MIFARE_ULTRALIGHT_SIZE_LIMIT];
        Arrays.fill(payload, (byte) 0);
        write(tag, payload);
    }

    private void write(MifareUltralight tag, byte[] payload) {
        Log.v(TAG, String.format(
                "Payload: %s [%d bytes].",
                Arrays.toString(payload),
                payload.length
        ));

        if (payload.length > MIFARE_ULTRALIGHT_SIZE_LIMIT) {
            Toast.makeText(this, "Payload is too long.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            tag.connect();
            for (int i = 0; i < Math.ceil(payload.length / 4.0); i++) {
                Log.v(TAG, String.format(
                        "Page %d with values %s", 4 + i,
                        Arrays.toString(Arrays.copyOfRange(payload, i * 4, (i * 4) + 4))
                ));
                tag.writePage(4 + i, Arrays.copyOfRange(payload, i * 4, (i * 4) + 4));
            }
            Toast.makeText(this, "Successfully written to tag.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, "Could not write to tag.", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                tag.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close tag.", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume called");
        setupForegroundDispatch();
    }

    private void setupForegroundDispatch() {
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause called");
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent called");
        Log.v(TAG, intent.toString());
        handleTag(intent);
    }
}