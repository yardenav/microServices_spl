package bgu.spl.app.services;

import bgu.spl.mics.MicroService;
import bgu.spl.app.DiscountSchedule;
import bgu.spl.app.Receipt;
import bgu.spl.app.Store;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminationBroadcast;
import bgu.spl.app.messages.TickBroadcast;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagementService extends MicroService {

	private ArrayList<DiscountSchedule> discountSchedule;
	private ConcurrentHashMap<String,Integer> shoesBeingManufactured;
	private ConcurrentHashMap<String,Integer> waitingSellers;
	private int currTick;
	private Store storeInstance;
	private CountDownLatch clock;
	private ConcurrentHashMap<String, ConcurrentLinkedQueue<RestockRequest>> restockRequests;
	
	private static final Logger log = Logger.getLogger("MyLog");
	
	public ManagementService(DiscountSchedule[] discountSchedule, CountDownLatch clock) {
		super("manager");
		this.clock = clock;
		this.discountSchedule = new ArrayList<DiscountSchedule>();
		this.shoesBeingManufactured = new ConcurrentHashMap<String,Integer>();
		this.waitingSellers = new ConcurrentHashMap<String,Integer>();
		this.restockRequests = new ConcurrentHashMap<String, ConcurrentLinkedQueue<RestockRequest>>();
		
		
		for(int i=0; i<discountSchedule.length; i++){
			this.discountSchedule.add(discountSchedule[i]);
		}	
		currTick = 0;
		storeInstance = Store.getInstance();
	}
	
	private boolean  shouldManufacture(RestockRequest restockReq){
		
		if (this.shoesBeingManufactured.containsKey(restockReq.getShoeType())) // make sure the record is existed
		{
			int shoesBeingManufactured = this.shoesBeingManufactured.get(restockReq.getShoeType());
			int watingSellers = this.waitingSellers.get(restockReq.getShoeType());
	
			//log.log(Level.INFO, "There are" + watingSellers + "waiting sellers" + "and there are" + shoesBeingManufactured+"shoes being manufactured");
			if(watingSellers >= shoesBeingManufactured){
				return true;
			}
			else
			{
				return false;
			}
		}
		//log.log(Level.INFO, "There are" + this.waitingSellers.get(restockReq.getShoeType()) + "waiting sellers" + "and there are" + 0 +"shoes being manufactured");

		return true;	
	}

	@Override
	protected synchronized void initialize() {
		log.log(Level.INFO, "^^ manger started working");
		
		subscribeBroadcast(TickBroadcast.class, tick -> { //gets the current tick and check to see which shoes need to be purchase.
			this.currTick = tick.getTick();
			for (int i = 0; i < discountSchedule.size(); i++)
			{
				String shoeType = discountSchedule.get(i).getShoeType();
			    if (discountSchedule.get(i).getTick() == currTick)
			    {
			    	log.log(Level.INFO, "^^ Manager sent a Discount Broadcast for " + shoeType);
			    	sendBroadcast(new NewDiscountBroadcast(shoeType, discountSchedule.get(i).getAmount()));
			    	storeInstance.addDiscount(shoeType, discountSchedule.get(i).getAmount()); //* if shoe not exists yet, it will create a record.
			    	discountSchedule.remove(i);
			    
			    }
			}
				});
		
		subscribeRequest(RestockRequest.class, restockReq -> {
			
			if (waitingSellers.containsKey(restockReq.getShoeType()))
			{
				int newVal = waitingSellers.get(restockReq.getShoeType()) + 1;
				waitingSellers.put(restockReq.getShoeType(), newVal);
			}
			else
			{
				
				waitingSellers.put(restockReq.getShoeType(), 1);
			}
			
			if(this.shouldManufacture(restockReq)){
				
				if(this.shoesBeingManufactured.containsKey(restockReq.getShoeType())){ //true - there are shoes from that type that are being manufactured but not enough.
					int newVal = this.shoesBeingManufactured.get(restockReq.getShoeType()) + (this.currTick%5) + 1;
					this.shoesBeingManufactured.put(restockReq.getShoeType(), newVal);//updates the manufactured shoes  amount
				}
				
				else{ //no shoes from that type that are being manufactured
					this.shoesBeingManufactured.put(restockReq.getShoeType(),  (this.currTick%5) + 1);
				}
				
				if(this.restockRequests.containsKey(restockReq.getShoeType())){//checks if there is already a queue for the shoe being restocked
					
					this.restockRequests.get(restockReq.getShoeType()).add(restockReq);;//add the restock request to a queue (if there is a need to manufacture more shoes) so we can complete them when factory finished manufactured.
				}
				
				else{ //need to create a new queue for the requested shoes
					ConcurrentLinkedQueue<RestockRequest> shoeQueue = new ConcurrentLinkedQueue<RestockRequest>();
					shoeQueue.add(restockReq);
					this.restockRequests.put(restockReq.getShoeType(), shoeQueue);
				}
				
				log.log(Level.INFO, "      ^^ Manager: "+ this.getName() +"sent Manufacture Request to produce " + restockReq.getShoeType());
				this.sendRequest(new ManufacturingOrderRequest(restockReq.getShoeType(), (this.currTick%5)+1 ), receipt ->{
						
					log.log(Level.INFO, "      ^^ The restock request for " + restockReq.getShoeType() + " was finished and " + receipt.getAmountSold() + " was produced");
					for(int i =0; i<receipt.getAmountSold(); i++){ //completes the restock requests for the manufactured shoes amount.
						if(!this.restockRequests.get(receipt.getShoeType()).isEmpty()){
							complete(this.restockRequests.get(receipt.getShoeType()).poll(), true);
						}
					}
					storeInstance.add(receipt.getShoeType(), receipt.getAmountSold());
					storeInstance.file(receipt);
						
				});
			}
			
			else{
				this.restockRequests.get(restockReq.getShoeType()).add(restockReq); //add the restock request to a queue (if no need to manufacture more shoes) so we can complete them when factory finished manufactured.
			}
			
			
		});
		
		subscribeBroadcast(TerminationBroadcast.class,  req -> {
			log.log(Level.INFO, "Manager was terminated");
			this.terminate();
		});
		
		log.log(Level.INFO, this.getName()+ " was init");
			clock.countDown();
	
	}

}
