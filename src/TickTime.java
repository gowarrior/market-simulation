
public class TickTime {

	int time=0; // number of million seconds
	
	TickTime(){}
	TickTime(int time){ 
		this.time=time;	
	}
	TickTime(int hour,int minute,int second, int ms){
		time = convert(hour, minute, second, ms);
	}
	
	void add(int n)  {	this.time += n;	}

	public int getTime() {	return this.time;	}
	public int getHour() 	{ return this.time/3600000;	}
	public int getMinute() 	{ return this.time%3600000/60000; }
	public int getSecond() 	{ return this.time%60000/1000; }
	public int getMs() 		{ return this.time%1000; }
	
	public String getString() {	return format(time);	}
	
	public static String format(int tickTime) {
		return String.format("%02d:%02d:%02d-%03d",
				tickTime/3600000, // hour 
				tickTime%3600000/60000, // minute
				tickTime%60000/1000, //second
				tickTime%1000);  //ms
	}	
	
	public int convert(int hour,int minute,int second, int ms) {
		return (((hour*60+minute)*60+second)*1000 + ms);
	}
	
};