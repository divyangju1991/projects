package com.movie.client;

import com.movie.domain.MovieInfo;
import com.movie.exception.MoviesInfoClientException;
import com.movie.exception.MoviesInfoServerException;
import com.movie.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
public class MoviesInfoRestClient {

    private WebClient.Builder webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;


    public MoviesInfoRestClient(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId, String authnHeader){

        var url = moviesInfoUrl.trim().concat("/{id}");

        return webClient
                .build()
                .get()
                .uri(url, movieId)
                .header(HttpHeaders.AUTHORIZATION, authnHeader)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.error("Status code is : {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.error(new MoviesInfoClientException(
                                "There is no MovieInfo Available for the passed in Id : " + movieId,
                                clientResponse.statusCode().value()
                        ));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                    responseMessage, clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.error("Status code is : {}", clientResponse.statusCode().value());

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                                  "Server Exception in MoviesInfoService " +  responseMessage
                            )));
                })
                .bodyToMono(MovieInfo.class)
                //.retry(3)
                .retryWhen(RetryUtil.retrySpec())
                .log();

    }
}
