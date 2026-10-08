package br.com.candidflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class HmacAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String TIMESTAMP_HEADER = "X-Timestamp";
    public static final String NONCE_HEADER = "X-Nonce";
    public static final String SIGNATURE_HEADER = "X-Signature";

    private final HmacProperties properties;
    private final HmacSignatureService signatureService;
    private final Map<String, Long> usedNonces = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled()
                || !request.getRequestURI().startsWith("/api/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String apiKey = request.getHeader(API_KEY_HEADER);
        String timestamp = request.getHeader(TIMESTAMP_HEADER);
        String nonce = request.getHeader(NONCE_HEADER);
        String signature = request.getHeader(SIGNATURE_HEADER);

        if (isBlank(apiKey) || isBlank(timestamp) || isBlank(nonce) || isBlank(signature)) {
            unauthorized(response, request, "Cabeçalhos de autenticação HMAC ausentes");
            return;
        }

        if (!constantTimeEquals(properties.getApiKey(), apiKey)) {
            unauthorized(response, request, "Credenciais HMAC inválidas");
            return;
        }

        long requestTimestamp;
        try {
            requestTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            unauthorized(response, request, "Timestamp HMAC inválido");
            return;
        }

        long now = Instant.now().getEpochSecond();
        long allowedSkew = properties.getMaxClockSkewSeconds();
        if (requestTimestamp < now - allowedSkew || requestTimestamp > now + allowedSkew) {
            unauthorized(response, request, "Assinatura HMAC expirada");
            return;
        }

        String requestTarget = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            requestTarget += "?" + request.getQueryString();
        }

        boolean validSignature = signatureService.isValid(
                signature,
                request.getMethod(),
                requestTarget,
                timestamp,
                nonce,
                cachedRequest.getCachedBody(),
                properties.getSecret()
        );

        if (!validSignature) {
            unauthorized(response, request, "Assinatura HMAC inválida");
            return;
        }

        removeExpiredNonces(now);
        if (usedNonces.putIfAbsent(nonce, requestTimestamp) != null) {
            unauthorized(response, request, "A requisição HMAC já foi utilizada");
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private void removeExpiredNonces(long now) {
        long oldestAccepted = now - properties.getMaxClockSkewSeconds();
        usedNonces.entrySet().removeIf(entry -> entry.getValue() < oldestAccepted);
    }

    private boolean constantTimeEquals(String expected, String provided) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void unauthorized(
            HttpServletResponse response,
            HttpServletRequest request,
            String message
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("WWW-Authenticate", "HMAC");

        String body = """
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"%s","path":"%s"}
                """.formatted(
                LocalDateTime.now(),
                escapeJson(message),
                escapeJson(request.getRequestURI())
        );
        response.getWriter().write(body);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
