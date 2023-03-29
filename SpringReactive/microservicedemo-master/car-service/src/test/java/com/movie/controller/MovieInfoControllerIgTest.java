package com.movie.controller;


import com.movie.domain.MovieInfo;
import com.movie.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MovieInfoControllerIgTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    WebTestClient webTestClient;

    public static final String MOVIE_INFO_URL = "/v1/movieInfos";

    @BeforeEach
    void setUp(){

        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                .apply(springSecurity())
                .configureClient()
                .build();

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo(){

        //given
        var movieInfo = new MovieInfo(null, "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(moviInfoEntryExchangeResult -> {
                    var savedMovieInfo = moviInfoEntryExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });


        //then

    }

    @Test
    void getAllMovieInfos(){

        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById(){

        var movieInfoId = "abc";
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                    assertEquals("abc", movieInfo.get(0).getMovieInfoId());
                    assertEquals("Dark Knight Rises", movieInfo.get(0).getName());
                });
    }

    @Test
    void getMovieInfoById_isNotFound(){

        var movieInfoId = "def";
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfosByYear(){

        var uri = UriComponentsBuilder.fromUriString(MOVIE_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoByIdWithJSONPath(){

        var movieInfoId = "abc";
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo(){

        //given
        var movieInfoId = "abc";
        var movieInfo = new MovieInfo(null, "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .put()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(moviInfoEntryExchangeResult -> {
                    var updatedMovieInfo = moviInfoEntryExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals("Batman Begins55", updatedMovieInfo.getName());
                });


        //then

    }

    @Test
    void deleteMovieInfoById(){

        var movieInfoId = "abc";
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .delete()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
