import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//
//In order to maintain this program, please add your information and your work.    
//
//
//Time       : Coder     : Email            : work
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
//   
// 12 - 13 , only buy/sell on NBBO    only
// 14 -15 , buy1-sell3 on qty < 4        55%


// 16 - 17, buy1-sell3 , qty = sigma 5  55%
// 18 - 19, buy1-sell3 , qty > sigma 4.2 +mean 1.9 , 6->7 

// 20 - 21, buy1-sell3 , qty > sigma 2*4.2 +mean 1.9 , 11->12
//22 - 23, buy1-sell3 , qty > sigma 1*3.2 +mean 1.5 , 6
//24 - 230 users, spoofing and layering , rand(4) * its depths
//previous one is at 24   
//current one should be 25

public class MainSimulation {

	static Market theMarket=new Market();
	String symbols[]= {""};
	long   msfirst=0;
	
	public static void main(String[] args) {
		
		MainSimulation simulate = new MainSimulation();
		Config.ReadFromFile();
		
		simulate.run();
	}
	
	public void run()	{

		startApp();
		
		// create an array including all traders
		ArrayList<Trader> traderList = new ArrayList<>(); 
		addTraders(traderList);

		// run market to new, execute, cancel and display
		for  (; Global.inst().IsRunningTime(); Global.inst().tick()) {

			// shuffle trader list
			Collections.shuffle(traderList);
			
			//for (Iterator<Trader> iter = traderList.iterator(); iter.hasNext();) {
			for (Trader trader :  traderList) {
				trader.talk(theMarket);
			}
			
			// Display level1, bars, and order book
			DisplayMarket();				 
		}
		
		endApp();
	}
	
	void addTraders(ArrayList<Trader> traderList)	{
		int ntime=1; // for debugging

		for (int i=0; i<1268/ntime; i++) {	traderList.add( new FundamentalBuyer   (Global.inst().getPlusAccountID(), this.symbols)); }
		for (int i=0; i<1276/ntime; i++) {	traderList.add( new FundamentalSeller  (Global.inst().getPlusAccountID(), this.symbols)); }
		for (int i=0; i<16/ntime; i++)   {	traderList.add( new HighfrequencyTrader(Global.inst().getPlusAccountID(), this.symbols)); }
		for (int i=0; i<176/ntime; i++)  {	traderList.add( new MarketMaker		   (Global.inst().getPlusAccountID(), this.symbols)); }
		for (int i=0; i<5808/ntime; i++) {	traderList.add( new OpportunisticTrader(Global.inst().getPlusAccountID(), this.symbols)); }
		for (int i=0; i<6880/ntime; i++) {	traderList.add( new SmallTrader		   (Global.inst().getPlusAccountID(), this.symbols)); }
		

		if (Config.hasSpoofing) {
			addSpoofingTrader(ntime,traderList);		/**Spoofing Traders**/
			System.out.println("Add Spoofing Traders... "); 
		}
		
		if (Config.hasLayering) {
			addLayeringTrader(ntime, traderList);		/**Layering Traders**/
			System.out.println("Add Layering Traders... ");
		}
			
		
		if (Config.hasSideLayering) {
			addSideLayeringTrader(ntime,traderList);	/**SideLayering Traders**/
			System.out.println("Add SideLayering Traders... ");
		}
		System.out.println("....\n");
	}
 
	//	1268+1276+16+176+5808+6880
	void startApp() {
		msfirst = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("Start from " + dateFormat.format(new Date(msfirst))+"\n"); 
		
		symbols =  Global.inst().getSymbols();
		for (int i=0; i<symbols.length; i++) 		{
			theMarket.setSymbol(symbols[i]);
		}
		
		OutPut.inst().openOrder();
	}

	void endApp() {
		
		OutPut.inst().closeOrder();

		// output chart
		for (int i=0; i<symbols.length; i++)		{
			Matching match=theMarket.getMatching(symbols[i]);
			Chart cht=match.getMinuteChart();
			OutPut.inst().printChart(cht);
		}

		// output OrderStat
		for (int i=0; i<symbols.length; i++)		{
			Matching match=theMarket.getMatching(symbols[i]);
			StatHolder stats=match.getMinuteStats();
			OutPut.inst().printOrderStat(stats);
		}
		
		for (int i=0; i<8; i++)		{
			System.out.printf("Trader Type=%d, max poisition=%d\n",i+1,Config.getMaxPos(i+1));
		}
		
		long ngapms = System.currentTimeMillis()-msfirst;
		long ngaps  = ngapms/1000; 
		System.out.printf("\n\nThe running costs: %02d:%02d:%02d-%03d\n" , ngaps/3600 , ngaps%3600/60 , ngaps%60, ngapms%1000);
		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("Start at " + dateFormat.format(new Date(msfirst))); 
		System.out.println("..End at " + dateFormat.format(new Date()));
		
	}
	
	
	// Display below information in GUI instead console if having GUI1
	void DisplayMarket() 	{
		
		if (Global.inst().isOnDisplayInterval()==false) // 30 second
			return;
 		
		Matching match 	= theMarket.getMatching(symbols[0]);
		Level1 l1		= match.getLevel1();
		OrderBook book	= match.getOrderBook();
		Chart cht = match.getMillisChart();
		double retPrice = match.getLastReturn();
		
		OutPut.inst().printSymbol(symbols[0]);
		OutPut.inst().printCurrentTime();
		OutPut.inst().printReturn(retPrice);
		OutPut.inst().printLevel1(l1);
		OutPut.inst().printNBBO(l1);
		OutPut.inst().printChartBar(cht.getLastBar());
		OutPut.inst().printBook(book);
	}
	
	/**Layering Traders**/
	void addLayeringTrader(int ntime, ArrayList<Trader> traderLst) 	{
		

		//case 8267-20, A genuine order, large orders (50 or 100 lots), the genuine order first, both on opposite sides, 
		//				cancel large orders as time-out or genuine order was filled.
		LayeringTrader trader8267 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8267.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader8267.setGenuineNumbers(new int[]{1}); 	// choose one only randomly
		trader8267.setGenuineDepths(new int[]{1}); 		// choose many randomly
		trader8267.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader8267.setSpoofingNumbers(new int[]{5,6,7,8,9,10}); 	// choose one only randomly
		trader8267.setSpoofingDepths(new int[]{2,3,4,5}); 	// choose many randomly
		trader8267.setSpoofingShares(new int[]{50,100}); 	// choose one	randomly 
		trader8267.setSpoofingMultiple(0); 					// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8267);
		//System.out.println("Type:" +trader8267.traderType + " Account:" + trader8267.accountID + " Case: 8267" );
		
		//case 8261-20, genuine orders, large orders, the genuine order first, 
		LayeringTrader trader8261 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8261.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8261.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader8261.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8261.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8261.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8261.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8261.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8261.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8261);
		//System.out.println("Type:" +trader8261.traderType + " Account:" + trader8261.accountID + " Case: 8261" );

		// case , 8260-20, spoof orders, against other orders they wanted filled.
		LayeringTrader trader8260 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8260.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8260.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader8260.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8260.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8260.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8260.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8260.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8260.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8260);
		//System.out.println("Type:" +trader8260.traderType + " Account:" + trader8260.accountID + " Case: 8260" );
		
		// case  8221-20, spoof orders, against other orders they wanted filled.
		LayeringTrader trader8221 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8221.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8221.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader8221.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8221.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8221.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8221.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8221.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8221.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8221);
		//System.out.println("Type:" +trader8221.traderType + " Account:" + trader8221.accountID + " Case: 8221" );
		
		// case 8105-20, Genuine orders, spoofing orders (5 times), Cancel after placing them or genuine were filled.
		LayeringTrader trader8105 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8105.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8105.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader8105.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8105.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8105.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8105.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8105.setSpoofingShares(new int[]{}); 						// doesn't select fixed shares 
		trader8105.setSpoofingMultiple(5); 								// spoofing share = multiple *  genuine share
		traderLst.add(trader8105);
		//System.out.println("Type:" +trader8105.traderType + " Account:" + trader8105.accountID + " Case: 8105" );

		// case 8104-19, large orders, a small order, cancel after fill
		LayeringTrader trader8104 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8104.setFirstOrder(1); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8104.setGenuineNumbers(new int[]{1}); 				// choose one only randomly
		trader8104.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8104.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8104.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8104.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8104.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8104.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8104);
		//System.out.println("Type:" +trader8104.traderType + " Account:" + trader8104.accountID + " Case: 8104" );
		
		// case 8075-19, 1)A large order, a small, cancel after fill, at times, large orders, small orders, layering
		LayeringTrader trader8075 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8075.setFirstOrder(1); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8075.setGenuineNumbers(new int[]{1,2}); 				// choose one only randomly
		trader8075.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8075.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8075.setSpoofingNumbers(new int[]{1,1,1,1,10,15}); 	// choose one only randomly
		trader8075.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8075.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8075.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8075);
		//System.out.println("Type:" +trader8075.traderType + " Account:" + trader8075.accountID + " Case: 8075" );

		// case 8014-19,Spoofing orders, genuine orders (itself or other want), no detail on enter
		LayeringTrader trader8014 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8014.setFirstOrder(2); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8014.setGenuineNumbers(new int[]{0,1,2}); 				// choose one only randomly
		trader8014.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8014.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8014.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8014.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8014.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8014.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8014);
		//System.out.println("Type:" +trader8014.traderType + " Account:" + trader8014.accountID + " Case: 8014" );

		// case 8013-19, Spoofing orders (can be thousand), genuine orders (wanted fill), no detail on who is first
		LayeringTrader trader8013 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8013.setFirstOrder(2); 									// 0-genuine, 1-spoofing, 2,other -random
		trader8013.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader8013.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader8013.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader8013.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20});// only set the maximum number is 100 for limitation
		trader8013.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader8013.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader8013.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8013);
		//System.out.println("Type:" +trader8013.traderType + " Account:" + trader8013.accountID + " Case: 8013" );
		
		// case 7988-19, Genuine orders, spoofing orders, cancel after filled
		LayeringTrader trader7988 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7988.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7988.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7988.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7988.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7988.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7988.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7988.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7988.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7988);
		//System.out.println("Type:" +trader7988.traderType + " Account:" + trader7988.accountID + " Case: 7988" );
		
		// case 7983-19, Spoofing orders, genuine orders(wanted fill),, cancel before fill, no detail on who is first
		LayeringTrader trader7983 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7983.setFirstOrder(2); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7983.setGenuineNumbers(new int[]{0,1,2}); 				// choose one only randomly
		trader7983.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7983.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7983.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7983.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7983.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7983.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7983);
		//System.out.println("Type:" +trader7983.traderType + " Account:" + trader7983.accountID + " Case: 7983" );
		
		
		// case 7946-19,Genuine orders, spoofing orders, cancel before fill, genuine first
		LayeringTrader trader7946 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7946.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7946.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7946.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7946.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7946.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7946.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7946.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7946.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7946);
		//System.out.println("Type:" +trader7946.traderType + " Account:" + trader7946.accountID + " Case: 7946" );
		
		
		// case 7867, Genuine orders, spoofing orders, cancel before fill
		LayeringTrader trader7867 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7867.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7867.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7867.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7867.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7867.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7867.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7867.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7867.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7867);	
		//System.out.println("Type:" +trader7867.traderType + " Account:" + trader7867.accountID + " Case: 7867" );
		
		// case 7865-19, Genuine orders, spoofing orders, cancel before fill  
		LayeringTrader trader7865 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7865.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7865.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7865.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7865.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7865.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7865.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7865.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7865.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7865);
		//System.out.println("Type:" +trader7865.traderType + " Account:" + trader7865.accountID + " Case: 7865" );
		
		// case 7683-18, A small order, large orders, 
		LayeringTrader trader7683 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7683.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7683.setGenuineNumbers(new int[]{1}); 				// choose one only randomly
		trader7683.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7683.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7683.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7683.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7683.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7683.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7683);
		//System.out.println("Type:" +trader7683.traderType + " Account:" + trader7683.accountID + " Case: 7683" );
		
		
		// case 7682-18, A Small order, spoofing orders (grouping, individual)
		LayeringTrader trader7682 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7682.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7682.setGenuineNumbers(new int[]{0,1}); 				// individual - 1, grouping-0
		trader7682.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7682.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7682.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7682.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7682.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7682.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7682);
		//System.out.println("Type:" +trader7682.traderType + " Account:" + trader7682.accountID + " Case: 7682" );
		
		// case 7598-17, multiple orders, genuine orders nearly same time. Cancel before fill, random
		LayeringTrader trader7598 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7598.setFirstOrder(2);
		trader7598.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7598.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7598.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7598.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7598.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7598.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7598.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7598);	
		//System.out.println("Type:" +trader7598.traderType + " Account:" + trader7598.accountID + " Case: 7598" );
		
		
		// case 7542-17, A small order, spoofing orders (total = 1000 lots for many orders)
		LayeringTrader trader7542 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7542.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader7542.setGenuineNumbers(new int[]{1}); 				// individual - 1, grouping-0
		trader7542.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7542.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7542.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7542.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7542.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7542.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7542);
		//System.out.println("Type:" +trader7542.traderType + " Account:" + trader7542.accountID + " Case: 7542" );
		
		// case 7504-16, Spoofing orders, genuine orders, enter or cancel  simultaneously. 
		LayeringTrader trader7504 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7504.setFirstOrder(1);  /// 0-genuine, 1-spoofing, 2,other -random
		trader7504.setGenuineNumbers(new int[]{1,2,3}); 				// choose one only randomly
		trader7504.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader7504.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader7504.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader7504.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader7504.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader7504.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7504);
		//System.out.println("Type:" +trader7504.traderType + " Account:" + trader7504.accountID + " Case: 7504" );
		
		//case 6649-13, A small order, sequence to repeat large orders, cancel before fill
		LayeringTrader trader6649 = new LayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader6649.setFirstOrder(0); 									// 0-genuine, 1-spoofing, 2,other -random
		trader6649.setGenuineNumbers(new int[]{1}); 				// individual - 1, grouping-0
		trader6649.setGenuineDepths(new int[]{1,2}); 				  	// choose many randomly
		trader6649.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); 	// choose one	  randomly 
		trader6649.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader6649.setSpoofingDepths(new int[]{2,3,4,5}); 				// choose many randomly
		trader6649.setSpoofingShares(new int[]{50,60,70,80,90,100}); 	// choose one	randomly 
		trader6649.setSpoofingMultiple(0); 								// 0 = none,  spoofing share = multiple *  genuine share
		trader6649.setSpoofingPlaceTime(new int[]{0,Config.TICK_MS});     // place order from 0,  step on each tick,
		trader6649.setSpoofingCancelTime(new int[]{LayeringTrader.ORDER_LENGTH,0}); 
		traderLst.add(trader6649);
		//System.out.println("Type:" +trader6649.traderType + " Account:" + trader6649.accountID + " Case: 6649" );

	}
	
	/**Spoofing Traders**/
	void addSpoofingTrader(int ntime, ArrayList<Trader> traderLst)	{

		//case 8395-21,  A genuine order, a larger order, the genuine order first, both on opposite sides
		SpoofingTrader trader8395 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8395.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader8395.setGenuineNumbers(new int[]{1}); 	// choose one only randomly
		trader8395.setGenuineDepths(new int[]{1}); 		// choose many randomly
		trader8395.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader8395.setSpoofingNumbers(new int[]{1}); 	// choose one only randomly
		trader8395.setSpoofingDepths(new int[]{2,3}); 	// choose many randomly
		trader8395.setSpoofingShares(new int[]{100,150,200,250,300}); // choose one	randomly 
		trader8395.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8395);
		//System.out.println("Type:" +trader8395.traderType + " Account:" + trader8395.accountID + " Case: 8395" );
		 
		
		//case 8259-20, A small order<10, a large= 5*small, IOC or Cxl after fill, small first
		SpoofingTrader trader8259 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8259.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader8259.setGenuineNumbers(new int[]{1}); 	// choose one only randomly
		trader8259.setGenuineDepths(new int[]{1}); 		// choose many randomly
		trader8259.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader8259.setSpoofingNumbers(new int[]{1}); 	// choose one only randomly
		trader8259.setSpoofingDepths(new int[]{2,3}); 	// choose many randomly
		trader8259.setSpoofingMultiple(5); 				// 0 = none,  spoofing share = multiple *  genuine share
		trader8259.setSpoofingCancelTime(new int[]{SpoofingTrader.ORDER_LENGTH/10,0}); // to cancel order, after 1/10 of ORDER_LENGTH
		traderLst.add(trader8259);
		//System.out.println("Type:" +trader8259.traderType + " Account:" + trader8259.accountID + " Case: 8259" );
		
		// case 8074-19, genuine orders, spoofing orders (small and random side, the total quantity is larger),
		SpoofingTrader trader8074 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8074.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader8074.setGenuineNumbers(new int[]{1,2,3}); // choose one only randomly
		trader8074.setGenuineDepths(new int[]{1,2}); 	// choose many randomly
		trader8074.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader8074.setSpoofingNumbers(new int[]{10,11,12,13,14,15,16,17,18,19,20}); 	// choose one only randomly
		trader8074.setSpoofingDepths(new int[]{2,3}); 	// choose one depth randomly
		trader8074.setSpoofingMultiple(5); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8074);
		//System.out.println("Type:" +trader8074.traderType + " Account:" + trader8074.accountID + " Case: 8074" );
		
		// case 8015-19, a small order (1 lot), a large order (20 lots), cancel after fill
		SpoofingTrader trader8015 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8015.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader8015.setGenuineNumbers(new int[]{1}); 	// choose one only randomly
		trader8015.setGenuineDepths(new int[]{1}); 		// choose many randomly
		trader8015.setGenuineShares(new int[]{1}); // choose one	  randomly 
		trader8015.setSpoofingNumbers(new int[]{1}); 	// choose one only randomly
		trader8015.setSpoofingDepths(new int[]{2,3}); 	// choose many randomly
		trader8015.setSpoofingShares(new int[]{20}); // choose one	randomly 
		trader8015.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader8015);
		//System.out.println("Type:" +trader8015.traderType + " Account:" + trader8015.accountID + " Case: 8015" );
		
		// case 7818-18, A Small order (1,2 level), a spoofing order (large away from best)
		SpoofingTrader trader7818 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7818.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7818.setGenuineNumbers(new int[]{1}); 	// choose one only randomly
		trader7818.setGenuineDepths(new int[]{1,2}); 		// choose many randomly
		trader7818.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7818.setSpoofingNumbers(new int[]{1}); 		// choose one only randomly
		trader7818.setSpoofingDepths(new int[]{2,3,4}); 	// choose many randomly
		trader7818.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly 
		trader7818.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7818);
		//System.out.println("Type:" +trader7818.traderType + " Account:" + trader7818.accountID + " Case: 7818" );
		
		// case 7797-18, A small order, a large or serial orders , cancel before fill
		SpoofingTrader trader7797 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7797.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7797.setGenuineNumbers(new int[]{1}); // choose one only randomly
		trader7797.setGenuineDepths(new int[]{1}); 	// choose many randomly
		trader7797.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7797.setSpoofingNumbers(new int[]{1,1,10,20}); 	// choose one only randomly
		trader7797.setSpoofingDepths(new int[]{2,3,4}); 	// choose one depth randomly
		trader7797.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly
		trader7797.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		trader7797.setSpoofingPlaceTime(new int[]{0,Config.TICK_MS});     // place order from 0,  step on each tick,
		trader7797.setSpoofingCancelTime(new int[]{Config.TICK_MS,Config.TICK_MS});  // cancel order from one tick , step on each tick 
		traderLst.add(trader7797);
		//System.out.println("Type:" +trader7797.traderType + " Account:" + trader7797.accountID + " Case: 7797" );
		
		// case 7796-18, A small order, a large order, cancel before fill, and Cross market spoofing
		SpoofingTrader trader7796 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7796.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7796.setGenuineNumbers(new int[]{0,1}); // 1 for a small order, 0 for cross market spoofing (one side only) 
		trader7796.setGenuineDepths(new int[]{1}); 	// choose many randomly
		trader7796.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7796.setSpoofingNumbers(new int[]{1,1,10,20}); 	// choose one only randomly
		trader7796.setSpoofingDepths(new int[]{2,3,4}); 	// choose one depth randomly
		trader7796.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly
		trader7796.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7796);
		//System.out.println("Type:" +trader7796.traderType + " Account:" + trader7796.accountID + " Case: 7796" );
		
		// 7709-18, Spoofing(a large or larges), a small order, cancel before fill
		SpoofingTrader trader7709 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7709.setFirstOrder(1); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7709.setGenuineNumbers(new int[]{1}); // 1 for a small order, 0 for cross market spoofing (one side only) 
		trader7709.setGenuineDepths(new int[]{1,2}); 	// choose many randomly
		trader7709.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7709.setSpoofingNumbers(new int[]{1,1,10,20}); 	// choose one only randomly
		trader7709.setSpoofingDepths(new int[]{2,3,4}); 	// choose one depth randomly
		trader7709.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly
		trader7709.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7709);
		//System.out.println("Type:" +trader7709.traderType + " Account:" + trader7709.accountID + " Case: 7709" );
		
		
		// case 7627-17, A small order (<10), One or more large order(100lot), cancel before fill
		SpoofingTrader trader7627 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7627.setFirstOrder(0); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7627.setGenuineNumbers(new int[]{1}); // 1 for a small order, 0 for cross market spoofing (one side only) 
		trader7627.setGenuineDepths(new int[]{1,2}); 	// choose many randomly
		trader7627.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7627.setSpoofingNumbers(new int[]{1,1,10,20}); 	// choose one only randomly
		trader7627.setSpoofingDepths(new int[]{2,3,4}); 	// choose one depth randomly
		trader7627.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly
		trader7627.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7627);
		//System.out.println("Type:" +trader7627.traderType + " Account:" + trader7627.accountID + " Case: 7627" );
		
		// case 7594-17, A large order, one or more small order, cancel before fill. Large first
		SpoofingTrader trader7594 = new SpoofingTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7594.setFirstOrder(1); 					// 0-genuine, 1-spoofing, 2,other -random
		trader7594.setGenuineNumbers(new int[]{1,2,3}); // 1 for a small order, 0 for cross market spoofing (one side only) 
		trader7594.setGenuineDepths(new int[]{1,2}); 	// choose many randomly
		trader7594.setGenuineShares(new int[]{1,2,3,4,5,6,7,8,9,10}); // choose one	  randomly 
		trader7594.setSpoofingNumbers(new int[]{1}); 	// choose one only randomly
		trader7594.setSpoofingDepths(new int[]{2,3,4}); 	// choose one depth randomly
		trader7594.setSpoofingShares(new int[]{50,60,70,80,90,100}); // choose one	randomly
		trader7594.setSpoofingMultiple(0); 				// 0 = none,  spoofing share = multiple *  genuine share
		traderLst.add(trader7594);
		//System.out.println("Type:" +trader7594.traderType + " Account:" + trader7594.accountID + " Case: 7594" );
	}
	//LinkedList
	//ArrayList
	/**Side Layering Traders**/
	void addSideLayeringTrader(int ntime,ArrayList<Trader> traderLst)	{	

		
		//case 8265-20,  spoofing orders only
		SideLayeringTrader trader8265 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8265.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader8265);
		//System.out.println("Type:" +trader8265.traderType + " Account:" + trader8265.accountID + " Case: 8265" );
		
		//case 8243-20, Spoofing orders, cancel before fill
		SideLayeringTrader trader8243 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8243.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader8243);
		//System.out.println("Type:" +trader8243.traderType + " Account:" + trader8243.accountID + " Case: 8243" );

		// case 8024-19, Spoofing orders, 
		SideLayeringTrader trader8024 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader8024.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader8024);
		//System.out.println("Type:" +trader8024.traderType + " Account:" + trader8024.accountID + " Case: 8024" );
		
		// case 7877-19, (ignore genuine order for Group), Group trading: spoofing orders, genuine order (others and themselves want)
		SideLayeringTrader trader7877 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7877.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7877);
		//System.out.println("Type:" +trader7877.traderType + " Account:" + trader7877.accountID + " Case: 7877" );
		
		// case 7827-18,  (ignore genuine order for Group),, Group trading: Spoofing orders, genuine (theirself or others)(first)
		SideLayeringTrader trader7827 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7827.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7827);
		//System.out.println("Type:" +trader7827.traderType + " Account:" + trader7827.accountID + " Case: 7827" );
		
		//case 7800-18, Spoofing orders (large)
		SideLayeringTrader trader7800 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7800.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7800);
		//System.out.println("Type:" +trader7800.traderType + " Account:" + trader7800.accountID + " Case: 7800" );

		// case 7581-17, (ignore genuine order for Group),, group trading,  a small (each wanted to trade), a large,  cancel before fill 
		SideLayeringTrader trader7581 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7581.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7581);
		//System.out.println("Type:" +trader7581.traderType + " Account:" + trader7581.accountID + " Case: 7581" );
		
		// case 7567-17, (ignore genuine order for Group),, a genuine order, spoofing orders, genuine first
		SideLayeringTrader trader7567 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7567.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7567);
		//System.out.println("Type:" +trader7567.traderType + " Account:" + trader7567.accountID + " Case: 7567" );
		
		// case 7486-16,  4-6 large spoofing order, 3or 4 price point from best, modify 100 times,
		SideLayeringTrader trader7486 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7486.setGenuineNumbers(new int[]{}); 	// no genuine order,
		trader7486.setSpoofingNumbers(new int[]{20,30,40,50}); 	// choose one only randomly
		trader7486.setSpoofingDepths(new int[]{3,4});
		trader7486.setSpoofingPlaceTime(new int[]{0,Config.TICK_MS});     // place order from 0,  step on each tick,
		trader7486.setSpoofingCancelTime(new int[]{Config.TICK_MS*4,Config.TICK_MS});  // cancel order from one tick , step on each tick 
		traderLst.add(trader7486);
		//System.out.println("Type:" +trader7486.traderType + " Account:" + trader7486.accountID + " Case: 7486" );
		
		// case 7353-16,  (ignore genuine order for Group), Group and individual, small orders, large orders.
		SideLayeringTrader trader7353 = new SideLayeringTrader  (Global.inst().getPlusAccountID(),this.symbols);
		trader7353.setGenuineNumbers(new int[]{}); 	// no genuine order, 
		traderLst.add(trader7353);
		//System.out.println("Type:" +trader7353.traderType + " Account:" + trader7353.accountID + " Case: 7353" );
 
	}
}





