package bgu.spl.app.messages;

import bgu.spl.app.Receipt;
import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt>{
	
	private String shoeType; 
	private int numOfShoes;
	
	

	public ManufacturingOrderRequest(String shoeToManufacture, int numOfShoesToManufacture) {
		super();
		this.numOfShoes = numOfShoesToManufacture;
		this.shoeType = shoeToManufacture;
		
	}


	public String getShoeType() {
		return shoeType;
	}



	public int getNumOfShoes() {
		return numOfShoes;
	}


}
