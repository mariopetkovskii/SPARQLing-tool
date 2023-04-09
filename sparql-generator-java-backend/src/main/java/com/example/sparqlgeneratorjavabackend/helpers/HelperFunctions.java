package com.example.sparqlgeneratorjavabackend.helpers;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
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

    private static String getPrefix(String prefix){
        return switch (prefix) {
            case "rdf" -> OntologyPrefixesConstants.rdfPrefix;
            case "rdfs" -> OntologyPrefixesConstants.rdfsPrefix;
            case "dct" -> OntologyPrefixesConstants.dcTermsPrefix;
            case "dbo" -> OntologyPrefixesConstants.dboPrefix;
            case "dbr" -> OntologyPrefixesConstants.dbrPrefix;
            case "dbp" -> OntologyPrefixesConstants.dbpPrefix;
            case "foaf" -> OntologyPrefixesConstants.foafPrefix;
            case "xsd" -> OntologyPrefixesConstants.xsdPrefix;
            case "owl" -> OntologyPrefixesConstants.owlPrefix;
            default -> "";
        };
    }

    public static ResponseEntity<Map<String, String>> generateSparql(
            String dataResource,
            List<Map<String, String>> props,
            String propertyType,
            Integer maxLimit,
            Boolean selectDistinct){
        if(propertyType.equals("property")){
            return generateSparqlWithProperty(dataResource, props, maxLimit, selectDistinct);
        }else{
            return generateSparqlWithIsPropertyOf(dataResource, props, maxLimit, selectDistinct);
        }
    }

    public static ResponseEntity<Map<String, String>> generateSparqlWithProperty(
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct){
        StringBuilder queryBuilder = new StringBuilder();
        String querySelector;
        if(selectDistinct) {
            querySelector = "SELECT DISTINCT * WHERE {"
                    + "<http://dbpedia.org/resource/" + dataResource + "> ";
        }else {
            querySelector = "SELECT * WHERE {"
                    + "<http://dbpedia.org/resource/" + dataResource + "> ";
        }
        queryBuilder.append(querySelector);
        StringBuilder query = new StringBuilder();
        StringBuilder prefixes = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            query.append(ontology).append(" ?").append(property).append(dataResource.replace("_", "")).append(";").append("\n");
        }
        query = new StringBuilder(query.substring(0, query.length() - 2));
        queryBuilder.append(query);
        queryBuilder.append(". }");
        if(maxLimit > 0){
            queryBuilder.append("LIMIT ").append(maxLimit);
        }
        String queryString = queryBuilder.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(queryString);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", fullQuery);
        return ResponseEntity.ok(responseMap);
    }

    public static ResponseEntity<Map<String, String>> generateSparqlWithIsPropertyOf(
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct){
        StringBuilder queryBuilder = new StringBuilder();
        String querySelector;
        if(selectDistinct) {
            querySelector = "SELECT DISTINCT * WHERE {";

        }else {
            querySelector = "SELECT * WHERE {";
        }
        queryBuilder.append(querySelector);
        StringBuilder query = new StringBuilder();
        StringBuilder prefixes = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            query.append("?").append(dataResource.replace("_", "")).append(property).append(" ").append(ontology).append(" <http://dbpedia.org/resource/").append(dataResource).append(">.").append("\n");
        }
        query = new StringBuilder(query.substring(0, query.length() - 2));
        queryBuilder.append(query);
        queryBuilder.append(". }");
        if(maxLimit > 0){
            queryBuilder.append("LIMIT ").append(maxLimit);
        }
        String queryString = queryBuilder.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(queryString);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", fullQuery);
        return ResponseEntity.ok(responseMap);
    }

    public static ResponseEntity<Map<String, String>> generateSparqlWithPropertyWithLabels(
            String dataResource,
            List<Map<String, String>> props){
        StringBuilder queryBuilder = new StringBuilder();
        String querySelector = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "SELECT * WHERE {"
                + "<http://dbpedia.org/resource/" + dataResource + "> ";
        queryBuilder.append(querySelector);
        StringBuilder query = new StringBuilder();
        StringBuilder prefixes = new StringBuilder();
        StringBuilder labels = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            if(checkIfSubjectIsLiteral(dataResource, ontology))
                labels.append("?").append(property).append(" rdfs:label ").append("?").append(property).append("Label.").append("\n");
            query.append(ontology).append(" ?").append(property).append(";").append("\n");
        }
        query = new StringBuilder(query.substring(0, query.length() - 2));
        queryBuilder.append(query);
        queryBuilder.append(". ");
        queryBuilder.append(labels);
        queryBuilder.append("}");
        String queryString = queryBuilder.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(queryString);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", fullQuery);
        return ResponseEntity.ok(responseMap);
    }

    public static ResponseEntity<Map<String, String>> generateDynamicSparql(
            String sparqlQuery,
            String dataResource,
            List<Map<String, String>> props,
            String typeOfProperty,
            Integer maxLimit,
            Boolean selectDistinct){
        if(typeOfProperty.equals("property")){
            return generateDynamicSparqlWithProperty(sparqlQuery, dataResource, props, maxLimit, selectDistinct);
        }else{
            return generateDynamicSparqlWithIsPropertyOf(sparqlQuery, dataResource, props, maxLimit, selectDistinct);
        }
    }

    public static ResponseEntity<Map<String, String>> generateDynamicSparqlWithProperty(
            String sparqlQuery,
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct){
        String query = sparqlQuery.substring(0, sparqlQuery.length()-1);
        if(selectDistinct) {
            query = query.replace("SELECT *", "SELECT DISTINCT *");
        }else{
            query = query.replace("SELECT DISTINCT *", "SELECT *");
        }
        StringBuilder prefixes = new StringBuilder();
        StringBuilder newQuery = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            if(query.contains("?" + property))
                continue;
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            newQuery.append("<").append(dataResource).append(">").append(" ").append(ontology).append(" ?").
                    append(property).
                    append(dataResource.replace("http://dbpedia.org/resource/", "").replaceAll("[(){}_,.!#@]", "")).
                    append(".");
        }
        newQuery.append("}");
        String newQueryString = newQuery.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(query);
        fullQuery = fullQuery.concat(newQueryString);
        if(maxLimit > 0 && !fullQuery.contains("LIMIT ")){
            newQuery.append("LIMIT ").append(maxLimit);
        }else if(fullQuery.contains("LIMIT ")){
            fullQuery = fullQuery.replace("}LIMIT", "");
            fullQuery = fullQuery + ("LIMIT " + maxLimit);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", fullQuery);
        return ResponseEntity.ok(responseMap);
    }

    public static ResponseEntity<Map<String, String>> generateDynamicSparqlWithIsPropertyOf(
            String sparqlQuery,
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct){
        String query = sparqlQuery.substring(0, sparqlQuery.length()-1);
        if(selectDistinct) {
            query = query.replace("SELECT *", "SELECT DISTINCT *");
        }else{
            query = query.replace("SELECT DISTINCT *", "SELECT *");
        }
        StringBuilder prefixes = new StringBuilder();
        StringBuilder newQuery = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            if(query.contains("?" + property))
                continue;
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            newQuery.append("?").
                    append(dataResource.replace("http://dbpedia.org/resource/", "").replaceAll("[(){}_,.!#@]", "")).
                    append(property).append(" ").append(ontology).append(" <").append(dataResource).append(">.").append("\n");
        }
        newQuery.append("}");
        if(maxLimit > 0){
            newQuery.append("LIMIT ").append(maxLimit);
        }
        String newQueryString = newQuery.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(query);
        fullQuery = fullQuery.concat(newQueryString);
        if(maxLimit > 0 && !fullQuery.contains("LIMIT ")){
            newQuery.append("LIMIT ").append(maxLimit);
        }else if(fullQuery.contains("LIMIT ")){
            fullQuery = fullQuery.replace("}LIMIT", "");
            fullQuery = fullQuery + ("LIMIT " + maxLimit);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", fullQuery);
        return ResponseEntity.ok(responseMap);
    }

    private static Boolean checkIfSubjectIsLiteral(String dataResource, String property){
        Model model = ModelFactory.createDefaultModel();
        String subject = "https://dbpedia.org/data/" + dataResource + ".ttl";
        model.read(subject);
        String propBuilder = OntologyConstants.constants.get(property.split(":")[0]) + property.split(":")[1];
        Property predicate = model.getProperty(propBuilder);
        ResIterator resIterator = model.listResourcesWithProperty(RDFS.label);
        while(resIterator.hasNext()){
            Resource resource = resIterator.nextResource();
            if(!resource.getProperty(predicate).getObject().isLiteral())
                return true;
        }
        return false;
    }

    public static List<Map<String, String>> executeQuery(String queryString){

        List<Map<String, String>> resultList = new ArrayList<>();

        Query query = QueryFactory.create(queryString);
        String SPARQLEndpoint = "https://dbpedia.org/sparql";

        try(QueryExecution execution = QueryExecutionFactory.sparqlService(SPARQLEndpoint, query)){
            ResultSet resultSet = execution.execSelect();
            while (resultSet.hasNext()) {
                Map<String, String> resultMap = new HashMap<>();
                QuerySolution solution = resultSet.nextSolution();
                solution.varNames().forEachRemaining(varName -> {
                    String value = solution.get(varName).toString();
                    resultMap.put(varName, value);
                });
                resultList.add(resultMap);
            }
        }

        return resultList;
    }

}
