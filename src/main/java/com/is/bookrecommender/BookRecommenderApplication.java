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
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(BookRecommenderApplication.class, args);
    }

    @PostConstruct
    protected void init() {
        List<Authority> authorityList = new ArrayList<>();

        authorityList.add(createAuthority("USER", "User role"));
        authorityList.add(createAuthority("ADMIN", "Admin role"));

        User user = new User();
        user.setUsername("duy12345");
        user.setPassword(passwordEncoder.encode("duy12345"));
        user.setEnabled(true);

        user.setAuthorities(authorityList);
        userRepository.save(user);

    }

    private Authority createAuthority(String roleCode, String roleDescription) {
        Authority authority = new Authority();
        authority.setRoleCode(roleCode);
        authority.setRoleDescription(roleDescription);
        return authority;
    }
}
