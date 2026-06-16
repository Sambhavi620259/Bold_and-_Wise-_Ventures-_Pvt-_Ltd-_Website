package in.bawvpl.Authify.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class InviteTokenUtil {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private InviteTokenUtil() {
    }

    public static String generateToken() {

        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {

            sb.append(
                    String.format("%02x", b)
            );
        }

        return sb.toString();
    }

    public static String sha256(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder sb =
                    new StringBuilder();

            for (byte b : hash) {

                sb.append(
                        String.format("%02x", b)
                );
            }

            return sb.toString();

        } catch (Exception ex) {

            throw new RuntimeException(
                    ex
            );
        }
    }
}