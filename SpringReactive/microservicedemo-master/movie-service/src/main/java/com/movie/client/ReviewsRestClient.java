package com.movie.client;

import com.movie.domain.Review;
import com.movie.exception.MoviesInfoClientException;
import com.movie.exception.MoviesInfoServerException;
import com.movie.exception.ReviewsClientException;
import com.movie.exception.ReviewsServerException;
import com.movie.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String moviesInfoUrl;


    private WebClient.Builder webClient;

    public ReviewsRestClient(WebClient.Builder webClient){
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId, String authnHeader) {

        String url = null;
        try {
            url = UriComponentsBuilder.fromUri(new URI(moviesInfoUrl.trim()))
                    .queryParam("movieInfoId", movieId)
                    .buildAndExpand().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return webClient
                .build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, authnHeader)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.error("Status code is : {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new  ReviewsClientException(
                                    responseMessage)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.error("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsServerException(
                                    "Server Exception in ReviewsService " +  responseMessage
                            )));
                })
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec());
    }

}
