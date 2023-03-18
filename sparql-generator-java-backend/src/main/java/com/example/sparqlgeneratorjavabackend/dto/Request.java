package com.example.sparqlgeneratorjavabackend.dto;

import lombok.Data;

@Data
public class Request {
    private String ontology;
    private String property;
}
