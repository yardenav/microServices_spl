
package bgu.spl.app;

import java.util.ArrayList;
import java.util.Arrays;

public class GsonDataObject {
	
	public ShoeStorageInfo[] initialStorage;
	public Services services;
	
	


	public static class Shoe
	{
		public String shoeType;
		public int amount;
		public int tick;
	}
	
	public static class Services
	{
		public Time time;
		public Manager manager;
		public int factories;
		public int sellers;
		public Customer[] customers;
		
	}
	
	public static class Time
	{
		public int speed;
		public int duration;
	}
	
	public static class Manager
	{
		public DiscountSchedule[] discountSchedule;
	}
	
	public static class Customer
	{
		public String name;
		public String[] wishList;
		public Shoe[] purchaseSchedule;
	}
}
