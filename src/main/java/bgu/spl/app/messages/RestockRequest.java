package bgu.spl.app.messages;

import bgu.spl.app.Receipt;
import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	
	private String shoeType;
	 
	
	
	public RestockRequest(String shoeType ) {
		super();
		this.shoeType = shoeType;
	
	}


	public String getShoeType() {
		return shoeType;
	}


}
