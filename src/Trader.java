import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.lang.*;


/*The running costs: 00:01:09-815
Start at 2021/11/30 00:37:07
..End at 2021/11/30 00:38:17*/
 

public abstract class Trader {
	
	protected Market currentMarket; // point to a market
	
	// variable definitions
	protected int accountID=0;
	protected int traderStep=0;
	protected int timeCounter=0;
	protected int traderType;
	
	protected int tradeSpeed = 1000;  // million second    
	protected int orderLength = 1000; // + rand(1000) FundamentalBuyer/Seller
	protected int initTradeSpeed = 1000;  // million second    
	protected int initOrderLength = 1000; // + rand(1000) FundamentalBuyer/Seller
	protected boolean aliveOrder=true;

	protected int orderTime = 0;
	protected String symbols[]= new String[0];
	
	protected Map<String, Position> mapPosition = new TreeMap<>();
//	private   Map<String, MdpState> mapState 	= new TreeMap<>();
	protected Map<Integer, Order>   mapOrder 	= new TreeMap<>();
	protected LinkedList<Order> listPrepareOrder = new LinkedList <>();
	
	protected Random rand = new Random();
	
	public Trader(int accountID, String symbols[],int traderType,int initTradeSpeed, int initOrderLength) 	{
		this.accountID = accountID;
		this.traderType = traderType;
		this.initTradeSpeed = initTradeSpeed;
		this.initOrderLength= initOrderLength;	
		this.tradeSpeed = initTradeSpeed;
		this.orderLength= initOrderLength;
			
		setSymbol(symbols);
	}

	
	public int getAccountID() { return accountID;}
	abstract protected LinkedList <Order> generateNewOders();
	abstract protected void respondOrders(LinkedList <Order> ordList);

	
	public void setSymbol(String symb[]) 	{ 
		this.symbols = symb.clone();
		
		for (int i=0; i<symb.length; i++) {

			mapPosition.put(symb[i], new Position());

		}
	}
	
	
	public void setAliveOrder(boolean b) { aliveOrder=b; }
	
	
	public void talk(Market market) 	{ //got called earlier in loop 
		
		currentMarket = market;
 			
		if (hasActiveQuantity())  		{ // having existing order, wait new order->executed order or canceled order
			retrieveResponses(); // retrieve when it is available

			if ( isExpiredOrders() || (aliveOrder==false) ) {// cancel
				
				for (Map.Entry<Integer, Order> entry : mapOrder.entrySet()) {
					placeCancelOrder(entry.getValue(),Order.ACT_EXPIRED);
				}
				
				listPrepareOrder.clear(); //need to debug
				
			} else {

				for (Map.Entry<Integer, Order> entry : mapOrder.entrySet()) {
					Order ord =  entry.getValue();
					if (ord.isTimeOut(Global.inst().getCurrentTime())) {
						placeCancelOrder(ord,Order.ACT_TIMEOUT);
					}
				}
			}
				
		}	
		else 	if (isReadyToTrade())			{ // place new order
		 
			// initialize new orders
			initializeNewOrders();

			LinkedList <Order> newOrders  = generateNewOders();
			
			// place new orders through one by one
			for(Order ord: newOrders)	{
	    		listPrepareOrder.add(ord); //need to debug 
		    }
 		}


    	for (Iterator<Order> iter = listPrepareOrder.iterator(); iter.hasNext();) {
            Order ord = iter.next();
	    	if (ord.isToPlace(Global.inst().getCurrentTime())) {
	    		placeNewOrder(ord);
	    		iter.remove();
	    	}

	    }

    	
		//remove unlive orders
		Iterator<Map.Entry<Integer, Order>> it = mapOrder.entrySet().iterator(); 
		while (	it.hasNext() )  {
		    Map.Entry<Integer, Order>  entry = it.next();
			if (entry.getValue().getLiveQuantity()==0) {
				it.remove();
			}
		}
			
	};
	
	/////////////// below are private functions
	private void retrieveResponses() 	{ 
		for(int i = 0; i< symbols.length; i++) {
			retrieveResponse(symbols[i]);
		}
	}

	
	private void retrieveResponse(String symbol) 	{ 

		Matching macth =  currentMarket.getMatching(symbol);
		Position pos = mapPosition.get(symbol);
		
		// receive response
		LinkedList <Order> rcvOrders = new LinkedList<>();
		macth.response(accountID,rcvOrders); // receive response
		if (rcvOrders.size()==0) return;

		
		Set<Order> responseOrderSet = new LinkedHashSet<>();
		
		// modify position, orders, and print out
		for(Order ord: rcvOrders) 		{
			pos.responseOrder(ord);
			
			// display maximum position for each traderType
			int afterQty = pos.getQuantity();
			if ((Config.getMaxPos(traderType)<Math.abs(afterQty)) && ord.isExecuted() )				{
				Config.setMaxPos(traderType,Math.abs(afterQty));
				System.out.printf("Acc:%d, type=%d, qty=%d, b/s=%c ord=%d maxQty=%d\n",
						accountID,traderType,afterQty,ord.getBuySell(), ord.getLastExeQuantity(),
						Config.getMaxPos(traderType));
			}

			// Find saved order
			Order saveOrder = mapOrder.get(ord.getOrderID());
			if (saveOrder==null) {
				System.out.printf("Acc:%d, orderid=%d cannot find \n",accountID,ord.getOrderID());
				continue;
			}

			// assign saveOrder by return order that modify in exchange
			saveOrder.response(ord);
			
			// modify after state
	    	MdpState state = new MdpState(pos,macth.getOrderBook());
			saveOrder.assignAfterState(state);
			
			responseOrderSet.add(saveOrder);
		}

		//print out only one and  last one 
		for (Order rord : responseOrderSet) 		{
		    OutPut.inst().printOrder(rord, traderStep++);
		     
		    if (rord.getLiveQuantity()>0)		{
		    	rord.setWaitAction();						// action is 'wait'
				rord.assignStartState(rord.getAfterState()); // start state is change to previous after state.
			}
		}
		
		respondOrders(rcvOrders);
		
	}

	
	// place orders to matching in exchange
	private void placeNewOrder(Order ord) 	{
 
    	if (ord.getLiveQuantity()==0) // alive order only, some quantity may be zero  
    		return;
    	
    	if (ord.isNewOrder()==false) { //  new Order only
    		return;
    	}

    	// save new order to mapOrder
    	mapOrder.put(ord.getOrderID(),ord);
    	
    	
    	String strName = ord.getSymbol();
    	Matching macth =  currentMarket.getMatching(strName);
    	
		// assign states
    	Position pos = mapPosition.get(strName);
    	
		// modify after state
    	MdpState state = new MdpState(pos,macth.getOrderBook());

		ord.assignStartState(state);
		ord.assignAfterState(state);
		ord.assignOrderTime(Global.inst().getCurrentTime());
    	

		// Place orders
		macth.placeOrder(ord);
		
		// immediately retrieve response
		retrieveResponses(); 
	}
	
	private void placeCancelOrder(Order ord, char act)	{
    	if (ord.getLiveQuantity()==0) // alive only
    		return;

    	String strName = ord.getSymbol();
    	Matching macth =  currentMarket.getMatching(strName);
    	Position pos = mapPosition.get(strName);
    	
    	
    	MdpState state = new MdpState(pos,macth.getOrderBook());
        	
		// wait order-update afterstate and print it 
		Order saveOrder = mapOrder.get(ord.getOrderID());
		saveOrder.assignAfterState(state);
    	OutPut.inst().printOrder(saveOrder, traderStep++);

    	// cancel order
    	ord.setCancel(act);
		ord.assignStartState(state);
		ord.assignAfterState(state);
 
        		//place order
		macth.placeOrder(ord); 

		// retrieve response orders
    	retrieveResponses();
	}
	
	// function definitions
	private boolean isExpiredOrders() 	{
 		if ( (orderTime>0) && ( Global.inst().getCurrentTime() >= (orderTime+orderLength) ) ) {
			orderTime = 0;
			return true;
 		} else
			return false;
	}

	
	private boolean isReadyToTrade() 	{

		if (this.timeCounter==0)
			this.timeCounter= Global.inst().getCurrentTime()+tradeSpeed;
		
		if  (  Global.inst().getCurrentTime() > timeCounter ) {
			timeCounter = Global.inst().getCurrentTime()+tradeSpeed; 
			return true;
		} else
			return false;
	}
	
	
	private boolean hasActiveQuantity() 	{
		int qty=0;
		for (Map.Entry<String, Position> entry : mapPosition.entrySet()) {
			qty += entry.getValue().getActiveOrderQty();
		}
		return qty>0;
	}
	
	
	private void initializeNewOrders() 	{
		traderStep = 0;
		orderTime = Global.inst().getCurrentTime();
		orderLength = (int)(initOrderLength*(1+rand.nextFloat()));

		tradeSpeed = Global.inst().getRandPoisson(initTradeSpeed);
	}
	
	
	


//	public static void main(String[] args) {
//		Set<Integer> linkedHashSet = new LinkedHashSet<>();
//		linkedHashSet.add(3);
//		linkedHashSet.add(4);
//		linkedHashSet.add(2);
//		linkedHashSet.add(3);
//		 for (int i : linkedHashSet) {
//		     System.out.println(i);
//		 }
//	}

}
