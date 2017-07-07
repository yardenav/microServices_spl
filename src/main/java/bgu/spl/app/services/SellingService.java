package bgu.spl.app.services;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.spl.app.Receipt;
import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminationBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService {

	private Store storeInstance = Store.getInstance();
	private int currTick;
	private String sellerName;
	private CountDownLatch clock;
	
	private static final Logger log = Logger.getLogger("MyLog");
	
	public SellingService(String name, CountDownLatch clock) {
		super(name);
		this.clock = clock;
		this.sellerName = name;
		currTick = 0;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected synchronized void initialize() {
		
		log.log(Level.INFO,this.sellerName + " started working");
		subscribeBroadcast(TickBroadcast.class, tick -> { //gets the current tick and check to see which shoes need to be purchase.
			this.currTick = tick.getTick();

				});
		
		this.subscribeRequest(PurchaseOrderRequest.class, req -> {

			BuyResult result = storeInstance.take(req.getShoeType(), req.isOnlyDiscount());
			
			if (req.isOnlyDiscount()) // Request sended from the wishList (after discount broadcast)
			{
				if (result == BuyResult.DISCOUNTED_PRICE) // shoe was on discount - all good.
				{
					Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), true, this.currTick, req.getRequestTick(),1);
					storeInstance.file(receipt);
					complete(req, receipt);
					log.log(Level.INFO, "   && Seller: " + sellerName + " completed " + req.getCustomer() + " request for " + req.getShoeType() + " enum- " + result);
					
				}
				else if ((result == BuyResult.ON_STORAGE_BUT_NOT_ON_DISCOUNT) || (result == BuyResult.NOT_ON_STORAGE_NOT_ON_DISCOUNT))// not on discount
				{
					complete(req, null);
					log.log(Level.INFO, "   && Seller: " + sellerName + " didn't find the discounted " + req.getShoeType()+ " for " + req.getCustomer() + " enum- " + result );

				}else if (result == BuyResult.NOT_ON_STORAGE_BUT_ON_DISCOUNT) // shoe got discount but not on storage - means we need to order
				{
					sendRequest(new RestockRequest(req.getShoeType()), success -> {
						if (success)
						{
							Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), false, this.currTick, req.getRequestTick(),1);
							storeInstance.decreaseDiscountedShoe(req.getShoeType());
							storeInstance.file(receipt);
							complete(req, receipt);
							log.log(Level.INFO, "   && (after restock req) Seller: " + sellerName + " completed " + req.getCustomer() + " request for " + req.getShoeType() + " enum- " + result);
						}
						else
						{
							complete(req, null);
							log.log(Level.INFO, "No Factories to take the Restock request" + " enum- " + result);
						}
					});
					log.log(Level.INFO, "   && " + sellerName + " sent a restock request to produce " + req.getShoeType());
				}
			}
			else // request sended from the schedule
			{
				
				if (result == BuyResult.DISCOUNTED_PRICE) // the shoe exists with discount so we give him discounted shoe
				{
					Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), true, this.currTick, req.getRequestTick(),1);
					complete(req, receipt);
					storeInstance.file(receipt);
					log.log(Level.INFO, "   && Seller: " + sellerName + " completed " + req.getCustomer() + " request for " + req.getShoeType() + " enum- " + result);
				}
				else if (result == BuyResult.REGULAR_PRICE) // no discounted shoes - we give him normal price.
				{
					Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), false, this.currTick, req.getRequestTick(),1);
					storeInstance.file(receipt);
					complete(req, receipt);
					log.log(Level.INFO, "   && Seller: " + sellerName + " completed " + req.getCustomer() + " request for " + req.getShoeType() + " enum- " + result);
				}
				else if  (result == BuyResult.NOT_IN_STOCK) // need to send restock message
				{
					log.log(Level.INFO, "   && Restock Request was sended for: " + req.getShoeType() + " to give to " + req.getCustomer() + " enum- " + result);
					sendRequest(new RestockRequest(req.getShoeType()), success -> {
						if (success)
						{
							Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), false, this.currTick, req.getRequestTick(),1);
							storeInstance.decreaseShoe(req.getShoeType());
							storeInstance.file(receipt);
							complete(req, receipt); 
							log.log(Level.INFO, "   && (after restock req) Seller: " + sellerName + " completed " + req.getCustomer() + " request for " + req.getShoeType() + " enum- " + result);
						}
						else
						{
							complete(req, null);
							log.log(Level.INFO, "No Factories to take the Restock request" + " enum- " + result);
						}
					});
				}
				

			}
		});
		
		subscribeBroadcast(TerminationBroadcast.class,  req -> {
			log.log(Level.INFO, sellerName + " was terminated");
			this.terminate();
		});
		
		log.log(Level.INFO,this.getName()+ " was init");
		clock.countDown();
	}

}
