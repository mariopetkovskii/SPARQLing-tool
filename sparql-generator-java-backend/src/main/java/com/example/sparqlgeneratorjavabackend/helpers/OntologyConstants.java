package com.example.sparqlgeneratorjavabackend.helpers;

import java.util.HashMap;
import java.util.Map;

public class OntologyConstants {
    private static final String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String rdfsPrefix = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String dcTermsPrefix = "http://purl.org/dc/terms/";
    private static final String dboPrefix = "http://dbpedia.org/ontology/";
    private static final String dbrPrefix = "http://dbpedia.org/resource/";
    private static final String dbpPrefix = "http://dbpedia.org/property/";
    private static final String owlPrefix = "http://www.w3.org/2002/07/owl#";
    private static final String xsdPrefix = "http://www.w3.org/2001/XMLSchema#";
    private static final String foafPrefix = "http://xmlns.com/foaf/0.1/";
    public static Map<String, String> constants = new HashMap<>();

    static {
        constants.put("rdf", rdfPrefix);
        constants.put("rdfs", rdfsPrefix);
        constants.put("dct", dcTermsPrefix);
        constants.put("dbo", dboPrefix);
        constants.put("dbr", dbrPrefix);
        constants.put("dbp", dbpPrefix);
        constants.put("foaf", foafPrefix);
        constants.put("xsd", xsdPrefix);
        constants.put("owl", owlPrefix);
    }
}
