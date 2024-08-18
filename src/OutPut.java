/////////////////////////////////////////////////////
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
/////////////////////////////////////////////////////

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;


public class OutPut {
 
	static final int FLUSH_MAX_LINE = 500;
	
	// order file 
	private FileWriter orderFileWriter = null;
	private PrintWriter orderPrintWriter=null;
	private int mdpWriterCounter=0;
	
	//print file or display in screen
	public void printLevel1(Level1 l1) 		{ System.out.println("Level1, " + l1.getString());	}
	public void printNBBO(Level1 l1) {
		System.out.println("Best Ask, " + TickTime.format(l1.getTime()) + ", "+ Global.inst().formatPrice(l1.getAskPrice()) + ", " + l1.getAskSize());
		System.out.println("Best Bid, " + TickTime.format(l1.getTime()) + ", "+Global.inst().formatPrice(l1.getBidPrice()) + ", " + l1.getBidSize());
	}
	
	public void printTick(int tick) 		{ System.out.println("Tick, "+ tick); }
	public void printCurrentTime() 			{ System.out.println("Current Time, " + TickTime.format(Global.inst().getCurrentTime()));	}
	public void printBook(OrderBook book)	{ System.out.println(book.getString());	}
	public void printChartBar(Bar bar) 		{ System.out.println("Bar, " + bar.getString()); }
	public void printPosition(Position pos) { System.out.println(pos.getString());	}
	public void printSymbol(String s) 		{ System.out.println("Symbol, " + s);	};
	public void printReturn(double db) 		{ System.out.println("Return, " + db);	};

	// print OrderStat  
	public void printOrderStat(StatHolder stats) {
		try {
			String strFile= String.format("%s\\OrderStat_%d.csv", Config.RESULT_PATH, Config.FILE_ID);
			FileWriter chartFileWriter = new FileWriter(strFile);
			PrintWriter chartPrintWriter = new PrintWriter(chartFileWriter);

			chartPrintWriter.println(Stat.getTitle());
			chartPrintWriter.print(stats.getInverseString());
	        chartPrintWriter.close(); 
        } 
        catch (Exception e) { 
            System.out.println(e); 
        } 
	}
	
	// print chart  
	public void printChart(Chart cht) {

		try {
			String strFile= String.format("%s\\MinuteChart_%d.csv", Config.RESULT_PATH, Config.FILE_ID);
			FileWriter chartFileWriter = new FileWriter(strFile);
			PrintWriter chartPrintWriter = new PrintWriter(chartFileWriter);
			
			chartPrintWriter.println(Bar.getTitle());
	
			chartPrintWriter.print(cht.getInverseString());
			
//				LinkedList<Bar> bars = cht.getBars();
//		        Iterator<Bar> itr = bars.descendingIterator();
//		        while(itr.hasNext()){
//		            chartPrintWriter.println(itr.next().getString());
//		        }
			 
	        chartPrintWriter.close(); 
        } 
        catch (Exception e) { 
            System.out.println(e); 
        } 
	}

	
	// print combinedOrder 
	public void printOrder(Order ord, int traderStep) {
		
		if (orderPrintWriter==null ) {		openOrder();		}
 
		MdpState preState = ord.getStartState();
		MdpState aftState = ord.getAfterState();
		
		if (orderPrintWriter==null ) {		openOrder();		}
 
		orderPrintWriter.printf("%s,%s,%s,%d\n", 
							ord.getString(), 
							preState.getString(), 
							aftState.getString(), 
							traderStep);
		
		// flush order from buffer into file
		if (mdpWriterCounter++>FLUSH_MAX_LINE) {
			flushOrder();
			mdpWriterCounter=0;
		}
	}

	public void openOrder()  
	{ 
		try {
			String strFile= String.format("%s\\OrderDetail_%d.csv",Config.RESULT_PATH, Config.FILE_ID);
		    orderFileWriter = new FileWriter(strFile);
		    orderPrintWriter = new PrintWriter(orderFileWriter);
			
		    orderPrintWriter.printf("%s,%s,%s,%s\n", // Order, Prices, States,  AfterStates, PriceReturn 
		    		Order.getTitle(),
		    		MdpState.getTitle("Pre"),
		    		MdpState.getTitle("Aft"),
		    		"TraderStep"
		    		);
		    
		    flushOrder();
		    mdpWriterCounter = 0;
		    
		}   catch(IOException e) {
			orderPrintWriter.close();
		}
	}
	
	public void flushOrder() {
		if (orderPrintWriter!=null )
			orderPrintWriter.flush();
	}
	
	public void closeOrder() {
		if (orderPrintWriter!=null )
			orderPrintWriter.close();
	}
	
	// get instance and static functions 
	private OutPut()	{ 
	} 
	
	private static OutPut single_instance = null; 
	public static OutPut inst()	{
		if (single_instance == null)
			single_instance = new OutPut(); 
	  
	    return single_instance; 
	}

//	public static void main(String[] args) {  // unit test
//		
//		OutPut op = new OutPut();
//	    
//	    Order ordBuy1=new Order("FUTURE",1, 1, 'b', 100, 100, 888888);	
//	    
//	    MdpState preState = new MdpState(); 
//	    MdpState aftState = new MdpState(); 
//	    double pxReturn=0.0;
//	    
//	    op.openMdpOrder();
//	    op.printMdpOrder(ordBuy1,preState,aftState,pxReturn);
//	  //  op.closeMdpOrder();
//	}	
}
