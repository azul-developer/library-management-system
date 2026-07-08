package com.liz.library.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BookResponse {

    private UUID id;

    private String title;

    private String author;

    private String isbn;

    private Integer publishedYear;

    private String genre;

    private Integer totalCopies;

    private Integer availableCopies;

}