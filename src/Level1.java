////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
////  

import java.util.Map;

//import javafx.util.Pair; // this is for JDK 8.0 and lower version, 

public class Level1 {
 
	static final int ASK_BID_SPREAD=1000;
	private String symbol;
	private int    time=0;
	private int    askPrice=0;  
	private int    askSize=0;
	private int    bidPrice=0; 
	private int    bidSize=0;
	private int    executedTime;  
	private int    executedPrice=0;
	private int    executedVolume=0;
	private int    totalVolume=0;

	void initialize(int price) {
		executedPrice=price;
	}
	
	void setSymbol(String symbol) { this.symbol = symbol; }

	String getString() {
		return String.format("%s,%s,%s,%d,%s,%d,%s,%s,%d,%d",
				symbol,
				TickTime.format(time),
				Global.inst().formatPrice(askPrice),  
				askSize,
				Global.inst().formatPrice(bidPrice),
				bidSize,
				TickTime.format(executedTime),
				Global.inst().formatPrice(executedPrice),
				executedVolume,
				totalVolume
				);
	}
	String getSymbol(){ return symbol; }
	int    getTime(){ return time; }
	int    getAskPrice()	{
				if (askSize==0) {
					if (bidSize!=0) { return (bidPrice + ASK_BID_SPREAD);}
					else 			{ return (executedPrice + ASK_BID_SPREAD/2); } 
				}
				else				{ return askPrice; } 
		   } 
	int    getAskSize(){ return askSize; }
	int    getBidPrice()	{ 
				if (bidSize==0) {
					if (askSize!=0)	{	return (askPrice - ASK_BID_SPREAD); }
					else			{	return (executedPrice - ASK_BID_SPREAD/2); } 
				}
				else				{ 	return bidPrice;				} 
		   }
	
	int    getBidSize(){ return bidSize; }
	int    getExecutedTime(){ return executedTime; } 
	int    getExecutedPrice(){ return executedPrice; }
	int    getExecutedVolume(){ return executedVolume; }
	int    getTotalVolume() { return totalVolume; }
	
	
	
	void enterAsk(int time, int askPrice, int askSize) {
		this.time = time;
		this.askPrice = askPrice;
		this.askSize = askSize;
	}
 
	void enterAsk(int time, Map.Entry<Integer, Integer>ask) {
		enterAsk(time, ask.getKey(), ask.getValue());
	}

	
	void enterBid(int time, int bidPrice, int bidSize) {
		this.time = time;
		this.bidPrice =  bidPrice;
		this.bidSize = bidSize;
	}
	void enterBid(int time, Map.Entry<Integer, Integer>bid) {
		enterBid(time,bid.getKey(), bid.getValue());
	}
	
	void enterExecution(int executedTime, int executedPrice, int executedVolume) {
		this.executedTime = executedTime;
		this.executedPrice = executedPrice;
		this.executedVolume = executedVolume;
		this.totalVolume += executedVolume;
	}

	
//	public static void main(String[] args) {
//		
//		Level1 l1 = new Level1();
//		
//		l1.enterAsk(93001001, 123000,100);
//		l1.enterBid(93001002, 124010,200);
//		Pair<Integer, Integer>ask = new Pair<>(122,300);  l1.enterAsk(93002001, ask);
//		Pair<Integer, Integer>bid = new Pair<>(124,500);  l1.enterBid(93002003, bid);
//		System.out.println("Ask : " + l1.getTime() + ", "+ l1.getAskPrice() + ", " + l1.getAskSize());
//		System.out.println("Bid : " + l1.getTime() + ", "+l1.getBidPrice() + ", " + l1.getBidSize());
//		
//		l1.enterExecution(93003001,125000,600);
//		l1.enterExecution(93003001,127000,300);
//		System.out.println("Exe : " + l1.getExecutedTime() + ", "+l1.getExecutedPrice() + 
//							", " + l1.getExecutedVolume()+ ", " + l1.getTotalVolume());
//		
//		l1.setSymbol("SPY");
//		System.out.println("Name : " + l1.getSymbol());
//	}
	
}
