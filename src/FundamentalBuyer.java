////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
////  

import java.util.LinkedList;

 
public class FundamentalBuyer extends Trader {

	 static final int TRADER_TYPE = 1;
	 static final int TRADE_SPEED  = 4000*Config.TICK_MS; 
	 static final int ORDER_LENGTH = 400*Config.TICK_MS;
	 //static final int TRADE_SPEED  = 60*1000*10;  // copy from Yang's paper
	 //static final int ORDER_LENGTH = 60*1000;

	// constructors
	FundamentalBuyer(int accountID, String symbols[]){
		super(accountID,symbols,TRADER_TYPE,TRADE_SPEED,ORDER_LENGTH);
	}
 
	@Override
	protected void respondOrders(LinkedList <Order> ordList)	{
	}
	
	// This strategy only fit future market, you can change it to fit different symbols
	// generate new orders based on order book, chart, level1 and position
	@Override
	protected LinkedList <Order> generateNewOders()	{
		
		LinkedList <Order> ordList = new LinkedList <>();
		int curTime = Global.inst().getCurrentTime();
		
		for(int i = 0; i< symbols.length; i++){
			generateOrdersBySymbol(symbols[i], curTime, ordList);
		}
		
		return ordList;
	}

	void generateOrdersBySymbol(String symbol, int curTime, LinkedList <Order> ordList)	{
		
		Position pos   = mapPosition.get(symbol);
		if (pos.getActiveOrderQty() != 0)
			return;
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
	
		if (pos.getQuantity()==0) { // new order
			
			int px =  findBuyPrice(l1);
			int qty   =  findOrderQuantity();
			
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 'b', px, qty, curTime,1,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordBuy);

		} else { // cover order
			int 	qty  = pos.getQuantity();
			if (qty<0) {
				int px = l1.getAskPrice() + rand.nextInt(5)*Config.DOLLAR;
				Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 'b', px, -qty, curTime,2,
						 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
				ordList.add(ordBuy);
			} else {
				int px = l1.getBidPrice() - rand.nextInt(5)*Config.DOLLAR;
				Order ordSell=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 's', px, qty, curTime,3,
						 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
				ordList.add(ordSell);
			}
		}
	}
	


	int findBuyPrice(Level1 l1) {
		
		int r=rand.nextInt(100);
		
		if (r>97) 	   return (l1.getExecutedPrice() - 10*Config.DOLLAR);
		else if (r>92) return (l1.getExecutedPrice() - 9*Config.DOLLAR);
		else if (r>87) return (l1.getExecutedPrice() - 8*Config.DOLLAR);
		else if (r>82) return (l1.getExecutedPrice() - 7*Config.DOLLAR);
		else if (r>75) return (l1.getExecutedPrice() - 6*Config.DOLLAR);
		else if (r>68) return (l1.getExecutedPrice() - 5*Config.DOLLAR);
		else if (r>61) return (l1.getExecutedPrice() - 4*Config.DOLLAR);
		else if (r>52) return (l1.getExecutedPrice() - 3*Config.DOLLAR);
		else if (r>41) return (l1.getExecutedPrice() - 2*Config.DOLLAR);
		else if (r>13) return (l1.getExecutedPrice() - 1*Config.DOLLAR);
		else { 		   return (l1.getExecutedPrice() + 5*Config.DOLLAR); }
	}
 
	int findOrderQuantity() {
		int r=rand.nextInt(100);
		
		if (r > 95) return 7;
		else if (r>90) return 6;
		else if (r>75) return 5;
		else if (r>70) return 4;
		else if (r>65) return 3;
		else if (r>55) return 2;
		else return 1;
	}
	
	
//	public static void main(String[] args) {
//		
//		System.out.println("Start ..." + Global.inst().getTick());
//		
//		String[] symbols =  Global.inst().getSymbols();
//		
//		for (int i=0; i<symbols.length; i++) {
//			Exchange.inst().setSymbol(symbols[i]);
//		}
//
//		OutPut.inst().openMdpOrder();
//		Matching match =  Exchange.inst().getMatching(symbols[0]);
//		
//		FundamentalBuyer trader = new FundamentalBuyer(1,symbols) ;
//		
//		for (int i=0; i<20; i++) {
//			
//			System.out.println("Tick : " + i);
//			Global.inst().touchTick();
//			trader.ask();
//			Exchange.inst().match();
//			
//			match.show();
//			
//			trader.retrieve();
//			
//			for (Map.Entry<Integer, Order> entry : trader.mapOrder.entrySet()) {
//				Order ord = entry.getValue();
//				System.out.printf("Saved Order: order=%s \n"  , ord.getString());
//			}
//			for (Map.Entry<String, Position> entry : trader.mapPosition.entrySet()) {
//				Position pos = entry.getValue();
//				System.out.printf("Pos: %s \n"  , pos.getTitle());
//				System.out.printf("Pos: %s \n"  , pos.getString());
//			}
//			System.out.println("");
//		}
//		
//		OutPut.inst().closeMdpOrder();
//		System.out.println("It's done, tick is " + Global.inst().getTick());
//		
//		System.exit(0);
//	}
	
//	
//	public static void main(String[] args) {
//		
//		System.out.println("Start ..." + Global.inst().getTick());
//		
//		String[] symbols =  Global.inst().getSymbols();
//		
//		for (int i=0; i<symbols.length; i++) {
//			Exchange.inst().setSymbol(symbols[i]);
//		}
//
//		OutPut.inst().openMdpOrder();
//		Matching match =  Exchange.inst().getMatching(symbols[0]);
//		
//		FundamentalBuyer trader = new FundamentalBuyer(1,symbols) ;
	
//		match.show();
//		
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.response();	
//
//		match.show();
//		for (Map.Entry<Integer, Order> entry : buyer.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : buyer.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//			
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.response();	
//
//		match.show();
//		for (Map.Entry<Integer, Order> entry : buyer.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : buyer.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//		
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.response();	
//		
//		System.out.println("forth");
//		
//		match.show();
//		for (Map.Entry<Integer, Order> entry : buyer.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : buyer.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//			
//		OutPut.inst().closeMdpOrder();
//		System.out.println("It's done, tick is " + Global.inst().getTick());
//		
//		System.exit(0);
//	}

}



