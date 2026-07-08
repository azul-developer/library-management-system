package com.liz.library.domain.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BookQuery {

    private final String author;

    private final String genre;

    private final Boolean available;

    public static BookQuery of(String author, String genre, Boolean available) {
        return new BookQuery(author, genre, available);
    }
}
