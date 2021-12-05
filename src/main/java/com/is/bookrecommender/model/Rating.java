package com.is.bookrecommender.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name="rating", indexes = @Index(columnList = "book_id"))
public class Rating {
    @Embeddable
    public static class RatingId implements Serializable {
        @Column(name = "book_id")
        private Long bookId;

        @Column(name = "user_id")
        private Long userId;

        public RatingId() {

        }

        public RatingId(Long bookId, Long userId) {
            this.bookId = bookId;
            this.userId = userId;
        }

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RatingId ratingId = (RatingId) o;
            return bookId.equals(ratingId.bookId) && userId.equals(ratingId.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bookId, userId);
        }
    }

    @EmbeddedId
    private RatingId id = new RatingId();

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private int rating;


    public Rating() {
    }

    public RatingId getId() {
        return id;
    }

    public void setId(RatingId id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
