package br.com.candidflow.service;

import br.com.candidflow.exception.AddressIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AddressServiceTest {
    @Test
    void shouldCombineLocalCandidateWithBothExternalResponses() {
        var candidate = br.com.candidflow.dto.response.CandidateResponseDTO.builder()
                .id(7L).name("Paulo").email("paulo@example.com").cep("01001000").build();
        var candidates = new CandidateService(null, null, null) {
            @Override public br.com.candidflow.dto.response.CandidateResponseDTO findById(Long id) {
                assertEquals(7L, id);
                return candidate;
            }
        };
        var builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        var server = MockRestServiceServer.bindTo(builder).build();
        var controller = new br.com.candidflow.controller.CandidateController(candidates, new AddressService(builder.build()));
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("{\"cep\":\"01001-000\",\"uf\":\"SP\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.com.br/ws/SP/Sao%20Paulo/Praca%20da%20Se/json/"))
                .andRespond(withSuccess("[{\"cep\":\"01001-000\"}]", MediaType.APPLICATION_JSON));
        var address = controller.address(7L).getBody();
        assertEquals("Paulo", address.candidate().getName());
        assertEquals("SP", address.addresses().getFirst().uf());
        var search = controller.searchAddress(7L, "SP", "Sao Paulo", "Praca da Se").getBody();
        assertEquals(7L, search.candidate().getId());
        assertEquals("01001-000", search.addresses().getFirst().cep());
        server.verify();
    }

    @Test
    void shouldCallBothViaCepOperations() {
        var builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new AddressService(builder.build());
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("{\"cep\":\"01001-000\",\"localidade\":\"Sao Paulo\",\"uf\":\"SP\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.com.br/ws/SP/Sao%20Paulo/Praca%20da%20Se/json/"))
                .andRespond(withSuccess("[{\"cep\":\"01001-000\"}]", MediaType.APPLICATION_JSON));
        assertEquals("SP", service.findByCep("01001000").uf());
        assertEquals(1, service.search("sp", "Sao Paulo", "Praca da Se").size());
        server.verify();
    }

    @Test
    void shouldRejectInvalidInputWithoutCallingExternalApi() {
        var service = new AddressService(RestClient.builder().requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory()).build());
        assertEquals(400, assertThrows(AddressIntegrationException.class,
                () -> service.findByCep(null)).getStatus().value());
        assertThrows(AddressIntegrationException.class, () -> service.search("XX", "ab", "cd"));
    }

    @Test
    void shouldHandleMissingCepAndProviderFailure() {
        var builder = RestClient.builder().baseUrl("https://viacep.com.br/ws");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new AddressService(builder.build());
        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withServerError());
        assertEquals(404, assertThrows(AddressIntegrationException.class,
                () -> service.findByCep("99999999")).getStatus().value());
        assertEquals(502, assertThrows(AddressIntegrationException.class,
                () -> service.findByCep("01001000")).getStatus().value());
        server.verify();
    }
}
