package com.movie.controller;

import com.movie.client.MoviesInfoRestClient;
import com.movie.client.ReviewsRestClient;
import com.movie.domain.Movie;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MoviesInfoRestClient moviesInfoRestClient;
    private ReviewsRestClient reviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId, @RequestHeader("Authorization") String authnHeader){

        return moviesInfoRestClient.retrieveMovieInfo(movieId, authnHeader)
                .flatMap(movieInfo -> {
                    var reviewList = reviewsRestClient.retrieveReviews(movieId, authnHeader)
                            .collectList();
                    return reviewList.map(reviews -> new Movie(movieInfo, reviews));
                });
    }
}
