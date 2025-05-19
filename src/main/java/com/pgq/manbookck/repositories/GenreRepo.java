package com.pgq.manbookck.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pgq.manbookck.models.Genre;

public interface GenreRepo extends JpaRepository<Genre, Long> {
    List<Genre> findAllByOrderByNameAsc();
}