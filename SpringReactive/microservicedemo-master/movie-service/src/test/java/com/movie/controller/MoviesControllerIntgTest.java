package com.movie.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.movie.domain.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/v1/movieInfos",
                "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    WebTestClient webTestClient;

    /*@Autowired
    ApplicationContext context;

    @BeforeEach
    void setUp(){

        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }*/

    //@LoadBalanced please commented out since How to Mock Load Balanced I don't know
    @Test
    void retrieveMovieById(){

        var movieId = "abc";
        stubFor(get(urlPathMatching("/v1/movieInfos" + "/" + movieId))
        .willReturn(aResponse()
        .withHeader("Content-Type", "application/json")
        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathMatching("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);

                webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Authorization", "")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieInfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);


        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Authorization", "")
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo Available for the passed in Id : abc");

    }

    @Test
    void retrieveMovieById_reviews_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieInfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(404)));

        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);


        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Authorization", "")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 0;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });;

    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieInfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")

                ));

       /* stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));*/

        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Authorization", "")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieInfos" + "/" + movieId)));
    }

    @Test
    void retrieveMovieById_Reviews_5XX() {
        //given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieInfos" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                ));



        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Not Available")));

        final var headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Authorization", "")
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Not Available");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));

    }
}
