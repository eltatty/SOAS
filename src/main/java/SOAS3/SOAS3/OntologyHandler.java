package SOAS3.SOAS3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.PrintUtil;

import openllet.jena.PelletReasonerFactory;





public class OntologyHandler {
	/*
	 * This class handles the ontology with apache jena. It contains methods to create an ontology model, read ontology file, and pose 
	 * any query.
	 */
	private OntModel ontModel;
	private String NS ;

	public OntologyHandler() {
		ontModel=null;
		NS = PrintUtil.egNS;
	}



	/* CreateOntologyModel*/
	public void createOntologyModel(OntModelSpec spec,Model model)
	{
		this.ontModel= ModelFactory.createOntologyModel(spec, model);
	}

	/*Read the Ontology*/
	public OntModel readOntologyFile(String address, String type)
	{
		this.ontModel.read(address,type);
		return ontModel;
	}

	/* Get Model */
	public OntModel getOntModel() {
		return ontModel;
	}
	/*SetModel */
	public void setOntModel(OntModel ontModel) {
		this.ontModel = ontModel;
	}
	
	public void writeOntology(String productFile)
	{
		
	}
	/* Pose a query*/
	public List<String> QueryCreator(String the_query,String[] columnsName)
	{

		Query query = QueryFactory.create(the_query);
		QueryExecution qexec = QueryExecutionFactory.create(query,ontModel);
		ResultSet results = qexec.execSelect();
		List<String> answer = new ArrayList<String>();
		while(results.hasNext())
		{
			QuerySolution t = results.nextSolution();
			String queryAnswer="";
			for (String col : columnsName){
			RDFNode x = t.get(col);
			String s = x.toString();
			queryAnswer+=s+",";
			}
			answer.add(queryAnswer);
		}
		qexec.close();
		return answer;
	}

	/* Print Ontology*/
	public void PrintOntology()
	{
		ontModel.write(System.out, "RDF/XML");
	}

	/* Print Ontology to file */
	public void  PrintOntologyToFile(String productName){
//		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//		InfModel infmodel = ModelFactory.createInfModel(reasoner, ontModel);
//		ValidityReport validity = infmodel.validate();
//		if (validity.isValid()) {
//			System.out.println("OK");
//		} else {
//			System.out.println("Conflicts");
//			for (Iterator i = validity.getReports(); i.hasNext(); ) {
//				ValidityReport.Report report = (ValidityReport.Report)i.next();
//				System.out.println(" - " + report);
//			}
//		}
		File folder = new File( "Products");
		String product = folder.getAbsolutePath() + "/" + productName + ".ttl";
		try{
			OutputStream fileOut = new FileOutputStream(product);
			ontModel.write(fileOut, "TURTLE");
		}catch (Exception e) {
			e.getStackTrace();
		}
	}

}
