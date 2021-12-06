package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.dto.RatingDto;
import com.is.bookrecommender.dto.SearchDto;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;

import java.security.Principal;

public interface BookService {
    BookDto getBookFromBookId(Long id, Principal user) throws ResourceNotFoundException;

    RatingDto updateBookRating(Long id, Principal user, Integer rating) throws ResourceNotFoundException;

    Page<BookDto> searchBook(SearchDto searchDto);
}
