import java.util.LinkedList;


public class SpoofingTrader extends Trader {
	
	static final int TRADER_TYPE = 8;

	static final int TRADE_SPEED  = 100*Config.TICK_MS; //from original code 
	static final int ORDER_LENGTH = 10*Config.TICK_MS;

	
	int   firstOrder    = 0; 										// 0-genuine, 1-spoofing, 2,other -random,
	int[] genuineNumbers = {1,2,3};  								// The possible total numbers of genuine order, choose one only randomly
	int[] genuineDepths  = {1,2};  									// The possible depth of genuine order , choose many randomly
	int[] genuineShares  = {1,2,3,4,5,6,7,8,9,10}; 					// the possible share of  genuine order , choose many randomly

	int[] spoofingNumbers  = {5,6,7,8,9,10}; //{10,11,12,13,14,15,16,17,18,19,20};		// The possible total numbers of spoofing order, , choose one only randomly
	int[] spoofingDepths   = {1,2,3,4};								// The possible depth of spoofing order, choose many randomly
	int[] spoofingShares   = {50,60,70,80,90,100};					// The possible share of spoofing order, choose many randomly
	int   spoofingMultiple = 0;									// The spoofing share = multiple * genuine share
	
	int[] spoofingPlaceTime  = {0,0};  				//ms, the place time of first order, next steps for remaining orders
	int[] spoofingCancelTime = {ORDER_LENGTH,0};  	//ms, the cancel time of first order, next steps for remaining orders

	int   lastGenuineSize = 1; // variable for saving genuine size
	 
	 
	void setFirstOrder(int first) 		   { firstOrder = first; };
	void setGenuineNumbers(int [] numbers) { genuineNumbers = numbers;	};
	void setGenuineDepths(int [] depths)   { genuineDepths = depths;	}
	void setGenuineShares(int [] shares)   { genuineShares = shares;	}
	void setSpoofingNumbers(int [] numbers){ spoofingNumbers = numbers;	}
	void setSpoofingDepths(int [] depths)  { spoofingDepths = depths;	}
	void setSpoofingShares(int [] shares)  { spoofingShares = shares;	}
	void setSpoofingMultiple(int multiple) { spoofingMultiple = multiple;	}
	void setSpoofingPlaceTime(int [] time) { spoofingPlaceTime = time;	}
	void setSpoofingCancelTime(int [] time){ spoofingCancelTime = time;	}
		

    SpoofingTrader(int accountID, String symbols[]){
		super(accountID,symbols,TRADER_TYPE,TRADE_SPEED,ORDER_LENGTH);
	}
	
	@Override
	protected void respondOrders(LinkedList <Order> ordList)		{
		for(Order ord: ordList) {   
			if ( ord.getExecutedQuantity() > 0 ) {
				setAliveOrder(false); // to cancel all orders for a complete strategy if one of orders is done
			}
		}
	}
	
	// This strategy only fit future market, you can change it to fit different symbols
	@Override
	protected LinkedList <Order> generateNewOders()		{
		setAliveOrder(true);
		
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
		
		char signal = findSignal(symbol);
		switch (signal) {
			case 'b':	generateBuyOrders(symbol,curTime, ordList);			break;
			case 's':	generateSellOrders(symbol, curTime, ordList);		break;
			case 'c':	generateCoverOrders(symbol, curTime, ordList);		break;
			default:	break;
		}
	}
	
	char findSignal(String symbol) 	{
		Position pos   = mapPosition.get(symbol);
		if (pos.getQuantity()!=0) {
			return 'c';
		}
		
		Matching match = currentMarket.getMatching(symbol);
		Level1 l1      = match.getLevel1();
		
		int askDepth=match.getOrderBook().getAskDepth();
		int bidDepth=match.getOrderBook().getBidDepth();
		int askQtys = match.getOrderBook().getAskSum();
		int bidQtys = match.getOrderBook().getBidSum();
		
		//check the condition that orders can be placed
		if ( (Math.abs(askDepth-bidDepth) > 2)  
				|| (askDepth<5) 
				|| (bidDepth<5) 
				|| (Math.abs(askQtys-bidQtys)>50) 
				|| (l1.getAskPrice()-l1.getBidPrice()) > (3*Config.DOLLAR) 
				)
		{
			return ' ';
		}
		
		int r=rand.nextInt(100);
		if (r>50)  		return 'b';
		else  			return 's';
	}
	
	int generateSpoofingSize() 	{
		int nMethedID = 0;
		
		if (spoofingShares.length>0 && spoofingMultiple>0) {  // both, times, such as x5, and share list 
			nMethedID = rand.nextInt(2);
		}	
		else	if (spoofingShares.length>0 )		{ // having spoofing list
			nMethedID = 0;
		}
		else	{ // having spoofingtimes
			nMethedID = 1;	
		}

		int nOrderSize=1;
		if (nMethedID==0)
			nOrderSize = spoofingShares[rand.nextInt(spoofingShares.length)]; // Get a share from list rand
		else {
			nOrderSize = lastGenuineSize * spoofingMultiple; // Get a share from list rand
		}
		
		return nOrderSize>0?nOrderSize:1;
	}

	int generateGenuineBuyOrders(String symbol, int curTime, LinkedList <Order> ordList, int startID)	{
		if (genuineNumbers.length==0 || genuineDepths.length==0 || genuineShares.length==0)
			return (0);
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		// Gennuine orders, which are on or closes to best price
		int nGenuineNumber = genuineNumbers[rand.nextInt(genuineNumbers.length)];

		
		for(int j = 0; j< nGenuineNumber; j++){
			int nOrderSize = genuineShares[rand.nextInt(genuineShares.length)]; // Get a share from list randomly
			int nGenuineDepth  = genuineDepths[rand.nextInt(genuineDepths.length)]-1; // Get random depth
			
			lastGenuineSize = nOrderSize;
			
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, 
					 				TRADER_TYPE, 'b', l1.getBidPrice()- nGenuineDepth*Config.DOLLAR, 
					 				nOrderSize, curTime,j + startID,
					 				Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordBuy);	
		}
		return nGenuineNumber;
	}
	
	
	int generateGenuineSellOrders(String symbol, int curTime, LinkedList <Order> ordList, int startID)	{
		if (genuineNumbers.length==0 || genuineDepths.length==0 || genuineShares.length==0)
			return (0);
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		// Gennuine orders, which are on or closes to best price
		int nGenuineNumber = genuineNumbers[rand.nextInt(genuineNumbers.length)];

		
		for(int j = 0; j< nGenuineNumber; j++){
			int nOrderSize = genuineShares[rand.nextInt(genuineShares.length)]; // Get a share from list randomly
			int nGenuineDepth  = genuineDepths[rand.nextInt(genuineDepths.length)]-1; // Get random depth
			
			lastGenuineSize = nOrderSize;
			
			Order ordSell=new Order(symbol,Global.inst().getPlusOrderID(), accountID, 
					 				TRADER_TYPE, 's', l1.getAskPrice() + nGenuineDepth*Config.DOLLAR, 
					 				nOrderSize, curTime,j + startID,
					 				Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordSell);	
		}
		
		return nGenuineNumber;
	}
	
	int generateSpoofingSellOrders(String symbol, int curTime, LinkedList <Order> ordList, int startID)	{
		if (spoofingNumbers.length==0 || spoofingDepths.length==0 || spoofingDepths.length==0)
			return (0);
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		int nSpoofingNumber =  spoofingNumbers[rand.nextInt(spoofingNumbers.length)];
		int nSpoofingCancelTime = spoofingCancelTime[0];
		int nSpoofingPlaceTime  = spoofingPlaceTime[0];
		
		int nSpoofingDepth 	= spoofingDepths[rand.nextInt(spoofingDepths.length)]-1; // Get random depth
		for(int i = 0; i < nSpoofingNumber; i++){
			int nOrderSize 		= generateSpoofingSize();
			
			Order ordSell = new Order(symbol,Global.inst().getPlusOrderID(), accountID, 
					 					TRADER_TYPE, 's', l1.getAskPrice()+ nSpoofingDepth*Config.DOLLAR, 
					 					nOrderSize, curTime,i + startID,
					 					nSpoofingCancelTime, nSpoofingPlaceTime);
			ordList.add(ordSell);
			
			nSpoofingCancelTime +=  spoofingCancelTime[1];
			nSpoofingPlaceTime  +=  spoofingPlaceTime[1];
		}
		
		return nSpoofingNumber;
	}
	
	int generateSpoofingBuyOrders(String symbol, int curTime, LinkedList <Order> ordList, int startID)	{
		if (spoofingNumbers.length==0 || spoofingDepths.length==0 || spoofingDepths.length==0)
			return (0);
		
		Matching macth = currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		
		int nSpoofingNumber =  spoofingNumbers[rand.nextInt(spoofingNumbers.length)];
		int nSpoofingCancelTime = spoofingCancelTime[0];
		int nSpoofingPlaceTime  = spoofingPlaceTime[0];
		
		int nSpoofingDepth 	= spoofingDepths[rand.nextInt(spoofingDepths.length)]-1; // Get random depth		
		for(int i = 0; i < nSpoofingNumber; i++){
			int nOrderSize 		= generateSpoofingSize();
			
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, 
					 				TRADER_TYPE, 'b', l1.getBidPrice() - nSpoofingDepth*Config.DOLLAR, 
					 				nOrderSize, curTime,i + startID,
					 				nSpoofingCancelTime, nSpoofingPlaceTime);
			ordList.add(ordBuy);
			
			nSpoofingCancelTime +=  spoofingCancelTime[1];
			nSpoofingPlaceTime  +=  spoofingPlaceTime[1];
		}
		return nSpoofingNumber;
	}
	
	void generateBuyOrders(String symbol, int curTime, LinkedList <Order> ordList) 	{
		int first = (firstOrder==0||firstOrder==1)?firstOrder:rand.nextInt(2); 
				
		if (first==0) {
			int id= generateGenuineBuyOrders(symbol,curTime,ordList,0);
			generateSpoofingSellOrders(symbol,curTime,ordList,id);
		}else {
			int id = generateSpoofingSellOrders(symbol,curTime,ordList,0);
			generateGenuineBuyOrders(symbol,curTime,ordList,id);
		}
		
	}
	
	void generateSellOrders(String symbol, int curTime, LinkedList <Order> ordList) 	{
		
		int first = (firstOrder==0||firstOrder==1)?firstOrder:rand.nextInt(2); 
		
		if (first==0) {
			int id= generateGenuineSellOrders(symbol,curTime,ordList,0);
			generateSpoofingBuyOrders(symbol,curTime,ordList,id);
		}else {
			int id = generateSpoofingBuyOrders(symbol,curTime,ordList,0);
			generateGenuineSellOrders(symbol,curTime,ordList,id);
		}
		
	}
	
	void generateCoverOrders(String symbol, int curTime, LinkedList <Order> ordList) 	{
		
		Matching macth =  currentMarket.getMatching(symbol);
		Level1 l1      = macth.getLevel1();
		Position pos   = mapPosition.get(symbol);	
		int 	qty  = pos.getQuantity();
		
		if (qty<0) {
			int px = l1.getAskPrice() + rand.nextInt(5)*Config.DOLLAR;
			Order ordBuy=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 
									'b', px, -qty, curTime,9,
					 				Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordBuy);
		} else {
			int px = l1.getBidPrice() - rand.nextInt(5)*Config.DOLLAR;
			Order ordSell=new Order(symbol,Global.inst().getPlusOrderID(), accountID, TRADER_TYPE, 
									's', px, qty, curTime,10,
					 				Order.TIMEFORCE_DAY, Order.PLACE_NOW);
			ordList.add(ordSell);
		}
	}
	

	


}



















