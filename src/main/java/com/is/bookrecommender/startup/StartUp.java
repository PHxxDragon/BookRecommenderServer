package com.is.bookrecommender.startup;

import com.is.bookrecommender.model.*;
import com.is.bookrecommender.repository.*;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

@Component
public class StartUp {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private void init3() throws IOException {
        CSVReader ratingCSVReader = constructCSVReaderFromFile("/ratings.csv");
        String[] tokens = ratingCSVReader.readNext();
        
        int j = 0;
        List<Rating> ratings = new ArrayList<>();
        while (tokens != null) {
            j++;
            if (j % 1000 == 0) {
                System.out.println("rating: " + j);
            }
            long user_id = Long.parseLong(tokens[0]);
            long book_id = Long.parseLong(tokens[1]);
            int rating = Integer.parseInt(tokens[2]);

            Rating ratingObj = new Rating();
            ratingObj.setRating(rating);
            ratingObj.setId(new Rating.RatingId(book_id, user_id));
            ratings.add(ratingObj);
            if (j % 10000 == 0) {
                insertRating(ratings);
                ratings.clear();
            }

            tokens = ratingCSVReader.readNext();
        }

    }
    
    private void insertRating(List<Rating> ratings) {
        String SQL = "INSERT INTO rating(book_id,user_id,rating)"
                + "VALUES(?,?,?)";

        try (Connection connection = connect();
             PreparedStatement stmt = connection.prepareStatement(SQL)) {
            for (Rating rating : ratings) {
                stmt.setLong(1, rating.getId().getBookId());
                stmt.setLong(2, rating.getId().getUserId());
                stmt.setInt(3, rating.getRating());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    //@PostConstruct
    public void init() throws IOException {
        init1();
        init2();
        init3();
    }

    private void init2() throws IOException {
        CSVReader ratingCSVReader = constructCSVReaderFromFile("/ratings.csv");
        int maxID = -1;
        String [] tokens = ratingCSVReader.readNext();
        while(tokens != null) {
            int id = Integer.parseInt(tokens[0]);
            if (id > maxID) {
                maxID = id;
            }
            tokens = ratingCSVReader.readNext();
        }

        List<Authority> authorityList = new ArrayList<>();
        authorityList.add(createAuthority("USER", "User role"));
        authorityRepository.saveAll(authorityList);

        String password = passwordEncoder.encode("password");
        for (int i = 1; i <= maxID; i++){
            if (i % 1000 == 0) {
                System.out.println("user: " + i);
            }
            User user = new User();
            user.setId((long) i);
            user.setUsername("username" + i);
            user.setPassword(password);
            user.setEnabled(true);
            user.setName("name" + i);
            user.setMail("username" + i + "@gmail.com");
            user.setCountry("USA");
            user.setAge(20);
            user.setAuthorities(authorityList);
            userRepository.save(user);
        }

        System.out.println("done saving user");
    }

    private void init1() throws IOException {
        CSVReader bookCSVReader = constructCSVReaderFromFile("/books.csv");
        String[] tokens = bookCSVReader.readNext();
        Map<String, Author> authorMap = new HashMap<>();

        int k = 0;
        while (tokens != null) {
            k++;
            if (k % 1000 == 0) {
                System.out.println("book: " + k);
            }
            Book book = new Book();
            book.setId(Long.parseLong(tokens[0]));
            book.setTitle(tokens[10]);

            String[] authors = tokens[7].split(",");
            List<Author> authorList = new ArrayList<>();
            for (String author: authors) {
                if (authorMap.containsKey(author)) {
                    authorList.add(authorMap.get(author));
                } else {
                    Author authorObj = new Author();
                    authorObj.setName(author);
                    authorList.add(authorObj);
                    authorMap.put(author, authorObj);
                    authorRepository.save(authorObj);
                }
            }
            book.setAuthor(authorList);
            try {
                book.setPublishYear((int) Float.parseFloat(tokens[8]));
            } catch (NumberFormatException e) {
                book.setPublishYear(null);
            }

            book.setImageURL(tokens[21]);
            bookRepository.save(book);

            tokens = bookCSVReader.readNext();
        }

        System.out.println("Done saving book and author");
    }

    private CSVReader constructCSVReaderFromFile(String filename) {
        InputStream in = getClass().getResourceAsStream(filename);
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);

        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).withCSVParser(parser).build();
        return csvReader;
    }

    private Authority createAuthority(String roleCode, String roleDescription) {
        Authority authority = new Authority();
        authority.setRoleCode(roleCode);
        authority.setRoleDescription(roleDescription);
        return authority;
    }
}
