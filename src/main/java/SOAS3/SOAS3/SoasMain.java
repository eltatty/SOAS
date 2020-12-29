package SOAS3.SOAS3;

import io.swagger.v3.parser.OpenAPIV3Parser;
import openllet.jena.PelletReasoner;
import openllet.jena.PelletReasonerFactory;
import io.swagger.annotations.Contact;
import io.swagger.annotations.License;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.*;
import org.apache.jena.util.PrintUtil;

public class SoasMain {
	public static void main(String[] args)
	{ 
		SoasMapper control= new SoasMapper("TestServices/","openApi3.ttl", "shacl.ttl", PelletReasonerFactory.THE_SPEC,null,"RDF/XML");


//		control.PrintOntology();

		/****** Query 1 *******/
		/* Which paths use tag with name=pets */
		/*
		System.out.println("Query1");
		String[]cols ={"pathNames"};
		control.Query("SELECT ?pathNames"
				+" WHERE {"
				+"?document <http://www.intelligence.tuc.gr/ns/open-api#supportedOperation> ?operation ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#tag> ?tag ."
				+"?tag <http://www.intelligence.tuc.gr/ns/open-api#name> \"pets\" ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#onPath> ?pathInd ."
				+"?pathInd <http://www.intelligence.tuc.gr/ns/open-api#pathName> ?pathNames .}"
				,cols);
		 */
		/****** Query 2 *********/
		/*Get servers(URI) and operationId of operations, which return
		(response) a schema Error AnimalEntities */
		/*
		System.out.println("Query2");
		String[]cols ={"serverURL", "?operationId"};
		control.Query("SELECT ?serverURL ?operationId"
				+" WHERE {"
				+"?document <http://www.intelligence.tuc.gr/ns/open-api#supportedOperation> ?operation ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#method> <http://www.intelligence.tuc.gr/ns/open-api#GET> ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#response> ?responses ."
				+"?responses <http://www.intelligence.tuc.gr/ns/open-api#content> ?mediatypes ."
				+"?mediatypes <http://www.intelligence.tuc.gr/ns/open-api#schema> ?schema ."
				+"?schema <http://www.w3.org/2000/01/rdf-schema#label> \"ErrorNodeShape\" ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#serverInfo> ?servers ."
				+"?servers <http://www.intelligence.tuc.gr/ns/open-api#host> ?serverURL ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#name> ?operationId}"
				,cols);

		 */
		/*******Query 3 *******/
		/* Retrieve descriptions of responses with status code '200' */
		/*
		System.out.println("Query3");
		String[]cols ={"description"};
		control.Query("SELECT ?description"
				+" WHERE {"
				+"?document <http://www.intelligence.tuc.gr/ns/open-api#supportedOperation> ?operation ."
				+"?operation <http://www.intelligence.tuc.gr/ns/open-api#response> ?responses ."
				+"?responses <http://www.intelligence.tuc.gr/ns/open-api#statusCode> \"200\" ."
				+"?responses  <http://www.intelligence.tuc.gr/ns/open-api#description> ?description}"
				,cols);
		 */


		/*******Query 4 *******/
		/* Retrieve all names of parameters that are used in path */
		 /*
		System.out.println("Query3");
		String[]cols ={"parameterName"};
		control.Query( "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +        
			    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				"PREFIX openapi: <http://www.intelligence.tuc.gr/ns/open-api#>"+
			    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			    " SELECT ?parameterName " +
			    " WHERE{?document rdf:type openapi:Document ."+
			    		"?document openapi:supportedOperation ?operation ."+
			    		"?operation openapi:parameter ?parameter ."+
			    		"?parameter rdf:type openapi:Query ."+
			    		"?parameter openapi:name ?parameterName .}"
				,cols);
		 /*


		/******Query 5 (Retrieve operationIds with the corresponding tag name)*******/
		/*
		System.out.println("Query6");
		String[]cols ={"operationIds","tagNames"};
		control.Query( "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +        
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				"PREFIX openapi: <http://www.intelligence.tuc.gr/ns/open-api#>"+
				"PREFIX sh:<http://www.w3.org/ns/shacl#>"+
				"PREFIX schema: <http://schema.org/>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +

			    " SELECT ?operationIds ?tagNames " +
			    " WHERE{ ?document  openapi:supportedOperation ?operation ."+
			    "?operation openapi:tag ?tag ."+
			    "?tag openapi:name ?tagNames ."+
			    "?operation openapi:name ?operationIds}"

				,cols);
		*/
		/***** Query 6  (Retrieve service title) ******/
		/*
		String [] cols ={"label"};
		control.Query("SELECT ?serviceTitle WHERE { ?objectURI <http://www.intelligence.tuc.gr/ns/open-api#serviceTitle> ?serviceTitle}",cols);

		 */


		/***** Query 6.1 (Retrieve requestBody Individuals) *****/
//		System.out.println("Query 6.1");
//		String [] cols ={"requestBody"};
//		control.Query("PREFIX openapi: <http://www.intelligence.tuc.gr/ns/open-api#>" +
//				"SELECT ?requestBody WHERE { ?objectURI openapi:reqBody ?reqBody}", cols);
		
		
		/******Query 7 (Retrieve operationIds that related to a pet)*******/
		
//		System.out.println("Query9");
//		String[]cols ={"operationId"};
//		control.Query( "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
//			    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
//				"PREFIX openapi: <http://www.intelligence.tuc.gr/ns/open-api#>"+
//				"PREFIX sh:<http://www.w3.org/ns/shacl#>"+
//				"PREFIX schema: <http://schema.org/>"+
//			    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//
//			    " SELECT ?operationId " +
//			    " WHERE{{?AnimalEntities rdfs:subClassOf schema:Pet ."+
//			    "?nodeEntities rdf:type sh:NodeShape ."+
//			    "?nodeEntities sh:targetClass ?AnimalEntities ."+
//			    "?nodeEntities openapi:supportedOperation ?operation ."+
//			    "?operation openapi:name ?operationId }"+
//			    "UNION{"+
//			     "?AnimalEntities rdf:type sh:NodeShape ."+
//			    "?AnimalEntities openapi:supportedOperation ?operation ."+
//			    "?AnimalEntities sh:targetClass schema:Pet ."+
//			    "?operation openapi:name ?operationId}}"
//				,cols);



	}
}
