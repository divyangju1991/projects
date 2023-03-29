package com.movie.router;

import com.movie.domain.Review;
import com.movie.exceptionhandler.GlobalWebFunctionErrorHandler;
import com.movie.handler.ReviewHandler;
import com.movie.repository.ReviewReactiveRespository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static com.movie.router.ReviewsIntgTest.REVIEWS_URL;
import static org.mockito.ArgumentMatchers.isA;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalWebFunctionErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @MockBean
    private ReviewReactiveRespository reviewReactiveRespository;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void addReview(){
        //given
        var review = new Review(null, 1L, "Awesome Movie2", 9.0);

        when(reviewReactiveRespository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 1L, "Awesome Movie", 9.0)));
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

    }

    @Test
    void addReview_validation(){
        //given
        var review = new Review(null, null, "Awesome Movie2", -9.0);

        //when
        webTestClient
                .mutateWith(mockJwt())
                .mutateWith(csrf())
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : please pass a non-negative value");
    }
}
