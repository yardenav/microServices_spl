package bgu.spl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.app.Receipt;
import bgu.spl.app.ShoeStorageInfo;
import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;

public class StoreTest {
	
	private static Store store;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		store = Store.getInstance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		store.print();
	}

	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo[] stock = {new ShoeStorageInfo("red-sandals", 7, 0),
				new ShoeStorageInfo("green-boots", 9, 0),
				new ShoeStorageInfo("black-sneakers", 5, 0),
				new ShoeStorageInfo("pink-flip-flops", 8, 0)};
		store.load(stock);
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetStock() {
		assertEquals(store.getStock().size(), 4);
	}

	@Test
	public void testLoad() {
		assertTrue(store.getStock()!=null);
		assertEquals(store.getStock().size(), 4);
		assertTrue(store.getStock().get("red-sandals").getShoeType().equals("red-sandals"));
		assertTrue(store.getStock().get("green-boots").getShoeType().equals("green-boots"));
		assertTrue(store.getStock().get("black-sneakers").getShoeType().equals("black-sneakers"));
		assertTrue(store.getStock().get("pink-flip-flops").getShoeType().equals("pink-flip-flops"));
		// Checks that the stock exists
	}

	@Test
	public void testTake() {
		assertEquals(store.take("blue-vans", false), BuyResult.NOT_IN_STOCK);
		assertEquals(store.take("green-boots", false), BuyResult.REGULAR_PRICE);
		
	}

	@Test
	public void testAdd() {
		assertEquals(store.getStock().get("green-boots").getStorageAmount(), 9);
		store.add("green-boots", 3);
		assertEquals(store.getStock().get("green-boots").getStorageAmount(), 12);
	}

	@Test
	public void testAddDiscount() {
		assertEquals(store.getStock().get("black-sneakers").getDiscountedAmount(), 0);
		store.addDiscount("black-sneakers", 4);
		assertEquals(store.getStock().get("black-sneakers").getDiscountedAmount(), 4);
	}

	@Test
	public void testFile() {
		assertEquals(store.getReceiptsList().size(), 0);
		Receipt rec2 = new Receipt("Dylan", "Jeremy", "grey-source-sandals", false, 6, 6, 1);
		store.file(rec2);
		assertEquals(store.getReceiptsList().size(), 1);
		Receipt rec = new Receipt("Bob", "John", "super-amazing-swagger-shoes", false, 5, 4, 1);
		store.file(rec);
		assertEquals(store.getReceiptsList().size(), 2);
	}

}
