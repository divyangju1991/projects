package com.movie.repository;

import com.movie.domain.MovieInfo;
import com.movie.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
public class MovieInfoRepositoryIgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void setup(){
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
    void teardown(){
        movieInfoRepository.deleteAll().block();
    }


    @Test
    void findAll(){

        //when
        var moviInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById(){

        //given

        //when
        var moviesInfoMono = movieInfoRepository.findById("abc").log();

        //then
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void findByName(){

        //given

        //when
        var moviesInfoMono = movieInfoRepository.findByName("Batman Begins").log();

        //then
        StepVerifier.create(moviesInfoMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByYear_ByStepsVerifiy(){

        //given

        //when
        var moviesInfoMono = movieInfoRepository.findByYear(2005).log();

        //then
        StepVerifier.create(moviesInfoMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void saveMovieInfo(){

        //given
        var moviesInfo = new MovieInfo(null, "Batman Begins2",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        var moviesInfoMono = movieInfoRepository.save(moviesInfo).log();


        //then
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins2", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo(){

        //given
        var moviesInfo = movieInfoRepository.findById("abc").block();
        moviesInfo.setYear(2023);

        //when
        var moviesInfoMono = movieInfoRepository.save(moviesInfo).log();


        //then
        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals(2023, movieInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo(){

        //given

        //when
        var moviesInfoMono = movieInfoRepository.deleteById("abc").block();
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        //then
        StepVerifier.create(moviesInfoFlux)
               .expectNextCount(2)
                .verifyComplete();
    }
}
