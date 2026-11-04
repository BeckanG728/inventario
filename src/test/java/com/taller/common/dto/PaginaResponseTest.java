package com.taller.common.dto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CATALOGO_ENDPOINTS §5.2 — la respuesta paginada expone exactamente
 * content, page, size, totalElements y totalPages.
 */
class PaginaResponseTest {

    @Test
    void from_copiaLosDatosDeLaPagina() {
        PageImpl<String> pagina = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PaginaResponse<String> response = PaginaResponse.from(pagina);

        assertEquals(List.of("a", "b"), response.content());
        assertEquals(1, response.page());
        assertEquals(2, response.size());
        assertEquals(5, response.totalElements());
        assertEquals(3, response.totalPages());
    }

    @Test
    void json_exponeSoloLosCincoCamposDocumentados() throws Exception {
        PageImpl<String> pagina = new PageImpl<>(List.of("a"), PageRequest.of(0, 10), 1);

        JsonNode json = new ObjectMapper().valueToTree(PaginaResponse.from(pagina));

        List<String> campos = json.propertyNames().stream().sorted().toList();
        assertEquals(List.of("content", "page", "size", "totalElements", "totalPages"), campos);
    }
}
