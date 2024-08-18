

///
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
///

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class Chart { //default is a 50 ms per point
	final static char MINUTE='m';
	final static char SECOND='s';
	final static char DAY='d';
	
	char type='s';
	int  unit=50;  //unit is 50 ms
	int  size=100;
	
	LinkedList <Bar> Bars = new LinkedList <Bar>();

	public Chart(char type, int unit, int size) {
		this.type = type;
		this.unit = unit;
		this.size = size;
	}

	void setSize(int size) {
		this.size=size;
	}
	
	String getString() {
		String str="";
		for (Bar bar: Bars) {
			str = str + bar.getString() + "\n";
		}
		return str;
	}
	
	String getInverseString() {
		String str="";
	    Iterator<Bar> itr = Bars.descendingIterator();
	    while(itr.hasNext()){
	    	str = str +itr.next().getString() + "\n";
	    }
	    return str;
	}
    
	
	void enterOrder(Order ord) {
		
		if (ord.isExecuted()) { // executed order - only one side
			if (ord.isBuyOrder())// record buy order only
				enter( ord.getLastExeTime(), ord.getLastExePrice(),ord.getLastExeQuantity());
		}
	}
	
	int calcUnitTime(int time,int unit) {// such as 50 ms		
		return time/unit*unit;	
	} 
	
	private void enter(int lastTime,int lastExePrice, int lastExeQuantity ) {

		int chartTime = calcUnitTime(lastTime,this.unit); 
		
		Bar lbar=null;
		if ( Bars.isEmpty() )		{
			lbar = new Bar(this.type,chartTime, lastExePrice, lastExeQuantity);
			Bars.addFirst(lbar);
		}else { // existing bar
			lbar= Bars.getFirst();
			
			if (lbar.time!=chartTime) {
				lbar = new Bar(this.type,chartTime, lastExePrice, lastExeQuantity);
				Bars.addFirst(lbar);
			} else	{
				lbar.enter(chartTime, lastExePrice, lastExeQuantity);
			}
		}
		
		if (Bars.size()>this.size) // limited on size 100 
			Bars.removeLast();
	}
	
	Bar getLastBar() {
		if ( Bars.isEmpty() ) 
			return new Bar();
		else
			return Bars.getFirst();
	}

	LinkedList<Bar> getBars() {
		return Bars;
	}

	double getLastReturn() {
		Bar firstBar=null, secondBar=null;
		
		ListIterator<Bar> it = Bars.listIterator(0);
		if (it.hasNext())		{
			firstBar = it.next();
				
			if (it.hasNext()) {
				secondBar = it.next();
			}
		}

		if ((firstBar==null)||(secondBar==null) ) return 0.0;
		else
			return 1.0*(firstBar.getLast()-secondBar.getLast())/secondBar.getLast(); 
	}
	
	int getLastMA(int size) { // Moving Average 
		int i=0;
		double sumLast=0;
		for (Bar bar : Bars) {
		    sumLast +=  bar.last;
		    if (++i>=size) 
		    	break;
		}
		if (i>0) return (int)(sumLast/i);
		else	 return 0;
	}
	
	double getSMAIndicator(int size) {
		
		if (Bars.size()==0) return 1;
		
		int sma = getLastMA(size);
		int last=Bars.getFirst().getLast();

		return (1.0*sma/last);
	}
	
//	public static void main(String[] args) {
//		
//		Chart cht = new Chart();
//		System.out.println("Bar : " + Bar.getTitle());
//		
//		
//		Order ord1=new Order("F",1,1,'b',100100, 100,93001001);	cht.enterOrder(ord1);
//		Order ord2=new Order("F",2,1,'b',100200, 100,93001001); cht.enterOrder(ord2);
//		Order ord3=new Order("F",3,1,'b',100200, 200,93001001); cht.enterOrder(ord3);
//		Order ord4=new Order("F",4,1,'b',100300, 300,93001001); cht.enterOrder(ord4);
//		
//		System.out.println("Bar : " + cht.getLastBar().getString());
//
//		ord1.cancel(93001001);					cht.enterOrder(ord1);
//		ord2.execute(100200, 100, 93001001);	cht.enterOrder(ord2);
//		ord4.execute(100300, 100, 93001101);	cht.enterOrder(ord4);
//		
//		System.out.println("Bar : " + cht.getLastBar().getString());
//
//		System.out.printf("Return : %f\n" , cht.getLastReturn());
//		LinkedList<Bar> bars = cht.getBars();
//		System.out.println("Bar : " + Bar.getTitle());
//		for (Bar bar: bars) {
//			System.out.println("Bar : " + bar.getString());
//		}
//		System.out.println("Moving Average : " + 0.001*cht.getLastMovingAverage(2));
//	}
	
}
