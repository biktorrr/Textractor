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
		c.add(Calendar.HOUR_OF_DAY, 1);  // number of days to add
		return sdf.format(c.getTime());  // dt is now the new date
		}
	
	
	public static void main(String[] args) {
		
		String start = "2014-04-09T13:00:00Z";
		String end = "2014-04-09T14:00:00Z";
		
		String filename = "output" + Long.toString(new Date().getTime()) + ".csv";

		FileWriter fw;
		try {
			fw = new FileWriter(filename,true);
			 fw.write("OAI_ID ; DOC_ID ; AS ; URI ; PREFLABEL ; SCORE\n");//appends the string to the file
			    fw.close();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		} 
		   
		long starttime = System.currentTimeMillis();
		for (int i=0;i<3000;i++){
			
			try {
				Textractor gogo = new Textractor();
				gogo.setFrom(start);
				gogo.setUntil(end);
				long midtime = System.currentTimeMillis();
				System.out.println("Current duration = " + Long.toString(midtime-starttime));
				System.out.println("\n\n NEW INTERVAL: "+ i + " -> "+ start + " - "+end );

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
				
			start = end;
			try {
				end = getNextDate(start);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			} 
		}
		
		long endtime = System.currentTimeMillis();
		System.out.println("Duration = " + Long.toString(endtime-starttime));

	}


}
