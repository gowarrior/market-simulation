
///
//Time       : Coder     : Email            : Task
//08/28/2019 : Xugong Li : xli7@stevens.edu : create
///

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class StatHolder { 
	
	private Map<Integer, LinkedList<Stat>> statMap = new TreeMap<>();

	int  unit=50;  
	int  size=100;
	
	public StatHolder(int unit, int size) {

		this.unit = unit;
		this.size = size;
	}
	
	String getString() {
		String str="";
		
		for (Map.Entry<Integer, LinkedList<Stat>> entry : statMap.entrySet()) {
			LinkedList<Stat> stats = entry.getValue();
			for (Stat st: stats) {
				str = str + st.getString() + "\n";
			}
		}
		return str;
	}
	
	String getInverseString() {
		String str="";
		for (Map.Entry<Integer, LinkedList<Stat>> entry : statMap.entrySet()) {
			LinkedList<Stat> stats = entry.getValue();
	
		    Iterator<Stat> itr = stats.descendingIterator();
		    while(itr.hasNext()){
		    	str = str +itr.next().getString() + "\n";
		    }
		}
		return str;
	}
	
	void enter( Order ord) {
		
		if (ord.isExecuted()) { // executed order 
			enterStat( ord.getTraderType(), ord.getLastExeTime(), ord.getBuySell(),0, ord.getLastExePrice(),ord.getLastExeQuantity(),0);
		}
		else if (ord.isCanceled()) { //canceled order
			enterStat(ord.getTraderType(), ord.getCanceledTime(),ord.getBuySell(), 0,0,0,ord.getCanceledQuantity());
		}
		else if (ord.isAccepted()) { //new order
			enterStat(ord.getTraderType(), ord.getTime(),ord.getBuySell(),ord.getQuantity(), 0,0,0);
		}
	}
	
	int calcUnitTime(int time,int unit) {// such as 50 ms		
		return time/unit*unit;	
	} 
	
	private void enterStat(int tradertype, int lastTime, char buySell, int entQty, int exePx, int exeQty,  int cxlQty ) {

		LinkedList<Stat> statList = getStats(tradertype); 
		
		int statTime = calcUnitTime(lastTime,this.unit); 

		if ( statList.isEmpty() ) { // empty list
			
			Stat st = new Stat(tradertype);
			st.enter(statTime, buySell, entQty, exePx, exeQty, cxlQty);
			statList.addFirst(st);
			
		}else { // existing bar
			Stat st = statList.getFirst();
			
			if (st.time!=statTime) { // different stat time 
				st = new Stat(tradertype);
				st.enter(statTime, buySell, entQty, exePx, exeQty, cxlQty);
				statList.addFirst(st);
			} else	{
				st.enter(statTime, buySell, entQty, exePx, exeQty, cxlQty);
			}
		}
		
		if (statList.size()>this.size) // limited on size 100 
			statList.removeLast();
	}
	
	Stat getLastStat(int tradertype) { // find last stat for one trader
		LinkedList<Stat> stats = statMap.get(tradertype);
		if ( (stats!=null) && (stats.size()>0) ) {
			return stats.getFirst();
		} else {
			return (new Stat(tradertype));
		}
	}

	LinkedList<Stat> getStats(int tradertype) { // find stat list by trader type
		LinkedList<Stat> stats = statMap.get(tradertype);
		if (stats==null) {
			stats = new LinkedList<Stat>();
			statMap.put(tradertype,stats);
		}
		return stats;
	}
	
//		public static void main(String[] args) {
//			
//			Chart cht = new Chart();
//			System.out.println("Bar : " + Bar.getTitle());
//			
//			
//			Order ord1=new Order("F",1,1,'b',100100, 100,93001001);	cht.enterOrder(ord1);
//			Order ord2=new Order("F",2,1,'b',100200, 100,93001001); cht.enterOrder(ord2);
//			Order ord3=new Order("F",3,1,'b',100200, 200,93001001); cht.enterOrder(ord3);
//			Order ord4=new Order("F",4,1,'b',100300, 300,93001001); cht.enterOrder(ord4);
//			
//			System.out.println("Bar : " + cht.getLastBar().getString());
//
//			ord1.cancel(93001001);					cht.enterOrder(ord1);
//			ord2.execute(100200, 100, 93001001);	cht.enterOrder(ord2);
//			ord4.execute(100300, 100, 93001101);	cht.enterOrder(ord4);
//			
//			System.out.println("Bar : " + cht.getLastBar().getString());
//
//			System.out.printf("Return : %f\n" , cht.getLastReturn());
//			LinkedList<Bar> bars = cht.getBars();
//			System.out.println("Bar : " + Bar.getTitle());
//			for (Bar bar: bars) {
//				System.out.println("Bar : " + bar.getString());
//			}
//			System.out.println("Moving Average : " + 0.001*cht.getLastMovingAverage(2));
//		}
	
}
