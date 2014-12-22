package textract;

import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.DocumentException;
import org.json.simple.parser.ParseException;

public class P06tester {

	
	public  ImmixRecord doOneProgram(String docID, String inputString, ElasticGTAASearcher gtaaES, Textractor ttract) throws ParseException, IOException, DocumentException{
		ImmixRecord ir = new ImmixRecord();
		ir.setTTString(inputString);
		ir.setDocID(docID);			
		
		// get the tokenmatches
		ArrayList<TokenMatch> resultTM = ttract.getTokenMatches(gtaaES, ir);
		System.out.println("N=1 Matches: " + resultTM);
		
		ArrayList<TokenMatch> resultTM2 = ttract.getNGramMatches(gtaaES, ir, 2);
		System.out.println("N=2 Matches: " + resultTM2);

		ArrayList<TokenMatch> resultTM3 = ttract.getNGramMatches(gtaaES, ir, 3 );
		System.out.println("N=3 Matches: " + resultTM3);

		resultTM.addAll(resultTM2);
		resultTM.addAll(resultTM3);
		ir.setTokenMatches(resultTM);
		
		//get the ner results
		NERrer nerrer = new NERrer();
		ArrayList<NamedEntity> nes = nerrer.getGTAANES(gtaaES, ir.getTTString());
		ir.setNEList(nes);
				
		ir.consolidateGTAATerms();
		return ir;

	}
	
	public static void main(String[] args) {
		P06tester pt = new P06tester();
		
		String input1= "(19.52 en 20.20 reclame) 20.28 In de hoofdrol Een bekende Nederlander wordt ge¬ confronteerd met mensen die een be¬ langrijke rol hebben gespeeld of nog spelen in zijn/haar leven Presentatie: Mies Bouwman";
		String id1 = "1";
		
		
		Textractor gogo = new Textractor();
		gogo.setStopwords(new StopWords().getStopwords());
		gogo.setTermfreqFinder(new TermFrequency());;
		gogo.setMinAbsFreq(1);
		gogo.setMinNormFreq(0);
		gogo.setMinScore(3.0);
		ElasticGTAASearcher gtaaES = new ElasticGTAASearcher();

		
		ImmixRecord ir;
		try {
			ir = pt.doOneProgram(id1, input1, gtaaES, gogo);
			System.out.println("\n\nINPUT \n-----------------\n"+ ir.getTTString());
			System.out.println("\n\nCSV OUT\n-----------------\n"+ ir.toCSV());

		} catch (ParseException | IOException | DocumentException e) {
			e.printStackTrace();
		}		
		
		
		
	}
}
