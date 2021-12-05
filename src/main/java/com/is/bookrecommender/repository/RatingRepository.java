package com.is.bookrecommender.repository;

import com.is.bookrecommender.model.Rating;
import com.is.bookrecommender.repository.custom.CustomRatingRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Rating.RatingId>, CustomRatingRepository {
}
