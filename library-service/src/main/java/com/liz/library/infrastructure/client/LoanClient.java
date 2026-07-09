package com.liz.library.infrastructure.client;

import com.liz.library.application.dto.LoanResponse;
import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Component
public class LoanClient {

    private final WebClient webClient;

    public LoanClient(WebClient.Builder builder,
                      @Value("${LOAN_SERVICE_URL:http://localhost:8081}") String baseUrl) {

        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public LoanResponse createLoan(UUID userId, UUID bookId) {

        HashMap<String, String> payload = new HashMap<>();
        payload.put("userId", userId.toString());
        payload.put("bookId", bookId.toString());

        try {

            return webClient.post()
                    .uri("/api/loan")
                    .bodyValue(payload)
                    .retrieve()

                    .onStatus(HttpStatusCode::is4xxClientError, response -> {

                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new BusinessException(MessageCodes.BOOK_NOT_FOUND));
                        }

                        if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                            return Mono.error(new BusinessException(MessageCodes.BOOK_NOT_AVAILABLE));
                        }

                        return response.bodyToMono(String.class)
                                .flatMap(body -> {

                                    log.error(
                                            "Loan-service returned {}. Response: {}",
                                            response.statusCode(),
                                            body);

                                    return Mono.error(
                                            new BusinessException(MessageCodes.LOAN_SERVICE_ERROR));
                                });
                    })

                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {

                                        log.error(
                                                "Loan-service returned {}. Response: {}",
                                                response.statusCode(),
                                                body);

                                        return Mono.error(
                                                new BusinessException(MessageCodes.LOAN_SERVICE_ERROR));
                                    }))

                    .bodyToMono(LoanResponse.class)
                    .block(Duration.ofSeconds(5));

        } catch (WebClientRequestException e) {

            log.error("Unable to communicate with loan-service", e);

            throw new BusinessException(MessageCodes.LOAN_SERVICE_UNAVAILABLE);
        }
    }
}