package com.liz.library.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Optional filters for searching books")
public class BookFilter {

    @Schema(
            description = "Filter books by author. Example: J.R.R. Tolkien"
    )
    private String author;

    @Schema(
            description = "Filter books by genre. Example: Fantasy"
    )
    private String genre;

    @Schema(
            description = "Filter books by availability. " +
                    "Use true to return books with at least one available copy, " +
                    "or false to return books with no available copies. " +
                    "Example: true"
    )
    private Boolean available;
}