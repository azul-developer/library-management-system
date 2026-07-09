package com.liz.library.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class LoanResponse {
    private UUID id;
    private UUID userId;
    private UUID bookId;
    private OffsetDateTime createdAt;
    private OffsetDateTime returnedAt;
    private Boolean returned;
}