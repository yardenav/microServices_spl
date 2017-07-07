package bgu.spl.app.services;

import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.Store;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.example.messages.ExampleRequest;
import bgu.spl.app.PurchaseSchedule;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.GsonDataObject.Shoe;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.TerminationBroadcast;


public class WebsiteClientService extends MicroService {

	private String clientName;
	private Store storeInstance;
	private int currTick;
	private CountDownLatch clock;
	
	// think if to change to hash map and change the string to an object
	private String[] wishList;
	private ConcurrentHashMap<Integer , ArrayList<PurchaseSchedule>> purchaseSchedule;
	
	private static final Logger log = Logger.getLogger("MyLog");
	
	// In order to get notified when new discount is available, the client should subscribe to the NewDiscountBroadcast
	// message. If the client finish receiving all its purchases and have nothing in its
	// wishList it must immidiatly terminate.
	
	public WebsiteClientService(String name, Shoe[] ps, String[] wishList, CountDownLatch clock) {
		super(name);
		this.clock = clock;
		clientName = name;
		this.purchaseSchedule = new ConcurrentHashMap<Integer , ArrayList<PurchaseSchedule>>();
		
		if (wishList.length > 0 )
			this.wishList = wishList;
		else
			this.wishList = null;
		
		for (int i=0 ; i< ps.length ; i++)
		{
			if (!purchaseSchedule.containsKey(ps[i].tick))
			{
				purchaseSchedule.put(ps[i].tick, new ArrayList<PurchaseSchedule>());	
			}
			PurchaseSchedule schedule = new PurchaseSchedule(ps[i].shoeType,ps[i].tick);
			purchaseSchedule.get(ps[i].tick).add(schedule); 
	
		}
		
		
	}
	

	@Override
	protected synchronized void initialize() {
		
		log.log(Level.INFO, " ** Customer  " + clientName + " started");
		
		subscribeBroadcast(TickBroadcast.class, tick -> { //gets the current tick and check to see which shoes need to be purchase.
			this.currTick = tick.getTick();
			//log.log(Level.INFO, "name : " + clientName + " purchaseSchedule: " + purchaseSchedule.get(tick.getTick()) + "tikkk - " + tick.getTick());
			if (purchaseSchedule.get(currTick) != null){
				for(int i=0; i < purchaseSchedule.get(currTick).size(); i++){
					String shoeName = purchaseSchedule.get(currTick).get(i).getShoeType();
					log.log(Level.INFO, " ** " + clientName + " Sent a purchase request for " + shoeName);
					this.sendRequest(new PurchaseOrderRequest(this.getName(), shoeName, tick.getTick(), false), reciept -> {
						if(reciept != null ){
						deleteFromWishlist(reciept.getShoeType());
						log.log(Level.INFO, " ** The purchase by " + reciept.getCustomer() + " of " + reciept.getShoeType() + " was completed");
						}
						else
						{
							log.log(Level.INFO, " ** The purchase of " + clientName + " FAILD");
						}
					});
				}
			}
				purchaseSchedule.remove(currTick);
				});
		
		subscribeBroadcast(NewDiscountBroadcast.class, discountReq->{
			if (isOnWishlist(discountReq.getShoeType()))
			{
			
				log.log(Level.INFO, " ** " + clientName + " Sent a purchase request for " + discountReq.getShoeType());
				if (!this.sendRequest(new PurchaseOrderRequest(this.getName(), discountReq.getShoeType(), currTick,true), reciept -> {
				if(reciept != null){
					deleteFromWishlist(reciept.getShoeType());
					log.log(Level.INFO, " ** The purchase by " + reciept.getCustomer() + " of " + reciept.getShoeType()+  " was completed");
				
				}
				else
				{
					log.log(Level.INFO, " ** The purchase of " + clientName + " FAILED");
				}
			})) { log.log(Level.INFO, " ** The Purchase Message from " + this.getName()+ " wasn't sent");} // if sending was failed
			}

		});
		
		subscribeBroadcast(TerminationBroadcast.class,  req -> {
			log.log(Level.INFO, clientName + " was terminated");
			this.terminate();
		});
		
		log.log(Level.INFO, " ** " + this.getName()+ " was init");
		clock.countDown();
	}
	
	private void deleteFromWishlist(String type)
	{
		if (wishList != null)
		{
			for (int i = 0; i < wishList.length; i++)
			{
			    if ((wishList[i] != null) &&  (wishList[i].equals(type)))
			    {
			    	wishList[i] = null;
			        break;
			    }
			}	
		}
		
	}
	
	private boolean isOnWishlist(String type)
	{
		boolean ans = false;
		if (wishList != null)
		{
			for (int i = 0; i < wishList.length; i++)
			{
				//log.log(Level.INFO, "======wish> " + wishList[i] + "=====type>" + type + "==== i>" + i);
				String str = wishList[i]; 
			    if (str != null && str.equals(type))
			    {
			    	ans = true;
			    }
			}
		}
		return ans;
	}

}
