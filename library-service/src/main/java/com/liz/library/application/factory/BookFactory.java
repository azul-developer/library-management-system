package com.liz.library.application.factory;

import com.liz.library.application.dto.CreateBookRequest;
import com.liz.library.domain.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookFactory {

    public Book create(CreateBookRequest request) {

        return Book.create(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPublishedYear(),
                request.getGenre(),
                request.getTotalCopies()
        );
    }
}