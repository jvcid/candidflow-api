package br.com.candidflow.dto.response;

import java.util.List;

public record CandidateAddressResponseDTO(CandidateResponseDTO candidate,
        List<AddressResponseDTO> addresses) {}
