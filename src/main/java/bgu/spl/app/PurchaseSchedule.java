package bgu.spl.app;

public class PurchaseSchedule {
	

	private String shoeType;
	private int tick;
	private boolean isOnWishList;


	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
		isOnWishList = false;
	}
	
	
	public String getShoeType() {
		return shoeType;
	}


	public int getTick() {
		return tick;
	}


	public boolean isOnWishList() {
		return isOnWishList;
	}


	public void setOnWishList(boolean isOnWishList) {
		this.isOnWishList = isOnWishList;
	}
	
	

}
