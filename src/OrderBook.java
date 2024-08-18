/////////////////////////////////////////////////////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////////////////////////////////////////////////////

import java.util.*;
//import javafx.util.Pair;
import java.util.Map.Entry;


public class OrderBook {
	
	Map<Integer, Integer> askMap = new TreeMap<Integer, Integer>();
	Map<Integer, Integer> bidMap = new TreeMap<Integer, Integer>(Collections.reverseOrder());

	Map<Integer, Integer> getAsks() { return askMap; }
	Map<Integer, Integer> getBids() { return bidMap; }
	
	
	String getString() {
		
		String str="";
		 
		NavigableMap<Integer, Integer> askMapDesc = ((TreeMap<Integer, Integer>) askMap).descendingMap();
		 
		for (Map.Entry<Integer,Integer> entry : askMapDesc.entrySet()) {
			if (entry.getValue()!=0) {
				str = str + "Ask, " + Global.inst().formatPrice(entry.getKey()) + ", " + entry.getValue() + "\n";	
			}else {
				str = str + "Ask, " + "-------"  + ", " + "----" + "\n";
			}
		}
		
		str = str + "---, -------\n";
		
		for (Map.Entry<Integer,Integer> entry : bidMap.entrySet()) {
			if (entry.getValue()!=0) {
				str = str + "Bid, " + Global.inst().formatPrice(entry.getKey()) + ", " + entry.getValue() + "\n";
			}else {
				str = str + "Bid, " + "-------"  + ", " + "----" + "\n";
			}
		}
		
		return str;
	}
	
	int getAskDepth() { return askMap.size(); }
	int getBidDepth() { return bidMap.size(); }

	int getAskAverage(int EndDepth) { // exclude endDepth 
		int qty = 0;
		int count=0;
		for (Map.Entry<Integer,Integer> entry : askMap.entrySet()) {
			if (entry.getValue()!=0) {
				qty += 	entry.getValue();
				if ( (++count) >= EndDepth)
					break;
			}
		}
		
		if (count==0) 	return 1;
		else			return (qty/count); 
	}
	
	int getBidAverage(int EndDepth) { // exclude endDepth 
		int qty = 0;
		int count=0;
		for (Map.Entry<Integer,Integer> entry : bidMap.entrySet()) {
			if (entry.getValue()!=0) {
				qty += 	entry.getValue();
				if ( (++count) >= EndDepth)
					break;
			}
		}
		
		if (count==0) return 1;
		else  		  return (qty/count); 
	}
	
	
	int getAskQuantity(int Depth) { 
		int i=0;
		NavigableMap<Integer, Integer> askMapDesc = ((TreeMap<Integer, Integer>) askMap).descendingMap();
		 
		for (Map.Entry<Integer,Integer> entry : askMapDesc.entrySet()) {
			if (i++==Depth) {
				return entry.getValue();
			}
		}
		return 0;
	}
	
	int getBidQuantity(int Depth) { 
		int i=0;
		for (Map.Entry<Integer,Integer> entry : bidMap.entrySet()) {
			if (i++==Depth) {
				return entry.getValue();
			}
		}
		return 0;
	}
	
	int getAskSum() { 
		int qty = 0;
		int count=0;
		for (Map.Entry<Integer,Integer> entry : askMap.entrySet()) {
			if (entry.getValue()!=0) {
				qty += 	entry.getValue();
				if ( ++count > 5)
					break;
			}
		}
		return qty; 
	}
	int getBidSum() {
		int qty = 0;
		int count=0;
		for (Map.Entry<Integer,Integer> entry : bidMap.entrySet()) {
			if (entry.getValue()!=0) {
				qty += 	entry.getValue();
				if ( ++count > 5)
					break;
			}
		}
		return qty; 
	}

	
//	Pair<Integer, Integer> getBestAsk() {
	Entry<Integer, Integer> getBestAsk() {
	
		if (askMap.size()==0) {
			askMap.put(99999999, 0);
		}
		
		Entry<Integer, Integer> entry= ((TreeMap<Integer, Integer>)askMap).firstEntry();
		return entry;

	}

//	Pair<Integer, Integer> getBestBid() {
	Entry<Integer, Integer> getBestBid() {
		
		if (bidMap.size()==0) {
			bidMap.put(0, 0);
		}
		
		Entry<Integer, Integer> entry= ((TreeMap<Integer, Integer>)bidMap).firstEntry();
		return entry;// new Pair<Integer, Integer>(entry.getKey(),entry.getValue());
	}

	void enterNewOrder(boolean bBuyOrder, int price, int quantity)	{
			
		if (bBuyOrder) { //buy order -> bidMap
			
			if (bidMap.containsKey(price)) // has record
			{
				int qty = bidMap.get(price) + quantity;
				bidMap.put(price, qty);
			}else {
				bidMap.put(price, quantity);
			}
		}else { // sell order - > askMap

			if (askMap.containsKey(price)) // has record
			{
				int qty = askMap.get(price) + quantity;
				askMap.put(price, qty);
			}else {
				askMap.put(price, quantity);
			}
		}
	}

	void cancelOrder(boolean bBuyOrder, int price, int canceledQuantity)	{

		if (bBuyOrder) { //buy order -> bidMap
			if (bidMap.containsKey(price)) // has record
			{
				int qty = bidMap.get(price) - canceledQuantity;
				
				if (qty>0) {
					bidMap.put(price, qty);
				}else {
					bidMap.remove(price);
				}
			}
		} else { // sell order -> askMap
			if (askMap.containsKey(price)) // has record
			{
				int qty = askMap.get(price) - canceledQuantity;
				
				if (qty>0) {
					askMap.put(price, qty);
				}else {
					askMap.remove(price);
				}
			}
		}
	}

	void executeOrder(boolean bBuyOrder, int price, int executedQuantity)	{
		
		if (bBuyOrder) { //buy order -> bidMap
			if (bidMap.containsKey(price)) // has record
			{
				int qty = bidMap.get(price) - executedQuantity;
				
				if (qty>0) {
					bidMap.put(price, qty);
				}else {
					bidMap.remove(price);
				}
			}
		} else { // sell order -> askMap
			if (askMap.containsKey(price)) // has record
			{
				int qty = askMap.get(price) - executedQuantity;
				
				if (qty>0) {
					askMap.put(price, qty);
				}else {
					askMap.remove(price);
				}
			}
		}

	}

//	public static void main(String[] args) {
//		OrderBook book = new OrderBook();
//		book.test();
//	}
//	
//	public void test() {
//
//		Order ordb1 = new Order("FUTURE",1,1,1,'b',101,200,93001001,1);
//		Order ordb2 = new Order("FUTURE",1,1,1,'b',102,300,93002001,2);
//
//		enterNewOrder(ordb1.isBuyOrder(),ordb1.getPrice(),ordb1.getQuantity());
//		enterNewOrder(ordb2.isBuyOrder(),ordb1.getPrice(),ordb1.getQuantity());
//		
//		ordb1.cancel(91001111);
//		cancelOrder(ordb1.isBuyOrder(),ordb1.getPrice(),ordb1.getCanceledQuantity());
//		
//		ordb2.execute(103, 200, 91001111);
//		executeOrder(ordb2.isBuyOrder(),ordb1.getPrice(),ordb1.getLastExeQuantity());
//
//		Order ords1 = new Order("FUTURE",1,1,2,'s',111,100,93001001,3);
//		Order ords2 = new Order("FUTURE",1,1,2,'s',112,200,93002001,4);
//
//		enterNewOrder(ords1.isBuyOrder(),ords1.getPrice(),ords1.getQuantity());
//		enterNewOrder(ords2.isBuyOrder(),ords2.getPrice(),ords2.getQuantity());
//
//		
//		Map<Integer, Integer>mAsk = getAsks();
//		Map<Integer, Integer>mBid = getBids();
//
//		for (Map.Entry<Integer,Integer> entry : mAsk.entrySet()) {
//			System.out.println("ask : " + entry.getKey() + " => " + entry.getValue());
//		}
//
//		for (Map.Entry<Integer,Integer> entry : mBid.entrySet()) {
//			System.out.println("bid : " +entry.getKey() + " => " + entry.getValue());
//		}
//		
//		Entry<Integer,Integer> ask = getBestAsk();
//		if (ask!=null)
//			System.out.println("ask : " +ask.getKey() + " : " + ask.getValue());
//		
//		Entry<Integer,Integer> bid = getBestBid();
//		if (ask!=null)
//			System.out.println("bid : " +bid.getKey() + " : " + bid.getValue());
//
//	}

}
