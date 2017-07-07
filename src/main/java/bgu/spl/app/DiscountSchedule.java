package bgu.spl.app;

public class DiscountSchedule {

	private String shoeType;
	private int tick;
	private int amount;
	
		
	public DiscountSchedule(String shoeType, int tick, int amount) {
		super();
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	public int getTick() {
		return tick;
	}
	public int getAmount() {
		return amount;
	}
	
	
}
