package com.liz.library.domain.model;

import lombok.Getter;
import java.util.UUID;

@Getter
public class Book {

    private final UUID id;
    private final String title;
    private final String author;
    private final String isbn;
    private final Integer publishedYear;
    private final String genre;
    private final Integer totalCopies;

    private Integer availableCopies;

    private Book(
            UUID id,
            String title,
            String author,
            String isbn,
            Integer publishedYear,
            String genre,
            Integer totalCopies,
            Integer availableCopies) {

        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publishedYear = publishedYear;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public static Book create(
            String title,
            String author,
            String isbn,
            Integer publishedYear,
            String genre,
            Integer totalCopies) {


        return new Book(
                UUID.randomUUID(),
                title.trim(),
                author.trim(),
                isbn.trim(),
                publishedYear,
                genre.trim(),
                totalCopies,
                totalCopies
        );
    }

    public static Book restore(
            UUID id,
            String title,
            String author,
            String isbn,
            Integer publishedYear,
            String genre,
            Integer totalCopies,
            Integer availableCopies) {

        return new Book(
                id,
                title,
                author,
                isbn,
                publishedYear,
                genre,
                totalCopies,
                availableCopies
        );
    }
}