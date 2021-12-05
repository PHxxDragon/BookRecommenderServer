package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.BookRepository;
import com.is.bookrecommender.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;

@Service
@Transactional
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    public BookDto getBookFromBookId(Long id, Principal user) {
        Book book = bookRepository.findById(id).orElseThrow();
        BookDto bookDto = applicationMapper.mapBookToBookDto(book);
        bookDto.setRating(ratingRepository.getAverageRating(id));
        if (user != null) {
            User userObj = (User) userDetailsService.loadUserByUsername(user.getName());
            Optional<Rating> rating = ratingRepository.findById(new Rating.RatingId(id, userObj.getId()));
            if (rating.isPresent()) {
                bookDto.setUserRating(rating.get().getRating());
            }

        }
        return bookDto;
    }
}
