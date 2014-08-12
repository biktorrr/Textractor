package textract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class NERrer {

	// class to perform Named Entity Recognition on a String. 
	public NERrer() {
	}

   
   
	
	//get Named Entities from CLTL web services
	public ArrayList<NamedEntity> getNamedEntitiesFromCLTL(String inputString) throws IOException, DocumentException{
		ArrayList<NamedEntity> result = new ArrayList<NamedEntity>();
		System.out.print(" Recognizing Named Entities..");
		
			String kafResult = getTreeKafFromCLTL(inputString);
			String nerResult = getNerResultFromCLTL(kafResult);
			
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
			
		if (result.size()>0){System.out.println("..some done.");}
		else System.out.println("..none found.");
		return result;
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
		
		NERrer gogo = new NERrer();
		
		String test = "Hallo, mijn naam is Victor de Boer en ik woon in de mooie stad Haarlem. Ik werk nu bij het Nederlands Instituut voor Beeld en Geluid in Hilversum. Hiervoor was ik werkzaam bij de Vrije Universiteit. ";
		try {
			ArrayList result = gogo.getNamedEntitiesFromCLTL(test);
		
		} catch (IOException | DocumentException e ) {
			e.printStackTrace();
				}
		
	}

}
