package com.example.bankcards.util;

import com.example.bankcards.exception.AppRuntimeException;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Getter
public final class CryptoUtil {
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKey secretKey;

    public CryptoUtil(SecretKey key) {
        this.secretKey = key;
    }

    public static SecretKey getSecretKey(String key, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AppRuntimeException("Error card secret key initialization: " + e.getMessage());
        }
    }

    public static byte[] getInitializationVector() {
        byte[] initializationVector = new byte[IV_LENGTH_BYTE];
        RANDOM.nextBytes(initializationVector);
        return initializationVector;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        CryptoUtil cryptoUtil = new CryptoUtil(keyGen.generateKey());

        byte[] iv = getInitializationVector();
        String cardNumber = "1234567812345678";
        String encrypted = cryptoUtil.encrypt(cardNumber, iv);
        String decrypted = cryptoUtil.decrypt(encrypted, iv);

        System.out.println("SECRET_KEY: " + cryptoUtil.getSecretKey());
        System.out.println("cardNumber: " + cardNumber);
        System.out.println("encrypted: " + encrypted);
        System.out.println("decrypted: " + decrypted);
    }

    public String encrypt(String cardNumber, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            return ENCODER.encodeToString(cipher.doFinal(cardNumber.getBytes()));
        } catch (Exception e) {
            throw new AppRuntimeException("Error card number encrypting: " + e.getMessage());
        }
    }

    public String decrypt(String encryptedCard, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            return new String(cipher.doFinal(DECODER.decode(encryptedCard)));
        } catch (Exception e) {
            throw new AppRuntimeException("Error card number decrypting: " + e.getMessage());
        }
    }
}
