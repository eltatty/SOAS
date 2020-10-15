/**
 *
 */
package SOAS3.SOAS3;

/**
 * @author K.Karavasileiou
 *
 */
public class OpenApiOntUtils
{
	/* Contains all the ontology's uri.
	 *
	 */

	/*********************************** Info *******************************************/
	//Predefined URI's
	public static final String BasicURI="http://www.intelligence.tuc.gr/ns/open-api#";
	public static final String titleURI=BasicURI+"serviceTitle";
	public static final String descriptionURI=BasicURI+"description";
	public static final String termsOfServiceURI=BasicURI+"termsOfService";
	public static final String contactURI=BasicURI+"contact";
	public static final String licenseURI=BasicURI+"license";
	public static final String versionURI=BasicURI+"version";
	public static final String InfoClass=BasicURI+"Info";

	//Contact
	public static final String ClassContactURI= BasicURI+"Contact";
	public static final String contact_name_uri=BasicURI+"contactName";
	public static final String contact_email_uri=BasicURI+ "email";
	public static final String url_uri=BasicURI+"url";

	//License
	public static final String ClassLicenseURI= BasicURI+"#License";
	public static final String License_name_uri=BasicURI+"licenseName";

	/************************************Shape******************************************/
	public static final String ShapeBasicURI="http://www.w3.org/ns/shacl#";
	public static final String SchemaLabel="http://www.w3.org/2000/01/rdf-schema#label";

	public static final String PropertyShapeClassURI=ShapeBasicURI+"PropertyShape";
	public static final String propertyURI= ShapeBasicURI + "property";
	public static final String NodeShapeClassURI=ShapeBasicURI+"NodeShape";
	public static final String nodeURI=ShapeBasicURI+"node";
	public static final String shapeNameURI=ShapeBasicURI +"shapeName";
	public static final String ShapeClassURI= ShapeBasicURI +"Shape";
	public static final String maxExclusiveURI= ShapeBasicURI +"maxExclusive";
	public static final String minExclusiveURI= ShapeBasicURI +"minExclusive";
	public static final String minInclusiveURI= ShapeBasicURI +"minInclusive";
	public static final String maxInclusiveURI= ShapeBasicURI + "maxInclusive";
	public static final String maxLengthURI= ShapeBasicURI+"maxLength";
	public static final String minLengthURI= ShapeBasicURI + "minLength";
	public static final String patternURI= ShapeBasicURI +"pattern";
	public static final String maxCountURI= ShapeBasicURI + "maxCount";
	public static final String minCountURI= ShapeBasicURI + "minCount";
	public static final String inURI= ShapeBasicURI +"in";
	public static final String andURI= ShapeBasicURI +"and";
	public static final String xoneURI= ShapeBasicURI +"xone";
	public static final String orURI= ShapeBasicURI + "or";
	public static final String notURI= ShapeBasicURI+"not";
	public static final String defaultValueURI= ShapeBasicURI + "defaultValue";
	public static final String multipleOfURI= BasicURI + "multipleOf";
	public static final String uniqueItemsURI= BasicURI + "uniqueItems";
	public static final String maxPropertiesURI= BasicURI + "maxProperties";
	public static final String minPropertiesURI= BasicURI + "minProperties";
	public static final String allowExtraPropertiesURI=BasicURI +"allowExtraProperties";
	public static final String extraPropertySchemaURI=BasicURI + "extraPropertySchema";
	public static final String nullableURI= BasicURI + "nullable";
	public static final String discriminatorURI= BasicURI + "discriminator";
	public static final String readOnlyURI= BasicURI + "readOnly";
	public static final String writeOnlyURI= BasicURI + "writeOnly";
	public static final String xmlURI= BasicURI + "xml";
	public static final String deprecatedURI= BasicURI + "deprecated";
	public static final String datatypeURI= ShapeBasicURI + "datatype";
	public static final String pathURI= ShapeBasicURI + "path";
	public static final String targetClassURI= ShapeBasicURI + "targetClass";
	/******************************XML***************************************/
	public static final String xmlClassURI=BasicURI+"XML";
	public static final String xmlNameURI=BasicURI+"xmlName";
	public static final String namespaceURI=BasicURI+"namespace";
	public static final String prefixURI=BasicURI+"prefix";
	public static final String attributeURI=BasicURI+"attribute";
	public static final String wrappedURI=BasicURI+"wrapped";

	/************************Discriminator****************************/
	public static final String DiscriminatorClassURI=BasicURI +"Discriminator";
	public static final String discriminatorPropertyURI=BasicURI+ "discriminatorProperty";
	public static final String mapping=BasicURI+"mapping";



	/************************Properties Mapper*****************************/
	public static final String PropertyMapperClassURI=BasicURI + "PropertyMapper";
	public static final String mapKeyURI=BasicURI+"mapKey";
	public static final String mapValueURI=BasicURI+"mapValue";

	/********************** Server Object ***************************/
	public static final String ServerClassURI=BasicURI + "Server";
	public static final String hostURI=BasicURI + "host";
	public static final String variableURI=BasicURI+"variable";

	/*********************** Server Variable Object*****************/
	public static final String ServerVariableClassURI=BasicURI + "ServerVariable";
	public static final String nameURI=BasicURI + "name";
	public static final String variableValueURI=BasicURI+"variableValue";
	public static final String variableDefaultValueURI=BasicURI+"variableDefaultValue";
	//there is also description, which already exists

	/************************ Paths Object *****************************/
	public static final String PathClassURI=BasicURI + "Path";
	public static final String pathNameURI=BasicURI + "pathName";
	public static final String onPathURI=BasicURI + "onPath";

	/************************ Operation Object **************************/
	public static final String OperationClassURI=BasicURI + "Operation";
	public static final String tagURI=BasicURI + "tag";
	public static final String summaryURI=BasicURI + "summary";
	public static final String parameterURI=BasicURI + "parameter";
	public static final String cookieURI=BasicURI + "cookie";
	public static final String requestHeaderURI=BasicURI + "requestHeader";
	public static final String requestBodyURI=BasicURI + "requestBody";
	public static final String responseURI=BasicURI + "response";
	public static final String securityURI=BasicURI + "security";
	public static final String serverInfoURI=BasicURI + "serverInfo";

	/****************** External Documentation Object **************************/
	public static final String ExternalDocClassURI=BasicURI + "ExternalDoc";

	/********************** Parameter Object ****************************/

	public static final String ParameterClassURI=BasicURI + "PathParameter";
	public static final String QueryClassURI=BasicURI + "Query";
	public static final String CookieClassURI=BasicURI + "Cookie";
	public static final String HeaderClassURI=BasicURI + "Header";
	public static final String requiredURI=BasicURI + "required";
	public static final String allowEmptyValueURI=BasicURI + "allowEmptyValue";
	public static final String styleURI=BasicURI + "style";
	public static final String explodeURI=BasicURI + "explode";
	public static final String allowReservedURI=BasicURI + "allowReserved";
	public static final String schemaURI=BasicURI + "schema";
	public static final String contentURI=BasicURI + "content";



	/****************** Request Body Object ************************/
	public static final String RequestBodyClassURI=BasicURI + "Body";
	//description, content, required already written

	/******************* Media Type Object **************************/
	public static final String MediaTypeClassURI=BasicURI + "MediaType";
	public static final String mediaTypeNameURI=BasicURI + "mediaTypeName";
	public static final String encodingURI=BasicURI + "encoding";

	/********************** Encoding Object ****************************/
	public static final String EncodingClassURI=BasicURI + "Encoding";
	public static final String propertyNameURI=BasicURI + "propertyName";
	public static final String contentTypeURI=BasicURI + "contentType";
	public static final String encodingHeaderURI=BasicURI + "encodingHeader";


	/*********************** Response Object *****************************/
	public static final String ResponseClassURI=BasicURI + "Response";
	public static final String statusCodeURI=BasicURI + "statusCode";
	public static final String responseHeaderURI=BasicURI + "responseHeader";

	/********************** Tag Object ***********************************/
	public static final String TagClassURI=BasicURI + "Tag";

	// START OF SECURITY OBJECT
	/******************** Security Class ************************/
	public static final String SecurityClassURI=BasicURI + "Security";

	/********************** ApiKey Class *****************************/
	public static final String ApiKeyClassURI=BasicURI + "ApiKey";
	public static final String parameterNameURI=BasicURI + "parameterName";
	public static final String inApiKeyURI=BasicURI + "parameterName";

	/************************** Http Class ***************************/
	public static final String HttpClassURI=BasicURI + "Http";
	public static final String schemeURI=BasicURI + "scheme";
	public static final String bearerFormatURI=BasicURI + "bearerFormat";

	/********************* Oauth2 Class ******************************/
	public static final String OAuth2ClassURI=BasicURI + "OAuth2";
	public static final String flowURI=BasicURI + "flow";

	/******************** OpenIdConnect Class ************************/
	public static final String OpenIdConnectClassURI=BasicURI + "OpenIdConnect";
	public static final String openIdConnectUrlURI=BasicURI + "openIdConnectUrl";

	/*************************** Scope Class ***************************************/
	public static final String ScopeClassURI=BasicURI + "Scope";
	//property name already written

	/************************* Security Requirement Class ***************************/
	public static final String SecurityRequirementClassURI=BasicURI + "SecurityRequirement";
	public static final String securityTypeURI=BasicURI + "securityType";
	public static final String scopeURI=BasicURI + "scope";

	/**************************Style****************************************/
	public static final String StyleClassURI=BasicURI + "Style";
	public static final String pipeDelimitedURI=BasicURI + "pipeDelimited";
	public static final String spaceDelimitedURI=BasicURI + "spaceDelimited";
	public static final String labelURI=BasicURI + "label";
	public static final String matrixURI=BasicURI + "matrix";
	public static final String simpleURI=BasicURI + "simple";
	public static final String deepObjectURI=BasicURI + "deepObject";
	public static final String form=BasicURI + "form";

	/******************** Method***********************/
	public static final String methodURI=BasicURI + "method";
	public static final String MethodClassURI=BasicURI + "Method";


	//******* Get,Post,Delete etc******//
	public static final String putURI=BasicURI +"PUT";
	public static final String postURI=BasicURI +"POST";
	public static final String headURI=BasicURI +"HEAD";
	public static final String patchURI=BasicURI +"PATCH";
	public static final String optionsURI=BasicURI +"OPTIONS";
	public static final String getURI=BasicURI +"GET";
	public static final String deleteURI=BasicURI +"DELETE";
	public static final String traceURI=BasicURI +"TRACE";

	//************* OAuthFlow ***************//
	public static final String ImplicitClassURI=BasicURI +"Implicit";
	public static final String PasswordClassURI=BasicURI +"Password";
	public static final String ClientCredentialsClassURI=BasicURI +"ClientCredentials";
	public static final String AuthorizationCodeClassURI=BasicURI +"AuthorizationCode";
	public static final String refreshUrlURI=BasicURI +"refreshUrl";
	public static final String authorizationUrlURI=BasicURI +"authorizationUrl";
	public static final String tokenUrlURI=BasicURI +"tokenUrl";


	//Document
	public static final String DocumentURI=BasicURI +"Document";
	public static final String supportedSecurityURI=BasicURI +"supportedSecurity";
	public static final String infoURI=BasicURI +"info";
	public static final String supportedEntityURI=BasicURI +"supportedEntity";
	public static final String supportedOperationURI=BasicURI+"supportedOperation";
	public static final String externalDocURI=BasicURI +"externalDoc";

	//Collections (x-collectionOn)
	public static final String CollectionClassURI=BasicURI +"Collection";
	public static final String memberURI=BasicURI +"member";

	public static final String DefaultResponseURI=BasicURI +"DefaultResponse";
	public static final String XX1ResponseURI=BasicURI +"1xxResponse";
	public static final String XX2ResponseURI=BasicURI +"2xxResponse";
	public static final String XX3ResponseURI=BasicURI +"3xxResponse";
	public static final String XX4ResponseURI=BasicURI +"4xxResponse";
	public static final String XX5ResponseURI=BasicURI +"5xxResponse";
	public static final String ResponseURI=BasicURI +"Response";

	//Datatype
	public static final String BinaryURI=BasicURI +"binary";

	/************************* Reference *******************************/
	public static final String ReferenceURI = BasicURI + "reference";
}
