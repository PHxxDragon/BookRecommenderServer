package com.is.bookrecommender.controller;

import com.is.bookrecommender.dto.*;
import com.is.bookrecommender.exception.CannotRetrieveWebResponseException;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import com.is.bookrecommender.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookInfo(@PathVariable Long id, Principal user) throws ResourceNotFoundException {
        BookDto bookDto = bookService.getBookFromBookId(id, user);
        return ResponseEntity.ok(bookDto);
    }

    @PostMapping("books/{id}/rating")
    public ResponseEntity<?> updateRating(@PathVariable Long id, Principal user, @RequestParam(required = false, name = "rating") Integer rating) throws ResourceNotFoundException {
        RatingDto ratingDto = bookService.updateBookRating(id, user, rating);
        return ResponseEntity.ok(ratingDto);
    }

    @GetMapping("books/search")
    public ResponseEntity<?> searchBook(SearchDto searchDto) {
        PageResponseDto<BookDto> bookPage = bookService.searchBook(searchDto);
        return ResponseEntity.ok(bookPage);
    }

    @GetMapping("books/recommendation")
    public ResponseEntity<?> getBookRecommendation(Principal user, PageRequestDto pageDto) throws CannotRetrieveWebResponseException {
        PageResponseDto<BookDto> bookPage = bookService.getBookRecommendation(user, pageDto);
        return ResponseEntity.ok(bookPage);
    }

    @GetMapping ("books/popular")
    public ResponseEntity<?> getPopularBook(PageRequestDto pageDto) throws CannotRetrieveWebResponseException {
        PageResponseDto<BookDto> bookPage = bookService.getPopularBook(pageDto);
        return ResponseEntity.ok(bookPage);
    }

    @PostMapping("books/add")
    public ResponseEntity<?> addBook(BookDto bookDto, @RequestParam(required = false) MultipartFile image) throws IOException {
        BookDto bookDto1 = bookService.addBook(bookDto, image);
        return ResponseEntity.ok(bookDto1);
    }

    @PostMapping("books/update")
    public ResponseEntity<?> updateBook(BookDto bookDto, @RequestParam(required = false) MultipartFile image) throws ResourceNotFoundException, IOException {
        BookDto bookDto1 = bookService.updateBook(bookDto, image);
        return ResponseEntity.ok(bookDto1);
    }

    @GetMapping("books/history")
    public ResponseEntity<?> getBookRateHistory(Principal user, PageRequestDto pageDto) {
        PageResponseDto<BookDto> bookPage = bookService.getBookRateHistory(user, pageDto);
        return ResponseEntity.ok(bookPage);
    }
}
