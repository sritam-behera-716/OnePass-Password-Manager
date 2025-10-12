package com.securevault.onepass.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureEncryptionHelper {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "com.securevault.onepass.master_key";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private final Context context;

    public SecureEncryptionHelper(Context context) {
        this.context = context;
        initializeKeyStore();
    }

    private void initializeKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generateKey();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Initialize failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true);

            builder.setUnlockedDeviceRequired(false);
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            Toast.makeText(context, "Key generation failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                    KEY_ALIAS, null);
            return secretKeyEntry.getSecretKey();
        } catch (Exception e) {
            Toast.makeText(context, "Secret key failed!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public String encrypt(String plaintext) {
        try {
            if (plaintext == null || plaintext.isEmpty()) {
                throw new IllegalArgumentException("Plaintext cannot be null or empty");
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] iv = cipher.getIV();

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            Toast.makeText(context, "Encryption failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                throw new IllegalArgumentException("Encrypted text cannot be null or empty");
            }

            byte[] combined = Base64.decode(encryptedText, Base64.NO_WRAP);

            if (combined.length < 12) {
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            byte[] iv = new byte[12];
            byte[] encryptedBytes = new byte[combined.length - 12];

            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Toast.makeText(context, "Decryption failed!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}