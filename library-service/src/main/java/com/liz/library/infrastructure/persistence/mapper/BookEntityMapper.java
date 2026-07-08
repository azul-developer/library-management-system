package com.liz.library.infrastructure.persistence.mapper;

import com.liz.library.domain.model.Book;
import com.liz.library.infrastructure.persistence.entity.BookEntity;

public final class BookEntityMapper {

    private BookEntityMapper() {
    }

    public static Book toDomain(BookEntity entity) {

        return Book.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getIsbn(),
                entity.getPublishedYear(),
                entity.getGenre(),
                entity.getTotalCopies(),
                entity.getAvailableCopies()
        );
    }

    public static BookEntity toEntity(Book book) {

        return BookEntity.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publishedYear(book.getPublishedYear())
                .genre(book.getGenre())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .build();
    }
}