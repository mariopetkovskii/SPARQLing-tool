package com.example.sparqlgeneratorjavabackend.port;

import com.example.sparqlgeneratorjavabackend.helpers.HelperFunctions;
import org.apache.jena.atlas.json.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://localhost:4200/")
public class RequestController {
    @PostMapping("/generateSparql")
    public ResponseEntity<Map<String, String>> generateSparql(@RequestBody Map<String, Object> payload) {
        String dataResource = (String) payload.get("dataResource");
        List<Map<String, String>> props = (List<Map<String, String>>) payload.get("props");
        String typeOfProperty = (String) payload.get("propertyType");
        dataResource = HelperFunctions.capitalizeEveryWord(dataResource);
        Integer maxLimit = (Integer) payload.get("limit");
        Boolean selectDistinct = (Boolean) payload.get("selectDistinct");
        return HelperFunctions.generateSparql(dataResource, props, typeOfProperty, maxLimit, selectDistinct);
    }

    @PostMapping("/generateSparqlWithLabels")
    public ResponseEntity<Map<String, String>> generateSparqlWithLabels(@RequestBody Map<String, Object> payload) {
        String dataResource = (String) payload.get("dataResource");
        List<Map<String, String>> props = (List<Map<String, String>>) payload.get("props");
        dataResource = HelperFunctions.capitalizeEveryWord(dataResource);
        return HelperFunctions.generateSparqlWithPropertyWithLabels(dataResource, props);
    }

    @PostMapping("/executeSparql")
    public ResponseEntity<List<Map<String, String>>> executeSparql(@RequestBody Map<String, Object> payload){
        String query = (String) payload.get("query");
        return ResponseEntity.ok(HelperFunctions.executeQuery(query));
    }

    @PostMapping("/generateDynamicSparql")
    public ResponseEntity<Map<String, String>> generateDynamicSparql(@RequestBody Map<String, Object> payload){
        String dataResource = (String) payload.get("dataResource");
        List<Map<String, String>> props = (List<Map<String, String>>) payload.get("props");
        String query = (String) payload.get("query");
        String typeOfProperty = (String) payload.get("propertyType");
        Integer maxLimit = (Integer) payload.get("limit");
        Boolean selectDistinct = (Boolean) payload.get("selectDistinct");
        return HelperFunctions.generateDynamicSparql(query, dataResource, props, typeOfProperty, maxLimit, selectDistinct);
    }

}
