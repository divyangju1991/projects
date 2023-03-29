package com.movie.service;

import com.movie.domain.MovieInfo;
import com.movie.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoServiceImpl implements MovieInfoService {

    public MovieInfoServiceImpl(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    private MovieInfoRepository movieInfoRepository;

    @Override
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {

        return movieInfoRepository.save(movieInfo);
    }

    @Override
    public Flux<MovieInfo> getAllMovieInfo(){

        return movieInfoRepository.findAll();
    }

    @Override
    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    @Override
    public Mono<MovieInfo> updateMovieInfo(MovieInfo updatedMovieInfo, String id) {
       return movieInfoRepository.findById(id)
                .flatMap(movieInfo -> {
                    movieInfo.setCast(updatedMovieInfo.getCast());
                    movieInfo.setName(updatedMovieInfo.getName());
                    movieInfo.setYear(updatedMovieInfo.getYear());
                    movieInfo.setRelese_date(updatedMovieInfo.getRelese_date());
                    return movieInfoRepository.save(movieInfo);
                });
    }

    @Override
    public Mono<Void> deleteMovieInfo(String id){
        return movieInfoRepository.deleteById(id);
    }

    @Override
    public Flux<MovieInfo> getAllMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }


}
