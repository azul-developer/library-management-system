package com.liz.library.infrastructure.persistence.repository;

import com.liz.library.infrastructure.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface BookJpaRepository extends JpaRepository<BookEntity, UUID>, JpaSpecificationExecutor<BookEntity> {

    Optional<BookEntity> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);
}