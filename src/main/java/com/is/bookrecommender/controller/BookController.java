package com.is.bookrecommender.controller;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.dto.RatingDto;
import com.is.bookrecommender.dto.SearchDto;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("books/search")
    public ResponseEntity<?> searchBook(SearchDto searchDto) {
        Page<BookDto> bookPage = bookService.searchBook(searchDto);
        return ResponseEntity.ok(bookPage);
    }
}
