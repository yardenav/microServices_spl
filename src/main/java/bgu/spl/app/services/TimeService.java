package bgu.spl.app.services;
import bgu.spl.app.Store;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.mics.*;
import bgu.spl.app.messages.*;
public class TimeService extends MicroService {


	private Store storeInstance = Store.getInstance();
	private int speed;
	private int duration;
	private int currentTick;
	private Timer timer;
	private CountDownLatch clock;
	
	private static final Logger log = Logger.getLogger("MyLog");
	
	public TimeService(int speed, int duration, CountDownLatch clock)
	{
		super("timer");
		this.clock = clock;
		this.timer = new Timer();
		//TODO check what to add here
		this.speed = speed;
		this.duration = duration;
		
	}
	

	
	@Override
	public synchronized void initialize()
	{
		
		try {
			clock.await(); //waits for other services to be initialized
		} catch (InterruptedException e) {
			
		}
		
		log.log(Level.INFO, "===== All micro services were initialized ===== ");
		this.timer.scheduleAtFixedRate(new TimerTask(){
			int tick=0;
			public void run(){
				if(tick<duration){

					tick++;
					TickBroadcast tickbroad = new TickBroadcast(tick);
					log.log(Level.INFO, "Tick : " + tick);
					sendBroadcast(tickbroad);

				}
				
				else{
					log.log(Level.INFO, "===* TERMINATION BROADCAST WAS SENT TO ALL *===");
					sendBroadcast(new TerminationBroadcast());
					timer.cancel();
					timer.purge();
				}
			}
		}, speed, speed);
		
	 this.terminate();


	}

	
}
