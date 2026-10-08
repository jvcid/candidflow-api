package br.com.candidflow.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "candidflow.security.hmac")
@Getter
@Setter
public class HmacProperties {

    private boolean enabled = true;
    private String apiKey;
    private String secret;
    private long maxClockSkewSeconds = 300;

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("HMAC_API_KEY não foi configurada");
        }
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException("HMAC_SECRET deve possuir pelo menos 32 caracteres");
        }
        if (maxClockSkewSeconds <= 0) {
            throw new IllegalStateException("HMAC_MAX_CLOCK_SKEW_SECONDS deve ser positivo");
        }
    }
}
