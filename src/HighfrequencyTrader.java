import java.util.LinkedList;


/////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////  

public class HighfrequencyTrader extends Trader {

	static final int TRADER_TYPE  = 3;
	static final int TRADE_SPEED  = 100*Config.TICK_MS; //from original code 
	static final int ORDER_LENGTH = 10*Config.TICK_MS;
	//static final int TRADE_SPEED  = 1000*10;  // copy from Yang's paper
	//static final int ORDER_LENGTH = 1000;

	HighfrequencyTrader(int accountID, String symbols[]){
		super(accountID,symbols,TRADER_TYPE,TRADE_SPEED,ORDER_LENGTH);
	}

	@Override
	protected void respondOrders(LinkedList <Order> ordList)	{
	}
	
	// This strategy only fit future market, you can change it to fit different symbols
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
		
		Matching match = currentMarket.getMatching(symbol);
		Level1 l1      = match.getLevel1();

		int  posQty = pos.getQuantity();
		int  ordPrice = 0;
		char ordType='b';
		int  cond=0;
		

		
		if (posQty>3) {// try to cover 'long position' 

			if (rand.nextInt(100)<60) {
				cond=1;
				ordPrice =  l1.getExecutedPrice() - (rand.nextInt(4)-3)*Config.DOLLAR;
			} else { // remain 40%
				if (rand.nextInt(100)<10) 	{ 
					cond=2; 
					ordPrice =  l1.getAskPrice() - (rand.nextInt(4)-3)*Config.DOLLAR; 
				}	else 	{ 
					cond=3; 
					ordPrice =  l1.getAskPrice();	
				}
			}
			ordType = 's';
			
		} else if (posQty<-3) {// try to cover 'short position' 

			if (rand.nextInt(100)<60) {
				cond = 4;
				ordPrice =  l1.getExecutedPrice() + (rand.nextInt(4)-3)*Config.DOLLAR;
			}else {
				if (rand.nextInt(100)<10) 	{ 
					cond = 5; 
					ordPrice =  l1.getBidPrice() + (rand.nextInt(4)-3)*Config.DOLLAR; 
				} else	{ 
					cond = 6; 
					ordPrice =  l1.getBidPrice(); 
				}
			}
			ordType = 'b';
			
		}
		else  {//  place order randomly 
			 
			if ( Math.abs(match.getLastMA() - l1.getExecutedPrice()/(5*Config.DOLLAR) ) > 1006*Config.DOLLAR ) {
				
				if ((match.getLastMA()-1000*Config.DOLLAR) > l1.getExecutedPrice()/(5*Config.DOLLAR)) {
					cond = 7;
					ordType = 'b';
					ordPrice =  l1.getBidPrice()      + (rand.nextInt(4)-3)*Config.DOLLAR;		
				}	else  {
					cond = 8;
					ordType = 's';
					ordPrice =  l1.getExecutedPrice() - 1*Config.DOLLAR;			
				}
			}	else	{
				
				if (rand.nextInt(100)<30) {
					if (posQty>=0) {
						cond = 9;
						ordType='s';
						ordPrice =  l1.getAskPrice() - (rand.nextInt(4)-3)*Config.DOLLAR; 
					}	else {
						cond = 10;
						ordType='b';
						ordPrice =  l1.getBidPrice() + (rand.nextInt(4)-3)*Config.DOLLAR;
					}
				}	else {
					int askDepth=match.getOrderBook().getAskDepth();
					int bidDepth=match.getOrderBook().getBidDepth();
					if ( (askDepth-bidDepth) >= (rand.nextInt(21)-10)) {
						ordType='s';
						cond = 11;
						ordPrice =  l1.getExecutedPrice() - (rand.nextInt(3)-1)*Config.DOLLAR;
					}	else	{
						cond = 12;
						ordType='b';
						ordPrice =  l1.getExecutedPrice() + (rand.nextInt(3)-1)*Config.DOLLAR;
					}
				}
			}
		}
 
		if (ordPrice!=0) {
			
			int ordQty = findOrderQuantity();
			Order newOrd=new Order(symbol, Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, ordType, ordPrice, ordQty, curTime,cond,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(newOrd);
		}
	}
	
	int findOrderQuantity() {
		int r=rand.nextInt(100);
		
		if (r > 95) return 7;
		else if (r>90) return 6;
		else if (r>85) return 5;
		else if (r>80) return 4;
		else if (r>75) return 3;
		else if (r>57) return 2;
		else return 1;
	}
	


//	public static void main(String[] args) {
//	
//	System.out.println("Start ..." + Global.inst().getTick());
//	
//	String[] symbols =  Global.inst().getSymbols();
//	
//	for (int i=0; i<symbols.length; i++) {
//		Exchange.inst().setSymbol(symbols[i]);
//	}
//
//	OutPut.inst().openOrder();
//	Matching match =  Exchange.inst().getMatching(symbols[0]);
//	
//	HighfrequencyTrader trader = new HighfrequencyTrader(1,symbols) ;
//	
//	for (int i=0; i<20000; i++) {
//		
//		System.out.println("Tick : " + i);
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		
//		match.show();
//		
//		trader.retrieve();
//		
//		for (Map.Entry<Integer, Order> entry : trader.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Saved Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : trader.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//		System.out.println("");
//	}
//	
//	OutPut.inst().closeOrder();
//	System.out.println("It's done, tick is " + Global.inst().getTick());
//	
//	System.exit(0);
//}
//	
//	public static void main(String[] args) {
//	
//		
//		System.out.println("Start ..." + Global.inst().getTick());
//		
//		String[] symbols =  Global.inst().getSymbols();
//		
//		for (int i=0; i<symbols.length; i++) {
//			Exchange.inst().setSymbol(symbols[i]);
//		}
//
//		OutPut.inst().openOrder();
//		Matching match =  Exchange.inst().getMatching(symbols[0]);
//		
//		HighfrequencyTrader trader = new HighfrequencyTrader(1,symbols) ;
//		match.show();
//		
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.retrieve();	
//
//		match.show();
//		
//		for (Map.Entry<Integer, Order> entry : trader.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : trader.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//			
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.retrieve();	
//
//		match.show();
//		for (Map.Entry<Integer, Order> entry : trader.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : trader.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//		
//		Global.inst().touchTick();
//		trader.ask();
//		Exchange.inst().match();
//		trader.retrieve();	
//		
//		System.out.println("forth");
//		
//		match.show();
//		for (Map.Entry<Integer, Order> entry : trader.mapOrder.entrySet()) {
//			Order ord = entry.getValue();
//			System.out.printf("Response Order: order=%s \n"  , ord.getString());
//		}
//		for (Map.Entry<String, Position> entry : trader.mapPosition.entrySet()) {
//			Position pos = entry.getValue();
//			System.out.printf("Pos: %s \n"  , pos.getTitle());
//			System.out.printf("Pos: %s \n"  , pos.getString());
//		}
//			
//		OutPut.inst().closeOrder();
//		System.out.println("It's done, tick is " + Global.inst().getTick());
//		
//		System.exit(0);
//	}
	
}


