/////////////////////////////////////////////////////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////////////////////////////////////////////////////

public class Order {

	static final int TIMEFORCE_DAY = 86400000;// It is the order life time, one day, 8640 =  24h*60min*60sec,  
	static final int PLACE_NOW = 0;// It is the order life time, one day, 8640 =  24h*60min*60sec,
	static final char ACT_EXPIRED='c';
	static final char ACT_TIMEOUT='c';
	
	//variable definitions
	
	private int  	orderID=0;
	private int  	accountID=0;
	private int  	traderType=0;
	private String 	symbol="";
	private char 	buySell='b';  //  b-buy, s-sell
	private int  	price=0;
	private int  	quantity=0;
	private int  	time=0;
	private int		timeForce=TIMEFORCE_DAY; 
	private int		timeToPlace=0;

	private int  	executedQuantity=0;
	private int 	executedPrice=0;
	private int  	lastExeQuantity=0;
	private int 	lastExePrice=0;
	private int  	lastExeTime=0;
	private int  	canceledQuantity=0;
	private int  	canceledTime=0;
	private char 	action='n'; // n-new, c-cancel, w-wait
	private char 	status='a'; // a-accepted, e-executed, c-canceled
	private int		condition=0; //// for debug use,
	
	private MdpState startState=new MdpState();
	private MdpState afterState =new MdpState();
	
	
	// function definitions
	
	static String getTitle()	{
		return "OrderID,Account,TraderType,BuySell,Price,Quantity,Time,Action,Status,ExecutedQuantity,ExecutedPrice,LastExeQuantity,LastExePrice,LastExeTime,CanceledQuantity,CanceledTime,Condition,TimeForce,TimeToPlace";	
	}
	
	String getString()	{
		return  String.format("%d,%d,%d,%c,%s,%d,%s,%c,%c,%d,%s,%d,%s,%s,%d,%s,%d,%d,%d", 
				orderID, 
				accountID, 
				traderType,
				buySell,
				Global.inst().formatPrice(price), 
				quantity,
				TickTime.format(time),
				action,
				status,
				executedQuantity,
				Global.inst().formatPrice(executedPrice),
				lastExeQuantity,
				Global.inst().formatPrice(lastExePrice),
				TickTime.format(lastExeTime),
				canceledQuantity,
				TickTime.format(canceledTime),
				condition,
				timeForce,
				timeToPlace
				);
	}

	Order(){}
	
	Order( String symbol, int orderID, int accountID,int traderType, char buySell, int price, int quantity, int time,int condition, int timeForce, int timeToPlace)	{
		setOrder(symbol, orderID, accountID, traderType, buySell, price, quantity, time,condition, timeForce,timeToPlace);
	}
	
	Order( Order ord )	{ 
		this.orderID = ord.orderID;
		this.accountID = ord.accountID;
		this.traderType = ord.traderType;
		this.symbol = ord.symbol;
		this.buySell = ord.buySell;
		this.price = ord.price;
		this.quantity = ord.quantity;
		this.time = ord.time;
		this.timeForce = ord.timeForce;
		this.timeToPlace = ord.timeToPlace;

		this.executedQuantity = ord.executedQuantity;
		this.executedPrice = ord.executedPrice;
		this.lastExeQuantity = ord.lastExeQuantity;
		this.lastExePrice = ord.lastExePrice;
		this.lastExeTime = ord.lastExeTime;
		this.canceledQuantity = ord.canceledQuantity;
		this.canceledTime = ord.canceledTime;
		this.action =  ord.action;
		this.status = ord.status;
		this.condition = ord.condition;
		
		this.startState.assign(ord.startState);
		this.afterState.assign(ord.afterState);
	} 

	
	void setOrder(String symbol, int orderID, int accountID,int traderType, char buySell, int price, int quantity, int time,int condition, int timeForce, int timeToPlace)	{
		this.symbol = symbol;
		this.orderID = orderID;
		this.accountID = accountID;
		this.traderType = traderType;
		this.buySell = buySell;
		this.price = price;
		this.quantity = quantity;
		this.time = time;
		this.condition=condition;
		this.action='n'; 
		this.status='a'; 
		this.timeForce = timeForce;
		this.timeToPlace = timeToPlace;
	}
	
//	void setCondition(int n) { this.condition=n;} // for debug use
	
	void toExecute(int lastPrice, int lastQuantity, int lastTime )	{ 

	 	if (lastQuantity==0) 
	 		return; // ignore zero quantity

		this.status = 'e';
		
		double totalValue = 0.001*this.executedPrice*this.executedQuantity + 0.001*lastPrice*lastQuantity;
//		int totalValue = this.executedPrice*this.executedQuantity + lastPrice*lastQuantity;
		
		this.executedQuantity += lastQuantity;
		this.executedPrice = (int)(totalValue/this.executedQuantity*1000);

		this.lastExePrice = lastPrice;
		this.lastExeQuantity = lastQuantity;
		this.lastExeTime = lastTime;
		
		this.canceledTime = 0;
		this.canceledQuantity = 0;
	} 
	
	void setCancel(char act)	{
		this.action = act;
		this.status = 'a';
	}
	
	void toCancel()	{ 
//		if (getLiveQuantity()<=0) return;
		
		int canceledTime = Global.inst().getCurrentTime();
		this.status = 'c';
		this.canceledTime=canceledTime;
		canceledQuantity = quantity-executedQuantity; // one shot cancel, not partial cancel 
		
		this.lastExePrice = 0;
		this.lastExeQuantity = 0;
		this.lastExeTime = 0;
	} 
	
	void toCancel(Order ord)	{
 		toCancel();
	}

	void response(Order ord) {
		this.executedQuantity = ord.executedQuantity;
		this.executedPrice = ord.executedPrice;
		this.lastExeQuantity = ord.lastExeQuantity;
		this.lastExePrice = ord.lastExePrice;
		this.lastExeTime = ord.lastExeTime;
		this.canceledQuantity = ord.canceledQuantity;
		this.canceledTime = ord.canceledTime;
	
		this.status = ord.status;
	}
	
	void setOrderID(int id) {
		this.orderID = id;
	}
	void setWaitAction()  { 
		this.action = 'w';
		this.status = 'a'; 
	}

	boolean isNewOrder() 	{	return this.action=='n';	}
	boolean isCancelOrder() {	return (this.action=='c' || this.action=='C');	}
	boolean isWaitOrder() 	{	return this.action=='w';	}
	boolean isExecuted() {	return this.status=='e';	}
	boolean isCanceled() {	return this.status=='c';	}
	boolean isAccepted() {	return this.status=='a';	}
	boolean isBuyOrder() {	return this.buySell=='b';	}
	boolean isTimeOut(long currentTime) {	return (currentTime >= (time+timeForce))?true:false;	}
	boolean isToPlace(long currentTime) {	return (currentTime >= (time+timeToPlace))?true:false;	}

	String getSymbol() 		  { return symbol; }
	int  getOrderID() 		  { return orderID; }
	int  getAccountID()		  { return accountID; }
	int  getTime()			  { return time; }
	char getBuySell() 		  { return buySell;	}
	int  getTraderType() 	  { return traderType;	}
	int  getExecutedPrice()	  { return executedPrice;	}
	int  getExecutedQuantity(){	return executedQuantity;}
	int  getLastExePrice()	  { return lastExePrice;	}
	int  getLastExeQuantity() {	return lastExeQuantity;}
	int  getLastExeTime()	  { return lastExeTime;	}
	int  getCanceledQuantity(){	return canceledQuantity;}
	int  getCanceledTime()	  { return canceledTime; }
	int  getQuantity()		  { return quantity;	} 
	int  getPrice() 		  { return price;	}
	char getStatus() 		  { return status;	}
	char getAction()	  	  { return action; }
	
	
	int  getLiveQuantity()  { return quantity-executedQuantity-canceledQuantity; }
	void resetLastExecution() { 
		lastExePrice=0; 
		lastExeQuantity=0;
		lastExeTime=0;
	};

	
	/*
	void assignStartState(OrderBook book, int posQty, int buyOrderQty, int sellOrderQty) 	{	
		startState.assignOrderBook(book);
		startState.assignPosition(posQty);
		startState.assignActiveBuyOrderQty(buyOrderQty);
		startState.assignActiveSellOrderQty(sellOrderQty);
	};
	
	void assignAfterState(OrderBook book, int posQty, int buyOrderQty, int sellOrderQty) 				{	
		afterState.assignOrderBook(book);
		afterState.assignPosition(posQty);
		startState.assignActiveBuyOrderQty(buyOrderQty);
		startState.assignActiveSellOrderQty(sellOrderQty);
	}
	*/

	void assignStartState(MdpState state) 	{ startState.assign(state);	}
	void assignAfterState(MdpState state) 	{ afterState.assign(state);	}
	void assignOrderTime(int tm) {this.time = tm; };	
	
	MdpState getStartState() {	return startState;	}
	MdpState getAfterState() {	return afterState; };		

	 
	 
//	boolean isExpired(int curTime) {
//		int rmd = (int)(Math.random()*60*1000);  //10 minutes
// 		
//		if ( (curTime-time) > rmd ) {
//			return true;
//		}else {
//			return false;
//		}
//	}
//	
//	
//	public static void main(String[] args) {
//		Order ord=new Order("SPY", 1, 1, 1, 'b', 10, 100, 90201001,1);
//
//		ord.execute(11,100, 90221001);
//		System.out.printf("%c:%d,%d,%d,%d,%d\n", ord.getStatus(),ord.getExecutedPrice(),ord.getExecutedQuantity(),
//				ord.getLastExePrice(),ord.getLastExeQuantity(),ord.getLastExeTime());
//		
//		ord.cancel(90231001);
//		System.out.printf("%c:%d,%d", ord.getStatus(),ord.getCanceledQuantity(),ord.getCanceledTime());
//
//		System.out.printf("%s\n",Order.getTitle());
//		System.out.printf("%s\n",ord.getString());
//		
//		
//		MdpState aft = ord.getAfterState();
//		
//		aft.setPositionQuantity(20);
//		
//		int n=aft.getSavedQuantity();
//	}

}
