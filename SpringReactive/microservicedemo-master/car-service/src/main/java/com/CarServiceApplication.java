package com;

import com.example.carservice.domain.Car;
import com.example.carservice.repository.CarRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

@Log4j2
@EnableEurekaClient
@SpringBootApplication
public class CarServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarServiceApplication.class, args);
    }

    //Commented out init method while running Unit test case

    /*@Bean
    ApplicationRunner init(CarRepository repository) {
        Car ID = new Car(UUID.randomUUID(), "ID.", LocalDate.of(2019, Month.DECEMBER, 1));
        Car ID_CROZZ = new Car(UUID.randomUUID(), "ID. CROZZ", LocalDate.of(2021, Month.MAY, 1));
        Car ID_VIZZION = new Car(UUID.randomUUID(), "ID. VIZZION", LocalDate.of(2021, Month.DECEMBER, 1));
        Car ID_BUZZ = new Car(UUID.randomUUID(), "ID. BUZZ", LocalDate.of(2021, Month.DECEMBER, 1));

        return args -> {
            repository.deleteAll()
                    .thenMany(repository.saveAll(Flux.just(ID, ID_BUZZ, ID_CROZZ, ID_VIZZION)))
                    .thenMany(repository.findAll())
                    .subscribe(c -> log.info("Saving: " + c));
        };
    }*/

}