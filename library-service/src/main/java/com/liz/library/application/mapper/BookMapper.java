package com.liz.library.application.mapper;

import com.liz.library.application.dto.BookResponse;
import com.liz.library.domain.model.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookResponse toResponse(Book book);
}