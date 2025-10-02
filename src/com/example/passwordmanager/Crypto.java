package com.example.passwordmanager;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public class Crypto {
    private static final int SALT_LENGTH = 16; // bytes
    private static final int IV_LENGTH = 12;   // bytes
    private static final int KEY_LENGTH = 32;  // bytes (256-bit)
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final SecureRandom RNG = new SecureRandom();

    public static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        RNG.nextBytes(b);
        return b;
    }

    public static byte[] deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = skf.generateSecret(spec).getEncoded();
        Arrays.fill(password, '\0');
        return key;
    }

    public static byte[] encrypt(byte[] plaintext, char[] password, byte[] salt) throws Exception {
        byte[] key = deriveKey(password, salt);
        byte[] iv = randomBytes(IV_LENGTH);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plaintext);
        byte[] out = new byte[1 + salt.length + iv.length + ct.length];
        out[0] = 1; // version
        System.arraycopy(salt, 0, out, 1, salt.length);
        System.arraycopy(iv, 0, out, 1 + salt.length, iv.length);
        System.arraycopy(ct, 0, out, 1 + salt.length + iv.length, ct.length);
        Arrays.fill(key, (byte)0);
        return out;
    }

    public static byte[] decrypt(byte[] blob, char[] password) throws Exception {
        if (blob.length < 1 + SALT_LENGTH + IV_LENGTH + 16) {
            throw new IllegalArgumentException("Invalid blob");
        }
        int pos = 0;
        byte version = blob[pos++];
        if (version != 1) throw new IllegalArgumentException("Unsupported version");
        byte[] salt = Arrays.copyOfRange(blob, pos, pos + SALT_LENGTH); pos += SALT_LENGTH;
        byte[] iv = Arrays.copyOfRange(blob, pos, pos + IV_LENGTH); pos += IV_LENGTH;
        byte[] ct = Arrays.copyOfRange(blob, pos, blob.length);
        byte[] key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        byte[] pt = cipher.doFinal(ct);
        Arrays.fill(key, (byte)0);
        return pt;
    }

    public static int saltLength() { return SALT_LENGTH; }
}