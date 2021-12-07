package com.is.bookrecommender.mapper;

import com.is.bookrecommender.dto.BookDto;
import com.is.bookrecommender.dto.PageResponseDto;
import com.is.bookrecommender.dto.RatingDto;
import com.is.bookrecommender.dto.UserDto;
import com.is.bookrecommender.model.Author;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.model.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class ApplicationMapper {
    public BookDto mapBookToBookDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setAuthors(book.getAuthor().stream().map(Author::getName).collect(Collectors.joining(",")));
        bookDto.setId(book.getId());
        bookDto.setImageURL(book.getImageURL());
        bookDto.setTitle(book.getTitle());
        bookDto.setPublishedYear(book.getPublishYear());
        return bookDto;
    }

    public UserDto mapUserToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setAge(user.getAge());
        userDto.setUsername(user.getUsername());
        userDto.setCountry(user.getCountry());
        userDto.setMail(user.getMail());
        userDto.setName(user.getName());
        userDto.setAvatar(user.getAvatarURL());
        return  userDto;
    }

    public User mapUserDtoToUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setMail(userDto.getMail());
        user.setCountry(userDto.getCountry());
        user.setName(userDto.getName());
        user.setAge(userDto.getAge());
        return user;
    }

    public RatingDto ratingToRatingDto(Rating rating) {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setRating(rating.getRating());
        ratingDto.setBookId(rating.getBook().getId());
        ratingDto.setUsername(rating.getUser().getUsername());
        return ratingDto;
    }

    public <T> PageResponseDto<T> pageToPageResponseDto(Page<T> page) {
        PageResponseDto<T> pageResponseDto = new PageResponseDto<>();
        pageResponseDto.setContent(page.getContent());
        pageResponseDto.setTotalPage(page.getTotalPages());
        pageResponseDto.setLast(page.isLast());
        return pageResponseDto;
    }

    public Book mapBookDtoToBook(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setPublishYear(bookDto.getPublishedYear());
        book.setTitle(bookDto.getTitle());
        return book;
    }
}
