package bgu.spl.app.messages;

import bgu.spl.app.*;
import bgu.spl.mics.*;




public class PurchaseOrderRequest implements Request<Receipt> {
	
	private String customer;
	private String shoeType;
	private int requestTick;
	private boolean onlyDiscount;
	
	public PurchaseOrderRequest(String customer, String shoeType, int requestTick, boolean disc) {
		super();
		this.customer = customer;
		this.shoeType = shoeType;
		this.requestTick = requestTick;
		this.onlyDiscount = disc;
	}

	public String getCustomer() {
		return customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getRequestTick() {
		return requestTick;
	}

	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}
	
	
}
