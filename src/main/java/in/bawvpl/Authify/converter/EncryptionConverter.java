package in.bawvpl.Authify.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {

    private static final String SECRET_KEY =
            "12345678901234567890123456789012";

    @Override
    public String convertToDatabaseColumn(String value) {

        System.out.println(">>> Encrypting: " + value);

        if (value == null || value.isBlank()) {
            return value;
        }

        try {

            SecretKeySpec key = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, key);

            String encrypted = Base64.getEncoder().encodeToString(
                    cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))
            );

            System.out.println(">>> Encrypted Value: " + encrypted);

            return encrypted;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException("Encryption Error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String value) {

        if (value == null || value.isBlank()) {
            return value;
        }

        try {

            SecretKeySpec key = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);

            return new String(

                    cipher.doFinal(

                            Base64.getDecoder().decode(value)
                    ),

                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            // Old plain-text records will continue to work
            return value;
        }
    }
}
