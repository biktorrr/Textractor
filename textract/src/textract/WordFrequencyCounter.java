package textract;

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

	    public static void main(String[] args) {
	    	String test = "Hello world java code example hello hello hello";
	    	
	    	
	        Word[] frequency = new WordFrequencyCounter().getFrequentTokensFromString(test);
	        for(Word w:frequency){
	            System.out.println(w.word+"="+w.count);
	        }
	    }

	}
