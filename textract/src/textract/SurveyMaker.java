package textract;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

// from output, make html form thing
public class SurveyMaker {
	
	private int max = 3; 

	public void xml2SurveyHTML(Document xmlDoc, int maxRec){
		List list = xmlDoc.selectNodes("//results/record" );
		Iterator iter=list.iterator();			
		// Iterate through the records
		int i=0;
		while(iter.hasNext() && i <maxRec){
           Element elt =(Element) iter.next();
           if (!elt.attributeValue("ttlength").equals("0")){
        	   //there should be stuff
        	   makeOneHTMLPage(elt,i);
        	   i++;
           }
        	   
           }

	}
	
	
	private void makeOneHTMLPage(Element elt, int i) {
		Document htmldocument =  DocumentHelper.createDocument();
		Element htmlroot = htmldocument.addElement("html");
		
		Element player = getImmixPlayerIframe(elt.attributeValue("docid"));
		
		htmlroot.addElement("h1").addText("Testing "+elt.attributeValue("identifier"));
		htmlroot.addElement("p").addText("doc id = " + elt.attributeValue("docid"));
		htmlroot.addElement("hr");
		htmlroot.addElement("h2").addText("terms");
		Element terms = htmlroot.addElement("p");
		
		List<Element> strelements =  ((Element) elt.elements("terms").get(0)).elements("term");
		for(Element one: strelements){
			terms.addText(one.getText());
			terms.addElement("br");
			
		}
		
		htmlroot.addElement("hr");
		htmlroot.addElement("p").addElement("a").addAttribute("href","survey_"+ Integer.toString(i+1) +".html").addText("next");

		
		htmlroot.addElement("hr");
		htmlroot.addElement("p").add(player);


		try {
			write (htmldocument, "survey_"+ Integer.toString(i) +".html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	private Element getImmixPlayerIframe(String docID) {
		String source = "http://immix.beeldengeluid.nl/extranet/index.aspx?chapterid=1164&filterid=974&contentid=240&searchID=4366198&itemsOnPage=10&startrow=1&resultitemid=1&nrofresults=733198&verityid="+ docID + "@expressies";
		Element player = DocumentHelper.createElement("iframe");
		player.addAttribute("src", source);
		player.addAttribute("height", "700");
		player.addAttribute("width", "700");
		
		return player;
	}


	public Document openXMLFile(String fileName) throws SAXException, IOException, DocumentException 
    { 
			  File file = new File(fileName);
			  SAXReader reader = new SAXReader();
			  return reader.read(file);

    }
	
	
	 public void write(Document document, String fileName) throws IOException {

	        // lets write to a file
	        XMLWriter writer = new XMLWriter(
	            new FileWriter( fileName )
	        );
	        writer.write( document );
	        writer.close();


	        // Pretty print the document to System.out
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        writer = new XMLWriter( System.out, format );
	        writer.write( document );

	        // Compact format to System.out
	        format = OutputFormat.createCompactFormat();
	        writer = new XMLWriter( System.out, format );
	        writer.write( document );
	    }
	
	public static void main(String[] args) {
		SurveyMaker svm = new SurveyMaker();
		Document doc;
		try {
			doc = svm.openXMLFile("C:/Users/vdboer/git/textract/textract/output1411566113110.xml");
			svm.xml2SurveyHTML(doc, svm.max);

		} catch (DocumentException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
	}

}
