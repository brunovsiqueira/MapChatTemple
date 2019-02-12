package com.example.brunovsiq.mapchat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;


import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyService extends Service {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    private boolean generated = false;
    KeyPair keyPair;
    private final IBinder mBinder = new KeyBinder();

    public KeyService() throws NoSuchAlgorithmException {
    }

    public class KeyBinder extends Binder {
        KeyService getService() {
            return KeyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public KeyPair getMyKeyPair() {
        /* Generate and/or retrieve a user’s RSA KeyPair. The first call to this
            method will generate and store the keypair before returning it. Subsequent calls will return the
            same key pair */
        if (generated) {
            return keyPair;
        } else {
            keyPair = kpg.genKeyPair();
            generated = true;
            return keyPair;
        }
    }

    private void storePublicKey (String partnerName, String publicKey) {
        /* Store a key for a provided partner name */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(partnerName, publicKey);
        editor.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private RSAPublicKey getPublicKey(String partnerName) {
        /*
          Returns the public key associated with the
          provided partner name
         */
        RSAPublicKey rsaPublicKey;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String publicKeyString = sharedPref.getString(partnerName, "");

        byte[] publicBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
            return rsaPublicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }

    }

}
