/////////////////////////////////////////////////////
//Time       : Coder     : Email            : Actions
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////////////////////////////////////////////////////


import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

public class Matching {

// Variable definitions
	private String symbol;
	
	private Map<Long, Order> buyQueue = new TreeMap<>(Collections.reverseOrder());
	private Map<Long, Order> sellQueue = new TreeMap<>();
	private Map<Integer, LinkedList<Order>> responseMap = new TreeMap<>();
	
	private OrderBook book = new OrderBook();
	private Chart  millisChart = new Chart(Chart.SECOND,50,1200); 		// 50 ms, 20*60=1200 bars , total 1 minute
	private Chart  minuteChart = new Chart(Chart.MINUTE,60*1000,1200); 	// 60*1000ms =1 minute, 1200 bars (minutes) present 20 hours
	private StatHolder minuteStats = new StatHolder(60*1000,1200);  //  60*1000ms =1 minute, 1200 bars (minutes) present 20 hours
	private Level1    l1= new Level1();
	
// Function definitions
	
	// constructor
	Matching(String symbol)	{	
		this.symbol = symbol;	
	
		l1.setSymbol(symbol);
		l1.initialize(Global.inst().getOpenPrice(symbol));
	} 

	// get functions
	String getSymbol() 					{ return symbol; }
	OrderBook getOrderBook() 			{ return book;	};
	Chart getMillisChart() 				{ return millisChart; }; //default second  , 50 ms per bar 
	Chart getMinuteChart() 				{ return minuteChart; };
	StatHolder getMinuteStats() 		{ return minuteStats; };
	double getLastReturn() 				{ return millisChart.getLastReturn();	}
	Level1 getLevel1() 					{ return l1;	}
	
	 Map<Long, Order> getBuyQueue()		{ return buyQueue; }
	 Map<Long, Order> getSellQueue()	{ return sellQueue; }
	
	 int getNumberOfBidOrder()  { return buyQueue.size(); }
	 int getNumberOfAskOrder()  { return sellQueue.size(); }
	 
	int getLastMA() { return (int)millisChart.getLastMA(20); }; // 20 points*50ms = 1 second
	 
 	public void placeOrder(Order ord) {

		if (ord.isNewOrder()) {
			Order newOrder = new Order(ord);
			
			book.enterNewOrder(newOrder.isBuyOrder(), newOrder.getPrice(), newOrder.getQuantity());
			
			if (newOrder.isBuyOrder()) { // buy -> buyQueue
				buyQueue.put(combineBuyKey(newOrder.getPrice(),newOrder.getOrderID()), newOrder);
			//	l1.enterBid(Global.inst().getCurrentTime(), book.getBestBid());
			}else { // sell ->sell queue
				sellQueue.put(combineSellKey(newOrder.getPrice(),newOrder.getOrderID()), newOrder);
				//l1.enterAsk(Global.inst().getCurrentTime(), book.getBestAsk());
			}

			LinkedList<Order> lstOrd = executeOrder();

			AddResponseMap(new Order(ord)); // new accepted order
			
			for(Order order: lstOrd)	      {	  
				AddResponseMap(order); // executed orders
		    }
			
		} else if (ord.isCancelOrder()) { 

			// find saved order using client's order
			Order savedOrd =null;
			if (ord.isBuyOrder()) { // buy -> buyQueue
				savedOrd = buyQueue.get(combineBuyKey(ord.getPrice(),ord.getOrderID()));
			} else {
				savedOrd = sellQueue.get(combineSellKey(ord.getPrice(),ord.getOrderID()));
			}
			
			if ( (savedOrd==null) || (savedOrd.getLiveQuantity()<=0) ) // cancel live order 
				return;

			savedOrd.toCancel(ord); // cancel saved order
			
			book.cancelOrder(savedOrd.isBuyOrder(),savedOrd.getPrice(), savedOrd.getCanceledQuantity());
			
			if (savedOrd.isBuyOrder()) { // buy -> buyQueue
				buyQueue.remove(combineBuyKey(savedOrd.getPrice(),savedOrd.getOrderID()));
				l1.enterBid(Global.inst().getCurrentTime(), book.getBestBid());
				
			} else {
				sellQueue.remove(combineSellKey(savedOrd.getPrice(),savedOrd.getOrderID()));
				l1.enterAsk(Global.inst().getCurrentTime(), book.getBestAsk());
			}
			AddResponseMap(savedOrd);
		}
	}

	// match existing order from buy queue and sell queue
	public LinkedList<Order> executeOrder() {

		Iterator<Map.Entry<Long, Order>> sellIt = sellQueue.entrySet().iterator();
		Iterator<Map.Entry<Long, Order>> buyIt = buyQueue.entrySet().iterator();
		
		boolean toBuyNext=true, toSellNext=true;
		Map.Entry<Long, Order> sellEntry=null;
		Map.Entry<Long, Order> buyEntry= null;
		
		LinkedList<Order> lstOrder = new LinkedList<>();
		
		while ( ( (toBuyNext&&buyIt.hasNext()) || (toBuyNext==false) )  && 
				( (toSellNext&&sellIt.hasNext()) || (toSellNext==false) ) ) {

			if ( toBuyNext )	{ buyEntry  = buyIt.next();	 }
			if ( toSellNext ) 	{ sellEntry = sellIt.next(); }
			
			
			Order sellOrder = sellEntry.getValue();
			Order buyOrder  = buyEntry.getValue();
			
			if (buyOrder.getPrice()>=sellOrder.getPrice()) { // prices are crossed
				
				int exePrice =  sellOrder.getPrice();
				int exeQty = sellOrder.getLiveQuantity();
				
				if (buyOrder.getTime()<sellOrder.getTime()) { // newer order will get the executed price of older order 
					exePrice = buyOrder.getPrice();
				}
				if (buyOrder.getLiveQuantity()<sellOrder.getLiveQuantity()) { // executed quantity is the live quantity of smaller one
					exeQty =  buyOrder.getLiveQuantity();
				}
				
				// maintain order book and response map of Buy Order
				buyOrder.toExecute(exePrice, exeQty, Global.inst().getCurrentTime());
				book.executeOrder(true, buyOrder.getPrice(), exeQty);
				lstOrder.add(new Order(buyOrder));
				//AddResponseMap(buyOrder);
				
				// maintain order book and response map of Sell Order
				sellOrder.toExecute(exePrice, exeQty, Global.inst().getCurrentTime());
				book.executeOrder(false, sellOrder.getPrice(), exeQty);		
				lstOrder.add(new Order(sellOrder));
				//AddResponseMap(sellOrder);

				// remove if live quantity <= zero 
				if (buyOrder.getLiveQuantity()<=0) {
					buyIt.remove();
					toBuyNext = true;
				}else {toBuyNext = false;}

				// remove if live quantity <= zero
				if (sellOrder.getLiveQuantity()<=0)	{
					sellIt.remove();
					toSellNext = true;
				}else {toSellNext = false;}
				
			//	l1.enterExecution(Global.inst().getCurrentTime(),exePrice,exeQty);
				
			}else {
				break;
			}
		}
		
				
		// recalculate Level1
		l1.enterAsk(Global.inst().getCurrentTime(), book.getBestAsk());
		l1.enterBid(Global.inst().getCurrentTime(), book.getBestBid());
		
		return lstOrder;
	}
	
	// call by trader using account id
	//@Override
	public void response(int accountID, LinkedList <Order> orders) {
//	public LinkedList <Order> receiveResponse(int accountID) { 
		
		if (responseMap.containsKey(accountID)==false) {
			return ;
		}else {
			LinkedList <Order> retLst= responseMap.get(accountID);
			orders.addAll(retLst);
			responseMap.remove(accountID);
		}
	};	

	private long combineBuyKey(int price,int id) { // sorted by price and inverse order ID, same price, smaller id will be matched earlier
		return 1000000000L*price+(1000000000L-id);   // id is [0, 1000000000L)
	}

	private long combineSellKey(int price,int id) { // sorted by price and order ID 
		return 1000000000L*price+id;   // id is [0, 1000000000L)
	}

 	// Add canceled and executed order for response 
	private void AddResponseMap(Order ord) {
		
		Order respOrder = new Order(ord);
		
		// maintain minute chart
		millisChart.enterOrder(ord); 
		minuteChart.enterOrder(ord); 
		minuteStats.enter(ord);
		
		if ( responseMap.containsKey(respOrder.getAccountID()) ) {
			LinkedList<Order> lst = responseMap.get(respOrder.getAccountID());
			lst.add(respOrder);			
		}else { //empty
			LinkedList<Order> lst = new LinkedList<Order>();
			lst.add(respOrder);
			responseMap.put(respOrder.getAccountID(), lst);
		}
	}

	public void show() {
		OrderBook book=getOrderBook();
		for (Map.Entry<Integer,Integer> entry : book.getAsks().entrySet()) {
			System.out.println("book-ask side: " + entry.getKey() + " => " + entry.getValue());
		}
		for (Map.Entry<Integer,Integer> entry :  book.getBids().entrySet()) {
			System.out.println("book-bid side: " +entry.getKey() + " => " + entry.getValue());
		}
		
		Level1 l1 = getLevel1();
		System.out.println("Best Ask : " + l1.getTime() + ", "+ l1.getAskPrice() + ", " + l1.getAskSize());
		System.out.println("Best Bid : " + l1.getTime() + ", "+l1.getBidPrice() + ", " + l1.getBidSize());
		
		Map<Long, Order> bQueue = getBuyQueue();
		for (Map.Entry<Long,Order> entry :  bQueue.entrySet()) {
			System.out.printf("buy queue: key=%d, order=%s\n" ,entry.getKey() , entry.getValue().getString());
		}
		
		Map<Long, Order> sQueue = getSellQueue();
		for (Map.Entry<Long,Order> entry :  sQueue.entrySet()) {
			System.out.printf("sell queue: key=%d, order=%s \n" ,entry.getKey() , entry.getValue().getString());
		}
		
		Chart  cht  = getMillisChart();
		LinkedList<Bar>  bars = cht.getBars();
		System.out.println("Bar : " + Bar.getTitle());
		for (Bar bar: bars) {
			System.out.println("Bar : " + bar.getString());
		}
		
		System.out.println("Response  Order:" + Order.getTitle());
		for ( Map.Entry<Integer, LinkedList<Order>> entryLst :	responseMap.entrySet()) {
			for (Order ord: entryLst.getValue()) {
				System.out.println("Response Order : " + ord.getString());
			}
		}
		
		System.out.println("" );
		
	}
	
//	public static void main(String[] args) { //	for unit test 
//	
//	Matching match = new Matching("FUTURE");
//	System.out.print(match.symbol+"\n");
//	
//	int curTime = 1;//Global.inst().getTime();
//	
//	Order ordBuy1=new Order("FUTURE",1, 1, 1, 'b', 10000, 100, curTime,0);
//	Order ordBuy2=new Order("FUTURE",2, 1, 1, 'b', 10010, 200, curTime,0);
//	Order ordBuy3=new Order("FUTURE",3, 1, 1, 'b', 10100, 100, curTime,0);
//
//	LinkedList <Order> orders = new LinkedList <>();
//	orders.add(ordBuy1);
//	orders.add(ordBuy2);
//	orders.add(ordBuy3);
//	match.placeOrder(orders) ;
//
//	Order ordSell1=new Order("FUTURE",4, 1, 1, 's', 10200, 100, curTime,0);
//	Order ordSell2=new Order("FUTURE",5, 1, 1, 's', 10100, 100, curTime,0);
//	Order ordSell3=new Order("FUTURE",6, 1, 1, 's', 10300, 200, curTime,0);
//	match.placeOrder(ordSell1) ;
//	match.placeOrder(ordSell2) ;
//	match.placeOrder(ordSell3) ;
//
//	match.show();
//	
//	System.out.println("match");
//	match.match();
//	match.show();
//	
//	ordSell3.cancel(Global.inst().getCurrentTime());
//	match.placeOrder(ordSell3) ;
//	match.show();
//	LinkedList <Order> ordList = new LinkedList <>();
//	match.response(1, ordList); 
//	for(Order ord: ordList)	      {
//		System.out.printf("Response Order: order=%s \n"  , ord.getString());
//	}
//	match.show();
//}

}
