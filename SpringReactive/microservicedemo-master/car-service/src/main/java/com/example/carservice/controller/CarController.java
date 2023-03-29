package com.example.carservice.controller;

import com.example.carservice.domain.Car;
import com.example.carservice.repository.CarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
class CarController {
    private CarRepository repository;

    public CarController(CarRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Car> addCar(@RequestBody Car car) {
        return repository.save(car);
    }

    @GetMapping("/cars")
    public Flux<Car> getCars() {
        return repository.findAll();
    }

    @DeleteMapping("/car/{id}")
    public Mono<ResponseEntity<Void>> deleteCar(@PathVariable("id") UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }
}
