package bgu.spl.app;

public class ShoeStorageInfo {
    private final String shoeType;
    private int amount;
    private int discountedAmount;
    

    public ShoeStorageInfo(String type, int amount, int disAmount)
    {
    	shoeType = type;
    	this.amount = amount;
    	discountedAmount = disAmount;
    }
    
    public void updateStorageAmount(int newAmount)
    {
    	amount = newAmount;
    }
    
    public void increaseStorageAmount(int amount)
    {
    	this.amount = this.amount + amount;
    }
    
    public void decreaseStorageAmount()
    {
    	amount--;
    }
    
    public int getStorageAmount()
    {
    	return amount;
    }
    
       
    public void updateDiscountedAmount(int newAmount)
    {
    	discountedAmount = newAmount;
    }
    
    public void increaseDiscountedAmount(int amount)
    {
    	discountedAmount = discountedAmount + amount;
    }
    
    public void deccreaseDiscountedAmount()
    {
    	discountedAmount--;
    }    
    
    public int getDiscountedAmount()
    {
    	return discountedAmount;
    }
    
    public String getShoeType()
    {
    	return shoeType;
    }
    
    public void printShoe(){
    	System.out.println("shoe: " +this.getShoeType() + "    amount" + this.amount + "    discounted:" + this.discountedAmount);
    }
    
}
