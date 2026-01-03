package com.shieldauth.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int ARGON2_ITERATIONS = 10;
    private static final int ARGON2_MEMORY = 65536;
    private static final int ARGON2_PARALLELISM = 1;
    private static final int BCRYPT_COST = 12;

    public enum EncryptionType {
        SHA256,
        SHA512,
        BCRYPT,
        ARGON2
    }

    public static String hash(String password, EncryptionType type) {
        switch (type) {
            case SHA256:
                return hashSHA256(password);
            case SHA512:
                return hashSHA512(password);
            case BCRYPT:
                return hashBCrypt(password);
            case ARGON2:
            default:
                return hashArgon2(password);
        }
    }

    public static boolean verify(String password, String hash, EncryptionType type) {
        switch (type) {
            case SHA256:
                return verifySHA256(password, hash);
            case SHA512:
                return verifySHA512(password, hash);
            case BCRYPT:
                return verifyBCrypt(password, hash);
            case ARGON2:
            default:
                return verifyArgon2(password, hash);
        }
    }

    private static String hashSHA256(String password) {
        try {
            byte[] salt = generateSalt(16);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static boolean verifySHA256(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Base64.getDecoder().decode(parts[1]);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] newHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(originalHash, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static String hashSHA512(String password) {
        try {
            byte[] salt = generateSalt(16);
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 not available", e);
        }
    }

    private static boolean verifySHA512(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Base64.getDecoder().decode(parts[1]);
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(salt);
            byte[] newHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(originalHash, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static String hashBCrypt(String password) {
        byte[] salt = generateSalt(16);
        byte[] hash = BCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, BCRYPT_COST);
        return Base64.getEncoder().encodeToString(salt) + "$" + Hex.toHexString(hash);
    }

    private static boolean verifyBCrypt(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Hex.decode(parts[1]);
            byte[] newHash = BCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, BCRYPT_COST);
            return MessageDigest.isEqual(originalHash, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static String hashArgon2(String password) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            return argon2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, password.toCharArray());
        } finally {
            argon2.wipeArray(password.toCharArray());
        }
    }

    private static boolean verifyArgon2(String password, String hash) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            return argon2.verify(hash, password.toCharArray());
        } catch (Exception e) {
            return false;
        } finally {
            argon2.wipeArray(password.toCharArray());
        }
    }

    private static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public static EncryptionType fromString(String type) {
        try {
            return EncryptionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EncryptionType.ARGON2;
        }
    }

    public static String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    public static String hashPin(String pin) {
        try {
            byte[] salt = generateSalt(16);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean verifyPin(String pin, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Base64.getDecoder().decode(parts[1]);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] newHash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(originalHash, newHash);
        } catch (Exception e) {
            return false;
        }
    }
}
