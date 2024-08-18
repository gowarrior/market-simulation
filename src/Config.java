
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.*; 

		
public class Config { // define initial variables as static   
	
	static TickTime START_MARKET_TIME = new TickTime( 9,00,0,0);	// market start time
	static TickTime END_MARKET_TIME   = new TickTime( 16,30,0,0); //new TickTime(16,15,0,0);	// market start time
	
	static int DISPLAY_INTERVAL = 5*60*1000; // display market information per 1 minute

	static final int DOLLAR=1000;
	static final int CENT=10;
	static  int TICK_MS=100; // number of milliseconds per tick, 1ms per tick or 2ms per tick   

	//initialSymbols and initialPrice should same size
	static String INIT_SYMBOL[]= {"FUTURE"}; //{"FUTURE","ESU10"};
	static int INIT_PRICE []= {1350*DOLLAR};//{1350000,1234000}; is 1350.000
	
	static int FILE_ID=0;
	static String RESULT_PATH="result";
	
	static private int nMaxPos[] = {10,10,10,10,10,10,10,10,10}; // Initialize the maximum positions, then modify them  when they are increased 
	
	static int 	 getMaxPos(int id) { return nMaxPos[id-1]; }
	static void  setMaxPos(int id, int n) { nMaxPos[id-1] = n; }
	
	static boolean hasSpoofing = true;
	static boolean hasLayering = true;
	static boolean hasSideLayering = true;
	
	static void ReadFromFile()  {

		try {
			
	        //Reading properties file in Java example
	        Properties props = new Properties();
	        File file = new File("Config.xml");

	        FileInputStream fis = new FileInputStream(file);
	        props.loadFromXML(fis);//loading properties from a property file
	        
	        TICK_MS = Integer.parseInt(props.getProperty("TICK_MS"));
	        DISPLAY_INTERVAL = Integer.parseInt(props.getProperty("DISPLAY_MINUTE"))*60*1000;
	        
	        INIT_SYMBOL[0] 	 = props.getProperty("INIT_SYMBOL");
	        INIT_PRICE [0] 	 = Integer.parseInt(props.getProperty("INIT_PRICE"))*DOLLAR;
	        hasSpoofing 	 = Boolean.parseBoolean(props.getProperty("HAS_SPOOFING"));
	        hasLayering 	 = Boolean.parseBoolean(props.getProperty("HAS_LAYERING"));
	        hasSideLayering  = Boolean.parseBoolean(props.getProperty("HAS_SIDELAYERING"));

	        RESULT_PATH  	 = props.getProperty("RESULT_PATH");
	        FILE_ID  		 = Integer.parseInt(props.getProperty("FILE_ID"));
	        fis.close();
	        
	        //save value
	        FileOutputStream fos = new FileOutputStream (file);
	        props.setProperty("FILE_ID",String.valueOf(FILE_ID+1));
	        props.storeToXML(fos,null);
	        fos.close();
	        
	        System.out.printf("Current File ID =%d ", FILE_ID);
	        
		} catch (IOException e) {
			e.printStackTrace();
			System.out.printf("Simulation is on default!");
		}
	}
 	
	
}
 