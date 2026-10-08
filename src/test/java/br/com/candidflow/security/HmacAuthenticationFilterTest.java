package br.com.candidflow.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HmacAuthenticationFilterTest {

    private static final String API_KEY = "candidflow-test-client";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private HmacSignatureService signatureService;
    private HmacAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        HmacProperties properties = new HmacProperties();
        properties.setEnabled(true);
        properties.setApiKey(API_KEY);
        properties.setSecret(SECRET);
        properties.setMaxClockSkewSeconds(300);

        signatureService = new HmacSignatureService();
        filter = new HmacAuthenticationFilter(properties, signatureService);
    }

    @Test
    void shouldAcceptValidSignatureAndRejectNonceReplay() throws ServletException, IOException {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "a1aec8ce-bd7a-44c9-a5f5-93be6db7d488";
        byte[] body = "{\"name\":\"João\"}".getBytes(StandardCharsets.UTF_8);
        String signature = signatureService.sign(
                "POST", "/api/candidates", timestamp, nonce, body, SECRET
        );

        MockHttpServletResponse firstResponse = executeRequest(timestamp, nonce, signature, body);
        assertEquals(200, firstResponse.getStatus());

        MockHttpServletResponse replayResponse = executeRequest(timestamp, nonce, signature, body);
        assertEquals(401, replayResponse.getStatus());
    }

    @Test
    void shouldRejectRequestWithTamperedBody() throws ServletException, IOException {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonce = "07d45c89-d2ee-47bd-8e33-980d8779c211";
        byte[] originalBody = "{\"name\":\"João\"}".getBytes(StandardCharsets.UTF_8);
        String signature = signatureService.sign(
                "POST", "/api/candidates", timestamp, nonce, originalBody, SECRET
        );

        byte[] tamperedBody = "{\"name\":\"Outro nome\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletResponse response = executeRequest(timestamp, nonce, signature, tamperedBody);

        assertEquals(401, response.getStatus());
    }

    private MockHttpServletResponse executeRequest(
            String timestamp,
            String nonce,
            String signature,
            byte[] body
    ) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/candidates");
        request.setContent(body);
        request.addHeader(HmacAuthenticationFilter.API_KEY_HEADER, API_KEY);
        request.addHeader(HmacAuthenticationFilter.TIMESTAMP_HEADER, timestamp);
        request.addHeader(HmacAuthenticationFilter.NONCE_HEADER, nonce);
        request.addHeader(HmacAuthenticationFilter.SIGNATURE_HEADER, signature);

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
