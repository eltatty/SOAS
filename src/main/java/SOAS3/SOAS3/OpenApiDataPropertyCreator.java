package SOAS3.SOAS3;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

//This class contains methods in order to assign primitive values to the ontology's properties

public class OpenApiDataPropertyCreator
{
	private  OntModel model;
	//Constructor
	public OpenApiDataPropertyCreator(OntModel ontModel) {
		this.model = ontModel;
	}

	//All DataPropertyMethods methods

	public void AddContactName(Individual object,String contact_name_value)
	{
		if(object==null ||contact_name_value==null)
		{
			return;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.contact_name_uri);
		RDFNode value=model.createTypedLiteral(contact_name_value,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddEmail(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.contact_email_uri);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddURL(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.url_uri);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddHost(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.hostURI);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddLicenseName(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.License_name_uri);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddXMLName(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.xmlNameURI);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddNameSpace(Individual object,String val)
	{
		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.namespaceURI);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddPrefix(Individual object,String val)
	{

		if(object==null ||val==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.prefixURI);
		RDFNode value=model.createTypedLiteral(val,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddAttribute(Individual object,String val)
	{

		if(object==null ||val.equals("null"))
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.attributeURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));
	}


	public void AddWrapped(Individual object,String val)
	{
		if(object==null ||val.equals("null"))
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.wrappedURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));
	}


	public void AddVersion(Individual object, String version) {
		// TODO Auto-generated method stub
		if(version==null || object==null)
		{
			return;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.versionURI);
		RDFNode value=model.createTypedLiteral(version,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}


	public void AddTermsOfService(Individual object, String services) {
		// TODO Auto-generated method stub
		if(services==null || object==null)
		{
			return;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.termsOfServiceURI);
		RDFNode value=model.createTypedLiteral(services,XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddDescription(Individual object, String description) {
		// TODO Auto-generated method stub1111
		if(description==null || object==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.descriptionURI);
		RDFNode value=model.createTypedLiteral(description,XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddtTitle(Individual object, String title) {
		// TODO Auto-generated method stub
		if(title==null|| object==null)
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.titleURI);
		RDFNode value=model.createTypedLiteral(title,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddExclusiveMaximum(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.maxExclusiveURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddExclusiveMinimum(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.minExclusiveURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddInclusiveMaximum(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.maxInclusiveURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddInclusiveMinimum(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.minInclusiveURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddUniqueItems(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.uniqueItemsURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddParameterName(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.parameterNameURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddScheme(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.schemeURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddBearerFormat(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.bearerFormatURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddOpenIdConnectURL(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.openIdConnectUrlURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));

	}


	public void AddNullable(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.nullableURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddReadOnly(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.readOnlyURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddWriteOnly(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.writeOnlyURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddDeprecated(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.deprecatedURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddMaxLength(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.maxLengthURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddMinLength(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.minLengthURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddMaxItems(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.maxCountURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddMinItems(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.minCountURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddMaxProperties(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.maxPropertiesURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddMinProperties(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.minPropertiesURI);
		RDFNode value=model.createTypedLiteral(Integer.parseInt(val),  XSDDatatype.XSDinteger);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddPattern(Individual object, String val) {
		// TODO Auto-generated method stub
		if(object==null || val.equals("null"))
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.patternURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));

	}


	public void AddAuthorizationUrl	(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.authorizationUrlURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}
	public void AddTokenUrl(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.tokenUrlURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}
	public void AddRefreshUrl	(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.refreshUrlURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDanyURI);
		model.add(model.createStatement(object, pr, value));
	}



	public void AddName(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.nameURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddEnum(Individual object,String val )
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.variableValueURI);
		model.add(model.createStatement(object, pr, val));


	}
	public void AddStatusCode(Individual object,String val )
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.statusCodeURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}
	public void AddSchemaLabel(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.SchemaLabel);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}


	public void AddDefault(Individual object, String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.variableDefaultValueURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddPathName(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.pathNameURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddMethod(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.methodURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddSummary (Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.summaryURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}

	public void AddRequired(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.requiredURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddExplode(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.explodeURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddAllowEmptyValue(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.allowEmptyValueURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}
	public void AddAllowReserved(Individual object, String val)
	{
		if(object==null || val.equals("null"))
		{
			return ;
		}

		Property pr =  model.getProperty(OpenApiOntUtils.allowReservedURI);
		RDFNode value=model.createTypedLiteral(Boolean.parseBoolean(val),  XSDDatatype.XSDboolean);
		model.add(model.createStatement(object, pr, value));

	}

	public void AddSecurityType(Individual main_indi,Individual security_scheme_ind)
	{
		if(main_indi==null || security_scheme_ind==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.securityTypeURI);
		model.add(model.createStatement(main_indi, pr, security_scheme_ind));
	}
	public void AddMediaTypeName(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.mediaTypeNameURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}
	public void AddPropertyName(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.propertyNameURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);
		model.add(model.createStatement(object, pr, value));
	}
	public void AddContentType(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.contentTypeURI);
		RDFNode value=model.createTypedLiteral(val,  XSDDatatype.XSDstring);

		model.add(model.createStatement(object, pr, value));
	}


	public void AddInApiKey(Individual object,String val)
	{
		if(object==null || val==null)
		{
			return ;
		}
		Property pr =  model.getProperty(OpenApiOntUtils.inApiKeyURI);

		model.add(model.createStatement(object, pr, val));
	}


}


