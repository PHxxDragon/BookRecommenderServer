package com.is.bookrecommender.service.implementation;

import com.is.bookrecommender.dto.*;
import com.is.bookrecommender.exception.CannotRetrieveWebResponseException;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.BookRepository;
import com.is.bookrecommender.repository.RatingRepository;
import com.is.bookrecommender.repository.UserRepository;
import com.is.bookrecommender.service.BookService;
import com.is.bookrecommender.service.UserService;
import com.querydsl.core.Tuple;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookServiceImpl implements BookService {
    public static String predictURI = "/predict";
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserService userService;

    @Value("${recommender.server}")
    private String recommenderServer;

    public BookDto getBookFromBookId(Long id, Principal user) throws ResourceNotFoundException {
        Optional<Book> book = bookRepository.findById(id);
        if (!book.isPresent()) {
            throw new ResourceNotFoundException(id, "Cannot find book");
        }
        BookDto bookDto = applicationMapper.mapBookToBookDto(book.get());
        bookDto.setRating(ratingRepository.getAverageRating(id));
        if (user != null) {
            User userObj = (User) userService.loadUserByUsername(user.getName());
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
        User userObj = (User) userService.loadUserByUsername(user.getName());
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
    public PageResponseDto<BookDto> searchBook(SearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPageNum(), searchDto.getPageSize());
        Page<BookDto> bookPage = bookRepository.findBookByTitleContains(searchDto.getKeyword(), pageable).map(applicationMapper::mapBookToBookDto);

        Set<Long> book_ids = bookPage.map(BookDto::getId).stream().collect(Collectors.toSet());
        Map<Long, Double> ratings = ratingRepository.getAverageRatings(book_ids);

        for (int i = 0; i < bookPage.getNumberOfElements(); i++) {
            BookDto bookDto = bookPage.getContent().get(i);
            bookDto.setRating(ratings.get(bookDto.getId()));
        }

        return applicationMapper.pageToPageResponseDto(bookPage);
    }

    @Override
    public PageResponseDto<BookDto> getBookRecommendation(Principal user, PageRequestDto pageDto) throws CannotRetrieveWebResponseException {
        User userObj = userRepository.findUserByUsername(user.getName());
        WebClient webclient = WebClient.create(recommenderServer);
        UriSpec<RequestBodySpec> uriSpec = webclient.method(HttpMethod.GET);
        RequestBodySpec requestBodySpec = uriSpec.uri(predictURI + "/" + userObj.getId());
        Mono<String> responseMono = requestBodySpec.exchangeToMono(response -> {
            if (response.statusCode()
                    .equals(HttpStatus.OK)) {
                return response.bodyToMono(String.class);
            } else if (response.statusCode()
                    .is4xxClientError()) {
                return Mono.just("Error response");
            } else {
                return response.createException()
                        .flatMap(Mono::error);
            }
        });
        String responseText = responseMono.block();
        if (responseText.equals("Error response")) {
            throw new CannotRetrieveWebResponseException(responseText);
        }

        JSONObject jsonObject = new JSONObject(responseText);

        var keys = jsonObject.keys();
        Map<Long, Double> map = new HashMap<>();
        for (Iterator<String> it = keys; it.hasNext(); ) {
            String book_id = it.next();
            map.put(Long.parseLong(book_id), jsonObject.getDouble(book_id));
        }
        var s = map.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        Page<BookDto> bookDtoPage = bookRepository
                .findBooksByIdIn(s, PageRequest.of(pageDto.getPageNum(), pageDto.getPageSize()))
                .map(applicationMapper::mapBookToBookDto);

        Set<Long> book_ids = bookDtoPage.map(BookDto::getId).stream().collect(Collectors.toSet());
        Map<Long, Double> ratings = ratingRepository.getAverageRatings(book_ids);

        for (int i = 0; i < bookDtoPage.getNumberOfElements(); i++) {
            BookDto bookDto = bookDtoPage.getContent().get(i);
            bookDto.setRating(ratings.get(bookDto.getId()));
        }

        return applicationMapper.pageToPageResponseDto(bookDtoPage);
    }


}
