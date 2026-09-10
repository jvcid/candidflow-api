package br.com.candidflow.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " não encontrado(a) com o identificador " + id);
    }
}
