package com.liz.library.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookRequest {

    @NotBlank(message = "{book.title.required}")
    private String title;

    @NotBlank(message = "{book.author.required}")
    private String author;

    @NotBlank(message = "{book.isbn.required}")
    private String isbn;

    @NotNull(message = "{book.publishedYear.required}")
    @Min(value = 1, message = "{book.publishedYear.min}")
    private Integer publishedYear;

    @NotBlank(message = "{book.genre.required}")
    private String genre;

    @NotNull(message = "{book.totalCopies.required}")
    @Min(value = 1, message = "{book.totalCopies.min}")
    private Integer totalCopies;

}