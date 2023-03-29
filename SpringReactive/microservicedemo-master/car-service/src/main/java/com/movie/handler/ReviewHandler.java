package com.movie.handler;

import com.movie.domain.Review;
import com.movie.exception.ReviewDataException;
import com.movie.exception.ReviewNotFoundException;
import com.movie.repository.ReviewReactiveRespository;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRespository reviewReactiveRespository;

    public ReviewHandler(ReviewReactiveRespository reviewReactiveRespository) {
        this.reviewReactiveRespository = reviewReactiveRespository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRespository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        var constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);
        if(constraintViolations.size() > 0) {
            var errorMessge = constraintViolations
                                            .stream()
                                            .map(ConstraintViolation::getMessage)
                                            .sorted()
                                            .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessge);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");
        Flux<Review> reviewFlux = null;
        if(movieInfoId.isPresent()){
            reviewFlux = reviewReactiveRespository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else {
            reviewFlux = reviewReactiveRespository.findAll();
        }
        return ServerResponse.ok().body(reviewFlux, Review.class);

    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRespository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for given review id : "+ reviewId)));
        return existingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                    .map(reqReview -> {
                        review.setComment(reqReview.getComment());
                        review.setRating(reqReview.getRating());
                        review.setMovieInfoId(reqReview.getMovieInfoId());
                        return review;
                    })
                    .flatMap(reviewReactiveRespository::save)
                    .flatMap(ServerResponse.ok()::bodyValue)
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRespository.findById(reviewId);
        return existingReview
                    .flatMap(review -> reviewReactiveRespository.deleteById(reviewId)
                    .then(ServerResponse.noContent().build()));
    }
}
