package bgu.spl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import bgu.spl.mics.impl.MessageBusImpl;

public class MessageBusImplTest {
	
	
	private testMicros sender;
	private testMicros reciver;
	@Before
	public void setUp() throws Exception {
		//create 
		sender = new testMicros("sender");
		reciver= new testMicros("reciver");
		MessageBusImpl.getInstance().register(sender);
		MessageBusImpl.getInstance().register(reciver);
	}
	
	@After
	public void tearDown() throws Exception {
	MessageBusImpl.getInstance().unregister(sender);
	MessageBusImpl.getInstance().unregister(reciver);
	}

	
	
	 private class testMicros extends MicroService
	 {

		public testMicros(String name) {
			super(name);
		}

		@Override
		protected void initialize() {
			
		}
		 
	 }



	@Test
	public void TestBroadcasts() {
		MessageBusImpl.getInstance().subscribeBroadcast(TestBroadcasts.class, sender);
		TestBroadcasts message = new TestBroadcasts("this is a bordcast message");
		MessageBusImpl.getInstance().sendBroadcast(message);
		TestBroadcasts messageRecived = null;
		//TestBroadcasts messageRecivedrevicer = null;

		try {
			messageRecived =  (TestBroadcasts) MessageBusImpl.getInstance().awaitMessage(sender);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(messageRecived.getMessage(),"this is a bordcast message");

	}

	
	
	 
	 private class TestBroadcasts implements Broadcast
	 {
		    private String message;

		 public TestBroadcasts(String _message )
		 {
			 message=_message;
		 }
		 public String getMessage ()
		 {
			 return message; 
		 }
	 }
	 
	 private class TestReq implements Request<String>
	 {
		 private String message;
		 
		 public TestReq (String _message)
		 {
			 this.message=_message;
		 }
		 public String getMessage()
		 {
			 return message;
		 }
	 }
	 
	 @Test
		public void TestReq() {
			MessageBusImpl.getInstance().subscribeRequest(TestReq.class,reciver);
			TestReq messageRequest = new TestReq("test req");
			
			MessageBusImpl.getInstance().sendRequest(messageRequest, sender);
			TestReq TestReq = null ;
			try {
				TestReq= (TestReq) MessageBusImpl.getInstance().awaitMessage(reciver);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertEquals(TestReq.getMessage(),"test req");
			
			//Checkk that sender got back a complete message
			MessageBusImpl.getInstance().complete(TestReq, "completed!!!");
			RequestCompleted<String> reqcomp = null;
			try {
				reqcomp=(RequestCompleted<String>) MessageBusImpl.getInstance().awaitMessage(sender);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			assertEquals(reqcomp.getResult(),"completed!!!");

		}

}
