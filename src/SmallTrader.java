///////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
///////

import java.util.LinkedList;

public class SmallTrader extends Trader {
	

	 static final int TRADER_TYPE = 6;
	 static final int TRADE_SPEED  = 120000*Config.TICK_MS;
	 static final int ORDER_LENGTH = 12000*Config.TICK_MS;
	 //static final int TRADE_SPEED  = 2*60*60*1000;  // copy from Yang's paper
	 //static final int ORDER_LENGTH = 2*60*60*1000/10;


	 SmallTrader(int accountID, String symbols[]){
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
	
	void generateOrdersBySymbol(String symbol, int curTime, LinkedList <Order> ordList) 	{
		
		Position pos   = mapPosition.get(symbol);
		if (pos.getActiveOrderQty() != 0)
			return;
		
 		int qty   =  findOrderQuantity();
		
		if ( rand.nextBoolean() )	{ // buy
			int px =  findBuyPrice();
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 'b', px, qty, curTime,1,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordBuy);
		}
		else	{ // sell
			int px =  findSellPrice();
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 's', px, qty, curTime,2,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordBuy);
		}
	}
	
	
	int findBuyPrice() {
		Matching macth =  currentMarket.getMatching(symbols[0]);
		Level1 l1 = macth.getLevel1();

		int r=rand.nextInt(100);
		if (r>88) return (l1.getExecutedPrice() - 10*Config.DOLLAR);
		else if (r>78) return (l1.getExecutedPrice() - 9*Config.DOLLAR);
		else if (r>73) return (l1.getExecutedPrice() - 8*Config.DOLLAR);
		else if (r>68) return (l1.getExecutedPrice() - 7*Config.DOLLAR);
		else if (r>61) return (l1.getExecutedPrice() - 6*Config.DOLLAR);
		else if (r>54) return (l1.getExecutedPrice() - 5*Config.DOLLAR);
		else if (r>47) return (l1.getExecutedPrice() - 4*Config.DOLLAR);
		else if (r>40) return (l1.getExecutedPrice() - 3*Config.DOLLAR);
		else if (r>31) return (l1.getExecutedPrice() - 2*Config.DOLLAR);
		else if (r>20) return (l1.getExecutedPrice() - 1*Config.DOLLAR);
		else { return (l1.getExecutedPrice() + 5*Config.DOLLAR); }
	}

	int findSellPrice() {
		Matching macth =  currentMarket.getMatching(symbols[0]);
		Level1 l1 = macth.getLevel1();
		
		int r=rand.nextInt(100);		
		if (r>88) return (l1.getExecutedPrice() + 10*Config.DOLLAR);
		else if (r>78) return (l1.getExecutedPrice() + 9*Config.DOLLAR);
		else if (r>73) return (l1.getExecutedPrice() + 8*Config.DOLLAR);
		else if (r>68) return (l1.getExecutedPrice() + 7*Config.DOLLAR);
		else if (r>61) return (l1.getExecutedPrice() + 6*Config.DOLLAR);
		else if (r>54) return (l1.getExecutedPrice() + 5*Config.DOLLAR);
		else if (r>47) return (l1.getExecutedPrice() + 4*Config.DOLLAR);
		else if (r>40) return (l1.getExecutedPrice() + 3*Config.DOLLAR);
		else if (r>31) return (l1.getExecutedPrice() + 2*Config.DOLLAR);
		else if (r>20) return (l1.getExecutedPrice() + 1*Config.DOLLAR);
		else { return (l1.getExecutedPrice() - 5*Config.DOLLAR); }
	}
 
	int findOrderQuantity() {
		int r=rand.nextInt(100);
		
		if (r > 98) return 2;
		else return 1;
	}
	
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.print("Hello SmallTrader!");
//	}

}

 

