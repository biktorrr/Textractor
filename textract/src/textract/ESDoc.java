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

	@Override
	public boolean equals(Object o) {
		ESDoc to = (ESDoc) o;
		if (to.uri.equals(this.uri)){
			return true;
		}
		else
			return false;
	}
	
	
	
}