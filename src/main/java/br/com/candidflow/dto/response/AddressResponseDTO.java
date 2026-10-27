package br.com.candidflow.dto.response;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record AddressResponseDTO(String cep, String logradouro, String complemento,
        String bairro, String localidade, String uf, String ibge, String ddd, Boolean erro) {}
