package com.liz.library.infrastructure.persistence.repository.specification;

import com.liz.library.infrastructure.persistence.entity.BookEntity;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<BookEntity> byFilters(String author, String genre, Boolean available) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            if (author != null && !author.isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }

            if (genre != null && !genre.isBlank()) {
                predicates = cb.and(predicates, cb.equal(cb.lower(root.get("genre")), genre.toLowerCase()));
            }

            if (available != null) {
                if (available) {
                    predicates = cb.and(predicates, cb.greaterThan(root.get("availableCopies"), 0));
                } else {
                    predicates = cb.and(predicates, cb.equal(root.get("availableCopies"), 0));
                }
            }

            return predicates;
        };
    }
}
