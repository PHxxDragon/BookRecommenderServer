package com.is.bookrecommender.repository;

import com.is.bookrecommender.model.Test;
import com.is.bookrecommender.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
}
