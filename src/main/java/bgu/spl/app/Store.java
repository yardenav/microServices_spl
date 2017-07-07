package bgu.spl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.text.html.HTMLDocument.Iterator;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map.Entry;

import bgu.spl.app.GsonDataObject.*;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.mics.Message;



public class Store {
	
	public enum BuyResult
	{
		NOT_IN_STOCK,NOT_ON_DISCOUNT,REGULAR_PRICE,DISCOUNTED_PRICE,ON_STORAGE_BUT_NOT_ON_DISCOUNT,NOT_ON_STORAGE_NOT_ON_DISCOUNT,NOT_ON_STORAGE_BUT_ON_DISCOUNT;
	}
	
	private ConcurrentHashMap<String,ShoeStorageInfo> shoesMap;
	private ArrayList<Receipt> receiptsList;

	// ======== singleton stuff =======
	 private static class SingletonHolder {
	        private static Store instance = new Store();
	    }
	
	 public static Store getInstance() {
	      return SingletonHolder.instance;
	  }
	
	 // ======== constructor
	private Store()
	{
		shoesMap = new ConcurrentHashMap<String,ShoeStorageInfo>();
		receiptsList = new ArrayList<Receipt>();
	}
	
	
	/*
	This method should be called in order to initialize the store storage before starting an execution
	(by the ShoeStoreRunner class defined later). The method will add the items in the given array to
	the store.*/

	public void load (ShoeStorageInfo[] storage) 
	{
		for (int i=0; i < storage.length ; i++)
		{
			this.shoesMap.put(storage[i].getShoeType(), new ShoeStorageInfo(storage[i].getShoeType(), storage[i].getStorageAmount(), 0));
		}
	}
	
	//TODO check what the enum returns.
	public BuyResult take(String shoeType , boolean onlyDiscount )
	{
		if (!this.isShoeExists(shoeType))
		{
			this.add(shoeType, 0);
		}
		synchronized(this.shoesMap.get(shoeType)){
		if (onlyDiscount) // Request sended from the wishList (after discount broadcast)
		{
			if (checkDiscountAvilability(shoeType))
			{
				if (checkStorageAvilability(shoeType)){
					decreaseDiscountedShoe(shoeType); 
					return BuyResult.DISCOUNTED_PRICE; }
				else {
					return BuyResult.NOT_ON_STORAGE_BUT_ON_DISCOUNT;
					
					
				}


				
			}
			else // no discount was available
			{
				if (checkStorageAvilability(shoeType))
					return BuyResult.ON_STORAGE_BUT_NOT_ON_DISCOUNT;
				else
					return BuyResult.NOT_ON_STORAGE_NOT_ON_DISCOUNT;

			}
		}
		else
		{

			if (checkStorageAvilability(shoeType)) // check if exists at storage at all
			{
				if (checkDiscountAvilability(shoeType)) // the shoe exists with discount so we give him discounted shoe
				{
					decreaseDiscountedShoe(shoeType);
					return BuyResult.DISCOUNTED_PRICE;
				}
				else // no discounted shoes - we give him normal price.
				{
					decreaseShoe(shoeType);
					return BuyResult.REGULAR_PRICE;
				}
			}
			else
			{
				return BuyResult.NOT_IN_STOCK;
			}
		
		}}
	}
	
	public void add (String shoeType , int amount )
	{
		
		if (isShoeExists(shoeType))
		{
			synchronized(this.shoesMap.get(shoeType)){
			shoesMap.get(shoeType).increaseStorageAmount(amount);
			}
		}
		else
		{
			this.shoesMap.put(shoeType, new ShoeStorageInfo(shoeType, amount, 0));
		}
	}
	
	public void addDiscount (String shoeType , int amount )
	{

		if (isShoeExists(shoeType))
		{
			synchronized(this.shoesMap.get(shoeType)){
			shoesMap.get(shoeType).increaseDiscountedAmount(amount);
		}}
		else
		{
			
			this.shoesMap.put(shoeType, new ShoeStorageInfo(shoeType, 0, amount));
		}
	}
	
	public void file(Receipt receipt )
	{
		synchronized(this.receiptsList)
		{
			if (receipt != null){
				if(receipt.getIssuedTick() == receipt.getRequestTick()){
				}
				this.receiptsList.add(receipt);
			}
			else
				System.out.println("NULL RECEIPT");

		}	
	}
	

	
	public  void decreaseShoe(String type)
	{
		synchronized(this.shoesMap.get(type)){
		shoesMap.get(type).decreaseStorageAmount();
		}
	}
	public void decreaseDiscountedShoe(String type)
	{
		synchronized(this.shoesMap.get(type)){
		shoesMap.get(type).decreaseStorageAmount();
		shoesMap.get(type).deccreaseDiscountedAmount();
	}
	}
	
	
	
	public  boolean checkStorageAvilability(String type)
	{
		if(shoesMap.containsKey(type)){
			synchronized(this.shoesMap.get(type)){
				if(this.shoesMap.get(type).getStorageAmount() > 0){
					return true;
				}
			}
		}
		return false;
	}
	
	public  boolean checkDiscountAvilability(String type)
	{
		if(shoesMap.containsKey(type)){
			synchronized(this.shoesMap.get(type)){
	
				if((this.shoesMap.get(type).getDiscountedAmount() > 0)){
						return true;
				}
			}  
		}
		return false;
	}
	
	public  boolean checkAvailibility(String type, boolean needDiscount)
	{
		synchronized(this.shoesMap.get(type)){
		if (needDiscount)
		{
			return (checkDiscountAvilability(type) && checkStorageAvilability( type));
		}
		else
		{
			return checkStorageAvilability(type);
		}
		}
	}
	
	public  boolean isShoeExists(String type)
	{
		if (this.shoesMap.get(type) != null)
		{
			synchronized(this.shoesMap.get(type))
			{
			return shoesMap.containsKey(type);
			}
		}
		else
			return false;
	}
	
	public void print(){
		System.out.println("Store info:");
		this.shoesMap.forEach((k,v) ->{
			v.printShoe();	
		});
		
		System.out.println("\n Total Amount of receipts :" + receiptsList.size() );
		for (int i=0; i< receiptsList.size() ; i++)
		{
			if (receiptsList.get(i) != null)
				receiptsList.get(i).printReceipt();
			else
				System.out.println("SAVEDDDDD");
		}
		
		

	}

	public ConcurrentHashMap<String, ShoeStorageInfo> getStock() {
		return shoesMap;
	}

	public ArrayList<Receipt> getReceiptsList() {
		return receiptsList;
	}
	
	
	
	
	
	
}
