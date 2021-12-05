package com.is.bookrecommender.repository.custom;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface CustomRatingRepository {
    Double getAverageRating(Long book_id);
}
