package com.example.sparqlgeneratorjavabackend.port;

import com.example.sparqlgeneratorjavabackend.helpers.HelperFunctions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200/")
public class RequestController {
    @PostMapping("/my-endpoint")
    public ResponseEntity<Map<String, String>> generateSparql(@RequestBody Map<String, Object> payload) {
        String dataResource = (String) payload.get("dataResource");
        List<Map<String, String>> props = (List<Map<String, String>>) payload.get("props");
        dataResource = HelperFunctions.capitalizeEveryWord(dataResource);
        return HelperFunctions.generateSparqlWithProperty(dataResource, props);
    }



}
