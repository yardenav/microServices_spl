package bgu.spl.app.services;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.Receipt;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.TerminationBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService {
	
	private int currTick; 
	private int flagTick; //flag to check if the current tick was changed
	private ConcurrentLinkedQueue<Receipt> shoesToManofacureQueue;
	private ConcurrentLinkedQueue<ManufacturingOrderRequest> manuRequestsBeingHandled;
	private CountDownLatch clock;

	private static final Logger log = Logger.getLogger("MyLog");
	
	public ShoeFactoryService(String name, CountDownLatch clock) {
		super(name);
		this.clock = clock;
		currTick = 0;
		this.manuRequestsBeingHandled = new ConcurrentLinkedQueue<ManufacturingOrderRequest>();
		this.shoesToManofacureQueue = new ConcurrentLinkedQueue<Receipt>();
		
	}
	
	private boolean currTickVerify(){
		if(currTick == flagTick){
			return true;
		}
		
		else{
			return false;
		}
	}

	@Override
	protected synchronized void initialize() {
		
		log.log(Level.INFO,  this.getName()+ " started working");
		subscribeBroadcast(TickBroadcast.class, tick -> { //updates the currTickick
			this.currTick = tick.getTick();
			Receipt currReceipt = this.shoesToManofacureQueue.poll();
			if(currReceipt != null && currReceipt.getSeller().equals("ShoeFactory")){ //checks each tick to see if a requests was finished in that tick.
				
				this.complete(this.manuRequestsBeingHandled.poll(), currReceipt);
				log.log(Level.INFO, "         ## Factory " + this.getName() + " Manufactured: "  + currReceipt.getAmountSold() + " " +  currReceipt.getShoeType() + " in tick " + tick.getTick());
			}
			});
		
		subscribeRequest(ManufacturingOrderRequest.class, manufacturingOrderRequest -> { //creates the requested shoes - each tick on shoes.
			this.manuRequestsBeingHandled.add(manufacturingOrderRequest); //saves the manufacturing request in the reqs queue.
			for(int i = 1; i < manufacturingOrderRequest.getNumOfShoes(); i++){ //add n-1 empty receipts to the receipts queue

				this.shoesToManofacureQueue.add(new Receipt(new String("still working"), new String("Store"), new String(manufacturingOrderRequest.getShoeType()), false, this.currTick, this.currTick-i, i) );
			}	 // create the last shoes with the proper receipt.
				this.shoesToManofacureQueue.add(new Receipt(new String("ShoeFactory"), new String("Store"), new String(manufacturingOrderRequest.getShoeType()), false, 1+this.currTick+shoesToManofacureQueue.size(), this.currTick,manufacturingOrderRequest.getNumOfShoes()) );

			});
		
		subscribeBroadcast(TerminationBroadcast.class,  req -> {
			log.log(Level.INFO, name + "was terminated");
			this.terminate();
		});
		
		log.log(Level.INFO, this.getName()+ " was init");
		clock.countDown();

	}

}
