package SOAS3.SOAS3;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDFS;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import openllet.jena.PelletReasonerFactory;

public class SoasMapper {

	private String directoryofOpenApiFiles;
	private String ontologyPath;
	private String shaclPath;
	private OntModelSpec spec;
	private Model createModelInfo;
	private String modelType;
	//Handlers of OpenApi and Ontology Files
	private OntologyHandler ontHandler;
	private OpenApiHandler openapiHandler;
	// Ontology's instance
	private OntModel ontModel;
	// OpenApi's instance
	private OpenAPI openapi;

	/*Constructor*/
	public SoasMapper(String directoryofOpenApiFiles, String ontologyPath, String shaclPath, OntModelSpec spec,
			Model createModelInfo,String modelType)
	{
		this.directoryofOpenApiFiles=directoryofOpenApiFiles;
		this.ontologyPath=ontologyPath;
		this.shaclPath=shaclPath;
		this.spec=spec;
		this.createModelInfo=createModelInfo;
		this.modelType=modelType;
		//create instances of handlers
		openapiHandler= new OpenApiHandler();
		ontHandler = new OntologyHandler();
		//Read Ontology file
		ReadOntologyFile();
		//Read each file in directory of OpenAPI files.
		ReadOpenApiFiles(directoryofOpenApiFiles);

	}

	//Read Ontology File
	public void ReadOntologyFile()
	{
		ontHandler.createOntologyModel(spec, createModelInfo);
		ontModel=ontHandler.readOntologyFile(shaclPath, modelType);
		ontModel=ontHandler.readOntologyFile(ontologyPath, modelType);
	}



	public void ReadOpenApiFiles(String directoryofOpenApiFiles)
	{
		File folder = new File(directoryofOpenApiFiles);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				openapi=openapiHandler.ReadFile(file.getAbsolutePath());
				System.out.println(file.getName());
				// Add prefix
				String NS = "https://www.example.com/service/" + FilenameUtils.removeExtension(file.getName()) + "#";
				ontModel.setNsPrefix("myOnt", NS);
				//Convert OpenApi To Ontology
				 new Convert2Ontology(openapi,ontModel, NS);
				 this.ontHandler.PrintOntologyToFile(FilenameUtils.removeExtension(file.getName()));
			}
		}
	}



	public void PrintOntology()
	{
		this.ontHandler.PrintOntology();
	}

	public void PrintOntologyToFile(String productName) {
		this.ontHandler.PrintOntologyToFile(productName);
	}

	public void Query(String query,String[] columnsName) {
		List<String> answer =ontHandler.QueryCreator(query,columnsName);
		for(String a : answer){
			System.out.println(a);
		}
	}



}
