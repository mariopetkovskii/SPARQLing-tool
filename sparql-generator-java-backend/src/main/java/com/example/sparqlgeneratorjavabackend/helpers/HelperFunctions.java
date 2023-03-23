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

    public static ResponseEntity<Map<String, String>> generateSparqlWithProperty(
            String dataResource,
            List<Map<String, String>> props){
        StringBuilder queryBuilder = new StringBuilder();
        String querySelector = "SELECT * WHERE {"
                + "<http://dbpedia.org/resource/" + dataResource + "> ";
        queryBuilder.append(querySelector);
        StringBuilder query = new StringBuilder();
        StringBuilder prefixes = new StringBuilder();
        for (Map<String, String> prop : props) {
            String property = prop.get("property");
            String ontology = prop.get("ontology");
            String prefix = ontology.split(":")[0];
            if(!prefixes.toString().contains(HelperFunctions.getPrefix(prefix)))
                prefixes.append(getPrefix(prefix)).append("\n");
            query.append(ontology).append(" ?").append(property).append(";").append("\n");
        }
        query = new StringBuilder(query.substring(0, query.length() - 2));
        queryBuilder.append(query);
        queryBuilder.append(". }");
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

//    public static JsonArray executeQuery(String queryString, List<Map<String, String>> props){
//
//        JsonArray jsonArray = new JsonArray();
//
//
//        Query query = QueryFactory.create(queryString);
//        String SPARQLEndpoint = "https://dbpedia.org/sparql";
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < props.size(); i++){
//            Map<String, String> prop = props.get(i);
//            stringBuilder.append(prop.get("property"));
//            if (i < props.size() - 1) {
//                stringBuilder.append(",");
//            }
//        }
//        stringBuilder.append("\n");
//        try(QueryExecution execution = QueryExecutionFactory.sparqlService(SPARQLEndpoint, query)){
//            ResultSet resultSet = execution.execSelect();
//            while (resultSet.hasNext()) {
//                JsonObject jsonObject = new JsonObject();
//                QuerySolution solution = resultSet.nextSolution();
//                for (int i = 0; i < props.size(); i++){
//                    Map<String, String> prop = props.get(i);
//                    stringBuilder.append(solution.get(prop.get("property")));
//                    jsonObject.add(prop.get("property"), (JsonElement) solution.get(prop.get("property").toString()));
//                    if (i < props.size() - 1) {
//                        stringBuilder.append(",");
//                    }
//                }
//                jsonArray.add(jsonObject);
//                stringBuilder.append("\n");
//            }
//        }
//        String csv = stringBuilder.toString();
//        csv = csv.substring(0, csv.length() - 1 );
//        return jsonArray;
//    }

    public static List<Map<String, String>> executeQuery(String queryString, List<Map<String, String>> props){

        List<Map<String, String>> resultList = new ArrayList<>();

        Query query = QueryFactory.create(queryString);
        String SPARQLEndpoint = "https://dbpedia.org/sparql";

        try(QueryExecution execution = QueryExecutionFactory.sparqlService(SPARQLEndpoint, query)){
            ResultSet resultSet = execution.execSelect();
            while (resultSet.hasNext()) {
                Map<String, String> resultMap = new HashMap<>();
                QuerySolution solution = resultSet.nextSolution();
                for (int i = 0; i < props.size(); i++){
                    Map<String, String> prop = props.get(i);
                    resultMap.put(prop.get("property"), String.valueOf(solution.get(prop.get("property"))));
                }
                resultList.add(resultMap);
            }
        }

        return resultList;
    }

}
