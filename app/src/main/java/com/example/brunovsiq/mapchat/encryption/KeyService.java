package com.example.brunovsiq.mapchat.encryption;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class KeyService extends Service {

//    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    KeyPairGenerator kpg;
    private boolean generated = false;
    KeyPair keyPair;
    private final IBinder mBinder = new KeyBinder();
    private final String STORE_NAME = "AndroidKeyStore";
    private final String STORE_ALIAS = "KEY_STORE_ALIAS";
    KeyStore keyStore;

    {
        try {
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, STORE_NAME);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public KeyService() throws NoSuchAlgorithmException {
    }

    public class KeyBinder extends Binder {
        KeyService getService() {
            return KeyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            keyStore = KeyStore.getInstance( STORE_NAME, STORE_NAME);
            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public KeyPair getMyKeyPair() {
        /* Generate and/or retrieve a user’s RSA KeyPair. The first call to this
            method will generate and store the keypair before returning it. Subsequent calls will return the
            same key pair */
//        try {
//            PrivateKey privateKey = (PrivateKey) keyStore.getKey("user_key_private", null);
//            PublicKey publicKey = (PublicKey) keyStore.getKey("user_key_public", null);
//            if ((privateKey != null) && (publicKey != null)) {
//                keyPair = new KeyPair(publicKey, privateKey);
//            } else {
//                resetMyKeypair();
//            }
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnrecoverableKeyException e) {
//            e.printStackTrace();
//        } catch (InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }


        try {
            if(keyStore.containsAlias(STORE_ALIAS)) {
                Certificate c = keyStore.getCertificate(STORE_ALIAS);
                keyPair = new KeyPair(c.getPublicKey(), (PrivateKey) keyStore.getKey(STORE_ALIAS, null));
                //TODO: refactor keyPair load
            }
            else {
                resetMyKeypair();

            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();} catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

//        if (generated) {
//            return keyPair;
//        } else {
//            try {
//                resetMyKeypair();
//            } catch (InvalidAlgorithmParameterException e) {
//                e.printStackTrace();
//            }
//            generated = true;
//            return keyPair;
//        }
        return  keyPair;
    }

    private void storePublicKey (String partnerName, PublicKey publicKey) throws KeyStoreException {
        /* Store a key for a provided partner name */
        keyStore.setKeyEntry(partnerName, publicKey, null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void resetMyKeypair() throws InvalidAlgorithmParameterException {
        KeyGenParameterSpec kgps = new KeyGenParameterSpec.Builder
                (STORE_ALIAS, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_SIGN)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build();
//        try {
//            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, STORE_NAME);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        }
        kpg.initialize(kgps);

        keyPair = kpg.generateKeyPair();
//        try {
//            keyStore.setKeyEntry("user_key_private", keyPair.getPrivate(), null, null);
//            keyStore.setKeyEntry("user_key_public", keyPair.getPrivate(), null, null);
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private RSAPublicKey getPublicKey(String partnerName) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    /*
     Returns the public key associated with the
     provided partner name
     */

        RSAPublicKey rsaPublicKey;
        rsaPublicKey = (RSAPublicKey)keyStore.getKey(partnerName, null);
        return rsaPublicKey;


    }

    void resetKey(String partnername) throws KeyStoreException {
        keyStore.deleteEntry(partnername);
    }

    //        try {
//            KeyFactory fact = KeyFactory.getInstance("RSA");
//            cipher = Cipher.getInstance("RSA");
//            privateKey = (RSAPrivateKey) keyPair.getPrivate();
//            publicKey = (RSAPublicKey) keyPair.getPublic();
//
//            privateKeyString = privateKey.getPrivateExponent().toString();
//            publicKeyString = publicKey.getPublicExponent().toString();
//
//            Log.d("Public", publicKeyString);
//            Log.d("Private", privateKeyString);
//            Log.d("Mod", publicKey.getModulus().toString());
//            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(privateKey.getModulus(), new BigInteger(privateKeyString));
//            privateKey = (RSAPrivateKey) fact.generatePrivate(privKeySpec);
//            RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(publicKey.getModulus(), new BigInteger(publicKeyString));
//            publicKey = (RSAPublicKey) fact.generatePublic(pubKeySpec);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }

}
