import java.util.LinkedList;

/////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////

public class OpportunisticTrader extends Trader {

	static final int TRADER_TYPE = 5;
	static final int TRADE_SPEED  = 8000*Config.TICK_MS;// copy from original code
	static final int ORDER_LENGTH = 800*Config.TICK_MS;
	//static final int TRADE_SPEED  = 2*60*1000;  // copy from Yang's paper
	//static final int ORDER_LENGTH = 2*60*1000/10;
	 
	OpportunisticTrader(int accountID, String symbols[]){
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
			
			Position pos   = mapPosition.get(symbols[i]);
			if (pos.getActiveOrderQty() != 0)
				continue;
			
			int pct = rand.nextInt(100);
			
			if (pct<5)		generateOrders1(symbols[i], curTime, ordList);	
			else if (pct<10)	generateOrders2(symbols[i], curTime, ordList);
			else if (pct<55)	generateOrders3(symbols[i], curTime, ordList);
			else				generateOrders4(symbols[i], curTime, ordList);
		}
		
		return ordList;
	}
	
	void generateOrders1(String symbol, int curTime, LinkedList <Order> ordList) 
	{
		Position pos   = mapPosition.get(symbol);
		
		Matching match = currentMarket.getMatching(symbol);
		Level1 l1      = match.getLevel1();
	
		int  condition=0;
		int  ordPrice = 0;
		char ordType='b';
		int  posQty = pos.getQuantity();
	
		if (posQty>4) {// Mandatory sell to cover 'long position' 
			condition = 1;
			ordType  = 's';
			ordPrice =  l1.getExecutedPrice() - (rand.nextInt(4)-3)*Config.DOLLAR;
			
		}	else if (posQty<-4) {// Mandatory buy to cover 'short position' 
			condition = 2;
			ordType  = 'b';
			ordPrice =  l1.getExecutedPrice() + (rand.nextInt(4)-2)*Config.DOLLAR;
			
		}	else if (posQty>2) {// try to cover 'long position' 

			ordType  = 's';
			if (rand.nextInt(100) < 10)	{
				condition = 3;
				ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
			}	else {
				if (rand.nextInt(100) < 50) {
					condition = 4;
					ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
				}else {
					condition = 5;
					ordPrice =  l1.getAskPrice();
				}
			}
		}	else { // if (posQty<=-2)// try to cover 'short position' 
			
			int askDepth=match.getOrderBook().getAskDepth();
			int bidDepth=match.getOrderBook().getBidDepth();
			if ( (askDepth-bidDepth) >= 30) {
				ordType  = 'b';
				if (rand.nextInt(100) < 10)	{
					condition = 6;
					ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
				}	else {
					if (rand.nextInt(100) < 50) {
						condition = 7;
						ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
					} else {
						condition = 8;
						ordPrice =  l1.getBidPrice();
					}
				}
			}	else {
				condition = 9;
				ordType  = 'b';
				ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
			}
		}
		
		if (ordPrice!=0) {
			int ordQty = findOrderQuantity();
			Order newOrd=new Order(symbol, Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, ordType, ordPrice, ordQty, curTime,condition,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(newOrd);
		}
	}
	
	void generateOrders2(String symbol, int curTime, LinkedList <Order> ordList) 
	{
		Position pos   = mapPosition.get(symbol);
		Matching match = currentMarket.getMatching(symbol);
		Level1 l1      = match.getLevel1();
	
		int  condition = 0;
		int  ordPrice = 0;
		char ordType='b';
		int  posQty = pos.getQuantity();
	
		if (posQty>1) {// Mandatory sell to cover 'long position' 
 			ordType  = 's';
			if (rand.nextInt(100) < 10)			{
				condition =  11;
				ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
			} else	{
				if (rand.nextInt(100) < 90) {
					condition =  12;
					ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;
				} else {
					condition =  13;
					ordPrice =  l1.getAskPrice();
				}
			}
		} else if (posQty<-1) {// Mandatory buy to cover 'short position' 
			ordType  = 'b';
			if (rand.nextInt(100) < 10)			{
				condition =  14;
				ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
			}	else	{
				if (rand.nextInt(100) < 90) {
					condition =  15;
					ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;
				} else {
					condition =  16;
					ordPrice =  l1.getBidPrice();
				}
			}
		}	else  { //[-1,1] 
 			ordType  = 's';
			if ( Math.abs(match.getLastMA() - l1.getExecutedPrice()/(5*Config.DOLLAR) ) > 1010*Config.DOLLAR ) {
				if ( match.getLastMA()-l1.getExecutedPrice()/(5*Config.DOLLAR)  > 1000*Config.DOLLAR ){
					condition =  17;
					ordType = 'b';
					ordPrice =  l1.getBidPrice() + 1*Config.DOLLAR;		
				}	else			{
					condition =  18;
					ordType = 's';
					ordPrice =  l1.getExecutedPrice() - 1*Config.DOLLAR;			
				}
			}	else {
				if (rand.nextInt(100) < 25)		{
					if (posQty>0) 	{
						condition =  19;
						ordType = 's';
						ordPrice =  l1.getAskPrice();
					}	else {
						condition =  20;
						ordType = 'b';
						ordPrice =  l1.getBidPrice();
					}
				} else	{
					int numBid = match.getNumberOfBidOrder();
					int numAsk = match.getNumberOfAskOrder();
					if (rand.nextInt(100) < 50)		{
						if (numAsk>=numBid) 	{
							condition =  21;
							ordType = 's';
							ordPrice =  l1.getAskPrice() - 2*Config.DOLLAR;		
						}	else	{	
							condition =  22;
							ordType = 'b';
							ordPrice =  l1.getBidPrice() + 2*Config.DOLLAR;			
						}
					}	else	{
						if (numAsk>=numBid)		{
							condition =  23;
							ordType = 's';
							ordPrice =  l1.getAskPrice();		
						}	else	{	
							condition =  24;
							ordType = 'b';
							ordPrice =  l1.getBidPrice() ;			
						}
					}
				}
			}
		}
		
		if (ordPrice!=0) {
			int ordQty = findOrderQuantity();
			Order newOrd=new Order(symbol, Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, ordType, ordPrice, ordQty, curTime, condition,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(newOrd);
		}
	}
	
	void generateOrders3(String symbol, int curTime, LinkedList <Order> ordList) 
	{
		Matching macth = currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		int ordQty = findOrderQuantity();
		int ordPrice =  findSellPrice(l1);
		
		Order ordSell=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 's', ordPrice, ordQty, curTime,30,
				 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
		ordList.add(ordSell);
	}
	
	void generateOrders4(String symbol, int curTime, LinkedList <Order> ordList) 
	{
		Matching macth = currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();

		int ordQty = findOrderQuantity();
		int ordPrice =  findBuyPrice(l1);
		
		Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 'b', ordPrice, ordQty, curTime,31,
				 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
		ordList.add(ordBuy);
	}
	
	int findOrderQuantity() {
		int r = rand.nextInt(100);
		
		if (r > 97) return 7;
		else if (r>94) return 6;
		else if (r>91) return 5;
		else if (r>87) return 4;
		else if (r>82) return 3;
		else if (r>66) return 2;
		else return 1;
	}

	int findSellPrice(Level1 l1) {
		int r = rand.nextInt(100);
		
		if 		(r>96) return (l1.getExecutedPrice() + 10*Config.DOLLAR);
		else if (r>90) return (l1.getExecutedPrice() + 9*Config.DOLLAR);
		else if (r>85) return (l1.getExecutedPrice() + 8*Config.DOLLAR);
		else if (r>80) return (l1.getExecutedPrice() + 7*Config.DOLLAR);
		else if (r>75) return (l1.getExecutedPrice() + 6*Config.DOLLAR);
		else if (r>70) return (l1.getExecutedPrice() + 5*Config.DOLLAR);
		else if (r>65) return (l1.getExecutedPrice() + 4*Config.DOLLAR);
		else if (r>60) return (l1.getExecutedPrice() + 3*Config.DOLLAR);
		else if (r>55) return (l1.getExecutedPrice() + 2*Config.DOLLAR);
		else if (r>35) return (l1.getExecutedPrice() + 1*Config.DOLLAR);
		else { return (l1.getExecutedPrice() - 5*Config.DOLLAR); }
	}
	int findBuyPrice(Level1 l1) {
		int r = rand.nextInt(100);
		
		if 		(r>96) return (l1.getExecutedPrice() - 10*Config.DOLLAR);
		else if (r>90) return (l1.getExecutedPrice() - 9*Config.DOLLAR);
		else if (r>85) return (l1.getExecutedPrice() - 8*Config.DOLLAR);
		else if (r>80) return (l1.getExecutedPrice() - 7*Config.DOLLAR);
		else if (r>75) return (l1.getExecutedPrice() - 6*Config.DOLLAR);
		else if (r>70) return (l1.getExecutedPrice() - 5*Config.DOLLAR);
		else if (r>65) return (l1.getExecutedPrice() - 4*Config.DOLLAR);
		else if (r>60) return (l1.getExecutedPrice() - 3*Config.DOLLAR);
		else if (r>55) return (l1.getExecutedPrice() - 2*Config.DOLLAR);
		else if (r>35) return (l1.getExecutedPrice() - 1*Config.DOLLAR);
		else { return (l1.getExecutedPrice() + 5*Config.DOLLAR); }
	}
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.print("Hello MarketMaker!");
//	}
}

