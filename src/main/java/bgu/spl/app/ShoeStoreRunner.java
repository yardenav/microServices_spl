package bgu.spl.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;

import bgu.spl.app.services.*;
import bgu.spl.mics.MicroService;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ShoeStoreRunner 
{
	
	
	
    public static void main( String[] args )
    {

    	
    	ArrayList<Thread> threadsList = new ArrayList<Thread>();
    	// ================ LOGGER stuff
    	//Logger format - -Djava.util.logging.SimpleFormatter.format=%5$s%6$s%n
    	// for full class info - -Djava.util.logging.SimpleFormatter.format=%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n
    	
    	Path path1 = Paths.get("/users/studs/bsc/2016/yardenav/workspace/java_workspace/spl-assignment2/MyLogFile.log");
    	Path path2 = Paths.get("/users/studs/bsc/2016/yardenav/workspace/java_workspace/spl-assignment2/MyLogFile.log.lck");
    	try {
    	Files.deleteIfExists(path1);
    	Files.deleteIfExists(path2);
    	} catch (IOException e)
    	{
    		
    	}
    	Logger log = Logger.getLogger("MyLog");
        
        FileHandler fh;
        
        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("/users/studs/bsc/2016/yardenav/workspace/java_workspace/spl-assignment2/MyLogFile.log");  
            log.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

            // the following statement is used to log any messages  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  

        
    	//============== Json parsing
    	Gson gson = new Gson();
    	GsonDataObject dataStorage;

    	try {
    		
			Scanner user_input = new Scanner( System.in );
			
    		BufferedReader br = new BufferedReader(
    			new FileReader("/users/studs/bsc/2016/yardenav/workspace/java_workspace/spl-assignment2/src/main/java/bgu/spl/" + user_input.nextLine() ));
    		user_input.close();
    		//convert the json string back to object
    		dataStorage = gson.fromJson(br, GsonDataObject.class);
        	int x = dataStorage.services.factories + dataStorage.services.sellers + dataStorage.services.customers.length + 1;
        	CountDownLatch clock = new CountDownLatch(x);
        	
        	log.log(Level.INFO, " ========== INITIALZATION STARTS =============");
        	
        	// Init the store info
        	Store store = Store.getInstance();
        	store.load(dataStorage.initialStorage);
        	
        	int threadIndex = 0;
        	
        	threadsList.add(new Thread(new ManagementService(dataStorage.services.manager.discountSchedule, clock)));
        	threadsList.get(threadIndex).start();
        	threadIndex++;
        	
        	for(int i=0 ; i<dataStorage.services.factories; i++){
        		threadsList.add(new Thread(new ShoeFactoryService("factory" + i, clock)));
        		threadsList.get(threadIndex).start();
        		threadIndex++;
        	}
        	
        	
        	for(int i=0 ; i<dataStorage.services.sellers; i++){
        		threadsList.add(new Thread(new SellingService("seller" + i, clock)));
        		threadsList.get(threadIndex).start();
        		threadIndex++;
        	}
        	
        	for(int i=0 ; i<dataStorage.services.customers.length; i++){
        		threadsList.add(new Thread(new WebsiteClientService(dataStorage.services.customers[i].name, dataStorage.services.customers[i].purchaseSchedule, dataStorage.services.customers[i].wishList, clock )));
        		threadsList.get(threadIndex).start();
        		threadIndex++;
        	}
        	
        	threadsList.add(new Thread(new TimeService(dataStorage.services.time.speed, dataStorage.services.time.duration, clock)));
    		threadsList.get(threadIndex).start();
   
           

    		

    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		for (int i=0; i<threadsList.size(); i++)
    		{
    			try {
					threadsList.get(i).join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
			Store.getInstance().print();
    
    }
    	
    	
    	
    	
 
    	
    
    	

    }
    
    
    
}
