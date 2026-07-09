package com.liz.library.bootstrap.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liz.library.bootstrap.AbstractDataSeeder;
import com.liz.library.bootstrap.SeedType;
import com.liz.library.domain.model.Book;
import com.liz.library.domain.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookDataSeeder extends AbstractDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(BookDataSeeder.class);

    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SeedType type() {
        return SeedType.BOOK;
    }

    @Override
    @Transactional
    public void seed() throws Exception {
        ClassPathResource resource = new ClassPathResource("seed/books.json");
        if (!resource.exists()) {
            log.warn("Book seed file not found: seed/books.json");
            return;
        }

        List<BookSeed> seeds;
        try (InputStream is = resource.getInputStream()) {
            seeds = objectMapper.readValue(is, new TypeReference<List<BookSeed>>() {});
        }

        for (BookSeed s : seeds) {
            if (s.isbn() == null || s.isbn().isBlank()) {
                log.warn("Skipping seed with missing isbn: {}", s.title());
                continue;
            }
            if (bookRepository.existsByIsbn(s.isbn())) {
                log.info("Seed: book with isbn {} already exists — skipping", s.isbn());
                continue;
            }

            Book book = Book.create(s.title(), s.author(), s.isbn(), s.publishedYear(), s.genre(), s.totalCopies());
            bookRepository.save(book);
            log.info("Seed: created book {} ({})", s.title(), s.isbn());
        }
    }

    private record BookSeed(String isbn, String title, String author, Integer publishedYear, String genre, Integer totalCopies) {}
}
