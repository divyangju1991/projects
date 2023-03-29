package com.movie.router;

import com.movie.domain.MovieInfo;
import com.movie.domain.Review;
import com.movie.repository.ReviewReactiveRespository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRespository reviewReactiveRespository;

    public static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setup(){
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewReactiveRespository.saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown(){
        reviewReactiveRespository.deleteAll().block();
    }

    @Test
    void addReview(){
        //given
        var review = new Review(null, 1L, "Awesome Movie2", 9.0);
        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntryExchangeResult -> {
                    var savedReview = reviewEntryExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });

        //then
    }

    @Test
    void updateReview(){
        //given
        var reviewId = "abc";
        var review = new Review(null, 1L, "Awesome Movie55", 10.0);
        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .put()
                .uri(REVIEWS_URL+"/{id}", reviewId)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntryExchangeResult -> {
                    var updatedReview = reviewEntryExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assert updatedReview.getReviewId() != null;
                    assertEquals("Awesome Movie55", updatedReview.getComment());
                });

        //then
    }

    @Test
    void getReviews(){
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);
    }
}
