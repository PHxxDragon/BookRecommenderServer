package com.is.bookrecommender.repository.custom;

import com.querydsl.core.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CustomRatingRepository {
    Double getAverageRating(Long book_id);

    Map<Long, Double> getAverageRatings(Set<Long> book_id);

    List<Long> getRatedBook(Long user_id);
}
