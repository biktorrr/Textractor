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
					//TODO: what here? 
				}
				else if (ne.neClass=="MISC") {
					//TODO: what here? 
				}
				ne.gtaaMatches = matches;
			}
			
		}
		else {
			System.out.println("..none found.");}
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
			String urlString = "http://ic.vupr.nl:8081/treetagger_plain?lang=nl";
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
		
		System.out.println(cleanNerResult("bladiev a asdlasdlasd. asd.as.d qwd.w.wkks dasd <?xml version=\"1.0\" "));
		
		NERrer gogo = new NERrer();
		
		String test = "Hallo, mijn naam is Victor de Boer en ik woon in de mooie stad Haarlem. Ik werk nu bij het Nederlands Instituut voor Beeld en Geluid in Hilversum. Hiervoor was ik werkzaam bij de Vrije Universiteit. ";
		try {
			ArrayList result = gogo.getNamedEntitiesFromCLTL(test);
			System.out.println(result.toString());
		} catch (IOException | DocumentException e ) {
			e.printStackTrace();
				}
		
	}

}
