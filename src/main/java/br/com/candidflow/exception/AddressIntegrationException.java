package br.com.candidflow.exception;

import org.springframework.http.HttpStatus;

public class AddressIntegrationException extends RuntimeException {
    private final HttpStatus status;
    public AddressIntegrationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() { return status; }
}
