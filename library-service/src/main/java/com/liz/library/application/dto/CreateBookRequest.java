package com.liz.library.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create a new book")
public class CreateBookRequest {

    @Schema(
            description = "Book title",
            example = "The Hobbit",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{book.title.required}")
    private String title;

    @Schema(
            description = "Book author",
            example = "J.R.R. Tolkien",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{book.author.required}")
    private String author;

    @Schema(
            description = "International Standard Book Number (ISBN)",
            example = "9780261103344",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{book.isbn.required}")
    private String isbn;

    @Schema(
            description = "Publication year",
            example = "1937",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "{book.publishedYear.required}")
    @Min(value = 1, message = "{book.publishedYear.min}")
    private Integer publishedYear;

    @Schema(
            description = "Book genre",
            example = "Fantasy",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "{book.genre.required}")
    private String genre;

    @Schema(
            description = "Total number of copies owned by the library",
            example = "5",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "{book.totalCopies.required}")
    @Min(value = 1, message = "{book.totalCopies.min}")
    private Integer totalCopies;
}