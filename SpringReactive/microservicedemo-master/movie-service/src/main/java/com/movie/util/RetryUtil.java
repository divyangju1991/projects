package com.movie.util;

import com.movie.exception.MoviesInfoServerException;
import com.movie.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

import java.time.Duration;

public class RetryUtil {

    public static Retry retrySpec(){

        return Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof ReviewsServerException ||
                                ex instanceof MoviesInfoServerException
                        )
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) ->
                        Exceptions.propagate(retrySignal.failure())));
    }
}
