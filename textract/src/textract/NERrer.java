package textract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.simple.JSONArray;

public class NERrer {

	//default minimun Elasticsearch score for NE matching 
	double minScore = 3.5;
	
	// class to perform Named Entity Recognition on a String. 
	public NERrer() {
	}

   
   // super-method, get named entity objects, including matching gtaa concepts
	public ArrayList<NamedEntity> getGTAANES (ElasticGTAASearcher gtaaES, String inputString) throws IOException, DocumentException{
		System.out.print(" Recognizing Named Entities..");
		TermFinder tf = new TermFinder();
		ArrayList<NamedEntity> result = getNamedEntitiesFromCLTL(inputString);
		
		if (result.size()>0){
			System.out.println("..some found.");
			for(int i=0;i<result.size();i++){
				NamedEntity ne = result.get(i);
				JSONArray matches = new JSONArray();
				if (ne.neClass.equalsIgnoreCase("PERSON")) {
					matches = tf.matchPersonNames(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass.equalsIgnoreCase("LOCATION")) {
					matches= tf.matchGeo(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass.equalsIgnoreCase("ORGANIZATION")) {
					matches= tf.matchNames(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass=="MISC") {
					matches= tf.matchNames(ne.neString, gtaaES, minScore);
					matches.addAll(tf.matchPersonNames(ne.neString, gtaaES, minScore));
					matches.addAll(tf.matchOnderwerpen(ne.neString, gtaaES, minScore));
				}
				ne.gtaaMatches = matches;
			}
			
		}
		else {
			System.out.println("..none found.");}
		if (Textractor.debug==true){System.out.println("\nNER result: "+ result);}
		return result;
	}
	

	
	
	
	//get Named Entities from CLTL web services
	public ArrayList<NamedEntity> getNamedEntitiesFromCLTL( String inputString) throws IOException, DocumentException{
		ArrayList<NamedEntity> result = new ArrayList<NamedEntity>();

			String kafResult = getTreeKafFromCLTL(inputString);
			String nerResult_dirty = getNerResultFromCLTL(kafResult);
			
			String nerResult = cleanNerResult(nerResult_dirty);
			
			Document document = DocumentHelper.parseText(nerResult);
			List list = document.selectNodes("//entity");
			
			for (int i=0;i<list.size();i++){
				NamedEntity ne = new NamedEntity();
				Element entity = (Element) list.get(i);
				ne.neClass = entity.attributeValue("type");
				String neString = "";
				Element refs =  entity.element("references");
				for (Iterator it = refs.nodeIterator(); it.hasNext();){
					Node node = (Node) it.next();
					if (node.getNodeType()==Node.COMMENT_NODE){
						neString = node.getText();					
					}
				}
				ne.neString = neString;
				result.add(ne);
			}
			

		return result;
	}
	
	private static String cleanNerResult(String in) {
		return "<?xml version" + in.split("<?xml version")[1];

	}


	private String getNerResultFromCLTL(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/opener_ner?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();		
	}

	// get the KAF representation of a string (uses webservice)
	private String getKafFromCLTL(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/opener_tokenizer?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();
	}
	
	// get the KAF Treetag representation of a string (uses webservice)
		private String getTreeKafFromCLTL(String inputString) throws IOException {
			String urlString = "http://ic.vupr.nl:8081/treetagger_plain_to_kaf?lang=nl";
			// Connect to google.com
	        URL url = new URL(urlString);
	        String postData = inputString;
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
	         
	        // Write data
	        OutputStream os = connection.getOutputStream();
	        os.write(postData.getBytes());
	         
	        // Read response
	        StringBuilder responseSB = new StringBuilder();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          
	        String line;
	        while ( (line = br.readLine()) != null)
	            responseSB.append(line);
	                 
	        // Close streams
	        br.close();
	        os.close();
	         
	        return responseSB.toString();
		}
	
	// get the NER representation of a string (uses webservice)
	private String getBasicPipeline(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/pipeline_basic?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();
	}
	
	

	public static void main(String[] args) {
		
		
		NERrer gogo = new NERrer();
		String test1 = "De Nederlandse economie is in het tweede kwartaal harder gegroeid dan eerder werd gedacht. Economie in tweede kwartaal harder gegroeid dan gedacht Foto:  Hollandse Hoogte Dat blijkt woensdag uit een tweede raming van het Centraal Bureau voor de Statistiek (CBS). De economie groeide met 0,7 procent ten opzichte van het eerste kwartaal. In augustus werd er nog vanuit gegaan dat de economie met 0,5 procent was gegroeid. Uit de tweede raming blijkt dat consumenten iets meer hebben uitgegeven dan eerder was berekend. Ook is er meer geïnvesteerd in zogenoemde vaste activa. Dat zijn dingen die langer dan een jaar worden gebruikt zoals gebouwen, machines en software. Het statistiekbureau past het beeld van de economie overigens niet aan. De groei van het tweede kwartaal is nog steeds vooral aan de export toe te schrijven.  Jaar op jaar Ook de groei van jaar op jaar was groter dan eerder gemeld. Volgens de nieuwe gegevens is de economie met 1,1 procent gegroeid ten opzichte van het tweede kwartaal in 2013. Eerder werd een groei van 0,9 procent berekend. Dat verschil ligt vooral in de consumptie van huishoudens en investeringen in bouwwerken. Een aantal percentages viel echter lager uit. Dat gold onder meer voor het handelssaldo, dat is de export minus de import. Ook heeft de overheid minder uitgegeven dan eerder was berekend. Beweeg de cursor over de lijn om de percentages te zien. De gegevens zijn afkomstig van het CBS. - (c)NU.nl/Jerry Vermanen Verder heeft het CBS de gegevens over banen ook aangepast. Er waren in het tweede kwartaal duizend minder banen dan in het voorgaande kwartaal. De eerste raming ging uit van een daling van achtduizend banen. Bij de vergelijking van jaar op jaar is de daling bijgesteld van 80.000 naar 70.000 banen. Informatie De eerste raming is 45 dagen na afloop van het tweede kwartaal berekend. Maar daarna kwam er meer informatie binnen van bedrijven in de zakelijke dienstverlening, de bouw en de horeca.In de afgelopen vijf jaar weken tweede ramingen gemiddeld 0,06 procentpunt af van de aanvankelijk gepubliceerde gegevens.";
		String test = "Hallo, mijn naam is Victor de Boer en ik woon in de mooie stad Haarlem. Ik werk nu bij het Nederlands Instituut voor Beeld en Geluid in Hilversum. Hiervoor was ik werkzaam bij de Vrije Universiteit. ";
		try {
			ArrayList<NamedEntity> result =  gogo.getNamedEntitiesFromCLTL(test1);
			//ArrayList result = gogo.getNamedEntitiesFromCLTL(test1);
			System.out.println(result.toString());
		} catch (IOException | DocumentException e ) {
			e.printStackTrace();
				}
		
	}

}
