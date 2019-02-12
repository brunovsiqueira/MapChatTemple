package com.example.brunovsiq.mapchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    /*
    Bruno Viana de Siqueira
    TUid: 915777602
     */

    private Button keyPairButton;
    private EditText entreeText;
    private Button encryptButton;
    private Button decryptButton;
    private TextView outputText;
    private KeyService keyService;
    private boolean isBound;
    private KeyPair userKeyPair;

    private IvParameterSpec ivSpec;
    private SecretKeySpec keySpec;
    private Cipher cipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doBindService();

        keyPairButton = findViewById(R.id.button_request_keypair);
        encryptButton = findViewById(R.id.encryption_button);
        decryptButton = findViewById(R.id.decryption_button);
        entreeText = findViewById(R.id.text_entree);
        outputText = findViewById(R.id.text_output);

        keyPairButton.setOnClickListener(keyPairClickListener);
        encryptButton.setOnClickListener(encryptClickListener);
        decryptButton.setOnClickListener(decryptClickListener);


        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    public String encrypt(String text) throws Exception {
        if (text == null || text.length() == 0) {
            throw new Exception("Empty string");
        } else {

            byte[] encrypted = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, userKeyPair.getPrivate());
                encrypted = cipher.doFinal(text.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }

    }

    public String decrypt(String text) throws Exception {
        String s = "";
        if (text == null || text.length() == 0)
            throw new Exception("Empty string");

        byte[] encryptedText = Base64.decode(text, Base64.DEFAULT);
        try {
            cipher.init(Cipher.DECRYPT_MODE, userKeyPair.getPublic());
            s = new String(cipher.doFinal(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {

            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);

            }
            return buffer;
        }
    }

    private static String padString(String source) {
        char paddingChar = 0;
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;
        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }
        return source;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            // This is called when the connection with the service has
            // been established, giving us the service object we can use
            // to interact with the service.  Because we have bound to a
            // explicit service that we know is running in our own
            // process, we can cast its IBinder to a concrete class and
            // directly access it.
            keyService = ((KeyService.KeyBinder)service).getService();

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected -- that is, its process
            // crashed. Because it is running in our same process, we
            // should never see this happen.
            keyService = null;

        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation
        // that we know will be running in our own process (and thus
        // won't be supporting component replacement by other
        // applications).
        bindService(new Intent(this, KeyService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            isBound = false;
        }
    }

    View.OnClickListener keyPairClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            userKeyPair = keyService.getMyKeyPair();
            //keySpec = new SecretKeySpec(userKeyPair.getPrivate().getAlgorithm().getBytes(), "RSA");

            Toast.makeText(MainActivity.this, "KeyPair successfully generated!", Toast.LENGTH_SHORT).show();

        }
    };

    View.OnClickListener encryptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(entreeText.getText().toString().length() > 0) {
                try {
                    outputText.setText(encrypt(entreeText.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                entreeText.setError("No entree");
            }

        }
    };

    View.OnClickListener decryptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                outputText.setText(decrypt(outputText.getText().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

}
