package textract;

	import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

	public class WordFrequencyCounter {

	    public class Word implements Comparable<Word>{
	        String word;
	        int count;
	        public Word(String word, int count) {
	            this.word = word;
	            this.count = count;
	        }
	        public int compareTo(Word otherWord) {
	            if(this.count==otherWord.count){
	                return this.word.compareTo(otherWord.word);
	            }
	            return otherWord.count-this.count;
	        }
	    }

	    private Word[] getFrequentWords(String words[]){
	        HashMap<String,Word> map = new HashMap<String,Word>();
	        for(String s:words){
	            Word w = map.get(s);
	            if(w==null)
	                w = new Word(s,1);
	            else
	                w.count++;
	            map.put(s, w);
	        }
	        Word[] list = map.values().toArray(new Word[]{});
	        Arrays.sort(list);
	        return list;
	    }
	    
	    public Word[] getFrequentTokensFromString(String myString){
	    	StringTokenizer st = new StringTokenizer(myString);
	    	int size = st.countTokens();
	    	String[] tokens = new String[size];
	    	int i = 0;
	    	while (st.hasMoreTokens()) {
		    	String curToken = st.nextToken();
		    	tokens[i] = curToken.toLowerCase();
		    	i++;
	    	}
	    	return this.getFrequentWords(tokens);
	    }

	    // For NGrams
	    public Word[] getFrequentNGramsFromString(String myString, int n){
	    	ArrayList<String> ngrams = new ArrayList<String>();
	    	
	    	StringTokenizer st = new StringTokenizer(myString);
	    	int size = st.countTokens();
	    	String[] tokens = new String[size];
	    	int i = 0;
	    	while (st.hasMoreTokens()) {
		    	String curToken = st.nextToken();
		    	tokens[i] = curToken.toLowerCase();
		    	i++;
	    	}
	    	
	    	int maxlength = tokens.length - n + 1;
	    	for (int j=0; j<maxlength ; j++){
	    		String ngram = "";
	    		for (int k = 0; k<n; k++){
	    			if (k>0){ ngram += " ";}
	    			ngram += tokens[j+k];
	    		}
	    		ngrams.add(ngram);
	    	}
	    	String[] ngramsArray = new String[ngrams.size()];
	    	ngramsArray = ngrams.toArray(ngramsArray);
	    	return this.getFrequentWords(ngramsArray);
	    }
	    
	    
	    public static void main(String[] args) {
	    	String test = "Hello world java code example hello hello hello";
	    	
	        Word[] frequency2 = new WordFrequencyCounter().getFrequentNGramsFromString(test, 2);
	        for(Word w:frequency2){
	            System.out.println(w.word+"="+w.count);
	        }
	    }

	}
