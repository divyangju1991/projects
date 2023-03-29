package com.movie.service;

import com.movie.domain.MovieInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoService {
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo);
    public Flux<MovieInfo> getAllMovieInfo();
    public Mono<MovieInfo> getMovieInfoById(String id);
    public Mono<MovieInfo> updateMovieInfo(MovieInfo movieInfo, String id);
    Mono<Void> deleteMovieInfo(String id);

    public Flux<MovieInfo> getAllMovieInfoByYear(Integer year);
}
