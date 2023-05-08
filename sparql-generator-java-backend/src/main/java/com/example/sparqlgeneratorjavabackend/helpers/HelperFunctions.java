package com.example.sparqlgeneratorjavabackend.helpers;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            Boolean selectDistinct,
            String variableName){
        if(propertyType.equals("property")){
            return generateSparqlWithProperty(dataResource, props, maxLimit, selectDistinct, variableName);
        }else{
            return generateSparqlWithIsPropertyOf(dataResource, props, maxLimit, selectDistinct, variableName);
        }
    }

    public static ResponseEntity<Map<String, String>> generateUnionQuery(List<String> queries){
        StringBuilder queryBuilder = new StringBuilder();
        StringBuilder queryInside = new StringBuilder();
        Set<String> ontologies = new HashSet<>();
        for (String query : queries) {
            int selectIndexBeforeSelect = query.indexOf("SELECT");
            String prefixes = query.substring(0, selectIndexBeforeSelect);
            String [] prefixesParts = prefixes.split("\n");
            ontologies.addAll(Arrays.asList(prefixesParts));
            int openingBraceIndex = query.indexOf("{");
            int closingBraceIndex = query.indexOf("}");
            String part = query.substring(openingBraceIndex, closingBraceIndex + 1);
            queryInside.append(part + " union ");
            System.out.println(part);
        }
        ontologies.forEach(queryBuilder::append);
        queryBuilder.append("\nSELECT * WHERE {");
        queryBuilder.append(queryInside.substring(0, queryInside.length() - 7));
        queryBuilder.append("}");
        System.out.println(queryBuilder);
        String queryToBeReturned = queryBuilder.toString();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("query", queryToBeReturned);
        return ResponseEntity.ok(responseMap);
    }

    public static ResponseEntity<Map<String, String>> generateSparqlWithProperty(
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct,
            String variableName){
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
            query.append(ontology).append(" ?").append(property).append(variableName).append(";").append("\n");
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
            Boolean selectDistinct,
            String variableName){
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
            query.append("?").append(variableName).append(property).append(" ").append(ontology).append(" <http://dbpedia.org/resource/").append(dataResource).append(">.").append("\n");
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

//    public static ResponseEntity<Map<String, String>> generateSparqlWithIsPropertyOf(
//            String dataResource,
//            List<Map<String, String>> props,
//            Integer maxLimit,
//            Boolean selectDistinct,
//            String variableName){
//        StringBuilder queryBuilder = new StringBuilder();
//        String querySelector;
//        if(selectDistinct) {
//            querySelector = "SELECT DISTINCT * WHERE {";
//
//        }else {
//            querySelector = "SELECT * WHERE {{";
//        }
//        queryBuilder.append(querySelector);
//        StringBuilder query = new StringBuilder();
//        StringBuilder prefixes = new StringBuilder();
//        for (Map<String, String> prop : props) {
//            String property = prop.get("property");
//            String ontology = prop.get("ontology");
//            String prefix = ontology.split(":")[0];
//            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
//                prefixes.append(getPrefix(prefix)).append("\n");
//            query.append("?").append(variableName).append(property).append(" ").append(ontology).append(" <http://dbpedia.org/resource/").append(dataResource).append(">.").append("\n");
//            query.append("} union {");
//        }
//        query = new StringBuilder(query.substring(0, query.length() - 10));
//        queryBuilder.append(query);
//        queryBuilder.append(" }}");
//        if(maxLimit > 0){
//            queryBuilder.append("LIMIT ").append(maxLimit);
//        }
//        String queryString = queryBuilder.toString();
//        String prefixesString = prefixes.toString();
//        String fullQuery = prefixesString.concat(queryString);
//        Map<String, String> responseMap = new HashMap<>();
//        responseMap.put("query", fullQuery);
//        return ResponseEntity.ok(responseMap);
//    }

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
            Boolean selectDistinct,
            String variableName,
            String unionQueryColumnClicked,
            Boolean isNewUrl){
        if(typeOfProperty.equals("property")){
            return generateDynamicSparqlWithProperty(sparqlQuery, dataResource, props, maxLimit, selectDistinct, variableName, unionQueryColumnClicked, isNewUrl);
        }else{
            return generateDynamicSparqlWithIsPropertyOf(sparqlQuery, dataResource, props, maxLimit, selectDistinct, variableName);
        }
    }

    public static ResponseEntity<Map<String, String>> generateDynamicSparqlWithProperty(
            String sparqlQuery,
            String dataResource,
            List<Map<String, String>> props,
            Integer maxLimit,
            Boolean selectDistinct,
            String variableName,
            String unionQueryColumnClicked,
            Boolean isNewUrl){
        String query = sparqlQuery.substring(0, sparqlQuery.length()-1);
        if(selectDistinct) {
            query = query.replace("SELECT *", "SELECT DISTINCT *");
        }else{
            query = query.replace("SELECT DISTINCT *", "SELECT *");
        }
        StringBuilder prefixes = new StringBuilder();
        StringBuilder newQuery = new StringBuilder();
        String [] parts = new String[0];
        Boolean isNewPartAlreadyExist = false;
        String newPart = "";
        List<String> newParts = new ArrayList<>();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix))) {
                prefixes.append(getPrefix(prefix)).append("\n");
                if(query.contains(prefixes)){
                    prefixes.delete(0, prefixes.length());
                }
            }
            if(query.contains(" union ")){
                String regex = "\\{(.*?)\\}";

                Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(query);

                StringBuilder extractedBlock = new StringBuilder();

                while (matcher.find()) {
                    extractedBlock.append(matcher.group(1)).append("\n");
                }

                String extractedQuery = extractedBlock.toString().trim();

                if (!extractedQuery.isEmpty()) {
                    parts = extractedQuery.split("\n");
                    parts[0] = parts[0].substring(1);
                    for (String part: parts
                         ) {
//                        part = part.replace(" ", "");
                        String getFirstPart = part.split(" ")[0];
                        String [] partialQueryParts = part.split(" ");

                        if(partialQueryParts.length > 3){
                            if(!isNewPartAlreadyExist) {
                                newPart = part.concat(partialQueryParts[partialQueryParts.length - 1].
                                        substring(0, partialQueryParts[partialQueryParts.length - 1].length() - 1)
                                        + " " + ontology + " ?" + property + variableName + ".");
                                isNewPartAlreadyExist = true;
                            }
                        }else{
                            newPart = part.concat(getFirstPart + " " + ontology + " ?" + property + variableName + ".");
                        }
                        if(getFirstPart.equals("")){
                            continue;
                        }
                        newParts.add(newPart);
                    }

                }
            }else {
                newQuery.append("<").append(dataResource).append(">").append(" ").append(ontology).append(" ?").
                        append(property).
                        append(variableName).
                        append(".");
            }
        }
        List<String> partsThatWillBeReplaced = new ArrayList<>(Arrays.asList(parts));
        partsThatWillBeReplaced.removeIf(e -> e == null || e.trim().isEmpty());
        newParts.removeIf(e -> e == null || e.trim().isEmpty());
        for(int i = 0; i < partsThatWillBeReplaced.size(); i++){
            query = query.replace(partsThatWillBeReplaced.get(i), newParts.get(i));
        }
        newQuery.append("}");
        String newQueryString = newQuery.toString();
        String prefixesString = prefixes.toString();
        String fullQuery = prefixesString.concat(query);
        fullQuery = fullQuery.concat(newQueryString);
        if(maxLimit > 0 && !fullQuery.contains("LIMIT ")){
            fullQuery = fullQuery + ("LIMIT " + maxLimit);
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
            Boolean selectDistinct,
            String variableName){
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
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix))) {
                prefixes.append(getPrefix(prefix)).append("\n");
                if(query.contains(prefixes)){
                    prefixes.delete(0, prefixes.length());
                }
            }
            newQuery.append("?").
                    append(variableName).
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
            fullQuery = fullQuery + ("LIMIT " + maxLimit);
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

    public static List<Map<String, String>> executeQuery(String queryString) {

        List<Map<String, String>> resultList = new ArrayList<>();

        Query query = QueryFactory.create(queryString);
        String SPARQLEndpoint = "https://dbpedia.org/sparql";

        try (QueryExecution execution = QueryExecutionFactory.sparqlService(SPARQLEndpoint, query)) {
            ResultSet resultSet = execution.execSelect();
            while (resultSet.hasNext()) {
                Map<String, String> resultMap = new HashMap<>();
                QuerySolution solution = resultSet.nextSolution();

                // Get the variable names
                Iterator<String> varNames = solution.varNames();
                while (varNames.hasNext()) {
                    String varName = varNames.next();
                    String value = solution.contains(varName) ? solution.get(varName).toString() : "";
                    resultMap.put(varName, value);
                }

                // Add missing variables with empty values
                for (String varName : query.getResultVars()) {
                    if (!resultMap.containsKey(varName)) {
                        resultMap.put(varName, "");
                    }
                }

                resultList.add(resultMap);
            }
        }

        return resultList;
    }


}
