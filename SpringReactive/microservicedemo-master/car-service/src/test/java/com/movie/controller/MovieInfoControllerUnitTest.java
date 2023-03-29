package com.movie.controller;

import com.movie.domain.MovieInfo;
import com.movie.service.MovieInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static com.movie.controller.MovieInfoControllerIgTest.MOVIE_INFO_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MovieInfoControllerUnitTest {
    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    @Test
    void getAllMoviesInfo(){

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));


        when(movieInfoServiceMock.getAllMovieInfo()).thenReturn(Flux.fromIterable(movieinfos));

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
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

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
                    var movieInfoRes = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfo);
                    Assertions.assertEquals("abc", movieInfoRes.get(0).getMovieInfoId());
                    Assertions.assertEquals("Dark Knight Rises", movieInfoRes.get(0).getName());
                });
    }

    @Test
    void getMovieInfoById_notFound(){

        var movieInfoId = "def";

        when(movieInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.empty());

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
    void addMovieInfo(){

        //given
        var movieInfo = new MovieInfo("mockId", "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(
                Mono.just(movieInfo)
        );

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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });


        //then

    }

    @Test
    void addMovieInfo_validation(){

        //given
        var movieInfo = new MovieInfo("mockId", "",
                -2005, List.of(""), LocalDate.parse("2020-06-15"));

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String error = stringEntityExchangeResult.getResponseBody();
                    assertNotNull(error);
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be Positive Value";
                    assertEquals(expectedErrorMessage, error);
                });


        //then

    }

    @Test
    void updateMovieInfo(){

        //given
        var movieInfoId = "mockId";
        var movieInfo = new MovieInfo(movieInfoId, "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
                Mono.just(movieInfo)
        );
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
                    Assertions.assertEquals("Batman Begins55", updatedMovieInfo.getName());
                });


        //then

    }

    @Test
    void updateMovieInfo_notFound(){

        //given
        var movieInfoId = "def";
        var movieInfo = new MovieInfo(null, "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        //when
        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(
                Mono.empty());

        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .put()
                .uri(MOVIE_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();

        //then

    }

    @Test
    void deleteMovieInfoById(){
        var movieInfoId = "mockId";
        var movieInfo = new MovieInfo(movieInfoId, "Batman Begins55",
                2005, List.of("Christian Bale55", "Michael Cane55"), LocalDate.parse("2020-06-15"));

        when(movieInfoServiceMock.deleteMovieInfo( isA(String.class))).thenReturn(Mono.empty());

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
