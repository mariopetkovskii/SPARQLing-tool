package com.example.sparqlgeneratorjavabackend.helpers;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelperFunctions {
    public static String capitalizeEveryWord(String sentence){
        String[] words = sentence.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase())
                    .append("_");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    public static ResponseEntity<Map<String, String>> generateSparqlWithProperty(
            String dataResource,
            List<Map<String, String>> props){
        StringBuilder queryBuilder = new StringBuilder();
        String query = "SELECT * WHERE {"
                + "<http://dbpedia.org/resource/" + dataResource + "> ";
        queryBuilder.append(query);
        StringBuilder test = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            test.append(ontology).append(" ?").append(property).append(";").append("\n");
        }
        test = new StringBuilder(test.substring(0, test.length() - 2));
        queryBuilder.append(test);
        queryBuilder.append(". }");
        String queryString = queryBuilder.toString();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", queryString);
        return ResponseEntity.ok(responseMap);
    }
}
