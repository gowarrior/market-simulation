import java.util.LinkedList;


/////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////  

public class FundamentalSeller extends Trader {
	
	static final int TRADER_TYPE = 2;
	static final int TRADE_SPEED = 4000*Config.TICK_MS;
	static final int ORDER_LENGTH = 400*Config.TICK_MS;
	//static final int TRADE_SPEED  = 60*1000*10;  // copy from Yang's paper
	//static final int ORDER_LENGTH = 60*1000;


	FundamentalSeller(int accountID, String symbols[]){
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
		int ii=0;
		Position pos   = mapPosition.get(symbol);
		if (pos.getActiveOrderQty() != 0) {
			ii++;
			return;
		}
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		if (pos.getQuantity()==0) { // new order
			int px =  findSellPrice(l1);
			int qty   =  findOrderQuantity();
			Order ordSell=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 's', px, qty, curTime,1,
					 Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordSell);

		}else { // cover order
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
	
	
	int findSellPrice(Level1 l1) {
		int r=rand.nextInt(100);
		
		if (r>97) return (l1.getExecutedPrice() + 10*Config.DOLLAR);
		else if (r>92) return (l1.getExecutedPrice() + 9*Config.DOLLAR);
		else if (r>87) return (l1.getExecutedPrice() + 8*Config.DOLLAR);
		else if (r>82) return (l1.getExecutedPrice() + 7*Config.DOLLAR);
		else if (r>75) return (l1.getExecutedPrice() + 6*Config.DOLLAR);
		else if (r>68) return (l1.getExecutedPrice() + 5*Config.DOLLAR);
		else if (r>61) return (l1.getExecutedPrice() + 4*Config.DOLLAR);
		else if (r>52) return (l1.getExecutedPrice() + 3*Config.DOLLAR);
		else if (r>41) return (l1.getExecutedPrice() + 2*Config.DOLLAR);
		else if (r>13) return (l1.getExecutedPrice() + 1*Config.DOLLAR);
		else { return (l1.getExecutedPrice() - 5*Config.DOLLAR); }
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
//		System.out.printf("Hello FundamentalSeller!=%d",Math.abs(-1));
//	}

}


 
