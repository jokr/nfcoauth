package mobilesecurity.ini.cmu.edu.oauthdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivtiy";
    private static final int MIFARE_ULTRALIGHT_SIZE_LIMIT = 48; // bytes, 12 pages a 4 bytes

    private NfcAdapter nfcAdapter;
    private PublicKey publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button simulateScan = (Button) findViewById(R.id.textView_main_tap);

        try {
            publicKey = readPublicKey();
            Log.v(TAG, publicKey.toString());
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading public key.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            handleTag(tag);
        }
    }

    private void handleTag(Tag tag) {
        Log.v(TAG, tag.toString());

        try {
            byte[] payload = readTag(tag);
            if (payload.length < 32) {
                throw new ReadingTagException("Payload shorter than 32 bytes.");
            }
            String url = readUrlFromPayload(payload);
            Log.v(TAG, "URL: " + url);
            String nonce = readNonceFromPayload(payload, url.length());
            Log.v(TAG, "Nonce: " + nonce);
            boolean verified = verifySignature(payload);
            Log.v(TAG, "Verified: " + verified);
            if (!verified) {
                throw new ReadingTagException("Verifiacation of signature failed.");
            }
            login(url);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, "Error while reading tag.", Toast.LENGTH_SHORT).show();
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

    private byte[] readTag(Tag tag) throws IOException, FormatException, ReadingTagException {
        List<String> tech = Arrays.asList(tag.getTechList());
        if (tech.contains(Ndef.class.getName())) {
            Log.v(TAG, "Read formatted tag.");
            return readNdeftag(Ndef.get(tag));
        } else if (tech.contains(MifareUltralight.class.getName())) {
            Log.v(TAG, "Read Mifare ultralight tag.");
            return readMifareUltralight(MifareUltralight.get(tag));
        }
        Toast.makeText(this, "No supported tag found.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "No supported tag found: " + tech);
        throw new ReadingTagException("No supported tag found.");
    }

    private byte[] readNdeftag(Ndef tag) throws IOException, FormatException, ReadingTagException {
        NdefMessage message = tag.getNdefMessage();
        if (message.getRecords().length == 0) {
            Toast.makeText(this, "Empty tag found.", Toast.LENGTH_SHORT).show();
            throw new ReadingTagException("Empty tag.");
        }
        return message.getRecords()[0].getPayload();
    }

    private byte[] readMifareUltralight(MifareUltralight tag) throws IOException {
        byte[] payload = new byte[MIFARE_ULTRALIGHT_SIZE_LIMIT];
        try {
            tag.connect();
            for (int i = 4; i < 16; i++) {
                System.arraycopy(
                        tag.readPages(i),
                        0,
                        payload,
                        (i - 4) * 4,
                        4
                );
            }
        } finally {
            tag.close();
        }

        return payload;
    }

    private String readUrlFromPayload(byte[] payload) throws ReadingTagException {
        for(int i = 0; i < 32; i++) {
            byte c = (byte) payload[i];
            if (c == 0x0A) {
                return new String(payload, 0, i);
            }
        }
        throw new ReadingTagException("No new line character found.");
    }

    private String readNonceFromPayload(byte[] payload, int offset) {
        return new String(payload, offset + 1, 31 - offset);
    }

    private byte[] genDigest(byte[] payload, int length) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("SHA-256");
        m.update(payload, 0, 32);
        if (length == 0) {
            return m.digest();
        }
        byte[] digest = new byte[length];
        System.arraycopy(m.digest(), 0, digest, 0, digest.length);
        return digest;
    }

    private boolean verifySignature(byte[] payload) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] digest = genDigest(payload, 5);
        Signature signature = Signature.getInstance("NONEwithRSA");
        signature.initVerify(publicKey);
        signature.update(digest);
        return signature.verify(payload, 32, payload.length - 32);
    }

    private PublicKey readPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        InputStream input = this.getResources().openRawResource(R.raw.publickey);
        int b;
        while((b = input.read()) != -1) {
            byteStream.write(b);
        }

        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(byteStream.toByteArray());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicKeySpec);
    }
}