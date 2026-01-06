package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/*
 * Crittografia e decrittografia AES => dati utente
 * Chiave segreta in config.properties
 * Tutti i dati criptati vengono codificati in Base64 nel DB.
*/

public class Crypto {

    private static final String alg = "AES";

    public static String encrypt(String input) throws Exception {
        // Chiave
        String keyStr = StandardConfig.getInstance().getProps("CRYPTO_KEY");
        SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(), alg);

        // Cifrario AES
        Cipher cipher = Cipher.getInstance(alg);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // Cripta la stringa
        byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        // Codifica in Base64 per salvare in DB
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String input) throws Exception {
        // Chiave
        String keyStr = StandardConfig.getInstance().getProps("CRYPTO_KEY");
        SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(), alg);

        // Cifrario AES
        Cipher cipher = Cipher.getInstance(alg);
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Decodifica Base64 e decripta
        byte[] decoded = Base64.getDecoder().decode(input);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
