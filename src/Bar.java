///
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
///  

public class Bar {
	char type='m'; //m-minute, d - day , s-second
	int  time=0;
	int  begin=0;
	int  high=0;
	int  low=0;
	int  last=0;
	int  average=0; // special from netlogo version
	int  volume=0;
 
	Bar() {}
	
	Bar(char type) {
		this.type = type;
	}
	
	Bar(char type,int time, int price, int qty ) {
		this.type = type;
		enter (time, price, qty);
	}
	
	static String getTitle() {
			return  "ChartType," +
					"Time," +
					"Begin," +
					"High," +
					"Low," +
					"Last," +
					"Average," +
					"Volume,"
					;
	}

	String getString() {
		return  type + ", " +
				TickTime.format(time) + ", " +
				Global.inst().formatPrice(begin) + ", " +
				Global.inst().formatPrice(high) + ", " +
				Global.inst().formatPrice(low) + ", " +
				Global.inst().formatPrice(last) + ", " +
				Global.inst().formatPrice(average) + ", " +
				volume ;
	}

	 
	boolean isMinute()  {  return type=='m'; }
	boolean isDay() 	{  return type=='d'; }
	
	int getBegin()  { return time; }
	int getHigh() 	{ return high; }
	int getLow() 	{ return low; }
	int getLast() 	{ return last; }
	int getAverage(){ return average; }
	int getVolume() { return volume; }
	int getTime() 	{ return time; }	


	void enter(int time, int exePrice, int exeQty) {		

		if( exeQty==0 ) return;
		
		// check time
		if (this.time==0) this.time = time;
		else if (this.time!=time) return ;
			
		if (exeQty!=0) { 
			if (  begin==0 ) 				 	{ begin = exePrice;}// begin
			if ( (high==0) || (exePrice>high) ) { high 	= exePrice; }// high
			if ( (low==0)  || (exePrice<low)  ) { low 	= exePrice;	 }// low
			
			last = exePrice; //last

			double avg = (0.001*average*volume + 0.001*exePrice*exeQty)/(volume+exeQty);
			average = (int)(avg*1000); //average
	
			volume += exeQty; // sum
		}
	}

 
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}

}

