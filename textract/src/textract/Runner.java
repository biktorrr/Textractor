package textract;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.json.simple.parser.ParseException;

import se.kb.oai.OAIException;

public class Runner {
	// Run all for Bouke
	
	
	public static String getNextDate(String prevDate) throws java.text.ParseException{
		//"2014-01-06T04:59:00Z"
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Calendar c = Calendar.getInstance();
		c.setTime(sdf.parse(prevDate));
		c.add(Calendar.DATE, 1);  // number of days to add
		return sdf.format(c.getTime());  // dt is now the new date
		}
	
	
	public static void main(String[] args) {
		Textractor gogo = new Textractor();
		
		String start = "2013-09-01T00:00:00Z";
		String end = "2013-09-02T00:00:00Z";
		
		String filename = "output" + Long.toString(new Date().getTime()) + ".csv";

		FileWriter fw;
		try {
			fw = new FileWriter(filename,true);
			 fw.write("OAI_ID ; DOC_ID ; AS ; URI ; PREFLABEL ; SCORE\n");//appends the string to the file
			    fw.close();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		} 
		   
		for (int i=0;i<3;i++){
			
			try {
				gogo.setTheResults(gogo.run());
				String result = gogo.resultToCSV(gogo.getTheResults());	
				FileWriter fw2 = new FileWriter(filename,true); //the true will append the new data
			    fw2.write(result);//appends the string to the file
			    fw2.close();
			    
			} catch (DocumentException | OAIException | ParseException
					| IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
			
			i++;
			start = end;
			try {
				end = getNextDate(start);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			} 
		}

		
	}


}
