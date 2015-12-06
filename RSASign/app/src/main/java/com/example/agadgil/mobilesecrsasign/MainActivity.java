package com.example.agadgil.mobilesecrsasign;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

/**
 * Created by agadgil on 12/2/15.
 */
public class MainActivity extends Activity{
    TextView t;
    private final static String RSA = "RSA";

    public static PublicKey uk;

    public static PrivateKey rk;



    public static void generateKey() throws Exception {

        KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);

        gen.initialize(128, new SecureRandom());

        KeyPair keyPair = gen.generateKeyPair();

        uk = keyPair.getPublic();

        rk = keyPair.getPrivate();

    }

    private static byte[] encrypt(String text, PrivateKey pubRSA)

            throws Exception {

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.ENCRYPT_MODE, pubRSA);

        return cipher.doFinal(text.getBytes());

    }

    public final static String encrypt(String text) {

        try {

            return byte2hex(encrypt(text, rk));

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }



    public final static String decrypt(String data) {

        try {

            return new String(decrypt(hex2byte(data.getBytes())));

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }
    private static byte[] decrypt(byte[] src) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA);

        cipher.init(Cipher.DECRYPT_MODE, uk);

        return cipher.doFinal(src);

    }

    public static String byte2hex(byte[] b) {

        String hs = "";

        String stmp = "";

        for (int n = 0; n < b.length; n++) {

            stmp = Integer.toHexString(b[n] & 0xFF);

            if (stmp.length() == 1)

                hs += ("0" + stmp);

            else

                hs += stmp;

        }

        return hs.toUpperCase();

    }



    public static byte[] hex2byte(byte[] b) {

        if ((b.length % 2) != 0)

            throw new IllegalArgumentException("hello");



        byte[] b2 = new byte[b.length / 2];



        for (int n = 0; n < b.length; n += 2) {

            String item = new String(b, n, 2);

            b2[n / 2] = (byte) Integer.parseInt(item, 16);

        }

        return b2;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = (TextView) findViewById(R.id.textView_main);

        try {

            generateKey();
            t.append("\nHi\n" + encrypt("10.0.0.212:8090"));
            t.append("\nHi\n" + decrypt(encrypt("10.0.0.212:8090")));


            FileOutputStream fos1 = openFileOutput("rsapublickey", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fos1);
            out.writeObject(uk);
            out.close();

            FileOutputStream fos2 = openFileOutput("encryptedbytes", Context.MODE_PRIVATE);
            fos2.write(encrypt("10.0.0.212:8090", rk));
            fos2.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
