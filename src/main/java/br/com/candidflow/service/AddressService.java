package br.com.candidflow.service;

import br.com.candidflow.dto.response.AddressResponseDTO;
import br.com.candidflow.exception.AddressIntegrationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class AddressService {
    private final RestClient client;

    public AddressService() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        client = RestClient.builder().baseUrl("https://viacep.com.br/ws")
                .requestFactory(factory).build();
    }

    AddressService(RestClient client) { this.client = client; }

    public AddressResponseDTO findByCep(String cep) {
        if (cep == null || !cep.matches("[0-9]{8}")) {
            throw new AddressIntegrationException(HttpStatus.BAD_REQUEST, "Cadastre um CEP valido no candidato");
        }
        AddressResponseDTO address;
        try {
            address = client.get().uri("/{cep}/json/", cep).retrieve().body(AddressResponseDTO.class);
        } catch (RestClientException exception) {
            throw unavailable();
        }
        if (address == null) throw unavailable();
        if (Boolean.TRUE.equals(address.erro())) {
            throw new AddressIntegrationException(HttpStatus.NOT_FOUND, "CEP nao encontrado no ViaCEP");
        }
        return address;
    }

    public List<AddressResponseDTO> search(String uf, String city, String street) {
        if (uf == null || !uf.matches("(?i)AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO")
                || city == null || city.trim().length() < 3 || city.length() > 120
                || street == null || street.trim().length() < 3 || street.length() > 120) {
            throw new AddressIntegrationException(HttpStatus.BAD_REQUEST,
                    "Informe UF valida e cidade/rua com 3 a 120 caracteres");
        }
        try {
            AddressResponseDTO[] addresses = client.get().uri("/{uf}/{city}/{street}/json/",
                    uf.toUpperCase(Locale.ROOT), city.trim(), street.trim())
                    .retrieve().body(AddressResponseDTO[].class);
            if (addresses == null) throw unavailable();
            return Arrays.asList(addresses);
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private AddressIntegrationException unavailable() {
        return new AddressIntegrationException(HttpStatus.BAD_GATEWAY,
                "Nao foi possivel consultar o ViaCEP. Tente novamente mais tarde");
    }
}
