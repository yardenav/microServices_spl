package bgu.spl.app;

public class Receipt {
	

	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;


	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick,int amountSold) 
	{
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}	
	
	
	public String getSeller() {
		return seller;
	}

	public String getCustomer() {
		return customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isDiscount() {
		return discount;
	}

	public int getIssuedTick() {
		return issuedTick;
	}

	public int getRequestTick() {
		return requestTick;
	}

	public int getAmountSold() {
		return amountSold;
	}
	
	public void printReceipt(){
		System.out.println("Receipt info: " );
		System.out.println("Seller: " + this.seller);
		System.out.println("Costumer: " + this.customer);
		System.out.println("Shoe type: " + this.shoeType);
		System.out.println("Is on discount: " + this.discount);
		System.out.println("Issued tick: " + this.issuedTick);
		System.out.println("Requested tick: " + this.requestTick);
		System.out.println("Amount sold: " + this.amountSold);
		System.out.println("############################");

	}
	

}
