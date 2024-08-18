/////////////////////////////////////////////////////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////////////////////////////////////////////////////

import java.lang.Math;
import java.util.Map;
import java.util.TreeMap;
 

public class Position {
	
	// Variables definitions
	// basic information
	//	private String symbol;
		private int    time=0;
		private int    quantity=0;
		private int    price=0; //It is multiplied by 10000
		private int    profitAndLoss=0; // profit and loss  
		
		// summarized order information working on this position
	
		private int   sumBoughtQty=0;
		private int   avgBoughtPrice =0;
		private int   sumSoldQty  =0;
		private int   avgSoldPrice   =0;
		private int   sumCxlQty=0;  
		
		private int   activeBuyOrderQty=0;
		private int   activeSellOrderQty=0;
	
		private int    numbOrder=0;
		private int    sumOrder=0;
	
		protected Map<Integer, Integer>   mapBuyLevel 	= new TreeMap<>();
		protected Map<Integer, Integer>   mapSellLevel 	= new TreeMap<>();
	
 
	String getTitle() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
				"time",
				"quantity",
				"price",
				"PnL",
				"sumBoughtQty",
				"avgBoughtPrice",
				"sumSoldQty",
				"avgSoldPrice",
				"sumCxlQty",
				"activeOrderQty"
				);
	}
	
	String getString() {
		return String.format("%s,%d,%s,%s,%d,%s,%d,%s,%d,%d",
				TickTime.format(time),
				quantity,
				Global.inst().formatPrice(price),
				Global.inst().formatPrice(profitAndLoss),
				sumBoughtQty,
				Global.inst().formatPrice(avgBoughtPrice),
				sumSoldQty,
				Global.inst().formatPrice(avgSoldPrice),
				sumCxlQty,
				activeBuyOrderQty +	activeSellOrderQty
				);
	}
	
	// Function definitions
 
	int getsumCanceledQty() 	 { return sumCxlQty; }
	int getActiveOrderQty() 	 { return activeBuyOrderQty + activeSellOrderQty;	} 

	int getActiveBuyOrderQty()   { return activeBuyOrderQty; }
	int getActiveSellOrderQty()  { return activeSellOrderQty; }
	
	int getActiveBuyOrderLevelNum()   { return mapBuyLevel.size(); }
	int getActiveSellOrderLevelNum()  { return mapSellLevel.size(); }

	

//	void  	setSymbol(String symbol) { this.symbol = symbol; }
	
//	String 	getSymbol() 		{ return symbol; }
	int 	getQuantity() 		{ return quantity; }
	int 	getPrice() 			{ return price; }
	int 	getProfitAndLoss()	{ return profitAndLoss; }
	int 	getTime() 			{ return time; }
	
 
	
	void responseOrder(Order ord) {

		Map<Integer, Integer>   map = null;
		if (ord.isBuyOrder()) 	map = mapBuyLevel;
		else					map = mapSellLevel;
		

		if (ord.isAccepted()) { //new order
			addActiveOrderQty(ord.getBuySell(), ord.getQuantity());
			
			Integer nNum = map.get(ord.getPrice());
			if (nNum==null)		map.put(ord.getPrice(),1);
			else				map.put(ord.getPrice(),nNum+1);
		} 
		else {

			if (ord.isExecuted()) { // executed order
				addExecute(ord.getOrderID(), ord.getBuySell(),ord.getLastExePrice(),ord.getLastExeQuantity(), ord.getLastExeTime());
			}else if (ord.isCanceled()) { //canceled order
				addCxlOrderQuantity(ord.getBuySell(), ord.getCanceledQuantity());
			}
			
			if (ord.getLiveQuantity()<=0) {
				Integer nNum = map.get(ord.getPrice());
				if(nNum==null || nNum<=1)	map.remove(ord.getPrice());
				else						map.put(ord.getPrice(),nNum-1);
			}
		} 	
	}

	private void addActiveOrderQty(char buySell, int qty) {

		if (buySell=='b')  { //buy
			activeBuyOrderQty += qty;
		} else {  //sell
			activeSellOrderQty += qty;
		}
		numbOrder ++;
		sumOrder += qty;
	}
	
	private void addCxlOrderQuantity(char buySell, int qty) { 

		sumCxlQty += qty;
		
		if (buySell=='b')  { //buy
			activeBuyOrderQty -= qty;
		} else {  //sell
			activeSellOrderQty -= qty;
		}
		
		if ( (activeBuyOrderQty<0) ||(activeSellOrderQty<0) )  {
			int ii=0;
			ii++;
		}

	}

	private void addExecute( int id, char buySell, int px, int qty, int tm) {
		
		
		if (buySell=='b')  { //buy
			activeBuyOrderQty -= qty;
		} else {  //sell
			activeSellOrderQty -= qty;
		}
		

		if ( (activeBuyOrderQty<0) ||(activeSellOrderQty<0) )  {
			int ii=0;
			ii++;
		}
		
		cumulateBySide(buySell,px,qty);
		
		if (buySell=='s')  //sell
			qty *= -1;
		
		if (quantity==0) {// is empty, set
			this.quantity = qty;
			this.price = px;
			this.time = tm;
		}	
		else if (quantity>0) { //has long position
			if (buySell=='b') { 
				addPosition(px,qty,tm);  // buy to open
			}else {
				coverPosition(px,qty,tm); // sell to close
			}
		}
		else { // <0  , has short position
			if (buySell=='s') { 
				addPosition(px,qty,tm); // short to add
			}else {
				coverPosition(px,qty,tm);  // buy to cover
			}
		} 
	}

	private void cumulateBySide(char buySell, int px, int qty) { 
		if (buySell=='b') { //buy
			double bSum = 0.001*sumBoughtQty*avgBoughtPrice + 0.001*px*qty;
			sumBoughtQty += qty;
			avgBoughtPrice = (int)(bSum/sumBoughtQty*1000);  
		} else { // sell
			double sSum = 0.001*sumSoldQty*avgSoldPrice + 0.001*px*qty;
			sumSoldQty += qty;
			avgSoldPrice = (int)(sSum/sumSoldQty*1000);  
		}
	} 
	
	private void addPosition(int px, int qty, int tm) {
		double totalValue = 0.001*this.price*this.quantity + 0.001*px*qty;
		this.quantity += qty;
		this.price = (int)(totalValue/this.quantity*1000);
	}

	private void coverPosition(int px, int qty, int tm) {
		
		if (Math.abs(this.quantity)>=Math.abs(qty)) {
			profitAndLoss += qty*(this.price-px);
		}else {
			profitAndLoss += quantity*(px-this.price);
			this.price = px;
		}
		this.quantity += qty;
	}
 
/*
	public static void main(String[] args) {
		Position pos = new Position();

			
		Order ord=new Order("SPY", 1, 1, 1, 'b', 1123, 100, 90201001,1);
		pos.responseOrder(ord);	
		
		ord.execute(1123,10, 90221001);
		pos.responseOrder(ord);
		
		ord.execute(1123,20, 90221001);
		pos.responseOrder(ord);

		ord.cancel( 90221001);
		pos.responseOrder(ord);

		
		System.out.printf("0: Price=%d, Quantity=%d, PnL=%d \n",pos.getPrice(), pos.getQuantity(), pos.getProfitAndLoss() );
	}
*/
}
