package com.liz.library.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookFilter {

    private String author;

    private String genre;

    private Boolean available;

}
