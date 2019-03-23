package com.example.brunovsiq.mapchat.map;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class EncryptionController {


    Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");

    public EncryptionController() throws NoSuchPaddingException, NoSuchAlgorithmException {
    }


    public String encrypt(String text, PublicKey publicKey) throws Exception {
        if (text == null || text.length() == 0) {
            throw new Exception("Empty string");
        } else {

            byte[] encrypted = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                encrypted = cipher.doFinal(text.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }

    }

    public String decrypt(String text, PrivateKey privateKey) throws Exception {
        String s = "";
        if (text == null || text.length() == 0)
            throw new Exception("Empty string");

        byte[] encryptedText = Base64.decode(text, Base64.DEFAULT);
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            s = new String(cipher.doFinal(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
