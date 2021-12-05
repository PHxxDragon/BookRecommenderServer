package com.is.bookrecommender.controller;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> getBookInfo(@PathVariable Long id, Principal user) {
        BookDto bookDto = bookService.getBookFromBookId(id, user);
        return ResponseEntity.ok(bookDto);
    }
}
