package com.is.bookrecommender.repository;

import com.is.bookrecommender.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findBookByTitleContains(String keyword, Pageable pageable);

    Page<Book> findBooksByIdIn(List<Long> ids, Pageable pageable);
}
