package com.pgq.manbookck.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

import com.pgq.manbookck.models.Author;

import java.util.List;

public interface AuthorRepo extends JpaRepository<Author, Long> {
    List<Author> findAllByOrderByNameAsc();
}