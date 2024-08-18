////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
////  

import java.util.*;
import java.lang.Math; 


public class Global {

	// variables	
	private int orderID=1;
	private int accountID=1;
	
	private boolean pausedMarket=false;
	private  TickTime currentTime = Config.START_MARKET_TIME; 									// market start time
	private TreeMap<String,Integer> openPriceMap = new TreeMap<String,Integer>();

	
	int 	 getPlusOrderID() 			 { return orderID++;	}
	int 	 getPlusAccountID()			 { return accountID++;	}

	int 	 getOpenPrice(String symbol) { return openPriceMap.get(symbol);	}
	String[] getSymbols() 	  			 { return openPriceMap.keySet().toArray(new String[0]);	}
	void 	 tick() 	  	  { currentTime.add(Config.TICK_MS); }; // plus milliseconds per touch
	int 	 getCurrentTime() { return currentTime.getTime();	}
	boolean  IsRunningTime()  { return currentTime.getTime() <= Config.END_MARKET_TIME.getTime(); }  // market end time	
	boolean  isPausedMarket() 			 { return pausedMarket; }
	void 	 setPausedMarket(boolean b) { pausedMarket=b;}

	boolean	 isOnDisplayInterval() 	 	{
		int id=getCurrentTime()%Config.DISPLAY_INTERVAL;
		return (id==0); 
	}
	void 	 setDisplayInterval(int n) { Config.DISPLAY_INTERVAL=n; }
	
	String 	 formatPrice(int price) {	return String.format("%.2f",1.0/Config.DOLLAR*price);	}
	
	int getPoissonRandom(double mean) 	{
	    Random r = new Random();
	    double L = Math.exp(-mean);
	    int k = 0;
	    double p = 1.0;
	    do {
	        p = p * r.nextDouble();
	        k++;
	    } while (p > L);
	    return k - 1;
	}
 
	int getRandPoisson(int mean) 	{
		int randid = mean/10; 		// only generate poisson(10)
		if (randid<=0) randid=1;
		int remind  = mean/randid; 	// 100 ms
		
		return Global.inst().getPoissonRandom(remind)*randid;
	}

    


 	// get instance and static functions 
	private Global()		{
		if (Config.INIT_SYMBOL.length!=Config.INIT_PRICE.length) {
			System.out.println("The number of initial symbols is not equal to intial price!");
			return;
		}

		for (int i=0; i<Config.INIT_SYMBOL.length; i++) {
			openPriceMap.put(Config.INIT_SYMBOL[i],Config.INIT_PRICE[i]); 
		}
		

	} 
	
	private static Global single_instance = null; 
	public static Global inst()	{
		if (single_instance == null)
			single_instance = new Global(); 
	  
	    return single_instance; 
	} 
}
