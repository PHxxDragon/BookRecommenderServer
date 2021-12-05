package com.is.bookrecommender.repository;

import com.is.bookrecommender.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}
