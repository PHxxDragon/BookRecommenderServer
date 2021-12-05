package com.is.bookrecommender;

import com.is.bookrecommender.model.Authority;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class BookRecommenderApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookRecommenderApplication.class, args);
    }
}
