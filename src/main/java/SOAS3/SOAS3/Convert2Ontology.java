package SOAS3.SOAS3;

import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import io.swagger.parser.util.RefUtils;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.parser.util.ResolverFully;
import org.apache.jena.dboe.sys.Sys;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.impl.IndividualImpl;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.Parameter.StyleEnum;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.tags.Tag;


public class Convert2Ontology {
	private OpenApiDataPropertyCreator property_creator;
	private RDFNode securityReqInd;
	private OpenAPI openApi4header;
	
	public Convert2Ontology(OpenAPI openapi, OntModel ontModel) {
		property_creator=new OpenApiDataPropertyCreator(ontModel);
		openApi4header = openapi;
		parseDocumentObject(ontModel, openapi);
	}

	//Document Object
	private void parseDocumentObject(OntModel ontModel, OpenAPI openapi) {
		//Create a Document Individual
		OntClass documentClass=ontModel.getOntClass(OpenApiOntUtils.DocumentURI);
		Individual documentInd = ontModel.createIndividual("service_document", documentClass);
		//Parse Info Object
		parseInfoObject(ontModel,documentInd, openapi.getInfo());
		//Parse global servers and create individuals
		List<Individual> globalServerIndList= new ArrayList<Individual>();
		for(Server serverObject: openapi.getServers()){
			Individual serverInd = parseServerObject(ontModel, serverObject);
			globalServerIndList.add(serverInd);
		}
		//Parse all security schemes
		if(openapi.getComponents().getSecuritySchemes()!=null){
			for(Entry<String, SecurityScheme> secSchemeEnty: openapi.getComponents().getSecuritySchemes().entrySet())
			{
				parseSecuritySchemeObject(ontModel, documentInd, secSchemeEnty.getKey(), secSchemeEnty.getValue());
			}
		}
		//Parse global security requirements 
		List<Individual> globSecReqIndList = null;
		if(openapi.getSecurity()!=null){
			globSecReqIndList= new ArrayList<Individual>();
			for (SecurityRequirement securityReqObject: openapi.getSecurity()) {
				Individual securityReqInd = parseSecurityReqObject(ontModel, securityReqObject);
				globSecReqIndList.add(securityReqInd);
			}
		}
		//Parse Tags
		//Keep a list that contains <tag, shape Individual>
		//shape Individual is connected with a tag via x-On Resource
		HashMap<String, Individual> tagShapeMap = null;
		if(openapi.getTags()!=null){
			tagShapeMap=new HashMap<String, Individual>();
			for(Tag tag : openapi.getTags()){
				Entry<String, Individual> tagShapeEntry = parseTagObject(ontModel, tag, openapi.getComponents().getSchemas());
				if(tagShapeEntry.getValue()!=null){
					Property property=ontModel.getProperty(OpenApiOntUtils.supportedEntityURI);
					Statement st= ontModel.createStatement(documentInd,property,tagShapeEntry.getValue());
					ontModel.add(st);

				}
				tagShapeMap.put(tagShapeEntry.getKey(),tagShapeEntry.getValue());
			}
		}
		//Parse external docs in document level
		if(openapi.getExternalDocs()!=null){
			Individual exDocInd= parseExternalDocObject(ontModel, openapi.getExternalDocs());
			Property property=ontModel.getProperty(OpenApiOntUtils.externalDocURI);
			Statement st= ontModel.createStatement(documentInd,property,exDocInd);
			ontModel.add(st);
		}

		//Parse path object into Ontology
		for(Entry<String, PathItem> pathEntry : openapi.getPaths().entrySet()){
			//Get servers located in path level
			List<Individual> pathServerIndList= new ArrayList<Individual>();
			if(pathEntry.getValue().getServers()!=null){
				for (Server serverObject : pathEntry.getValue().getServers()) {
					Individual serverInd = parseServerObject(ontModel, serverObject);
					pathServerIndList.add(serverInd);
				}
			}
			//If there no path servers, keep global servers
			List<Individual> finalServerList;
			if(pathServerIndList.isEmpty()){
				finalServerList = globalServerIndList;
			}
			else {
				finalServerList = pathServerIndList;
			}
			//Get all parameters that are in path-level

			List<Parameter> parametersListFromPath=null;
			if(pathEntry.getValue().getParameters()!=null){
				parametersListFromPath=pathEntry.getValue().getParameters();
			}

			//Parse path Object
			Individual pathInd=parsePathObject(ontModel, pathEntry.getKey());
			//Parse all existed Operations
			if(pathEntry.getValue().getGet()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.GET,documentInd, pathInd, pathEntry.getValue().getGet(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getHead()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.HEAD,documentInd, pathInd, pathEntry.getValue().getHead(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getOptions()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.OPTIONS,documentInd, pathInd, pathEntry.getValue().getOptions(),tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getPatch()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.PATCH,documentInd, pathInd, pathEntry.getValue().getPatch(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getPost()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.POST,documentInd, pathInd, pathEntry.getValue().getPost(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getPut()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.PUT,documentInd, pathInd, pathEntry.getValue().getPut(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getTrace()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.TRACE,documentInd, pathInd, pathEntry.getValue().getTrace(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
			if(pathEntry.getValue().getDelete()!=null)
				parseOperationObject(ontModel, EnumerationsClass.Operation.DELETE,documentInd, pathInd, pathEntry.getValue().getDelete(), tagShapeMap,
						openapi.getComponents().getSchemas(), parametersListFromPath,finalServerList,
						globSecReqIndList, openapi);
		}
	}

	private void parseOperationObject(OntModel ontModel, EnumerationsClass.Operation operationMethod, Individual documentInd, Individual pathInd, Operation operationObject,
			Map<String, Individual> tagShapeMap, Map<String, Schema> schemas,
			List<Parameter> parametersListFromPath, List<Individual> finalServerList,
			List<Individual> globSecReqIndList, OpenAPI openapi) {
		Individual operationInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.OperationClassURI));
		//x-operationType
		if(operationObject.getExtensions()!=null){
			Object operationType = operationObject.getExtensions().get("x-operationType");
			if(operationType != null){
				Resource second_class = ResourceFactory.createResource(operationType.toString());
				operationInd.addOntClass(second_class);
			}
		}
		property_creator.AddName(operationInd, operationObject.getOperationId());
		property_creator.AddDeprecated(operationInd, String.valueOf(operationObject.getDeprecated()));
		property_creator.AddSummary(operationInd, operationObject.getSummary());
		property_creator.AddDescription(operationInd, operationObject.getDescription());
		//Path Ind
		ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.onPathURI),pathInd));
		//Extract Type of Operation, could be : (Get, Post, Delete,..)
		//Method individuals are already created in ontology
		Individual methodInd=getMethodIndividual(ontModel,operationMethod);
		ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.methodURI),methodInd));
		//Extract external doc
		if(operationObject.getExternalDocs()!=null){
			Individual exDocInd=parseExternalDocObject(ontModel, operationObject.getExternalDocs());
			ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.externalDocURI),exDocInd));
		}
		//Get operation's tags
		if (operationObject.getTags() != null){
			for (String tag :operationObject.getTags()) {
				Individual tagInd;
				//Get tag individual with name "tag"
				if (tagShapeMap==null || !tagShapeMap.containsKey(tag)) {
					//if does not exist, create Tag Individual
					tagInd =  ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.TagClassURI));
					property_creator.AddName(tagInd, tag);
				}
				else {
					//Search Tag with name "tag" in ontology
					//FindTag returns tag Individual and the corresponding shape
					//that comes from x-onResourse
					tagInd = ontModel.getIndividual(tag);
					Individual shape = tagShapeMap.get(tag);
					if(shape!=null){
						ontModel.add(ontModel.createStatement(shape, ontModel.getProperty(OpenApiOntUtils.supportedOperationURI),operationInd));
					}
				}
				ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.tagURI),tagInd));
			}
		}
		//Get operation's RequestBody
		if(operationObject.getRequestBody()!=null){
			Individual requestInd = parseRequestBodyObject(ontModel, operationObject.getRequestBody(),schemas, openapi);
			ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.requestBodyURI),requestInd));
		}
		//Get operation's Response
		for(Entry<String, ApiResponse> responseEntry :operationObject.getResponses().entrySet()){
			Individual responseInd=parseResponseObject(ontModel, responseEntry.getKey(), responseEntry.getValue(),schemas);
			ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.responseURI),responseInd));
		}
		//Get operation's Security Requirements
		//if there are no operation's security requirements, use global ones.
		if(operationObject.getSecurity()!=null || globSecReqIndList!=null)
			if(operationObject.getSecurity()==null){
				for (Individual securiryReqInd : globSecReqIndList) {
					ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.securityURI),securityReqInd));
				}
			}
		//else define new security requirements for this operation.
			else{
				for (SecurityRequirement securiryReqObject : operationObject.getSecurity()) {
					Individual securityReqInd = parseSecurityReqObject(ontModel,securiryReqObject);
					ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.securityURI),securityReqInd));
				}
			}

		/*Parameters shared by all operations of a path can be defined on the path
		 * level instead of the operation level. Path-level parameters are inherited
		 * by all operations of that path.
		 * If there are any extra parameters defined at the operation level they are used together
		 * with path-level parameters. Specific path-level parameters can be overridden
		 * on the operation level, but cannot be removed.
		 * combineParameters function handles the aforementioned scenaria.
		 */


		List<Parameter> combinedParametersList= combineParameters(parametersListFromPath,operationObject.getParameters());
		for (Parameter parameter : combinedParametersList){
			//Parameters could be in different positions
			switch(parameter.getIn())
			{
			case "path":
				Individual parameterInd=parsePathParameter(ontModel,parameter,schemas);
				ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.parameterURI),parameterInd));
				break;
			case "query":
				Individual queryInd=parseQueryObject(ontModel,parameter,schemas);
				ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.parameterURI),queryInd));
				break;
			case "header":
				String headerName=parameter.getName();
				Individual headerInd=parseHeaderObject(ontModel,headerName,parameter,schemas);
				ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.requestHeaderURI),headerInd));
				break;
			case "cookie":
				Individual cookieInd=parseCookieObject(ontModel,parameter,schemas);
				ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.cookieURI),cookieInd));
			}
		}

		//Get operation's servers.
		//If there are no operation's servers, use global servers
		// or path servers (finalServerList).
		if(operationObject.getServers()!=null || finalServerList!=null){
			if(operationObject.getServers()==null){
				for (Individual serverInd : finalServerList) {
					ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.serverInfoURI),serverInd));
				}
			}
			else{
				for (Server serverObject : operationObject.getServers()) {
					Individual serverInd = parseServerObject(ontModel, serverObject);
					ontModel.add(ontModel.createStatement(operationInd, ontModel.getProperty(OpenApiOntUtils.serverInfoURI),serverInd));
				}
			}
		}
		//Save operation in document class
		ontModel.add(ontModel.createStatement(documentInd, ontModel.getProperty(OpenApiOntUtils.supportedOperationURI),operationInd));
	}






	private Individual parseHeaderObject(OntModel ontModel, String headerName, Parameter Parameter,
			Map<String, Schema> schemas) {

		Individual headerInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.HeaderClassURI));
		property_creator.AddName(headerInd,headerName);
		property_creator.AddDescription(headerInd, Parameter.getDescription());
		property_creator.AddRequired(headerInd,String.valueOf(Parameter.getRequired()));
		property_creator.AddDeprecated(headerInd, String.valueOf(Parameter.getDeprecated()));
		property_creator.AddExplode(headerInd, String.valueOf(Parameter.getExplode()));

		//Fotis
		Individual styleInd=getStyleIndividual(ontModel, Parameter.getStyle());
		ontModel.add(ontModel.createStatement(headerInd, ontModel.getProperty(OpenApiOntUtils.styleURI),styleInd));

		if(Parameter.getSchema()!=null){

			Individual schemaInd=parseSchemaObject(ontModel,null, null, Parameter.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(headerInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		if(Parameter.getContent()!=null) {
			for(Entry<String,MediaType> mediaTypeEntry : Parameter.getContent().entrySet()){
				Individual mediatypeInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(), mediaTypeEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(headerInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediatypeInd));
			}
		}
		return headerInd;
	}

	// When parameter location is in header..
	private Individual parseHeaderObject(OntModel ontModel, String headerName, Header header,
			Map<String, Schema> schemas) {

		if (header.get$ref() != null){
			// Get refernece name
			String objectName = RefUtils.computeDefinitionName(header.get$ref());

			// Get referenced object if exists in components
			ResolverFully resolver = new ResolverFully();
			resolver.resolveFully(openApi4header);
			header = resolver.resolveHeader(header);
		}

		Individual headerInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.HeaderClassURI));
		property_creator.AddName(headerInd,headerName);
		property_creator.AddDescription(headerInd, header.getDescription());
		property_creator.AddRequired(headerInd,String.valueOf(header.getRequired()));
		property_creator.AddDeprecated(headerInd, String.valueOf(header.getDeprecated()));
		property_creator.AddExplode(headerInd, String.valueOf(header.getExplode()));
		//Extract the schema defining the type used for the parameter.
		if(header.getSchema()!=null){

			Individual schemaInd=parseSchemaObject(ontModel,null, null, header.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(headerInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		//Extract a map containing the representations for the parameter
		if(header.getContent()!=null) {
			for(Entry<String,MediaType> mediaTypeEntry : header.getContent().entrySet()){
				Individual mediatypeInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(), mediaTypeEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(headerInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediatypeInd));
			}
		}
		return headerInd;
	}


	/*
	 *There are four possible parameter locations specified by the in field:
	 *path -  where the parameter values actually part of the operation's URL.
	 *query - Parameters that are appended to the URL.
	header - Custom headers that are expected as part of the request.
	cookie - Used to pass a specific cookie value to the API.
	 */

	// When parameter location is on cookie..
	private Individual parseCookieObject(OntModel ontModel, Parameter Parameter, Map<String, Schema> schemas) {
		Individual parameterInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.CookieClassURI));
		property_creator.AddName(parameterInd,Parameter.getName());
		property_creator.AddDescription(parameterInd, Parameter.getDescription());
		//Exctract whether this parameter is mandatory
		property_creator.AddRequired(parameterInd,String.valueOf(Parameter.getRequired()));
		//Extract whether parameter is deprecated and SHOULD
		//be transitioned out of usage.
		property_creator.AddDeprecated(parameterInd, String.valueOf(Parameter.getDeprecated()));
		property_creator.AddExplode(parameterInd, String.valueOf(Parameter.getExplode()));
		//Extract how the parameter value will be serialized
		Individual styleInd=getStyleIndividual(ontModel, Parameter.getStyle());
		ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.styleURI),styleInd));
		//Extract the schema defining the type used for the parameter.
		if(Parameter.getSchema()!=null){
			Individual schemaInd=parseSchemaObject(ontModel,null, null, Parameter.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		//Extract a map containing the representations for the parameter
		if(Parameter.getContent()!=null) {
			for(Entry<String,MediaType> mediaTypeEntry : Parameter.getContent().entrySet()){
				Individual mediatypeInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(), mediaTypeEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediatypeInd));
			}
		}
		return parameterInd;
	}

	// When parameter location is in query..
	private Individual parseQueryObject(OntModel ontModel, Parameter Parameter, Map<String, Schema> schemas) {
		Individual parameterInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.QueryClassURI));
		property_creator.AddName(parameterInd,Parameter.getName());
		property_creator.AddDescription(parameterInd, Parameter.getDescription());
		property_creator.AddRequired(parameterInd,String.valueOf(Parameter.getRequired()));
		property_creator.AddDeprecated(parameterInd, String.valueOf(Parameter.getDeprecated()));
		property_creator.AddExplode(parameterInd, String.valueOf(Parameter.getExplode()));
		property_creator.AddAllowEmptyValue(parameterInd, String.valueOf(Parameter.getAllowEmptyValue()));
		property_creator.AddAllowReserved(parameterInd, String.valueOf(Parameter.getAllowReserved()));
		Individual styleInd=getStyleIndividual(ontModel, Parameter.getStyle());
		ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.styleURI),styleInd));
		//Extract the schema defining the type used for the parameter.
		if(Parameter.getSchema()!=null){
			Individual schemaInd=parseSchemaObject(ontModel,null, null, Parameter.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		//Extract a map containing the representations for the parameter
		if(Parameter.getContent()!=null) {
			for(Entry<String,MediaType> mediaTypeEntry : Parameter.getContent().entrySet()){
				Individual mediatypeInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(), mediaTypeEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediatypeInd));
			}
		}
		return parameterInd;
	}

	// When parameter location is in path..
	private Individual parsePathParameter(OntModel ontModel, Parameter Parameter,
			Map<String, Schema> schemas) {

		Individual parameterInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ParameterClassURI));
		property_creator.AddName(parameterInd,Parameter.getName());
		property_creator.AddDescription(parameterInd, Parameter.getDescription());
		property_creator.AddRequired(parameterInd,String.valueOf(Parameter.getRequired()));
		property_creator.AddDeprecated(parameterInd, String.valueOf(Parameter.getDeprecated()));
		property_creator.AddExplode(parameterInd, String.valueOf(Parameter.getExplode()));
		Individual styleInd=getStyleIndividual(ontModel, Parameter.getStyle());
		//Extract the schema defining the type used for the parameter.
		ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.styleURI),styleInd));
		if(Parameter.getSchema()!=null){
			Individual schemaInd=parseSchemaObject(ontModel,null, null, Parameter.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		//Extract a map containing the representations for the parameter
		if(Parameter.getContent()!=null) {
			for(Entry<String,MediaType> mediaTypeEntry : Parameter.getContent().entrySet()){
				Individual mediatypeInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(), mediaTypeEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(parameterInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediatypeInd));
			}
		}
		return parameterInd;
	}

	private List<Parameter> combineParameters(List<Parameter> fromPath, List<Parameter> fromOperation) {
		List<Parameter> combinedList=new ArrayList<>();;
		//Specific path-level parameters can be overridden
		if(fromPath!=null && fromOperation!=null){
			for(Parameter paramFromPath : fromPath){
				boolean override=false;
				for(Parameter paramFromOperation : fromOperation){
					if(paramFromPath.getName()==paramFromOperation.getName())
						if(paramFromPath.getIn()==paramFromOperation.getIn()){
							combinedList.add(paramFromOperation);
							override=true;
							break;
						}
				}
				if(override==false)
					combinedList.add(paramFromPath);
			}
		}
		/*Any extra parameters defined at the operation level
		 * are used together with path-level parameters*/
		if(fromOperation!=null){
			for(Parameter paramFromOperation : fromOperation){
				if(!combinedList.contains(paramFromOperation)){
					combinedList.add(paramFromOperation);
				}
			}
		}
		return combinedList;
	}

	// Extract single response from an API Operation
	private Individual parseResponseObject(OntModel ontModel, String statusCode, ApiResponse responseObject,
			Map<String, Schema> schemas) {
		Individual responseInd=null;


		if (statusCode.equals("default"))
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.DefaultResponseURI));
		else if (statusCode == "1XX")
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.XX1ResponseURI));
		else if (statusCode == "2XX")
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.XX2ResponseURI));
		else if (statusCode == "3XX")
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.XX3ResponseURI));
		else if (statusCode == "4XX")
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.XX4ResponseURI));
		else if (statusCode == "5XX")
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.XX5ResponseURI));
		else {
			responseInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ResponseClassURI));
			property_creator.AddStatusCode(responseInd, statusCode);
		}
		property_creator.AddDescription(responseInd, responseObject.getDescription());
		if(responseObject.getHeaders()!=null){
			//Extract a map which allows additional information to be provided as headers
			for(Entry<String, Header> headerObjectEntry : responseObject.getHeaders().entrySet()){
				Individual headerInd=parseHeaderObject(ontModel,headerObjectEntry.getKey(),headerObjectEntry.getValue(), schemas);
				ontModel.add(ontModel.createStatement(responseInd, ontModel.getProperty(OpenApiOntUtils.requestHeaderURI),headerInd));
			}
		}
		if(responseObject.getContent()!=null){
			//Extract map containing descriptions of potential response payloads
			for(Entry<String, MediaType> mediaTypeEntry : responseObject.getContent().entrySet()){
				Individual mediaInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(),mediaTypeEntry.getValue(),schemas);
				ontModel.add(ontModel.createStatement(responseInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediaInd));
			}
		}
		return responseInd;
	}

	//Each Media Type Object provides schema for the media type
	//identified by its key
	private Individual parseMediaTypeObject(OntModel ontModel, String mediaTypeName, MediaType mediaTypeObject,
			Map<String, Schema> schemas) {
		Individual mediatypeInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.MediaTypeClassURI));
		//Keep mediaType name
		property_creator.AddMediaTypeName(mediatypeInd, mediaTypeName);
		if(mediaTypeObject.getEncoding()!=null) {
			//Extract a map between a property name and its encoding information.
			for(Entry<String, Encoding>  encodingEntry : mediaTypeObject.getEncoding().entrySet()){
				Individual encodingInd = parseEncodingObject(ontModel, encodingEntry.getKey(), encodingEntry.getValue(),schemas);
				ontModel.add(ontModel.createStatement(mediatypeInd, ontModel.getProperty(OpenApiOntUtils.encodingURI),encodingInd));
			}
		}
		if(mediaTypeObject.getSchema()!=null){
			//Extract schema defining the content of the request, response, or parameter.
			Individual schemaInd=parseSchemaObject(ontModel,null, null, mediaTypeObject.getSchema(),schemas);
			ontModel.add(ontModel.createStatement(mediatypeInd, ontModel.getProperty(OpenApiOntUtils.schemaURI),schemaInd));
		}
		return mediatypeInd;	
	}


	private Individual parseEncodingObject(OntModel ontModel, String encodingName, Encoding encodingObject, Map<String, Schema> schemas) {
		Individual encodingInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.EncodingClassURI));
		property_creator.AddPropertyName(encodingInd,encodingName);
		//Extract Content-Type for encoding a specific property.
		property_creator.AddContentType(encodingInd, encodingObject.getContentType());
		//Extract explode property
		property_creator.AddExplode(encodingInd, String.valueOf(encodingObject.getExplode()));
		//Extract whether the parameter value SHOULD allow reserved characters
		property_creator.AddAllowReserved(encodingInd, String.valueOf(encodingObject.getAllowReserved()));
		//Extract a map allowing additional information to be provided as headers

		if(encodingObject.getHeaders()!=null) {
			for(Entry<String,Header> headerEntry : encodingObject.getHeaders().entrySet()){
				Individual headerInd=parseHeaderObject(ontModel, headerEntry.getKey(), headerEntry.getValue(),schemas);
				ontModel.add(ontModel.createStatement(encodingInd, ontModel.getProperty(OpenApiOntUtils.encodingHeaderURI),headerInd));
			}
		}
		return encodingInd;
	}

	//Extract a single request body
	private Individual parseRequestBodyObject(OntModel ontModel, RequestBody requestBody, Map<String, Schema> schemas, OpenAPI openapi) {

		Individual requestInd = null;


		// Map if $ref != null.
		if (requestBody.get$ref() != null) {
			// Get reference name.
			String objectName = RefUtils.computeDefinitionName(requestBody.get$ref());

			// Get referenced object if exists in components.
			ResolverFully resolver = new ResolverFully();
			resolver.resolveFully(openapi);
			requestBody = resolver.resolveRequestBody(requestBody);
		}

		requestInd  = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.RequestBodyClassURI));
		property_creator.AddDescription(requestInd, requestBody.getDescription());
		//Extract if the request body is required in the request.
		property_creator.AddRequired(requestInd, String.valueOf(requestBody.getRequired()));
		//Extract contents of the request body
		for(Entry<String, MediaType> mediaTypeEntry : requestBody.getContent().entrySet()){
			Individual mediaInd=parseMediaTypeObject(ontModel,mediaTypeEntry.getKey(),mediaTypeEntry.getValue(),schemas);
			ontModel.add(ontModel.createStatement(requestInd, ontModel.getProperty(OpenApiOntUtils.contentURI),mediaInd));
		}

		return requestInd;
	}

	private Individual getMethodIndividual(OntModel ontModel, SOAS3.SOAS3.EnumerationsClass.Operation operationMethod) {
		String method_uri = null;
		if(operationMethod==EnumerationsClass.Operation.PUT)
		{
			method_uri=OpenApiOntUtils.putURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.POST)
		{
			method_uri=OpenApiOntUtils.postURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.HEAD)
		{
			method_uri=OpenApiOntUtils.headURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.PATCH)
		{
			method_uri=OpenApiOntUtils.patchURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.OPTIONS)
		{
			method_uri=OpenApiOntUtils.optionsURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.GET)
		{
			method_uri=OpenApiOntUtils.getURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.DELETE)
		{
			method_uri=OpenApiOntUtils.deleteURI;
		}
		else if(operationMethod==EnumerationsClass.Operation.TRACE)
		{
			method_uri=OpenApiOntUtils.traceURI;
		}
		else
		{
			method_uri="unknown";	
		}

		return ontModel.getIndividual(method_uri);

	}

	private Individual getStyleIndividual(OntModel ontModel, StyleEnum styleEnum){

		String style_uri = null;
		switch (styleEnum) 
		{ 
		case FORM : 
			style_uri= OpenApiOntUtils.form;
			break; 
		case MATRIX: 
			style_uri= OpenApiOntUtils.matrixURI;
			break; 
		case DEEPOBJECT: 
			style_uri=OpenApiOntUtils.deepObjectURI;
			break;
		case LABEL: 
			style_uri= OpenApiOntUtils.labelURI;
			break; 
		case SIMPLE:
			style_uri =OpenApiOntUtils.simpleURI;
			break;
		case SPACEDELIMITED:
			style_uri= OpenApiOntUtils.spaceDelimitedURI;
			break;
		case PIPEDELIMITED:
			style_uri= OpenApiOntUtils.pipeDelimitedURI;
			break;
		default: 
			break; 
		}
		return ontModel.getIndividual(style_uri);
	}

	private Individual parsePathObject(OntModel ontModel, String pathName) {
		Individual pathIndividual= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.PathClassURI));
		property_creator.AddPathName(pathIndividual,pathName);
		return pathIndividual;

	}
	// Extract metadata object that allows more fine-tuned
	// XML model definitions.

	private Individual parseXmlObject(OntModel ontModel,XML xml){
		Individual xmlInd = ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.xmlClassURI));
		// Extract the name of the element/attribute
		// used for the described schema property.
		property_creator.AddXMLName(xmlInd, xml.getName());
		property_creator.AddNameSpace(xmlInd, xml.getNamespace());
		property_creator.AddPrefix(xmlInd, xml.getPrefix());
		//Extract whether the property definition translates
		//to an attribute instead of an element.
		property_creator.AddAttribute(xmlInd, String.valueOf(xml.getAttribute()));
		//Extract whether the array is wrapped
		property_creator.AddWrapped(xmlInd, String.valueOf(xml.getWrapped()));
		return xmlInd;
	}
	//Extract external resource for extended documentation.
	private Individual parseExternalDocObject(OntModel ontModel, ExternalDocumentation externalDocs) {
		Individual exDocInd= ontModel.createIndividual("service_externalDoc", ontModel.getOntClass(OpenApiOntUtils.ExternalDocClassURI));
		property_creator.AddURL(exDocInd, externalDocs.getUrl());
		property_creator.AddDescription(exDocInd,externalDocs.getDescription());
		return exDocInd;
	}

	private Individual parseReferenceObject(OntModel ontModel){
		return null;
	}

	//Extract metadata to a single tag that is used by the Operation Object.
	private Entry<String, Individual> parseTagObject(OntModel ontModel, Tag tag, Map<String, Schema> schemas) {
		// used for findTagIndividual(OntModel model, name_of_Tag) -> ontModel.getIndividual(name_of_Tag)
		Individual tagInd =  ontModel.createIndividual(tag.getName(),ontModel.getOntClass(OpenApiOntUtils.TagClassURI));
		property_creator.AddName(tagInd, tag.getName());
		property_creator.AddDescription(tagInd, tag.getDescription());
		if(tag.getExternalDocs()!=null){
			Individual exdocInd=parseExternalDocObject(ontModel,tag.getExternalDocs());
			ontModel.add(ontModel.createStatement(tagInd, ontModel.getProperty(OpenApiOntUtils.externalDocURI),exdocInd));
		}
		//x-onResourse
		Individual schemaInd=null;
		if(tag.getExtensions()!=null){
			//get schema that tag object indicates
			Object xOnResourseExtension= tag.getExtensions().get("x-OnResource");
			if(xOnResourseExtension!=null){
				// extractSchemaName(#/components/schema/Pet)->{Pet}
				String schemaName= extractSchemaName(xOnResourseExtension.toString());
				//create that schema Object
				schemaInd=parseSchemaObject(ontModel,schemaName, null, schemas.get(schemaName),schemas);
			}
		}
		//return tag  with the corresponding schema
		return new java.util.AbstractMap.SimpleEntry<String,Individual>(tag.getName(),schemaInd);
	}

	private String extractSchemaName(String xOnResourseExtension) {
		String [] listOfComponents = xOnResourseExtension.split("/");
		String [] shape_name = listOfComponents[listOfComponents.length-1].split("\\.");
		if(shape_name.length>1)
			return shape_name[0];
		else
			return listOfComponents[listOfComponents.length-1];
	}

	/*The Schema Object allows the definition of input and output data types.
	These types can be objects, but also primitives and arrays.
	 */
	private Individual parseSchemaObject(OntModel ontModel, String schemaName, String composedParent, Schema schemaObject,
			Map<String, Schema> schemas) {

		Individual shapeInd = null;

		if(schemaObject.get$ref()!=null){
			//If schema is already defined in , retrieve it from its name.
			schemaName=extractSchemaName(schemaObject.get$ref());
			schemaObject=schemas.get(schemaName);
		}

		shapeInd = findShapeIndividual(ontModel, schemaName+"NodeShape");


		// Check for composed schema
		if (schemaObject.getClass().toString().endsWith("ComposedSchema") && shapeInd == null) {
			// Create conditions to loop.
			ComposedSchema cObj = new ComposedSchema();
			cObj = (ComposedSchema) schemaObject;

			// Create basic nodeshape
			// Will be used as a base for all three types.
			shapeInd = createNodeShape(ontModel, schemaName, null, schemaObject, schemas);

			// Three types
			if (cObj.getAllOf() != null) {
				List <Schema> allOfs = cObj.getAllOf();
				RDFList rdfList = complexSchema(ontModel, schemaName, allOfs, true, schemas);

				// Add list to nodeShape
				ontModel.add(ontModel.createStatement(shapeInd, ontModel.getProperty(OpenApiOntUtils.andURI), rdfList));
			} else if (cObj.getAnyOf() != null) {
				List <Schema> anyOfs = cObj.getAnyOf();
				RDFList rdfList = complexSchema(ontModel, schemaName, anyOfs, false, schemas);

				// Add list to nodeShape
				ontModel.add(ontModel.createStatement(shapeInd, ontModel.getProperty(OpenApiOntUtils.orURI), rdfList));

			} else if (cObj.getOneOf() != null) {
				List <Schema> oneOfs = cObj.getOneOf();
				RDFList rdfList = complexSchema(ontModel, schemaName, oneOfs, false, schemas);
				
				// Add list to nodeShape
				ontModel.add(ontModel.createStatement(shapeInd, ontModel.getProperty(OpenApiOntUtils.xoneURI), rdfList));
			}

		}

		// Den tha mpei giati to exw arxikopoihsei apo to allof
		if (shapeInd == null) {
			//Else create a new Schema individual

			if (schemaObject.getType().equals("object")) {
				shapeInd = createNodeShape(ontModel, schemaName, composedParent, schemaObject,schemas);
			}
			else if (schemaObject.getType().equals("array") && schemaObject.getExtensions()==null) {
				shapeInd =createCollectionNodeShape(ontModel, schemaName, schemaObject, schemas);
			}
			//integer, string, number ,boolean
			else {
				shapeInd = createPropertyShape(ontModel, null, schemaName, schemaObject, schemas);
			}
		}

		return shapeInd;
	}

	private RDFList complexSchema(OntModel ontModel, String schemaName, List<Schema> components, Boolean all, Map<String, Schema> schemas){
		RDFList rdfList = ontModel.createList();
		Individual listComponent = null;

		for (Schema subject : components) {
			listComponent = parseSchemaObject(ontModel, null, schemaName, subject, schemas);

			if (subject.get$ref() != null && all){
				createInheritance(ontModel, extractSchemaName(subject.get$ref()), schemaName, schemas);
			}

			//Add to list
			rdfList = rdfList.cons(listComponent);
			listComponent = null;
		}

		return rdfList;
	}


	private String inherit(OntModel ontModel, String subject, Map<String, Schema> schemas){

		Schema subjectSchema = schemas.get(subject);
		if (subjectSchema.getExtensions() == null){
			return subject;
		} else {
			if (subjectSchema.getExtensions().get("x-refersTo") != null) {
				// x-refesTo
				if (subjectSchema.getExtensions().get("x-refersTo").equals("none")) {
					return null;
				} else {
					// x-refersTo: none
					return subjectSchema.getExtensions().get("x-refersTo").toString();
				}
			} else if (subjectSchema.getExtensions().get("x-kindOf") != null) {
				// x-kindOf
				return subjectSchema.getExtensions().get("x-kindOf").toString();

			} else if (subjectSchema.getExtensions().get("x-mapsTo") != null) {
				// x-mapsTo
				String mappedSchema = extractSchemaName(subjectSchema.getExtensions().get("x-mapsTo").toString());
				Individual mappedInd = findShapeIndividual(ontModel, mappedSchema + "NodeShape");
				if (mappedInd != null) {
					return mappedInd.getPropertyResourceValue(ontModel.getProperty(OpenApiOntUtils.targetClassURI)).getURI();
				}
			}
		}

		return null;
	}

	private void createInheritance(OntModel ontModel, String subjectName, String schemaName, Map<String, Schema> schemas){
		Schema superModel = schemas.get(subjectName);
		Schema subModel = schemas.get(schemaName);
		String toSubClass = inherit(ontModel, schemaName, schemas);
		String toSuperClass = inherit(ontModel, subjectName, schemas);

		if(toSubClass != null && toSuperClass != null){
			OntClass newSubClass = ontModel.createClass(toSubClass);
			newSubClass.addSuperClass(ResourceFactory.createResource(toSuperClass));
		}
	}

	private boolean semanticValidation(Schema schemaObject, Map<String, Schema> schemas) {

		Boolean propertyAnnotation;

		if(schemaObject.getExtensions() == null){
			propertyAnnotation = false;
		} else {
			propertyAnnotation = true;
		}

		List<Boolean> subAnnotation = new ArrayList<Boolean>();

		if(schemaObject.getClass().toString().endsWith("ComposedSchema")){
			ComposedSchema cObj = new ComposedSchema();
			cObj = (ComposedSchema) schemaObject;

			if (cObj.getOneOf() != null){
				List<Schema> oneOfs = cObj.getOneOf();

				// For every schema in property
				for(Schema subject : oneOfs){
					// Check for annotations
					if(subject.getExtensions() != null){
						// If annotation exists true
						subAnnotation.add(true);
					} else {
						// If annotation does not exists investigate
						// Check if annotation is missing because of type
						if (subject.get$ref() != null){
							String[] parts = subject.get$ref().split("/");
							subject = schemas.get(parts[parts.length-1]);
							if (subject.getType().equals("object")){
								subAnnotation.add(false);
							} else {
								if(subject.getExtensions() == null)
									subAnnotation.add(false);
								else
									subAnnotation.add(true);
							}
						} else
							subAnnotation.add(false);
					}
				}

			} else if (cObj.getAnyOf() != null){
				List<Schema> anyOfs = cObj.getAnyOf();

				// For every schema in property
				for(Schema subject : anyOfs){
					// Check for annotations
					if(subject.getExtensions() != null){
						// If annotation exists true
						subAnnotation.add(true);
					} else {
						// If annotation does not exists investigate
						// Check if annotation is missing because of type
						if (subject.get$ref() != null){
							String[] parts = subject.get$ref().split("/");
							subject = schemas.get(parts[parts.length-1]);
							if (subject.getType().equals("object")){
								subAnnotation.add(false);
							} else {
								if(subject.getExtensions() == null)
									subAnnotation.add(false);
								else
									subAnnotation.add(true);
							}
						} else
							subAnnotation.add(false);
					}

				}

			}

			//System.out.println(Arrays.toString(subAnnotation.toArray()));

			// System Exits
			// if not ALL of the subproperties contain annotations
			// if both head property and sub properties contain annotations
			if((subAnnotation.contains(true) && subAnnotation.contains(false)) || (propertyAnnotation == true && subAnnotation.contains(true))){
				// If properties do not agree
				System.out.println("System exits dew to semantic malfunction");
				System.exit(1);
			}

			// Return true means sh:path property (=> pathInit) is going to be initialized later in composed schema
			return subAnnotation.contains(true);

		} else if (schemaObject.get$ref() != null){

			// It is handled from createNodeShape in embedded ref property
			return true;
		}

		// Not really necessary
		return subAnnotation.contains(true);
	}



	private Schema findSchema(String refString, Map<String, Schema> schemas){
		String[] parts = refString.split("/");
		return schemas.get(parts[parts.length-1]);
	}


	private Individual createPropertyShape(OntModel ontModel, String ownerName, String schemaName, Schema schemaObject,
			Map<String, Schema> schemas) {

		String oldSchemaName = null;
		String originalName = null;
		Boolean pathInit = null;


		if(schemaName!=null){
			originalName = schemaName;
			oldSchemaName = schemaName;
			schemaName=schemaName+"PropertyShape";
		}

		if(ownerName != null){
			oldSchemaName=ownerName+"_"+oldSchemaName;
			schemaName=ownerName+"_"+schemaName;
		}

		// Semantic validation
		pathInit  = semanticValidation(schemaObject, schemas);

		Individual propertyShapeInd =  ontModel.createIndividual(schemaName,ontModel.getOntClass(OpenApiOntUtils.PropertyShapeClassURI));
		property_creator.AddSchemaLabel(propertyShapeInd, schemaName);

		//handle semantics x-refersTo, x-kindOf, x-mapsTo
		Resource propertyUri;
		if(schemaObject.getExtensions()!=null) {
			if (schemaObject.getExtensions().get("x-refersTo")!=null && !schemaObject.getExtensions().get("x-refersTo").equals("none")) {
				//Get property uri that x-refersTo indicates.
				propertyUri = ResourceFactory.createResource(schemaObject.getExtensions().get("x-refersTo").toString());
				ontModel.createOntProperty(schemaObject.getExtensions().get("x-refersTo").toString());
				ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI), propertyUri));
			}
			else if (schemaObject.getExtensions().get("x-kindOf")!=null) {
				//Get property uri that x-kindOf indicates.
				String uri = schemaObject.getExtensions().get("x-kindOf").toString();
				//create a property with name "schemaName"
				Property property = ontModel.createOntProperty(oldSchemaName);
				//Set x-kindOf url as SuperProperty of our property
				property.addProperty( RDFS.subPropertyOf, ontModel.createOntProperty(uri)
                );
				//Get uri of the new property
				propertyUri = ResourceFactory.createResource(property.getURI());
				// create rdf property of referred uri
				ontModel.createOntProperty(uri);
				// add to path
				ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI), propertyUri));
			}
			else if (schemaObject.getExtensions().get("x-mapsTo")!=null) {
				//Get property that x-mapsTo indicates.
				String mappedSchemaProperty = schemaObject.getExtensions().get("x-mapsTo").toString();
				//Extract NodeShape schema individual that mappedSchemaProperty belongs to.
				String mappedSchema = extractSchemaName(mappedSchemaProperty);
				//Check if that schema is already defined in ontology
				Individual mappedShapeInd = findShapeIndividual(ontModel, mappedSchema);
				//If is not already defined, create a NodeShape individual
				if (mappedShapeInd == null) {
					//Get schema object from componentSchemas with name "mappedSchema"
					Schema mappedSchemaEntry = schemas.get(mappedSchema);
					mappedShapeInd = parseSchemaObject(ontModel, mappedSchema, null, mappedSchemaEntry, schemas);
				}
				//After extraction of NodeShape name, extract also the mapped property name
				String mappedProperty = extractPropertyName(mappedSchemaProperty);
				if (mappedProperty == null) {
					//If there is no mapped property name, copy the path value of mappedShapeInd
					Resource propertyValue=mappedShapeInd.getPropertyResourceValue(ontModel.getProperty(OpenApiOntUtils.pathURI));
					if(propertyValue == null){
						// If mappedShapeInd is of a NodeShape it wont have path but targetClass
						propertyValue=mappedShapeInd.getPropertyResourceValue(ontModel.getProperty(OpenApiOntUtils.targetClassURI));
					}
					ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI),propertyValue));
				}
				else {
					//Else locate x-mapsTo property in ontology
					Individual mappedPropertyShape = findShapeIndividual(ontModel,mappedSchema+"_"+mappedProperty+"PropertyShape");
					//copy the path value of mappedPropertyShape
					Resource propertyValue=mappedPropertyShape.getPropertyResourceValue(ontModel.getProperty(OpenApiOntUtils.pathURI));
					ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI),propertyValue));
				}
			}
		} else if (oldSchemaName != null && !pathInit){
			Resource propertyValue = ResourceFactory.createProperty(oldSchemaName);
			ontModel.createOntProperty(oldSchemaName);
			ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI), propertyValue));
		}


		// Embedded composedSchema in properties
		if (schemaObject.getType() == null ){
			if(schemaObject.getClass().toString().endsWith("ComposedSchema")) {

				ComposedSchema cObj = new ComposedSchema();
				cObj = (ComposedSchema) schemaObject;

				if (cObj.getOneOf() != null) {
					List<Schema> oneOfs = cObj.getOneOf();
					RDFList rdfList = ontModel.createList();

					Individual listComponent = null;
					for (Schema subject : oneOfs) {
						listComponent = parseSchemaObject(ontModel, null, null, subject, schemas);


						// If nodeshape needs calibrate
						if (subject.get$ref() != null) {
							Schema tmpSchema = findSchema(subject.get$ref(), schemas);
							if (tmpSchema.getType() != null && tmpSchema.getType().equals("object")){
								Individual refInd = ontModel.createIndividual(null, ontModel.getOntClass(OpenApiOntUtils.PropertyShapeClassURI));
								ontModel.add(ontModel.createStatement(refInd, ontModel.getProperty(OpenApiOntUtils.nodeURI), listComponent));
								listComponent = refInd;
							}
						}

						//Add to list
						rdfList = rdfList.cons(listComponent);
						listComponent = null;
					}

					// Add list to nodeShape
					ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.xoneURI), rdfList));

				} else if (cObj.getAnyOf() != null) {
					List<Schema> anyOfs = cObj.getAnyOf();
					RDFList rdfList = ontModel.createList();

					Individual listComponent = null;
					for (Schema subject : anyOfs) {
						listComponent = parseSchemaObject(ontModel, null, null, subject, schemas);

						if (subject.get$ref() != null) {
							Schema tmpSchema = findSchema(subject.get$ref(), schemas);
							if (tmpSchema.getType() != null && tmpSchema.getType().equals("object")){
								Individual refInd = ontModel.createIndividual(null, ontModel.getOntClass(OpenApiOntUtils.PropertyShapeClassURI));
								ontModel.add(ontModel.createStatement(refInd, ontModel.getProperty(OpenApiOntUtils.nodeURI), listComponent));
								listComponent = refInd;
							}
						}

						//Add to list
						rdfList = rdfList.cons(listComponent);
						listComponent = null;
					}

					// Add list to nodeShape
					ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.orURI), rdfList));

				}
			} else if(schemaObject.getNot() != null){
				Schema notSchema = schemaObject.getNot();

				// not property cannot have
				Map<String, String> not = new HashMap<>();

				not.put("x-refersTo", null);
				not.put("x-kindOf", null);
				not.put("x-mapsTo", null);

				notSchema.setExtensions(not);

				Individual component = parseSchemaObject(ontModel, null, null, notSchema, schemas);
				ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.notURI), component));
			}
		} else {
			if (schemaObject.getType().equals("array")) {
				ArraySchema arraySchema = (ArraySchema) schemaObject;
				//Extract items of array
				Schema itemsObject = arraySchema.getItems();
				Individual shapeInd = null;
				if (itemsObject.get$ref() != null) {
					schemaName = extractSchemaName(itemsObject.get$ref());
					itemsObject = schemas.get(schemaName);
				}
				shapeInd = findShapeIndividual(ontModel, schemaName + "NodeShape");
				// Prediction pe	itemsObject.getType() != null
				if (shapeInd == null && itemsObject.getType() != null) {
					//create a NodeShape individual for each item of the array
					if (itemsObject.getType().equals("object")) {
						Individual itemsNodeShape = createNodeShape(ontModel, schemaName, null, itemsObject, schemas);
						ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.nodeURI), itemsNodeShape));
					} else {
						//if items are not object type, keep only the datatype of items
						Resource resource = getDatatype(ontModel, itemsObject.getType(), itemsObject.getFormat());
						if (resource != null) {
							ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.datatypeURI), resource));
						}
					}
				}
				property_creator.AddMinItems(propertyShapeInd, String.valueOf(arraySchema.getMinItems()));
				property_creator.AddMaxItems(propertyShapeInd, String.valueOf(arraySchema.getMaxItems()));
			} else if (schemaObject.getType().equals("object")) {
				Individual nodeShape = createNodeShape(ontModel, schemaName, null, schemaObject, schemas);
				ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.nodeURI), nodeShape));
			} else {
				//Keep only the datatype
				Resource resource = getDatatype(ontModel, schemaObject.getType(), schemaObject.getFormat());
				ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.datatypeURI), resource));
			}
		}
		property_creator.AddDescription(propertyShapeInd, schemaObject.getDescription());
		property_creator.AddtTitle(propertyShapeInd, schemaObject.getTitle());
		property_creator.AddExclusiveMaximum(propertyShapeInd, String.valueOf(schemaObject.getExclusiveMaximum()));
		property_creator.AddExclusiveMinimum(propertyShapeInd, String.valueOf(schemaObject.getExclusiveMinimum()));
		property_creator.AddMaxLength(propertyShapeInd,String.valueOf(schemaObject.getMaxLength()));
		property_creator.AddMinLength(propertyShapeInd, String.valueOf(schemaObject.getMinLength()));
		property_creator.AddPattern(propertyShapeInd,String.valueOf(schemaObject.getPattern()));
		property_creator.AddMinItems(propertyShapeInd, String.valueOf(schemaObject.getMinItems()));
		property_creator.AddUniqueItems(propertyShapeInd, String.valueOf(schemaObject.getUniqueItems()));
		//Extract External Docs
		if(schemaObject.getExternalDocs()!=null){
			Individual exDocInd = parseExternalDocObject(ontModel, schemaObject.getExternalDocs());
			Property property=ontModel.getProperty(OpenApiOntUtils.externalDocURI);
			Statement st= ontModel.createStatement(propertyShapeInd,property,exDocInd);
			ontModel.add(st);
		}
		//Extract XML
		if(schemaObject.getXml()!=null){
			Individual xmlInd = parseXmlObject(ontModel, schemaObject.getXml());
			Property property=ontModel.getProperty(OpenApiOntUtils.xmlURI);
			Statement st= ontModel.createStatement(propertyShapeInd,property,xmlInd);
			ontModel.add(st);
		}
		return propertyShapeInd;
	}


	private Resource getDatatype(OntModel ontModel, String type, String format) {
		if (type.equals("string") && format==null) return XSD.xstring; 
		else if (type.equals("boolean") && format==null) return XSD.xboolean;
		else if (type.equals("integer") && format==null) return XSD.xint;
		else if (type.equals("number") && format==null) return XSD.decimal;
		else if(type.equals("integer") && format.equals("int32")) return XSD.xint;
		else if (type.equals("integer") &&	format.equals("int64")) return 	XSD.xlong;
		else if (type.equals("number") && format.equals("float"))	return XSD.xfloat;
		else if (type.equals("number")	&& format.equals("double")) return XSD.xdouble;
		else if (type.equals("string") && format.equals("byte")) return  XSD.base64Binary;
		else if (type.equals("string")	&& format.equals("binary")) return ontModel.getProperty(OpenApiOntUtils.BinaryURI);
		else if (type.equals("string") && format.equals("date")) return	XSD.date;
		else if (type.equals("string") && format.equals("date-time")) return	XSD.dateTime;
		else if (type.equals("string") && format.equals("uriref")) return	XSD.anyURI;
		return null;
	}



	private String extractPropertyName(String mappedSchemaProperty) {
		String [] listOfComponents = mappedSchemaProperty.split("/");
		String [] shape_name = listOfComponents[listOfComponents.length-1].split("\\.");
		if (shape_name.length > 1)
			return shape_name[1];
		else
			return null;
	}

	private Individual createCollectionNodeShape(OntModel ontModel, String schemaName, Schema schemaObject,
			Map<String, Schema> schemas) {
		String oldSchemaName = null;
		if(schemaName!=null){
			oldSchemaName = schemaName;
			schemaName=schemaName+"CollectionNodeShape";
		}
		Individual nodeShapeInd = ontModel.createIndividual(schemaName,ontModel.getOntClass(OpenApiOntUtils.NodeShapeClassURI));
		property_creator.AddSchemaLabel(nodeShapeInd, schemaName);
		//create a new class with name "schemaName"
//		OntClass newClass = ontModel.createClass(schemaName+"Class");
		OntClass newClass = ontModel.createClass(oldSchemaName);
		//Set collection as superClass
		newClass.addSuperClass(ontModel.getOntClass(OpenApiOntUtils.CollectionClassURI));
		Resource classURI= ResourceFactory.createResource(newClass.getURI());
		//Extract targetClass value of nodeShapeInd
		ontModel.add(ontModel.createStatement(nodeShapeInd, ontModel.getProperty(OpenApiOntUtils.targetClassURI), classURI));
		//create a property shape individual for the schemaObject
		Individual memberInd = createPropertyShape(ontModel, null, null, schemaObject, schemas);
		//Make this property shape a member of collection
		ontModel.add(ontModel.createStatement(memberInd, ontModel.getProperty(OpenApiOntUtils.pathURI),ontModel.getProperty(OpenApiOntUtils.memberURI)));
		return nodeShapeInd;
	}

	//It's called when schema is an object 

	private Individual createNodeShape(OntModel ontModel, String schemaName, String composedParent, Schema schemaObject,
			Map<String, Schema> schemas) {

		String oldSchemaName = null;
		if(schemaName!=null){
			oldSchemaName = schemaName;
			schemaName=schemaName+"NodeShape";
		}

		Individual nodeShapeInd =  ontModel.createIndividual(schemaName,ontModel.getOntClass(OpenApiOntUtils.NodeShapeClassURI));
		property_creator.AddSchemaLabel(nodeShapeInd, schemaName);
		//handle semantics x-refersTo, x-kindOf, x-mapsTo
		Resource classUri =null;
		String collectionMember = null; // assigned when x-collectionTo is used
		if(schemaObject.getExtensions()!=null) {
			if (schemaObject.getExtensions().get("x-refersTo")!=null && !schemaObject.getExtensions().get("x-refersTo").equals("none")) {
				//Extract x-refersTo class uri
				OntClass newClass = ontModel.createClass(schemaObject.getExtensions().get("x-refersTo").toString());
				classUri = ResourceFactory.createResource(schemaObject.getExtensions().get("x-refersTo").toString());
			}
			else if (schemaObject.getExtensions().get("x-kindOf")!=null) {
				//Extract x-kindof class uri
				String uri = schemaObject.getExtensions().get("x-kindOf").toString();
				// create class with the given uri
				ontModel.createClass(uri);
				//create class with name "schemaName"
				//OntClass newClass = ontModel.createClass(schemaName+"Class");
				OntClass newClass = ontModel.createClass(oldSchemaName);
				//Set x-kindOf class as superClass
				newClass.addSuperClass(ResourceFactory.createResource(uri));
				//get uri of our new class
				classUri = ResourceFactory.createResource(newClass.getURI());
			}
			else if (schemaObject.getExtensions().get("x-mapsTo")!=null) {
				//Extract x-mapsTo class uri
				String mappedSchemaString = schemaObject.getExtensions().get("x-mapsTo").toString();
				// Extract schema name
				String mappedSchemaName = extractSchemaName(mappedSchemaString);
				//Get component schema with name "mappedSchemaName"
				Schema mappedSchemaEntry = schemas.get(mappedSchemaName);
				//Check if that schema is already defined in ontology
				Individual mappedShapeInd = findShapeIndividual(ontModel, mappedSchemaName + "NodeShape");
				if (mappedShapeInd == null) {
					//Else create individual for that schema object
					mappedShapeInd = createNodeShape(ontModel, mappedSchemaName, null, mappedSchemaEntry, schemas);
				}
				//Extract class uri that targetClass points to.
				classUri = mappedShapeInd.getPropertyResourceValue(ontModel.getProperty(OpenApiOntUtils.targetClassURI));
			}
			else if (schemaObject.getExtensions().get("x-collectionTo")!=null) {
				//create a class with name "schemaName"
				//OntClass newClass = ontModel.createClass(schemaName+"Class");
				OntClass newClass = ontModel.createClass(oldSchemaName);
				//Set Collection as superClass of our class
				newClass.addSuperClass(ontModel.getOntClass(OpenApiOntUtils.CollectionClassURI));
				//Extract the object-member of collection
				collectionMember = schemaObject.getExtensions().get("x-collectionTo").toString();
				//Get uri of our class
				classUri = ResourceFactory.createResource(newClass.getURI());
			}

			//In case of x-refersTo: none nothing will happen
			if(classUri!=null ){
				ontModel.createClass(classUri.getURI());
				ontModel.add(ontModel.createStatement(nodeShapeInd, ontModel.getProperty(OpenApiOntUtils.targetClassURI), classUri));
			}
		} else if (oldSchemaName != null){
			// Create targetClass if SOAS annotations does not exist.
			OntClass newClass = ontModel.createClass(oldSchemaName);
			classUri = ResourceFactory.createResource(newClass.getURI());
			ontModel.add(ontModel.createStatement(nodeShapeInd, ontModel.getProperty(OpenApiOntUtils.targetClassURI), classUri));
		}
		Map<String,Schema> propertySchemas=schemaObject.getProperties();
		//Extract all properties of nodeShape
		if(propertySchemas!=null){
			for ( Map.Entry<String,Schema>  propertyEntry : propertySchemas.entrySet()) {
				Individual propertyShapeInd = null;
				if(propertyEntry.getValue().get$ref() != null){
					// Embedded ref in property
					propertyShapeInd = parseSchemaObject(ontModel, null, null, propertyEntry.getValue(), schemas);
				}
				else if (oldSchemaName == null && composedParent != null) {
					propertyShapeInd = createPropertyShape(ontModel, composedParent, propertyEntry.getKey(), propertyEntry.getValue(), schemas);
				} else if (oldSchemaName != null) {
					propertyShapeInd = createPropertyShape(ontModel, oldSchemaName, propertyEntry.getKey(), propertyEntry.getValue(), schemas);
				} else {
					propertyShapeInd = createPropertyShape(ontModel, null, propertyEntry.getKey(), propertyEntry.getValue(), schemas);
				}
				//Save property name
				property_creator.AddName(propertyShapeInd, propertyEntry.getKey());
				//If collection member is same with propertyName set property path
				//of the property shape to "member"
				if (propertyEntry.getKey().equals(collectionMember)) {
					ontModel.add(ontModel.createStatement(propertyShapeInd, ontModel.getProperty(OpenApiOntUtils.pathURI), ontModel.getProperty(OpenApiOntUtils.memberURI)));
				}
				if(propertyShapeInd != null){
					ontModel.add(ontModel.createStatement(nodeShapeInd, ontModel.getProperty(OpenApiOntUtils.propertyURI), propertyShapeInd));
				}
			}
		}
		//Extract a short description
		property_creator.AddDescription(nodeShapeInd, schemaObject.getDescription());
		return nodeShapeInd;
	}

	private Individual findShapeIndividual(OntModel ontModel, String schemaName) {
		return ontModel.getIndividual(schemaName);
	}

	//An object representing a Server.
	private Individual parseServerObject(OntModel ontModel, Server serverObject) {
		Individual serverInd = ontModel.createIndividual("service_server", ontModel.getOntClass(OpenApiOntUtils.ServerClassURI));
		//Extract server's url, description
		property_creator.AddHost(serverInd, serverObject.getUrl());
		property_creator.AddDescription(serverInd, serverObject.getDescription());
		//ServerVariable object
		if(serverObject.getVariables()!=null){
			//Extract server URL template substitution
			for (Entry<String, ServerVariable> serverVariableEntry: serverObject.getVariables().entrySet()){
				Individual serverVariableInd =  ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ServerVariableClassURI));
				ServerVariable sv=serverVariableEntry.getValue();
				property_creator.AddName(serverVariableInd, serverVariableEntry.getKey());
				//Extract enumeration of  values that will be used if
				// the substitution options are from a limited set.
				for (String serverEnum :sv.getEnum()){
					property_creator.AddEnum(serverVariableInd, serverEnum);
				}
				//Extract default value to use for substitution, which
				//SHALL be sent if an alternate value is not supplied.
				property_creator.AddDefault(serverVariableInd, sv.getDefault());
				property_creator.AddDescription(serverVariableInd, sv.getDescription());
				//Store server variable objects
				ontModel.add(ontModel.createStatement(serverInd, ontModel.getProperty(OpenApiOntUtils.variableURI),serverVariableInd));
			}
		}
		return serverInd;
	}

	/*
	 *Lists the required security schemes to execute this operation.
	 *The name used for each property MUST correspond to a security
	 *scheme declared in the Security Schemes under the Components Object.
	 *Security Requirement Objects that contain multiple schemes require
	 *that all schemes MUST be satisfied for a request to be authorized.
	 *This enables support for scenarios where multiple query parameters
	 *or HTTP headers are required to convey security information.
	 * When a List<> of Security Requirement Objects is defined on the OpenAPI Object
	 *or Operation Object, only one of the Security Requirement Objects in the List<>
	 * needs to be satisfied to authorize the request.
	 */
	private Individual parseSecurityReqObject(OntModel ontModel, SecurityRequirement securityReqObject) {
		Individual securityreqInd = ontModel.createIndividual("service_securityReq", ontModel.getOntClass(OpenApiOntUtils.SecurityRequirementClassURI));
		//get the security scheme with specific url (name)
		Individual securitySchemeInd;
		for(Entry<String, List<String>> securityReqEntry : securityReqObject.entrySet()){
			//get security scheme with url: securitySchemeName

			securitySchemeInd = ontModel.getIndividual(securityReqEntry.getKey());
			ontModel.add(ontModel.createStatement(securityreqInd, ontModel.getProperty(OpenApiOntUtils.securityTypeURI),securitySchemeInd));
			// Scope of Security Requirements

			for (String scope:securityReqEntry.getValue()){
				Individual scopeInd=parseScopeIndividual(ontModel,scope,null);
				ontModel.add(ontModel.createStatement(securityreqInd, ontModel.getProperty(OpenApiOntUtils.scopeURI),scopeInd));
			}
		}
		return securityreqInd;
	}

	private void parseSecuritySchemeObject(OntModel ontModel, Individual documentInd, String securityName,
			SecurityScheme securityObject) {
		Type type = securityObject.getType();
		Individual securityInd = null;
		switch (type) 
		{ 
		case APIKEY : 
			securityInd= ontModel.createIndividual(securityName,ontModel.getOntClass(OpenApiOntUtils.ApiKeyClassURI));
			property_creator.AddDescription(securityInd, securityObject.getDescription());
			//Extract the name of the header, query or cookie parameter to be used.
			property_creator.AddParameterName(securityInd,securityObject.getName());
			// Extact the location of the API key
			property_creator.AddInApiKey(securityInd,securityObject.getIn().toString());
			break; 
		case HTTP: 
			securityInd= ontModel.createIndividual(securityName,ontModel.getOntClass(OpenApiOntUtils.HttpClassURI));
			property_creator.AddDescription(securityInd, securityObject.getDescription());
			//Extract the name of the HTTP Authorization scheme
			property_creator.AddScheme(securityInd,securityObject.getScheme());
			//Extract a hint to the client to identify
			// how the bearer token is formatted.
			property_creator.AddBearerFormat(securityInd,securityObject.getBearerFormat());

			break; 
		case OAUTH2: 
			securityInd= ontModel.createIndividual(securityName,ontModel.getOntClass(OpenApiOntUtils.OAuth2ClassURI));
			property_creator.AddDescription(securityInd, securityObject.getDescription());
			//Extract an object containing configuration information for
			//the flow types supported.
			Individual flowsInd= parseOAuthFlowsIndividual(ontModel, securityObject.getFlows());
			ontModel.add(ontModel.createStatement(documentInd, ontModel.getProperty(OpenApiOntUtils.flowURI),securityInd));
			break;
		case OPENIDCONNECT: 
			securityInd= ontModel.createIndividual(securityName,ontModel.getOntClass(OpenApiOntUtils.OpenIdConnectClassURI));
			property_creator.AddDescription(securityInd, securityObject.getDescription());
			//Extract OpenId Connect URL to discover OAuth2 configuration values.
			property_creator.AddOpenIdConnectURL(securityInd, securityObject.getOpenIdConnectUrl());
		default: 
			break; 
		}
		Property property=ontModel.getProperty(OpenApiOntUtils.supportedSecurityURI);
		//Save security scheme in document class
		ontModel.add(ontModel.createStatement(documentInd, property, securityInd));
	}



	private Individual parseOAuthFlowsIndividual(OntModel ontModel, OAuthFlows flows) {
		Individual oauthflowsInd=null;
		if(flows.getImplicit()!=null)
			//create Individual  Implicit  with its properties.
			oauthflowsInd =parseImplicitFlowIndividual(ontModel, flows.getImplicit());
		else if (flows.getAuthorizationCode()!=null)
			//create Individual  AuthorizationCode  with its properties.
			oauthflowsInd=parseAuthorizationCodeFlowIndividual(ontModel,flows.getAuthorizationCode());
		else if (flows.getClientCredentials()!=null)
			//Configuration for the OAuth Client Credentials flow
			oauthflowsInd=parseClientCredentialsFlowIndividuall(ontModel, flows.getClientCredentials());
		else
			//create Password Individual 
			oauthflowsInd=parsePasswordFlowIndividual(ontModel, flows.getPassword());
		return oauthflowsInd;
	}


	private Individual parseScopeIndividual(OntModel ontModel, String scopeName, String scopeDescription) {
		Individual scopeInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ScopeClassURI));
		property_creator.AddName(scopeInd, scopeName);
		return scopeInd;

	}

	private Individual parsePasswordFlowIndividual(OntModel ontModel, OAuthFlow password) {
		Individual oauthflowInd=ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.PasswordClassURI));
		property_creator.AddAuthorizationUrl(oauthflowInd, password.getAuthorizationUrl());
		property_creator.AddTokenUrl(oauthflowInd, password.getTokenUrl());
		property_creator.AddRefreshUrl(oauthflowInd,password.getRefreshUrl());
		if(password.getScopes()!=null){
			for (Entry <String, String> scopeEntry : password.getScopes().entrySet()){
				Individual scopeInd=parseScopeIndividual(ontModel,scopeEntry.getKey(), scopeEntry.getValue());
				ontModel.add(ontModel.createStatement(oauthflowInd, ontModel.getProperty(OpenApiOntUtils.scopeURI),scopeInd));
			}
		}
		return oauthflowInd;
	}

	private Individual parseClientCredentialsFlowIndividuall(OntModel ontModel, OAuthFlow clientCredentials) {
		Individual oauthflowInd=ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ClientCredentialsClassURI));
		property_creator.AddTokenUrl(oauthflowInd, clientCredentials.getTokenUrl());
		property_creator.AddRefreshUrl(oauthflowInd,clientCredentials.getRefreshUrl());
		if(clientCredentials.getScopes()!=null){
			for (Entry <String, String> scopeEntry : clientCredentials.getScopes().entrySet()){
				Individual scopeInd=parseScopeIndividual(ontModel,scopeEntry.getKey(), scopeEntry.getValue());
				ontModel.add(ontModel.createStatement(oauthflowInd, ontModel.getProperty(OpenApiOntUtils.scopeURI),scopeInd));
			}
		}
		return oauthflowInd;
	}

	private Individual parseAuthorizationCodeFlowIndividual(OntModel ontModel, OAuthFlow authorizationCode) {
		Individual oauthflowInd=ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.AuthorizationCodeClassURI));
		property_creator.AddAuthorizationUrl(oauthflowInd, authorizationCode.getAuthorizationUrl());
		property_creator.AddTokenUrl(oauthflowInd, authorizationCode.getTokenUrl());
		property_creator.AddRefreshUrl(oauthflowInd,authorizationCode.getRefreshUrl());
		if(authorizationCode.getScopes()!=null){
			for (Entry <String, String> scopeEntry : authorizationCode.getScopes().entrySet()){
				Individual scopeInd=parseScopeIndividual(ontModel,scopeEntry.getKey(), scopeEntry.getValue());
				ontModel.add(ontModel.createStatement(oauthflowInd, ontModel.getProperty(OpenApiOntUtils.scopeURI),scopeInd));
			}
		}
		return oauthflowInd;
	}

	private Individual parseImplicitFlowIndividual(OntModel ontModel, OAuthFlow implicit) {
		Individual oauthflowInd=ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.AuthorizationCodeClassURI));
		property_creator.AddAuthorizationUrl(oauthflowInd, implicit.getAuthorizationUrl());
		property_creator.AddRefreshUrl(oauthflowInd,implicit.getRefreshUrl());
		if(implicit.getScopes()!=null){
			for (Entry <String, String> scopeEntry : implicit.getScopes().entrySet()){
				Individual scopeInd=parseScopeIndividual(ontModel,scopeEntry.getKey(), scopeEntry.getValue());
				ontModel.add(ontModel.createStatement(oauthflowInd, ontModel.getProperty(OpenApiOntUtils.scopeURI),scopeInd));
			}
		}
		return oauthflowInd;
	}

	/*
	 * The object provides metadata about the API.
	 *The metadata MAY be used by the clients if needed, and MAY be presented
	 *in editing or documentation generation tools for convenience
	 */

	private void parseInfoObject(OntModel ontModel, Individual documentInd, Info info) {
		OntClass infoClass= ontModel.getOntClass(OpenApiOntUtils.InfoClass);
		//Extract the title of the application.
		Individual infoInd= ontModel.createIndividual("service_info", infoClass);
		//Extract Contact  and License information for the exposed API.
		parseLicenseObject(ontModel,infoInd,info.getLicense());
		parseContactObject(ontModel,infoInd,info.getContact());
		property_creator.AddTermsOfService(infoInd, info.getTermsOfService());
//		property_creator.AddVersion(infoInd, info.getVersion());
		property_creator.AddtTitle(infoInd, info.getTitle());
		//Extract a short description of the application
		property_creator.AddDescription(infoInd, info.getDescription());
		//Extract version of the OpenAPI document
		property_creator.AddVersion(documentInd, info.getVersion());
		//Store info individual in document Class.
		ontModel.add(ontModel.createStatement(documentInd, ontModel.getProperty(OpenApiOntUtils.infoURI), infoInd));

	}
	/*Contact information for the exposed API.*/
	private void parseContactObject(OntModel ontModel,Individual main_indi,Contact contact_object)
	{
		if(contact_object ==null ) return;

		Individual contactInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ClassContactURI));
		property_creator.AddContactName(contactInd, contact_object.getName());
		property_creator.AddEmail(contactInd,contact_object.getEmail());
		property_creator.AddURL(contactInd, contact_object.getUrl());
		ontModel.add(ontModel.createStatement(main_indi, ontModel.getProperty(OpenApiOntUtils.contactURI),contactInd));


	}

	private void parseLicenseObject(OntModel ontModel,Individual main_indi,License license) {

		if(license==null) return;

		Individual licenseInd= ontModel.createIndividual(ontModel.getOntClass(OpenApiOntUtils.ClassLicenseURI));
		property_creator.AddLicenseName(licenseInd, license.getName());
		property_creator.AddURL(licenseInd, license.getUrl());
		ontModel.add(ontModel.createStatement(main_indi,ontModel.getProperty(OpenApiOntUtils.licenseURI), licenseInd));

	}

}
