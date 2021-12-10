package com.is.bookrecommender.service.implementation;

import com.is.bookrecommender.dto.*;
import com.is.bookrecommender.exception.CannotRetrieveWebResponseException;
import com.is.bookrecommender.exception.ResourceNotFoundException;
import com.is.bookrecommender.io.FileUpload;
import com.is.bookrecommender.mapper.ApplicationMapper;
import com.is.bookrecommender.model.Author;
import com.is.bookrecommender.model.Book;
import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.model.User;
import com.is.bookrecommender.repository.AuthorRepository;
import com.is.bookrecommender.repository.BookRepository;
import com.is.bookrecommender.repository.RatingRepository;
import com.is.bookrecommender.repository.UserRepository;
import com.is.bookrecommender.service.BookService;
import com.is.bookrecommender.service.UserService;
import com.querydsl.core.Tuple;
import org.json.JSONArray;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookServiceImpl implements BookService {
    public static String predictURI = "/predict";
    public static String predictPopularityURI = "/predict/popular";

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserService userService;

    @Value("${recommender.server}")
    private String recommenderServer;

    @Value("${book.resources.folder}")
    private String bookDir;

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

    private Mono<String> processResponse(ClientResponse response) {
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
    }

    @Override
    public PageResponseDto<BookDto> getBookRecommendation(Principal user, PageRequestDto pageDto) throws CannotRetrieveWebResponseException {
        User userObj = userRepository.findUserByUsername(user.getName());
        WebClient webclient = WebClient.create(recommenderServer);
        UriSpec<RequestBodySpec> uriSpec = webclient.method(HttpMethod.GET);
        RequestBodySpec requestBodySpec = uriSpec.uri(predictURI + "/" + userObj.getId());
        Mono<String> responseMono = requestBodySpec.exchangeToMono(this::processResponse);
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

        var rated = ratingRepository.getRatedBook(userObj.getId());
        s.removeAll(rated);

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

    @Override
    public PageResponseDto<BookDto> getPopularBook(PageRequestDto pageDto) throws CannotRetrieveWebResponseException {
        WebClient webclient = WebClient.create(recommenderServer);
        Mono<String> resultMono = webclient.method(HttpMethod.GET)
                .uri(predictPopularityURI)
                .exchangeToMono(this::processResponse);

        String responseText = resultMono.block();
        if (responseText.equals("Error response")) {
            throw new CannotRetrieveWebResponseException(responseText);
        }

        JSONArray jsonArray = new JSONArray(responseText);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            ids.add(jsonArray.getLong(i));
        }

        Page<BookDto> bookDtoPage = bookRepository
                .findBooksByIdIn(ids, PageRequest.of(pageDto.getPageNum(), pageDto.getPageSize()))
                .map(applicationMapper::mapBookToBookDto);

        Set<Long> book_ids = bookDtoPage.map(BookDto::getId).stream().collect(Collectors.toSet());
        Map<Long, Double> ratings = ratingRepository.getAverageRatings(book_ids);

        for (int i = 0; i < bookDtoPage.getNumberOfElements(); i++) {
            BookDto bookDto = bookDtoPage.getContent().get(i);
            bookDto.setRating(ratings.get(bookDto.getId()));
        }

        return applicationMapper.pageToPageResponseDto(bookDtoPage);
    }

    private boolean isStringEmpty(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }

    @Override
    public BookDto addBook(BookDto bookDto, MultipartFile image) throws IOException {
        Book book = applicationMapper.mapBookDtoToBook(bookDto);
        book.setId(null);
        if (bookDto.getAuthors() != null) {
            List<Author> authorList = Arrays.stream(bookDto.getAuthors().split(",")).map(this::processAuthor).collect(Collectors.toList());
            authorRepository.saveAll(authorList);
            book.setAuthor(authorList);
        }
        book = bookRepository.save(book);
        if (image != null) {
            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String filename = book.getId() + (!isStringEmpty(extension) ? "." + extension : "");
            FileUpload.saveFile(bookDir, filename, image);
            book.setImageURL(bookDir + "/" + filename);
        }
        return applicationMapper.mapBookToBookDto(book);
    }

    @Override
    public BookDto updateBook(BookDto bookDto, MultipartFile image) throws ResourceNotFoundException, IOException {
        Optional<Book> bookOptional = bookRepository.findById(bookDto.getId());
        if (!bookOptional.isPresent()) {
            throw new ResourceNotFoundException(bookDto.getId(), " Book not found");
        }

        Book book = bookOptional.get();
        if (bookDto.getTitle() != null && !isStringEmpty(bookDto.getTitle())) book.setTitle(bookDto.getTitle());
        if (bookDto.getPublishedYear() != null) book.setPublishYear(bookDto.getPublishedYear());
        if (bookDto.getAuthors() != null) {
            List<Author> authorList = Arrays.stream(bookDto.getAuthors().split(",")).map(this::processAuthor).collect(Collectors.toList());
            if (authorList.size() > 0) {
                authorRepository.saveAll(authorList);
                book.setAuthor(authorList);
            }
        }
        if (image != null) {
            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String filename = book.getId() + (!isStringEmpty(extension) ? "." + extension : "");
            FileUpload.saveFile(bookDir, filename, image);
            book.setImageURL(bookDir + "/" + filename);
        }
        return applicationMapper.mapBookToBookDto(book);
    }

    @Override
    public PageResponseDto<BookDto> getBookRateHistory(Principal user, PageRequestDto pageDto) {
        User userObj = userRepository.findUserByUsername(user.getName());
        var rated = ratingRepository.getRatedBook(userObj.getId());

        Page<BookDto> bookDtoPage = bookRepository
                .findBooksByIdIn(rated, PageRequest.of(pageDto.getPageNum(), pageDto.getPageSize()))
                .map(applicationMapper::mapBookToBookDto);

        Set<Long> book_ids = bookDtoPage.map(BookDto::getId).stream().collect(Collectors.toSet());
        Map<Long, Double> ratings = ratingRepository.getAverageRatings(book_ids);

        for (int i = 0; i < bookDtoPage.getNumberOfElements(); i++) {
            BookDto bookDto = bookDtoPage.getContent().get(i);
            bookDto.setRating(ratings.get(bookDto.getId()));
        }

        return applicationMapper.pageToPageResponseDto(bookDtoPage);
    }

    private Author processAuthor(String s) {
        Author author = authorRepository.findAuthorByName(s);
        if (author == null) {
            author = new Author();
            author.setName(s);
        }
        return author;
    }


}
