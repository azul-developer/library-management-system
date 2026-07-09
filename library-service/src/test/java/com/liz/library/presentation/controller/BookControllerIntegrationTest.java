package com.liz.library.presentation.controller;

import com.liz.library.application.dto.CreateBookRequest;
import com.liz.library.infrastructure.persistence.entity.BookEntity;
import com.liz.library.infrastructure.persistence.repository.BookJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Test
    void delete_removesBook_usingRealPostgres() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Integration Test Book");
        request.setAuthor("Integration Author");
        // Ensure ISBN fits into DB column (varchar(20)).
        String shortIsbn = "ISBN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        request.setIsbn(shortIsbn);
        request.setPublishedYear(2020);
        request.setGenre("Test");
        request.setTotalCopies(3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateBookRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> createResp = restTemplate.postForEntity("/api/books", entity, Map.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map body = createResp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("id");

        UUID id = UUID.fromString(body.get("id").toString());

        ResponseEntity<Void> del = restTemplate.exchange("/api/books/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar que el recurso ya no existe vía JPA/BD
        assertThat(bookJpaRepository.findById(id)).isEmpty();
    }
}
