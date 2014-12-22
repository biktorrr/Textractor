package textract;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.dom4j.DocumentException;
import org.json.simple.parser.ParseException;

public class MultipleRecordTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> input = Arrays.asList(
				"oai:beeldengeluid.nl:Expressie:4207278",
				"oai:beeldengeluid.nl:Expressie:4206475",
				"oai:beeldengeluid.nl:Expressie:4205355",
				"oai:beeldengeluid.nl:Expressie:4206275",
				"oai:beeldengeluid.nl:Expressie:4039252",
				"oai:beeldengeluid.nl:Expressie:4206473",
				"oai:beeldengeluid.nl:Expressie:4207278",
				"oai:beeldengeluid.nl:Expressie:4207393",
				"oai:beeldengeluid.nl:Expressie:4204250",
				"oai:beeldengeluid.nl:Expressie:4206620",
				"oai:beeldengeluid.nl:Expressie:4207391",
				"oai:beeldengeluid.nl:Expressie:893216",
				"oai:beeldengeluid.nl:Expressie:4206557",
				"oai:beeldengeluid.nl:Expressie:4207287",
				"oai:beeldengeluid.nl:Expressie:4207407");

		Textractor gogo = new Textractor();

			
		System.out.println("testing multiple records");
		// set to command line input, if available
		gogo.setStopwords(new StopWords().getStopwords());
		gogo.setTermfreqFinder(new TermFrequency());
		
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();
		FileWriter fw2;
		try {
			fw2 = new FileWriter("ArjenExamples.txt",true);
		
			for (int i=0;i<input.size();i++){
				try {
					ImmixRecord irec = gogo.retrieveAll(input.get(i), gtaaES);
					System.out.println("\n\nTT888 \n-----------------\n\n"+ irec.getTTString());
					System.out.println("\n\nCSV OUT\n-----------------\n\n"+ irec.toCSV());
					
					fw2.write("\n\nNEW ITEM: "+ input.get(i)+"\n");
				    fw2.write("\n\nTT888 \n-----------------\n\n"+ irec.getTTString());
				    fw2.write("\n\nCSV OUT\n-----------------\n\n"+ irec.toCSV());
				    
	
				} catch (ParseException | IOException | DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			fw2.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //the true will append the new data

				
				
	}

}
