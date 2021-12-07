package com.is.bookrecommender.repository.custom;

import com.is.bookrecommender.model.QRating;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomRatingRepositoryImpl implements CustomRatingRepository{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Double getAverageRating(Long book_id) {

        QRating rating = QRating.rating1;
        var result = new JPAQuery<>(entityManager)
                .select(rating.rating.avg())
                .from(rating)
                .where(rating.book.id.eq(book_id)).fetchFirst();

        return result;
    }

    @Override
    public Map<Long, Double> getAverageRatings(Set<Long> book_ids) {
        QRating rating = QRating.rating1;
        var result = new JPAQuery<>(entityManager)
                .select(rating.book.id, rating.rating.avg())
                .from(rating)
                .where(rating.book.id.in(book_ids))
                .groupBy(rating.book)
                .fetch();

        Map<Long, Double> ratings = new HashMap<>();
        for (Tuple tuple : result) {
            ratings.put(tuple.get(rating.book.id), tuple.get(rating.rating.avg()));
        }

        return ratings;
    }
}
