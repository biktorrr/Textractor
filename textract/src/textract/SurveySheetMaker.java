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
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

// from output, make html form thing
public class SurveySheetMaker {
	
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
        	   makeOneSheet(elt,i);
        	   i++;
           }
        	   
           }

	}
	
	
	private void makeOneSheet(Element elt, int i) {
		Document htmldocument =  DocumentHelper.createDocument();
		Element htmlroot = htmldocument.addElement("html").addElement("body");
		
		htmlroot.addCDATA("<?php $towrite = $_POST; ?>");
		htmlroot.addCDATA("<?php file_put_contents(\"result.txt\", $towrite, FILE_APPEND); ?>");
		
		Element player = getImmixPlayerIframe(elt.attributeValue("docid"));
		
		htmlroot.addElement("h1").addText("Testing "+elt.attributeValue("identifier"));
		
		
		
		Element intro = htmlroot.addElement("p").addText("doc id = " + elt.attributeValue("docid"));
		intro.addElement("br");
		intro.addText("user = ");
		intro.addCDATA("<?php echo $_GET[\"person\"]; ?>");
		
		
		
		htmlroot.addElement("hr");
		htmlroot.addElement("h2").addText("terms");
		Element form = htmlroot.addElement("form")
				.addAttribute("action","survey_"+ Integer.toString(i+1) + ".php")
				.addAttribute("method","POST");
		
		Element tab = form.addElement("table");
		Element row1 = tab.addElement("tr");
		row1.addElement("td").addElement("b").addText("Term");
		row1.addElement("td").addAttribute("align","middle").addElement("b").addText("0");
		row1.addElement("td").addAttribute("align","middle").addElement("b").addText("1");
		row1.addElement("td").addAttribute("align","middle").addElement("b").addText("2");
		row1.addElement("td").addAttribute("align","middle").addElement("b").addText("3");
		row1.addElement("td").addAttribute("align","middle").addElement("b").addText("4");

		
		List<Element> strelements =  ((Element) elt.elements("terms").get(0)).elements("term");
		for(Element one: strelements){
			Element row = tab.addElement("tr");
			Element row2 = row.addElement("td").addText(one.getText()+ " ("+one.attributeValue("axis")+ "): ");
			row2.addElement("td").addElement("input").addAttribute("type","radio").addAttribute("name",one.attributeValue("uri")).addAttribute("value","0");
			row2.addElement("td").addElement("input").addAttribute("type","radio").addAttribute("name",one.attributeValue("uri")).addAttribute("value","1");
			row2.addElement("td").addElement("input").addAttribute("type","radio").addAttribute("name",one.attributeValue("uri")).addAttribute("value","2");
			row2.addElement("td").addElement("input").addAttribute("type","radio").addAttribute("name",one.attributeValue("uri")).addAttribute("value","3");
			row2.addElement("td").addElement("input").addAttribute("type","radio").addAttribute("name",one.attributeValue("uri")).addAttribute("value","4");
		}
		
		form.addElement("input").addAttribute("type","hidden").addAttribute("name","id").addAttribute("value",elt.attributeValue("identifier"));
		form.addElement("input").addAttribute("type","text").addAttribute("name","person").addAttribute("value","<?php echo $_POST[\"person\"]; ?>");
		form.addElement("input").addAttribute("type","submit").addAttribute("value","next");
		
		htmlroot.addElement("hr");
		htmlroot.addElement("p").addElement("a").addAttribute("href","survey_"+ Integer.toString(i+1) +".html").addText("next");

		
		htmlroot.addElement("hr");
		htmlroot.addElement("p").add(player);


		try {
			write (htmldocument, "survey_"+ Integer.toString(i) +".php");
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
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        HTMLWriter writer = new HTMLWriter(
	            new FileWriter( fileName ), format
	        );
	        writer.write( document );
	        writer.close();

	    }
	
	public static void main(String[] args) {
		SurveySheetMaker svm = new SurveySheetMaker();
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
