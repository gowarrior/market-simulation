import java.util.LinkedList;

///////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
///////

public class MarketMaker extends Trader {

	static final int TRADER_TYPE = 4;
	static final int TRADE_SPEED  = 2000*Config.TICK_MS; // copy from original code
	static final int ORDER_LENGTH = 200*Config.TICK_MS;
//	static final int TRADE_SPEED  = 20*1000*10;  // copy from Yang's paper
//	static final int ORDER_LENGTH = 20*1000;

	MarketMaker(int accountID, String symbols[]){
		super(accountID,symbols,TRADER_TYPE,TRADE_SPEED,ORDER_LENGTH);
	}
	
	@Override
	protected void respondOrders(LinkedList <Order> ordList)	{
	}

	// This strategy only fit future market, you can change it to fit different symbols
	@Override
	protected LinkedList <Order> generateNewOders()
	{
		LinkedList <Order> ordList = new LinkedList <>();
		int curTime = Global.inst().getCurrentTime();
		
		for(int i = 0; i< symbols.length; i++){
			generateOrdersBySymbol(symbols[i], curTime, ordList);
		}
		
		return ordList;
	}
	
	void generateOrdersBySymbol(String symbol, int curTime, LinkedList <Order> ordList) 
	{
		Position pos   = mapPosition.get(symbol);
		if (pos.getActiveOrderQty() != 0)
			return;
		
		Matching match = currentMarket.getMatching(symbol);
		Level1 l1      = match.getLevel1();

		int  posQty = pos.getQuantity();
		int  ordPrice = 0;
		char ordType = 'b';
		int  condition = 0;

		
		if (posQty>4) {// try to cover 'long position' 
			condition = 1;
			ordType = 's';
			ordPrice =  l1.getExecutedPrice() - (rand.nextInt(4)-3)*Config.DOLLAR;	
		
		}	else if (posQty<-4)		{
			condition = 2;
			ordType = 'b';
			ordPrice =  l1.getExecutedPrice() + (rand.nextInt(4)-2)*Config.DOLLAR;			
		
		}	else  if (posQty>2) {// try to cover 'long position' 

			if (rand.nextInt(100)<10)	{
				condition = 3;
				ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
			}	else  { // remain 90%
				if (rand.nextInt(100)<50) 	{
					condition = 4;
					ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR; 
				}	else 	{ 
					condition = 5;
					ordPrice =  l1.getAskPrice();	
				}
			}
			ordType = 's';
			
		}	else if (posQty<-2) {// try to cover 'short position' 

			if ( (match.getOrderBook().getAskDepth()-match.getOrderBook().getBidDepth()) >= 30 ) {
				ordType = 'b';	
				if (rand.nextInt(100)<10)	{
					condition = 6;
					ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
				}	else	{
					if (rand.nextInt(100)<50)		{
						condition = 7;
						ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
					}	else	{
						condition = 8;
						ordPrice =  l1.getBidPrice();
					}
				}
			}	else		{
				condition = 9;
				ordType = 's';	// ?why to sell more
				ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
			}
		}	else  { //  place order randomly 
			
			if ( Math.abs(match.getLastMA() - l1.getExecutedPrice()/(5*Config.DOLLAR) ) > 1010*Config.DOLLAR ) {

				if ((match.getLastMA()-1000*Config.DOLLAR) > l1.getExecutedPrice()/(5*Config.DOLLAR))	{
					condition = 11;
					ordType = 'b';
					ordPrice =  l1.getBidPrice() + 1*Config.DOLLAR;
				}	else	{
					condition = 12;
					ordType = 's';
					ordPrice =  l1.getExecutedPrice() - 1*Config.DOLLAR;
				}

			}	else{
				if (rand.nextInt(100)<25)	{
					if (posQty>0)	{
						condition = 13;
						ordType='s';
						ordPrice =  l1.getAskPrice(); 
					}	else {
						condition = 14;
						ordType='b';
						ordPrice =  l1.getBidPrice();
					}
				}	else {
					int askDepth=match.getOrderBook().getAskDepth();
					int bidDepth=match.getOrderBook().getBidDepth();

					if (rand.nextInt(100)<50)	{
						if ( (askDepth>=bidDepth) && (askDepth<=(bidDepth+15)) ) {
							condition = 15;
							ordType='s';
							ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
						}	else	{
							condition = 16;
							ordType='b';
							ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
						}
					}	else {
						if ( (askDepth>=bidDepth) && (askDepth<=(bidDepth+15)) ) {
							condition = 17;
							ordType='s';
							ordPrice =  l1.getAskPrice();
						}	else {
							condition = 18;
							ordType='b';
							ordPrice =  l1.getBidPrice();
						}
					}
				}
			}
		}

		if (ordPrice!=0) {
			int ordQty = findOrderQuantity();
			Order newOrd=new Order(symbol, Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, ordType, ordPrice, ordQty, curTime,condition,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(newOrd);
		}
	}
	
	int findOrderQuantity() {
		int r=rand.nextInt(100);
		
		if (r > 93) return 5;
		else if (r>88) return 4;
		else if (r>82) return 3;
		else if (r>76) return 2;
		else return 1;
	}
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.print("Hello MarketMaker!");
//	}

}




