
public class Stat {

	int  trader=0; //trader type, 1-fundamentalbuyer,2,3,4,5 ..., 6-smalltrader
	int  time=0;
	int  enterQty=0;
	int  buyEnterQty=0;
	int  sellEnterQty=0;
	int  buyExecutedQty=0; 
	int  buyExecutedPx=0;
	int  sellExecutedQty=0;
	int  sellExecutedPx=0;
	int  canceledQty=0;  
 
	Stat(int trader) {
		this.trader = trader;
	}
	
	static String getTitle() {
			return  "TraderType," +
					"Time," +
					"EnterQty," +
					"BuyEnterQty," +
					"SellEnterQty," +
					"BuyExecutedQty," +
					"BuyExecutedPx," +
					"SellExecutedQty," +
					"SellExecutedPx," +
					"CanceledQty"
					;
	}

	String getString() {
		return  trader + ", " +
				TickTime.format(time) + ", " +
				enterQty + ", " +
				buyEnterQty + ", " +
				sellEnterQty + ", " +
				buyExecutedQty + ", " +
				Global.inst().formatPrice(buyExecutedPx) + ", " +
				sellExecutedQty + ", " +
				Global.inst().formatPrice(sellExecutedPx) + ", " +
				canceledQty
				;
	}
	
	int getType()  	  	 	{ return this.trader; }
	int getTime() 	  	 	{ return time; }
	int getEnterQty() 	 	{ return enterQty; }
	int getBuyEnterQty() 	{ return buyEnterQty; }
	int getSellEnterQty()	{ return sellEnterQty; }
	int getBuyExecutedQty() { return buyExecutedQty; }
	int getSellExecutedQty(){ return sellExecutedQty; }
	int getBuyExecutedPx() 	{ return buyExecutedPx; }
	int getSellExecutedPx()	{ return sellExecutedPx; }
	int getCanceledQty() 	{ return canceledQty; }

	void enter(int time, char buySell, int entQty, int exePx, int exeQty, int cxlQty ) {		

		if( (entQty==0)&&(exeQty==0)&&(cxlQty==0) ) return;
		
		// check time
		if (this.time==0) this.time = time;
		else if (this.time!=time) return ;
			
		// canceled quantity
		if (cxlQty!=0) { this.canceledQty += cxlQty; }
		
		// executed quantity and price
		if (exeQty!=0) { 
			if (buySell=='b') {
				double db = (0.001*this.buyExecutedPx*this.buyExecutedQty + 0.001*exePx*exeQty)*1000; // avoid memory flow out
				this.buyExecutedQty += exeQty;
				this.buyExecutedPx  = (int)(db/this.buyExecutedQty);
			}	else   {	
				double db = (0.001*this.sellExecutedPx*this.sellExecutedQty + 0.001*exePx*exeQty)*1000; // avoid memory flow out
				this.sellExecutedQty += exeQty;
				this.sellExecutedPx  = (int)(db/this.sellExecutedQty);
			}
		}
		
		// enter quantity
		if (entQty!=0) { 
			this.enterQty += entQty; 	
			if (buySell=='b') {	this.buyEnterQty  += entQty; }
			else 			  {	this.sellEnterQty += entQty; }
		}
	}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}

}