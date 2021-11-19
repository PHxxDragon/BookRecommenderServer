package com.is.bookrecommender.repository;

import java.util.List;

import com.is.bookrecommender.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByPublished(boolean published);

    List<Test> findByTitleContaining(String title);
}