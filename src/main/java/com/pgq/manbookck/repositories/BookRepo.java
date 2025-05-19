package com.pgq.manbookck.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pgq.manbookck.models.Book;

public interface BookRepo extends JpaRepository<Book, Long>  {
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.genres ORDER BY b.title ASC")
    List<Book> findAllWithAuthorsAndGenres();

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.genres WHERE b.id = :id")
    Book findByIdWithAuthorsAndGenres(Long id);
}
