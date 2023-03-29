package com.example.apigateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.security.oauth2.gateway.TokenRelayGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@EnableEurekaClient
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, TokenRelayGatewayFilterFactory filterFactory) {
        return builder.routes()
            .route("car-service", r -> r.path("/cars", "/car/*", "/v1/movieInfos"
                    , "/v1/reviews", "/v1/reviews/*")
                .filters(f -> f.filter(filterFactory.apply())
                    .removeRequestHeader("cookie"))
                .uri("lb://car-service/"))
                .route("movie-service", r -> r.path("/v1/movies", "/v1/movies/*")
                        .filters(f -> f.filter(filterFactory.apply())
                                .removeRequestHeader("cookie"))
                        .uri("lb://movie-service/"))
            .build();
    }
}

@RestController
class FaveCarsController {

    private static final MimeType TEXT_HTML = MediaType.TEXT_HTML;
    private final WebClient.Builder carClient;

    public FaveCarsController(WebClient.Builder carClient) {
        this.carClient = carClient;
    }

    /**
     * carClient is WebClient, so It is not compatible with PostMan call.
     * This URL is only compatible with Browser or WebClient Calls.
     * PostMan call with Setting Cookie, able to test.
     * Copy Cookie from Browser Network Calls.
     * @return
     */
    @GetMapping(value="/fave-cars")
    public Flux<Car> faveCars() {
        return carClient.build()
            .get()
            .uri("lb://car-service/cars")
            .retrieve()
            .bodyToFlux(Car.class)
            .filter(this::isFavorite);
    }


    private boolean isFavorite(Car car) {
        return car.getName()
            .equals("ID. BUZZ");
    }

    private void acceptedCodecs(ClientCodecConfigurer clientCodecConfigurer) {
        clientCodecConfigurer.customCodecs().encoder(new Jackson2JsonEncoder(new ObjectMapper(), TEXT_HTML));
        clientCodecConfigurer.customCodecs().decoder(new Jackson2JsonDecoder(new ObjectMapper(), TEXT_HTML));
    }
}


@Data
class Car {
    private String name;
    private LocalDate releaseDate;
}


@RestController
class CarsFallback {

    @GetMapping("/cars-fallback")
    public Flux<Car> noCars() {
        return Flux.empty();
    }
}
