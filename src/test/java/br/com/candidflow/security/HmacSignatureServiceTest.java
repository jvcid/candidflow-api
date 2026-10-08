package br.com.candidflow.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmacSignatureServiceTest {

    private final HmacSignatureService service = new HmacSignatureService();
    private final String secret = "0123456789abcdef0123456789abcdef";

    @Test
    void shouldValidateUntamperedRequest() {
        byte[] body = "{\"status\":\"INTERVIEW\"}".getBytes(StandardCharsets.UTF_8);
        String signature = service.sign(
                "PATCH",
                "/api/applications/1/status",
                "1791320000",
                "27c10f8f-25ef-46df-b19f-51bbc9b03eb2",
                body,
                secret
        );

        assertEquals(
                "e3cb3d3ac3ee8bce8b44921b7fc8b07fb33004cfb065aa3ddab5444c1a0a2446",
                signature
        );

        boolean valid = service.isValid(
                signature,
                "PATCH",
                "/api/applications/1/status",
                "1791320000",
                "27c10f8f-25ef-46df-b19f-51bbc9b03eb2",
                body,
                secret
        );

        assertTrue(valid);
    }

    @Test
    void shouldRejectTamperedBody() {
        byte[] originalBody = "{\"status\":\"INTERVIEW\"}".getBytes(StandardCharsets.UTF_8);
        String signature = service.sign(
                "PATCH",
                "/api/applications/1/status",
                "1791320000",
                "27c10f8f-25ef-46df-b19f-51bbc9b03eb2",
                originalBody,
                secret
        );

        byte[] tamperedBody = "{\"status\":\"APPROVED\"}".getBytes(StandardCharsets.UTF_8);

        assertFalse(service.isValid(
                signature,
                "PATCH",
                "/api/applications/1/status",
                "1791320000",
                "27c10f8f-25ef-46df-b19f-51bbc9b03eb2",
                tamperedBody,
                secret
        ));
    }
}
