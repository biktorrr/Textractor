package textract;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class StopWords {

	private  ArrayList<String> stopwords ;
	
	public ArrayList<String> getStopwords() {
		return stopwords;
	}

	public void setStopwords(ArrayList<String> stopwords) {
		this.stopwords = stopwords;
	}

	public StopWords(){
		try {
			stopwords = this.readStopWordFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public StopWords(String filename){
		try {
			stopwords = this.readStopWordFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public ArrayList<String> readStopWordFile () throws IOException{
		return readStopWordFile("./src/textract/dutch-stop-words.txt");
	   
	    }
	
	public ArrayList<String> readStopWordFile (String fileName) throws IOException{
		FileReader fileReader = new FileReader(fileName);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);
	    ArrayList<String> lines = new ArrayList<String>();
	    String line = null;
	    while ((line = bufferedReader.readLine()) != null) {
	        lines.add(line);
	    }
	    bufferedReader.close();
	    return lines;
	    }
	
}
