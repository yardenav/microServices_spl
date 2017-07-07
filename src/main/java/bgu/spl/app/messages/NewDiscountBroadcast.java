package bgu.spl.app.messages;

import bgu.spl.mics.Broadcast;

public class NewDiscountBroadcast implements Broadcast{

	private String shoeType;
	private int amount;

	public NewDiscountBroadcast(String shoeType, int amount) {
		super();
		this.shoeType = shoeType;
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

	public String getShoeType() {
		return shoeType;
	}

	
}
