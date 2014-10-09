package textract;

import java.io.IOException;
import org.dom4j.DocumentException;
import org.json.simple.parser.ParseException;

public class OneRecordTester {

	// Set this
	//private static String testRecord = "oai:beeldengeluid.nl:Expressie:4213418";
	
	private static String testRecord = "oai:beeldengeluid.nl:Expressie:4206620"; //oai:beeldengeluid.nl:Expressie:4203433";
	
	
	public static void main(String[] args) {
		Textractor gogo = new Textractor();
		//gogo.setMinNormFreq(1.00E-5);
		//gogo.setMinScore(0.0);
		
		Textractor.debug = true;
		
		System.out.println("testing one record");
		// set to command line input, if available
		if (args.length > 0)
			{testRecord = args[0];}
		gogo.setStopwords(new StopWords().getStopwords());
		gogo.setTermfreqFinder(new TermFrequency());;
		
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
		try {
			ImmixRecord irec = gogo.retrieveAll(testRecord, gtaaES);
			System.out.println(irec.toStringAll());	
			
			System.out.println("\n\nXML OUT\n-----------------\n\n"+ irec.toCSV());
		} catch (ParseException | IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("I'm done");

	}
		
}
