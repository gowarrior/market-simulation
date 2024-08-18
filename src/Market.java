
import java.util.Map;
import java.util.TreeMap;

public class Market  {

	private Map<String, Matching> symbolMap = new TreeMap<>();


	public Matching getMatching(String symbol) 	{
		if (symbolMap.containsKey(symbol))
			return  symbolMap.get(symbol);
		else
			return new Matching(symbol);
	}
	
	public void setSymbol(String symbol) 	{
		
		if (symbolMap.containsKey(symbol)==false) {
			symbolMap.put(symbol, new Matching(symbol));
		}
	}
	
//	public static void main(String[] args) {
//	}
	
	public Market()	{} 
	
}
