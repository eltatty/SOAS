
// openApiDoc is the openapi file
function convert2Ontology(openApiDoc)
  // initialize the open api
  ontModel : initModel()
  // createIndividual an  for Document Class
  documentInd : createIndividual("Document")
  // parse Info Object
  parseInfoObject(documentInd, openApiDoc.getInfo())
  // parse global servers
  List<>  globServerIndList
  for (serverObject in openApiDoc.getServers()) {
    serverInd : parseServerObject(serverObject)
    globServerIndList.add(serverInd)
  }
  // parse all security schemes
  securitySchemas: openApiDoc.getComponents().getSecuritySchemes()
  for (<securityName,securitySchemeObject> in securitySchemes) {
    parseSecuritySchemeObject(documentInd, securityName, securitySchemeObject)
  }
  // parse global security requirements and store them in a List<>
  List<> globSecReqIndList
  for (securityReqObject in openApiDoc.getSecurityRequirements()) {
    securityReqInd: parseSecurityReqObject( securityReqObject)
    globSecReqIndList.add(securityReqInd)
  }
  //Get component Schemas
  componentSchemas: openApiDoc.getComponents().getSchemas()

  //List<> that contains pairs of <tag , schema >
  List<> tagShapeList
  for (tag in openApiDoc.getTags()) {
     tagShapeEntry: parseTagObject(tag, componentSchemas)
     if(tagShapeEntry!=null){
        documentInd.supportedEntity: tagShapeEntry
      }
     tagShapeList.add(tagShapeEntry)
  }
  //Extract External Docs in Document level
  exDocInd:parseExternalDocObject(openApiDoc.getExternalDoc())
  documentInd.externalDoc: exDocInd

  // parse path objects into
  for (<pathName, pathItem> in openApiDoc.getPaths()) {
    //Get Path Servers
    List<> pathServerIndList
    for (serverObject in pathItem.getServers()) {
      serverInd : parseServerObject(serverObject)
      pathServerIndList.add(serverInd)
    }
    //If there are no path servers, keep global servers
    List<> finalServerList
    if(pathServerIndList.isEmpty()){
      finalServerList : globServerIndList
    }
    else {
      finalServerList : pathServerIndList
    }
    //Get all parameters that are in path-level.
    List<> parametersListFromPath:pathItem.getParameters()
    //Extract and save path Name.
    //createPathObject creates an individual of "Path"
    //with property name: "pathName"
    pathInd: createPathObject(pathName)
    //parse all operations that are in pathItem.
    for (operationObject in pathItem.getValue().getOperations()){
      parseOperationObject(documentInd, pathInd, operationObject, tagShapeList,
       parametersListFromPath,finalServerList,globSecReqIndList)
    }
end


/*********************Operation************************/
function parseOperationObject(documentInd, pathInd, operationObject, tagShapeList,
 parametersListFromPath,serverList,globSecReqIndList)

  operationInd : createIndividual( "Operation")

  //x-operationType
  //Add second parent class to operaion
  operationType : operationObject.getExtensions("x-operationType")
  if(operationType != null){
    operationInd.addOntClass(operationType)
  }
  operationInd.name: operationObject.getOperationId()
  operationInd.onPath: pathInd
  operationInd.deprecated: operationObject.getDeprecated()
  operationInd.summary: operationObject.getSummary()
  operationInd.discription: operationObject.getDescription()
  //Extract Type of Operation, could be : (Get, Post, Delete,..)
  //Method individuals are already created in ontology
  operationInd.method: getMethodΙndividual(operationObject.getMethodType())
  //Extract external doc
  exDocInd:parseExternalDocObject( operationObject.getExternalDoc())
  operationInd: exDocInd

  //Get operation's tags
  for (tag in operationObject.getTags()) {
		tagInd:null
    //Get tag individual with name "tag"
		tagShapeEntry : tagShapeList.getEntry(tag)
		if (tagShapeEntry == null) {
      //if does not exist, we create Tag Individual
			tagInd : createIndividual( "Tag")
			tagInd.name:  tag
		}
		else {
      //Search Tag with name "tag" in ontology
      //FindTag returns tag Individual and the corresponding shape
      //that comes from x-onResourse
			<tagInd, shape> : findTag(tag)
      if(shape!=null){
			   shape.supportedOperation: operationInd
       }
		}
		operationInd.tag: tagInd
	}
  //Get operation's RequestBody
  requestBodyObject: operationObject.getRequestBody()
  requestInd : parseRequestBodyObject(requestBodyObject,componentSchemas)
  operationInd.requestBody: requestInd

  //Get operation's Response
  for(<responseName, responseObject> in operationObject.getResponses()){
    responseInd:parseResponseObject(responseName,responseObject,componentSchemas)
    operationInd.response: responseInd
  }
  //Get operation's Security Requirements
  //if there are no operation's security requirements, use global ones.
  if(operationObject.getSecurityRequirements().isEmpty()){
    for (securiryReqInd in globSecReqIndList) {
      operationInd.security: securiryReqInd
    }
  }
  //else define new security requirements for this operation.
  else{
    for (securiryReqObject in operationObject.getSecurityRequirements()) {
      securityReqInd : parseSecurityReqObject(securiryReqObject)
      operationInd.security: securityReqInd)
    }
  }
  /*Parameters shared by all operations of a path can be defined on the path
  * level instead of the operation level. Path-level parameters are inherited
  * by all operations of that path.
  * If any extra parameters defined at the operation level are used together
  * with path-level parameters. Specific path-level parameters can be overridden
  * on the operation level, but cannot be removed.
  * combineParameters functions handles the aforementioned scenaria.
  */
paths:
  paramsInOperation: operationObject.getParameters()
  combinedParametersList:combineParameters(parametersListFromPath,paramsInOperation)
  for (parameterObject in combinedParametersList){
    //Parameters could be in different positions
    switch(parameterObject.getIn())
    {
      case "path":
        parameterInd:parsePathParameterObject(parameterObject,componentSchemas)
        operationInd.parameter: parameterInd
      case "query":
        queryInd:parseQueryObject(parameterObject,componentSchemas)
        operationInd.parameter: queryInd
      case "header":
        headerName:parameterObject.getName()
        headerInd:parseHeaderObject(headerName,parameterObject,componentSchemas)
        operationInd.requestHeader: headerInd
      case "cookie":
        cookieInd:parseCookieObject(parameterObject,componentSchemas)
        operationInd.cookie: cookieInd
    }
  }

  //Get operation's servers.
  //If there are no operation's servers, use global servers
  // or path servers (finalServerList).
  if(operationObject.getServers().isEmpty()){
    for (serverInd in finalServerList) {
      operationInd.serverInfo: serverInd
    }
  }
  else{
    for (serverObject in operationObject.getServers()) {
      serverInd: parseServerObject(serverObject)
      operationInd.serverInfo: serverInd
    }
  }
  //Save operation in document class
  documentInd.supportedOperation: operationInd
end



/*
* The object provides metadata about the API.
*The metadata MAY be used by the clients if needed, and MAY be presented
*in editing or documentation generation tools for convenience
*/
function parseInfoObject(documentInd,infoObject)
  infoInd : createIndividual ("Info")
  //Extract the title of the application.
  infoInd.title : infoObject.getTitle()
  //Extract a short description of the application
  infoInd.description: infoObject.getDescription()
  //Extract URL to the Terms of Service for the API.
  infoInd.termsOfService: infoObject.getTermsofService()
  //Extract Contact  and License information for the exposed API.
  infoInd.contact:parseContactObject(infoObject.getContact())
  infoInd.license:parseLicenseObject(infoObject.getLicense())
  //Extract version of the OpenAPI document
  documentInd.version: infoInd.getVersion()
  //Store info individual in document Class.
  documentInd.info: infoInd
end


/*Contact information for the exposed API.*/
function parseContactObject(contactObject)
  contInd: createIndividual ("Contact")
  //The identifying name of the contact person/organization.
  contInd.name: contactObject.getName()
  //The URL pointing to the contact information
  contInd.url: contactObject.getUrl()
  //Email address of the contact person/organization
  contInd.email: contactObject.getEmail()
  return contInd
end

function parseLicenseObject(licenceObject)
  licInd: createIndividual ("License")
  //Extract license name and url
  licInd.name: contactObject.getName()
  licInd.url: contactObject.getUrl()
  return licInd
end

//An object representing a Server.
function parseServerObject(serverObject)
  serverInd: createIndividual ("Server")
  //Extract server's url, description
  serverInd.url: serverObject.getUrl()
  serverInd.description: serverObject.getDescription()
  //Extract server URL template substitution
  for(<serverName, serverVariableObject> in serverObject.getServerVariables()){
    sVariable:createIndividual("ServerVariable")
    //Extract enumeration of  values that will be used if
    // the substitution options are from a limited set.
    for (enumeration in enum){
      sVariable.enum: serverVariableObject.getEnum()
    }
    //Extract default value to use for substitution, which
    //SHALL be sent if an alternate value is not supplied.
    sVariable.default: serverVariableObject.getDefault()
    sVariable.description: serverVariableObject.getDescription()
    //Store server variable objects
    serverInd.variable: sVariable
  }
  return serverInd
end

//Extract external resource for extended documentation.
function parseExternalDocObject(externalDocObject)
  exInd: createIndividual("ExternalDoc")
  exInd.description: externalDocObject.getDescription()
  exInd.url: externalDocObject.getURL()
  return exInd
end

//Extract a single request body
function parseRequestBoby(requestObject, componentSchemas)
  reqInd: createIndividual("RequestBody")
  reqInd.description: requestObject.getDescription()
  //Extract List<> of contents of the request body
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in requestObject.getMediaType()){
      mediaInd: parseMediaTypeObject (mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  reqInd.contect: mediatypeList
  //Extract if the request body is required in the request.
  req.required :requestObject.getRequired()
  return reqInd
end

//Each Media Type Object provides schema for the media type
//identified by its key
function parseMediaTypeObject(mediaName, mediatypeObject, componentSchemas)
  mediaInd: createIndividual("MediaType")
  //Keep mediaType name
  mediaInd.name: mediaName
  //Extract schema defining the content of the request, response, or parameter.
  mediaInd.schema: parseSchemaObject(null, mediatypeObject, componentSchemas)
  //Extract a map between a property name and its encoding information.
  List<> encodingList
  for(<encodName, encodingObject> in mediaType.getEncoding()){
    encodInd: parseEncodingObject(encodName, encodingObject, componentSchemas)
    encodingList.add(encodInd)
  }
  mediaInd.encoding: encodingList
  return mediaInd
end

function parseEncodingObject(encodName, encodingObject, componentSchemas)
  encodInd: createIndividual("Encoding")
  encodInd.propertyName: encodName
  //Extract Content-Type for encoding a specific property.
  encodInd.contentType: encodingObject.getContentType()
  //Extract explode property
  encodInd.explode: encodingObject.getExplode()
  //Extract whether the parameter value SHOULD allow reserved characters
  encodInd.allowReserved: encodingObject.getAllowReserved()
  //Extract how a specific property value
  //will be serialized depending on its type
  encodInd.style: encodingObject.getStyle()
  //Extract a map allowing additional information to be provided as headers
  List<> headerList
  for(<headerName, headerObject> in encodingObject.getHeaders()){
    headerInd: parseHeaderObject(headerName, headerObject, componentSchemas)
    headerList.add(headerInd)
  }
  encodInd.encodingHeader: headerList
  return  encodInd
end

// Extract single response from an API Operation
function parseResponseBodyObject(statusCode, responseObject, componentSchemas)
  if(statusCode=="default")
    responseInd: createIndividual("DefaultResponse")
  else if (statusCode in ["1XX:  "2XX:  "3XX:  "4XX:  "5XX"])
    responseInd: createIndividual(statusCode+"Response")
  else // [100-199], [200-299], etc
    responseInd: createIndividual("Response")

  responseInd.description: responseObject.getDescription()
  //Extract a map allowing additional information to be provided as headers
  List<> headerList
  for(<headerName, headerObject> in encodingObject.getHeaders()){
    headerInd: parseHeaderObject(headerName, headerObject, componentSchemas)
    headerList.add(headerInd)
  }
  responseInd.responseHeader: headerList
  //Extract map containing descriptions of potential response payloads
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in encodingObject.getMediaType()){
      mediaInd:parseMediaTypeObject(mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  reqInd.contect: mediatypeList
  return reqInd
end

//Extract metadata to a single tag that is used by the Operation Object.
function parseTagObject (tag, componentSchemas)
  tadInd: createIndividual("Tag")
  tagInd.name: tag.getName()
  tagInd.description: tag.getDescription()
  tagInd.externalDoc: parseExternalDocObject(tag.getExternalDoc())
  //x-on Resourse
  xonResourse : tag.getExtension("x-onResourse")
  schemaInd: null
  if(xonResourse!=null){
   //get schema that tag object indicates
   schemaObject: componentSchemas.get(xonResourse)
   //create that schema Object
   schemaInd: parseSchemaObject(xonResourse, schemaObject, componentSchemas)
 }
  //return tag  with the corresponding schema
  return <tagInd, schemaInd>
end

// Extract metadata object that allows for more fine-tuned
// XML model definitions.
function parseXMLObject(xmlObject)
  xmlInd: createIndividual("XML")
  // Extract the name of the element/attribute
  // used for the described schema property.
  xmlInd.name: xmlObject.getName()
  //Extract URI of the namespace definition.
  xmlInd.uri: xmlObject.getURI()
  //Extract prefix to be used for the name.
  xmlInd.prefix: xmlInd.getPrefix()
  //Extract whether the property definition translates
  //to an attribute instead of an element.
  xmlInd.attribute: xml.getAttribute()
  //Extract whether the array is wrapped
  xmlInd.wrapped: xml.getWrapped()
  return xmlInd
end





/*
*There are four possible parameter locations specified by the in field:
*path -  where the parameter values actually part of the operation's URL.
*query - Parameters that are appended to the URL.
header - Custom headers that are expected as part of the request.
cookie - Used to pass a specific cookie value to the API.
*/

// When parameter location is in cookie..
function  parseCookieObject( parameterObject, componentSchemas)
  cookieInd: createIndividual("Cookie")
  cookieInd.name: parameterObject.getName()
  cookieInd.description: parameterObject.getDescription()
  //Exctract whether this parameter is mandatory
  cookieInd.required: parameterObject.getRequired()
  //Extract whether parameter is deprecated and SHOULD
  //be transitioned out of usage.
  cookieInd.deprecated: parameterObject.getDeprecated()
  //Extract how the parameter value will be serialized
  cookieInd.style: parameterObject.getStyle()
  cookieObject.explode:  parameterObject.getExplode()
  //Extract the schema defining the type used for the parameter.
  if(cookieObject.getSchema()!=null){
    schemaObject: cookieObject.getSchema()
    schemaInd:parseSchemaObject(null,schemaObject,componentSchemas)
    cookieInd.schema: schemaInd
  }
  //Extract a map containing the representations for the parameter
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in cookieObject.getMediaType()){
      mediaInd:parseMediaTypeObject(mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  cookieInd.contect: mediatypeList
  return cookieInd
end

// When parameter location is in header..
function  parseHeaderObject(headerName, headerObject, componentSchemas)
  headerInd: createIndividual( "Header")
  headerInd.name:  headerName
  headerInd.description:  headerObject.getDescription()
  headerInd.required:  headerObject.getRequired()
  headerInd.deprecated:  headerObject.getDeprecated()
  headerInd.style: headerObject.getStyle()
  headerInd.explode:  headerObject.getExplode())
  //Extract the schema defining the type used for the parameter.
  if(headerObject.getSchema()!=null){
    schemaObject: headerObject.getSchema()
    schemaInd:parseSchemaObject(null,schemaObject,componentSchemas)
    headerInd.schema: schemaInd
  }
  //Extract a map containing the representations for the parameter
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in headerObject.getMediaType()){
      mediaInd:parseMediaTypeObject(mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  headerInd.contect: mediatypeList
  return headerInd
end


// When parameter location is in query..
function  parseQueryParameter(queryObject, componentSchemas)
   queryInd : createIndividual("Query")
  queryInd.name:  queryObject.getName()
  queryInd.description: queryObject.getDescription()
  queryInd.required: queryObject.getRequired()
  queryInd.deprecated: queryObject.getDeprecated()
  queryInd.style: queryObject.getStyle()
  queryInd.explode: queryObject.getExplode()
  queryInd.allowEmptyValue: queryObject.getAllowEmptyValue()
  queryInd.allowReserved: queryObject.getAllowReserved()
  //Extract the schema defining the type used for the parameter.
  if(queryObject.getSchema()!=null){
    schemaObject: queryObject.getSchema()
    schemaInd:parseSchemaObject(null,schemaObject,componentSchemas)
    queryInd.schema: schemaInd
  }
  //Extract a map containing the representations for the parameter
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in queryObject.getMediaType()){
      mediaInd:parseMediaType(mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  queryInd.contect: mediatypeList
  return queryInd
end

// When parameter location is in path..
method  parsePathParameter(parameterObject, componentSchemas)
  parameterInd : createIndividual("Parameter")
  parameterInd.name:  parameterObject.getName()
  parameterInd.description:  parameterObject.getDescription()
  parameterInd.required:  true
  parameterInd.deprecated:  parameterObject.getDeprecated()
  parameterInd.style: parameterObject.getStyle()
  parameterInd.explode:  parameterObject.getExplode())
  //Extract the schema defining the type used for the parameter.
  if(parameterObject.getSchema()!=null){
    schemaObject: parameterObject.getSchema()
    schemaInd:parseSchemaObject(null,schemaObject,componentSchemas)
    parameterInd.schema: schemaInd
  }
  //Extract a map containing the representations for the parameter
  List<>  mediatypeList
  for (<mediaName, mediatypeObject> in parameterObject.getMediaType()){
      mediaInd:parseMediaTypeObject(mediaName, mediatypeObject, componentSchemas)
      mediatypeList.add(mediaInd)
  }
  parameterInd.contect: mediatypeList
  return parameterInd
end


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
function parseSecurityReqObject(securityReqObject)
  secReqInd: createIndividual("SecurityRequirement")
  for(<securitySchemeName, scopes> in securityReqObject){
    //get security scheme with url: securitySchemeName
    secReqInd.securityType: get(securitySchemeName)
    // Scope of Security Requirements
    secReqInd.scopes: scopes
  }
  return secReqInd
end

//Extract configuration details for a supported OAuth Flow
method  parseOAuthFlows() oauthflowsObject){
  if(oauthflowsObject.getImplicit()!=null){
    //Configuration for the OAuth Implicit flow
    implicit: oauthflowsObject.getImplicit()
    //create Individual Implicit  with its properties.
    oauthflowsInd: createImplicitFlow(implicit)
  }
  else if (oauthflowsObject.getAuthorizationCode()!=null){
    //Configuration for the OAuth Authorization Code flow
    auth: oauthflowsObject.getAuthorizationCode()
    //create Individual AuthorizationCode  with its properties.
    oauthflowsInd: createAuthorizationCodeFlow(auth)
  }
  else if (oauthflowsObject.getClientCredentials()!=null){
    //Configuration for the OAuth Client Credentials flow
    cred: oauthflowsObject.getClientCredentials()
    //create Individual ClientCredentials  with its properties.
    oauthflowsInd: createCredentialsFlow(cred)
  else{
    //Configuration for the OAuth Resource Owner Password flow
    password: oauthflowsObject.getPassword()
    //create Individual Password  with its properties.
    oauthflowsInd: parsePasswordFlow(password)
  }
  return oauthflowsInd
}


function parseSecuritySchemeObject(documentInd, securityName, securityObject)
  switch (securityObject.getType())
  {
  case APIKEY :
    securityInd : createIndividual("APIKEY", url: securityName)
    //Extract the name of the header, query or cookie parameter to be used.
    securityInd.parameterName: securityObject.getName()
    // Extract the location of the API key
    securityInd.in: securityObject.getIn()
  case HTTP:
    securityInd : createIndividual("HTTP", url: securityName)
    //Extract the name of the HTTP Authorization scheme
    securityInd.scheme: securityObject.getScheme()
    //Extract a hint to the client to identify
    // how the bearer token is formatted.
    securityInd.bearerFormat: securityObject.getBearerFormat()
  case OAUTH2:
    securityInd : createIndividual("OAUTH2", url: securityName)
    //Extract an object containing configuration information for
    //the flow types supported.
    flowsInd: parseOAuthFlows(securityObject.getFlows())
    securityInd.flow: flowsInd
  case OPENIDCONNECT:
    securityInd : createIndividual("OPENIDCONNECT", url: securityName)
    //Extract OpenId Connect URL to discover OAuth2 configuration values.
    securityInd.openIdConnectUrl: securityObject.getOpenIdConnectUrl()
  }
  securityInd.description: securityObject.getDescription()
  //Save security scheme in document class
  documentInd.supportedSecurity: secInd
end

/*The Schema Object allows the definition of input and output data types.
These types can be objects, but also primitives and arrays.
*/
method parseSchemaObject(schemaName, schemaObject, componentSchemas)
  //If schema is alredy defined in , retrieve it from its name.
  shapeInd : findShape(schemaName)
  if (shapeInd == null) {
    //Else createIndividual a new Schema individual
    if (schemaObject.getType()==object) {
      shapeInd: createNodeShape(schemaName, schemaObject,componentSchemas)
    }
    else if (schemaObject.getType() ==array && !schemaObject.hasExtension()) {
      shapeInd: createCollectionNodeShape(schemaName, schemaObject, componentSchemas)
    }
    else { // is integer, , number ,boolean
      shapeInd: createPropertyShape( schemaName, schemaObject, componentSchemas)
    }
    //Keep schema name
    shapeInd.label: schemaName + "Shape"
  }
  return shapeInd
end

//It's called when schema is object type.
method  createNodeShape(schemaName, schemaObject, componentSchemas)
	 nodeShapeInd : createIndividual( "sh:NodeShape")
	//Handle semantics x-refersTo, x-kindOf, x-mapsTo
	 classUri:null
	 collectionMember:null
	if (schemaObject.hasExtension("x-refersTo")) {
    //Extract x-refersTo class uri
		classUri : schemaObject.getExtension("x-refersTo")
	}
	else if (schemaObject.hasExtension("x-kindOf")) {
    //Extract x-kindof class uri
		uri : schemaObject.getExtension("x-kindOf")
    //createIndividual class with name "schemaName"
	  class : ontModel.createClass(schemaName)
    //Set x-kindOf class as superClass
		class.addSuperClass(uri)
    //get uri of our new class
		classUri : class.getUri()
	}
	else if (schemaObject.hasExtension("x-mapsTo")) {
    //Extract x-mapsTo class uri
		mappedSchemaName : schemaObject.getExtension("x-mapsTo")
    //Get component schema with name "mappedSchemaName"
		<mappedName, mappedSchemaObject> : componentSchemas.get(mappedSchemaName)
    //Check if that schema is already defined in ontology
		mappedShapeInd : findShape(mappedName)
		if (mappedShapeInd == null) {
      //Else create individual for that schema object
		   mappedShapeInd : createNodeShape(mappedName,mappedSchemaObject,componentSchemas)
		}
    //Extract class uri that targetClass points to.
		classUri : mappedShapeInd.getProperty("sh:TargetClass")
	}
	else if (schemaObject.hasExtension("x-collectionTo")) {
    //create a class with name "schemaName"
		class : ontModel.createClass(schemaName)
    //Set Collection as superClass of our class
		class.addSuperClass("openapi:Collection")
    //Extract the object-member of collection
		collectionMember : schemaObject.getExtension("x-collectionTo")
    //Get uri of our class
		classUri : class.getUri()
	}
  //Set targetClass equal to classUri
	nodeShapeInd.targetClass: classUri
  //Extract all properties of nodeShape
  schemaProperties: schemaObject.getProperties()
	for (<propertyName, propertyObject> propertyEntry in schemaProperties) {
		 propertyShapeInd : createPropertyShape(propertyName,propertyObject,componentSchemas)
     //Save property name
     propertyShapeInd.name: propertyName
     //If collection member is same with propertyName set property path
     //of the property shape to "member"
		if (propertyName == collectionMember) {
			propertyShapeInd.path: "member"
		}
		nodeShapeInd.property: propertyShapeInd
	}
  //Extract a short description
	nodeShapeInd.description: schemaObject.getDescription())
	return nodeShapeInd
end


method  createPropertyShape(schemaName, schemaObject, componentSchemas)
	 propertyShapeInd : createIndividual( "sh:PropertyShape")
	//Handle semantics x-refersTo, x-kindOf, x-mapsTo
	 propertyUri:null
	if (schemaObject.hasExtension("x-refersTo")) {
    //Get property uri that x-refersTo indicates.
		propertyUri:schemaObject.getExtension("x-refersTo")
	}
	else if (schemaObject.hasExtension("x-kindOf")) {
    //Get property uri that x-kindOf indicates.
		uri : schemaObject.getExtension("x-kindOf")
    //create a property with name "schemaName"
		property : ontModel.createProperty(schemaName)
    //Set x-kindOf url as SuperProperty of our property
		property.addSuperProperty(uri)
    //Get uri of the new property
		propertyUri : property.getUri()
	}
	else if (schemaObject.hasExtension("x-mapsTo")) {
    //Get property that x-mapsTo indicates.
		mappedSchemaProperty : schemaObject.getExtension("x-mapsTo")
    //Extract NodeShape schema individual that mappedSchemaProperty belongs to.
		mappedSchema : extractSchemaName(mappedSchemaProperty)
    //Check if that schema is already defined in ontology
		mappedShapeInd : findShape( mappedSchema)
    //If is not already defined, create a NodeShape individual
		if (mappedShapeInd == null) {
      //Get schema object from componentSchemas with name "mappedSchema"
			<mappedName, mappedObject> mappedSchemaEntry : componentSchemas.get(mappedSchema)
			mappedShapeInd : createNodeShape(mappedName,mappedObject,componentSchemas)
		}
    //After extraction of NodeShape name, extract also the mapped property name
		mappedProperty : extractPropertyName(mappedSchemaProperty)
		if (mappedProperty == null) {
      //If there is no mapped property name, copy the path value of mappedShapeInd
			propertyShapeInd.path: mappedShapeInd.path
		}
		else {
      //Else locate x-mapsTo property in ontology
			mappedPropertyShape : findNodeProperty(mappedShapeInd, mappedProperty)
      //copy the path value of mappedPropertyShape
			propertyShapeInd.path: mappedPropertyShape.path
		}
	}

	if (schemaObject.getType() == array) {
    //Extract items of array
		itemsObject : schemaObject.getItems()
		if (itemsObject.getType() == object) {
      //create a NodeShape individual for each item of the array
			itemsNodeShape : createNodeShape(schemaName, itemsObject, componentSchemas)
			propertyShapeInd.node: itemsNodeShape
		}
		else {
      //if items are not object type, keep only the datatype of items
			propertyShapeInd.datatype: getDatatype(itemsObject.getType(), itemsObject.getFormat())
		}
    //Extract min/max number of items
		propertyShapeInd.minCount: itemsObject.getMinItems()
		propertyShapeInd.maxCount: itemsObject.getMaxItems()
  }
	else if (schemaObject.getType() == object) {
    nodeShape : createNodeShape( schemaName, schemaObject, componentSchemas)
		propertyShapeInd.node: nodeShape
	}
	else {
    //Keep only the datatype
		propertyShapeInd.datatype: getDatatype(itemsObject.getType(),itemsObject.getFormat())
	}

  propertyShapeInd.multipleOf: schemaObject.getMultipleOf()
  propertyShapeInd.maximum: schemaObject.getMaximum()
  propertyShapeInd.exclusiveMaximum: schemaObject.getExclusiveMaximum()
  propertyShapeInd.minimum: schemaObject.getMinimum()
  propertyShapeInd.exclusiveMinimum:schemaObject.getExclusiveMinimum()
  propertyShapeInd.maxLength:schemaObject.getMaxLength()
  propertyShapeInd.minLength:schemaObject.getMinLength()
  propertyShapeInd.pattern:schemaObject.getPattern()
  propertyShapeInd.pattern: schemaObject.getPattern()
  propertyShapeInd.description: schemaObject.getDescription()
  propertyShapeInd.readOnly: schemaObject.getReadOnly()
  propertyShapeInd.writeOnly: schemaObject.getWriteOnly()
  propertyShapeInd.deprecated: schemaObject.getDeprecated()
  propertyShapeInd.defaultValue: schemaObject.getDefault()
  //Extract External Docs
  exDocInd : parseExternalDocObject( schemaObject.getExternalDoc()
  nodeShapeInd.exDoc: exDocInd
  //Extract XML
  xmlInd : parseXmlObject(schemaObject.getXml())
  propertyShapeInd.xml: xmlInd

	return propertyShapeInd
end

method  createCollectionNodeShape(schemaName, schemaObject, componentSchemas)
	nodeShapeInd : createIndividual("NodeShape")
  //create a new class with name "schemaName"
	class : ontModel.createClass(schemaName)
  //Set collection as superClass
	class.addSuperClass("openapi:Collection")
  //Extract targetClass value of nodeShapeInd
	nodeShapeInd.targetClass: class.getUri())
  //create a property shape individual for the schemaObject
  memberInd : createPropertyShape( null, schemaObject, componentSchemas)
  //Make this property shape a member of collection
	memberInd.path: "member"
	return nodeShapeInd
end
