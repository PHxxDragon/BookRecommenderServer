package com.is.bookrecommender.service.implementation;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.dto.RatingDto;
import com.is.bookrecommender.dto.SearchDto;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.BookRepository;
import com.is.bookrecommender.repository.RatingRepository;
import com.is.bookrecommender.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;

@Service
@Transactional
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserDetailsService userDetailsService;

    public BookDto getBookFromBookId(Long id, Principal user) throws ResourceNotFoundException {
        Optional<Book> book = bookRepository.findById(id);
        if (!book.isPresent()) {
            throw new ResourceNotFoundException(id, "Cannot find book");
        }
        BookDto bookDto = applicationMapper.mapBookToBookDto(book.get());
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

    @Override
    public RatingDto updateBookRating(Long id, Principal user, Integer rating) throws ResourceNotFoundException {
        Optional<Book> book = bookRepository.findById(id);
        User userObj = (User) userDetailsService.loadUserByUsername(user.getName());
        if (!book.isPresent()) {
            throw new ResourceNotFoundException(id, "Cannot find book");
        }
        Optional<Rating> ratingOptional = ratingRepository.findById(new Rating.RatingId(id, userObj.getId()));
        if (ratingOptional.isPresent()) {
            if (rating != null && rating != -1) {
                ratingOptional.get().setRating(rating);
                return applicationMapper.ratingToRatingDto(ratingOptional.get());
            } else {
                ratingRepository.delete(ratingOptional.get());
                return null;
            }
        } else {
            if (rating != null && rating != -1) {
                Rating newRating = new Rating();
                newRating.setBook(book.get());
                newRating.setUser(userObj);
                newRating.setRating(rating);
                return applicationMapper.ratingToRatingDto(ratingRepository.save(newRating));
            } else {
                return null;
            }
        }
    }

    @Override
    public Page<BookDto> searchBook(SearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPageNum(), searchDto.getPageSize());
        return bookRepository.findBookByTitleContains(searchDto.getKeyword(), pageable).map(applicationMapper::mapBookToBookDto);
    }


}
