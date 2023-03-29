package com.movie.router;

import com.movie.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler){
        return route()
                .nest(path("/v1/reviews"), builder -> {
                    builder.POST("", request-> reviewHandler.addReview(request))
                            .GET("", request-> reviewHandler.getReviews(request))
                            .PUT("/{id}", request -> reviewHandler.updateReview(request))
                            .DELETE("/{id}", request -> reviewHandler.deleteReview(request));
                })
                .GET("/v1/helloword", (request->{
                    return ServerResponse.ok().bodyValue("helloworld");
                }))
                .build();
    }
}
