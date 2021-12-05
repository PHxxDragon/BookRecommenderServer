package com.is.bookrecommender.repository.custom;

import com.is.bookrecommender.model.QRating;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
}
