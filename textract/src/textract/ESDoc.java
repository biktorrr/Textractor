package textract;

// an elasticsearch document. 
// Actually also just the internal representation of a GTAA concept
public class ESDoc {
	public String preflabel;
	public String altlabel;
	public String conceptSchemes;
	public String uri;

	public String toString(){
		return "gtaa:" + preflabel;
	}
	
}