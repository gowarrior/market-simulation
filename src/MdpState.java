////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
////

import java.util.Iterator;
import java.util.Map;

public class MdpState {
	
	// saved information after placing a strategy of new orders
	private int   askPrice1 = 0;
	private int   askSize1  = 0;
	private int   askPrice2 = 0;
	private int   askSize2  = 0;
	private int   askPrice3 = 0;
	private int   askSize3  = 0;
	private int   bidPrice1 = 0;
	private int   bidSize1  = 0;
	private int   bidPrice2 = 0;
	private int   bidSize2  = 0;
	private int   bidPrice3 = 0;
	private int   bidSize3  = 0;
	
	private int   positionQuantity  = 0;
	private int   traderBuyQty  	 = 0;
	private int   traderSellQty 	 = 0;
	private int   traderBuyLevelNum  = 0;
	private int   traderSellLevelNum = 0;

	
	MdpState(){	}
	
	//MdpState(int pos, OrderBook book,int buyOrderQty, int sellOrderQty,int buyOrderLevelNum, int sellOrderLevelNum ){
	MdpState(Position pos, OrderBook book){
		assignPosition(pos);
		assignOrderBook(book);
	}
	
	void reset() {
		askPrice1 = 0;
		askSize1  = 0;
		askPrice2 = 0;
		askSize2  = 0;
		askPrice3 = 0;
		askSize3  = 0;
		bidPrice1 = 0;
		bidSize1  = 0;
		bidPrice2 = 0;
		bidSize2  = 0;
		bidPrice3 = 0;
		bidSize3  = 0;
		
		positionQuantity  = 0;
		traderBuyQty  = 0;
		traderSellQty  = 0;
		traderBuyLevelNum  = 0;
		traderSellLevelNum  = 0;
	}
	
	// function definitions
	static String getTitle(String prefix)	{
		return  prefix+"AskPrice1," + 
				prefix+"AskSize1," +
				prefix+"AskPrice2," + 
				prefix+"AskSize2," +
				prefix+"AskPrice3," +
				prefix+"AskSize3," +
				prefix+"BidPrice1," +
				prefix+"BidSize1," +
				prefix+"BidPrice2," +
				prefix+"BidSize2," +
				prefix+"BidPrice3," +
				prefix+"BidSize3," +
				prefix+"Position," +
				prefix+"TraderBuyQty," +
				prefix+"TraderSellQty," +
				prefix+"TraderBuyLevelNum," +
				prefix+"TraderSellLevelNum" ;
	}
	
	String getString()	{
		return  String.format("%s,%d,%s,%d,%s,%d,%s,%d,%s,%d,%s,%d,%d,%d,%d,%d,%d",
				Global.inst().formatPrice(askPrice1),
				askSize1,
				Global.inst().formatPrice(askPrice2),
				askSize2,				
				Global.inst().formatPrice(askPrice3),
				askSize3,
				Global.inst().formatPrice(bidPrice1),
				bidSize1,
				Global.inst().formatPrice(bidPrice2),
				bidSize2,
				Global.inst().formatPrice(bidPrice3),
				bidSize3,
				positionQuantity,
				traderBuyQty,
				traderSellQty,
				traderBuyLevelNum,
				traderSellLevelNum				
				);
	}
	
	int getSavedQuantity()  { return positionQuantity; }
	int getSavedAskPrice1() { return askPrice1; }
	int getSavedAskSize1()  { return askSize1; }
	int getSavedAskPrice2() { return askPrice2; }
	int getSavedAskSize2()  { return askSize2; }
	int getSavedAskPrice3() { return askPrice3; }
	int getSavedAskSize3()  { return askSize3; }
	int getSavedBidPrice1() { return bidPrice1; }
	int getSavedBidSize1()  { return bidSize1; }
	int getSavedBidPrice2() { return bidPrice2; }
	int getSavedBidSize2()  { return bidSize2; }
	int getSavedBidPrice3() { return bidPrice3; }
	int getSavedBidSize3()  { return bidSize3; }
	int getTraderBuyQty()   { return traderBuyQty;}
	int getTraderSellQty()  { return traderSellQty;}
	
	void assignPosition(Position pos)  	  	{ 
		this.positionQuantity	= pos.getQuantity();
		this.traderBuyQty		= pos.getActiveBuyOrderQty();
		this.traderSellQty		= pos.getActiveSellOrderQty();
		this.traderBuyLevelNum	= pos.getActiveBuyOrderLevelNum();
		this.traderSellLevelNum	= pos.getActiveSellOrderLevelNum();
	}

	/*
	 * void assignBuyOrderQty(int n) { this.buyOrderQty= n; } void
	 * assignSellOrderQty(int n) { this.sellOrderQty= n; } void
	 * assignBuyOrderLevelNum(int n) { this.buyOrderLevelNum=n; } void
	 * assignSellOrderLevelNum(int n) { this.sellOrderLevelNum=n; }
	 */	
	void assign(MdpState state) {
		askPrice1 = state.askPrice1;
		askSize1  = state.askSize1;
		askPrice2 = state.askPrice2;
		askSize2  = state.askSize2;
		askPrice3 = state.askPrice3;
		askSize3  = state.askSize3;
		bidPrice1 = state.bidPrice1;
		bidSize1  = state.bidSize1;
		bidPrice2 = state.bidPrice2;
		bidSize2  = state.bidSize2;
		bidPrice3 = state.bidPrice3;
		bidSize3  = state.bidSize3;
		
		positionQuantity  = state.positionQuantity;
		traderBuyQty = state.traderBuyQty;
		traderSellQty = state.traderSellQty;
		traderBuyLevelNum = state.traderBuyLevelNum;
		traderSellLevelNum = state.traderSellLevelNum;
	}

	
	void assignOrderBook(OrderBook book)	{
		// ask side 1 and 3
	    Iterator<Map.Entry<Integer, Integer>> itAsk = book.getAsks().entrySet().iterator();
	    if (itAsk.hasNext()) { // first
	    	Map.Entry<Integer, Integer> entry = itAsk.next(); 
	    	this.askPrice1= entry.getKey();
	    	this.askSize1 = entry.getValue();
	    }else {
	    	this.askPrice1= 0;
	    	this.askSize1 = 0;
	    }
	    	
	    if (itAsk.hasNext()) { // second 
	    	Map.Entry<Integer, Integer> entry = itAsk.next(); 
	    	this.askPrice2= entry.getKey();
	    	this.askSize2 = entry.getValue();
	    }else {
	    	this.askPrice2= 0;
	    	this.askSize2 = 0;
	    }
	    
	    if (itAsk.hasNext()) { // third 
	    	Map.Entry<Integer, Integer> entry = itAsk.next(); 
	    	this.askPrice3= entry.getKey();
	    	this.askSize3 = entry.getValue();
	    }else {
	    	this.askPrice3= 0;
	    	this.askSize3 = 0;
	    }

	    // bid side 1 and 3
	    Iterator<Map.Entry<Integer, Integer>> itBid = book.getBids().entrySet().iterator();
	    if (itBid.hasNext()) { // first
	    	Map.Entry<Integer, Integer> entry = itBid.next(); 
	    	this.bidPrice1= entry.getKey();
	    	this.bidSize1 = entry.getValue();
	    }   else {
	    	this.bidPrice1= 0;
	    	this.bidSize1 = 0;
	    }
	    
	    if (itBid.hasNext()) { // second 
	    	Map.Entry<Integer, Integer> entry = itBid.next(); 
	    	this.bidPrice2= entry.getKey();
	    	this.bidSize2 = entry.getValue();
	    }else {
	    	this.bidPrice2= 0;
	    	this.bidSize2 = 0;
	    }

	    if (itBid.hasNext()) { // third 
	    	Map.Entry<Integer, Integer> entry = itBid.next(); 
	    	this.bidPrice3= entry.getKey();
	    	this.bidSize3 = entry.getValue();
	    }else {
	    	this.bidPrice3= 0;
	    	this.bidSize3 = 0;
	    }
 	    
	}
	
}
