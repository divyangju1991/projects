package com.example.carservice.domain;

import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document
@Value
public class Car {
    @Id
    private final UUID id;
    private final String name;
    private final LocalDate releaseDate;
}