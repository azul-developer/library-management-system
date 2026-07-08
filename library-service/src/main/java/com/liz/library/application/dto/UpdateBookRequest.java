package com.liz.library.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookRequest {

    private String title;

    private String author;

    private String isbn;

    private Integer publishedYear;

    private String genre;

    private Integer totalCopies;

}
