package com.movie.repository;

import com.movie.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewReactiveRespository extends ReactiveMongoRepository<Review, String> {

    Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
