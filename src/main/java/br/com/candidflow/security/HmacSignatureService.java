package br.com.candidflow.security;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class HmacSignatureService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HASH_ALGORITHM = "SHA-256";

    public String sign(
            String method,
            String requestTarget,
            String timestamp,
            String nonce,
            byte[] body,
            String secret
    ) {
        String canonicalRequest = String.join("\n",
                method.toUpperCase(),
                requestTarget,
                timestamp,
                nonce,
                sha256Hex(body)
        );

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível calcular a assinatura HMAC", exception);
        }
    }

    public boolean isValid(
            String providedSignature,
            String method,
            String requestTarget,
            String timestamp,
            String nonce,
            byte[] body,
            String secret
    ) {
        if (providedSignature == null) {
            return false;
        }

        String expectedSignature = sign(method, requestTarget, timestamp, nonce, body, secret);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.US_ASCII),
                providedSignature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(body));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível calcular o hash da requisição", exception);
        }
    }
}
