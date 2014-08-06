package textract;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TermFrequency {

	
	private HashMap<String, Integer> wordFrequencyMap ;
	private String wfFile = "./assets/word_frequencies.csv";
	

	public TermFrequency() {
		try {
			wordFrequencyMap = readwfFile(wfFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public HashMap<String, Integer> readwfFile (String fileName) throws IOException{
		System.out.print("Loading term frequencies..");
		HashMap<String,Integer> map = new HashMap<String, Integer>();
		FileReader fileReader = new FileReader(fileName);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);
	    String line = null;
	    
	    while ((line = bufferedReader.readLine()) != null) {
	    	String str[] = line.split(";");
	    	map.put(str[0], Integer.parseInt(str[1]));
	    }
	    bufferedReader.close();
		System.out.println("done.");
	    return map;
	    }
	
	public int getWordFrequency(String word){
		if (wordFrequencyMap.get(word) == null) {return 1;} 
		else return wordFrequencyMap.get(word) ;
	}
	
	public HashMap<String, Integer> getWordFrequencyMap() {
		return wordFrequencyMap;
	}

	public void setWordFrequencyMap(HashMap<String, Integer> wordFrequencyMap) {
		this.wordFrequencyMap = wordFrequencyMap;
	}
	
	public static void main(String[] args) {
		TermFrequency tf = new TermFrequency();
		System.out.println(tf.getWordFrequency("mijn"));
		System.out.println(tf.getWordFrequency("naam"));
		System.out.println(tf.getWordFrequency("is"));
		System.out.println(tf.getWordFrequency("haas"));
		System.out.println(tf.getWordFrequency("finland"));
		System.out.println(tf.getWordFrequency("Finland"));

	}
		
}
