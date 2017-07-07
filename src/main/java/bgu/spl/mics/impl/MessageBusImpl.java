package bgu.spl.mics.impl;



import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.util.concurrent.*;

import javax.swing.text.Position;

import java.lang.Integer;

public class MessageBusImpl implements MessageBus {
	
	private ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>> servicesQueueMap; 
	private ConcurrentHashMap<String,ConcurrentLinkedQueue<MicroService>[]> requestsQueueMap;
	private ConcurrentHashMap<String,ConcurrentLinkedQueue<MicroService>> broadCastsQueueMap;
	private ConcurrentHashMap<Request<?>,MicroService> treatedRequestsMap;
	
	 private static class SingletonHolder {
	        private static MessageBusImpl instance = new MessageBusImpl();
	    }
	 
	 public static MessageBusImpl getInstance() {
	      return SingletonHolder.instance;
	  }
 
	private MessageBusImpl(){
		servicesQueueMap = new ConcurrentHashMap<Integer,LinkedBlockingQueue<Message>>(); 
		requestsQueueMap = new ConcurrentHashMap<String, ConcurrentLinkedQueue<MicroService>[]>();
		broadCastsQueueMap = new ConcurrentHashMap<String,ConcurrentLinkedQueue<MicroService>>();
		treatedRequestsMap = new ConcurrentHashMap<Request<?>,MicroService>();
		
	}
	
	@Override
	public synchronized void subscribeRequest(Class<? extends Request> type, MicroService m) {

		if(!requestsQueueMap.containsKey(type.getName())) //enters if the request type queue was not created yet
		{
			requestsQueueMap.put(type.getName(), new ConcurrentLinkedQueue[2]); 
			
			for(int i=0; i<2; i++){ //creates the 2 queues
				requestsQueueMap.get(type.getName())[i] = new ConcurrentLinkedQueue<MicroService>();
			}
			requestsQueueMap.get(type.getName())[0].add(m); //always add the new subscribed micro service to the 0 cell of the array.
		}
		else{
		requestsQueueMap.get(type.getName())[0].add(m);
		}

	}

	@Override
	public  synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

		if(!broadCastsQueueMap.containsKey(type.getName())) //enters if the request type queue was not created yet
		{
			broadCastsQueueMap.put(type.getName(), new ConcurrentLinkedQueue<MicroService>());
			broadCastsQueueMap.get(type.getName()).add(m);
			
		}
		else{
		broadCastsQueueMap.get(type.getName()).add(m);
		}

	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		
		RequestCompleted<T> completed = new RequestCompleted<T>(r,result);
		MicroService requester = treatedRequestsMap.get(r);
		servicesQueueMap.get(requester.getMicroServiceId()).add(completed);
		treatedRequestsMap.remove(r); 

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if(broadCastsQueueMap.get(b.getClass().getName())  == null){
			return;}

		Iterator<MicroService> itr = broadCastsQueueMap.get(b.getClass().getName()).iterator();
		while(itr.hasNext()){
			
			MicroService m = itr.next();
			servicesQueueMap.get(m.getMicroServiceId()).add(b);

			}	

	}

	@Override
	public synchronized boolean sendRequest(Request<?> r, MicroService requester) {
		
		if(requestsQueueMap.containsKey(r.getClass().getName())){ //checks if there is such a request queue
			ConcurrentLinkedQueue<MicroService> requestQueue = requestsQueueMap.get(r.getClass().getName())[0]; //return the queue 
			MicroService allocateService = requestQueue.poll();
			if(allocateService == null){
				return false;
			}
			
			servicesQueueMap.get(allocateService.getMicroServiceId()).add(r);
			treatedRequestsMap.put(r, requester);
			requestsQueueMap.get(r.getClass().getName())[1].add(allocateService);
			if(requestQueue.isEmpty()){ //switch between the queus when the 0 queue is emoty
				ConcurrentLinkedQueue<MicroService>[] newArray = new ConcurrentLinkedQueue[2];
				newArray[0] = requestsQueueMap.get(r.getClass().getName())[1];
				newArray[1] = requestQueue;
				this.requestsQueueMap.put(r.getClass().getName(), newArray);
			}
			
			return true;		
		}
		
		return false;
	}

	@Override
	public synchronized void register(MicroService m) {
		servicesQueueMap.put(m.getMicroServiceId(), new LinkedBlockingQueue<Message>());
		
	}

	@Override
	public synchronized void unregister(MicroService m) {
		
		requestsQueueMap.forEach((k,v) -> {
			
			for(int i = 0; i < v[0].size(); i++){
				MicroService curMicroService = v[0].poll();
				if(curMicroService.getMicroServiceId() != m.getMicroServiceId()){
					v[0].add(curMicroService);
				}
			}
			
			for(int i = 0; i < v[1].size(); i++){
				MicroService curMicroService = v[1].poll();
				if(curMicroService.getMicroServiceId() != m.getMicroServiceId()){
					v[1].add(curMicroService);
				}
			}
		});
		
		
			
		broadCastsQueueMap.forEach((k,v) -> {
			for(int i = 0; i < v.size(); i++){
				MicroService curMicroService = v.poll();
				if(curMicroService.getMicroServiceId() != m.getMicroServiceId()){
					v.add(curMicroService);	
				}
			}
	
		});

		servicesQueueMap.remove(m.getMicroServiceId());	
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!servicesQueueMap.containsKey(m.getMicroServiceId())) // map not contains the service - means we need to throw exception
		{
			
			throw new IllegalStateException("Not Registered");		
		}
		else
		{
			Message message = servicesQueueMap.get(m.getMicroServiceId()).take(); // take method Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.

			return message;
			
		}

		
		
	}

}
