package com.is.bookrecommender.service;

import com.is.bookrecommender.dto.*;
import com.is.bookrecommender.exception.CannotRetrieveWebResponseException;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

public interface BookService {
    BookDto getBookFromBookId(Long id, Principal user) throws ResourceNotFoundException;

    RatingDto updateBookRating(Long id, Principal user, Integer rating) throws ResourceNotFoundException;

    PageResponseDto<BookDto> searchBook(SearchDto searchDto);

    PageResponseDto<BookDto> getBookRecommendation(Principal user, PageRequestDto pageDto) throws CannotRetrieveWebResponseException;

    PageResponseDto<BookDto> getPopularBook(PageRequestDto pageDto) throws CannotRetrieveWebResponseException;

    BookDto addBook(BookDto bookDto, MultipartFile image) throws IOException;

    BookDto updateBook(BookDto bookDto, MultipartFile image) throws ResourceNotFoundException, IOException;
}
