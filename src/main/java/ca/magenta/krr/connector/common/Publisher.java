package ca.magenta.krr.connector.common;

import java.util.HashSet;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.fact.StateLifecycle;
import ca.magenta.krr.ruleEngin.CausalityAnalyser;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-11-29
 */
public class Publisher  implements Runnable {
	
	public static Logger logger = Logger.getLogger(Publisher.class);
	
	private String name = null;
	
	private HashSet<Subscriber> subscribers = new HashSet<Subscriber>();

	private BlockingQueue<StateLifecycle> outboundQueue = new ArrayBlockingQueue<StateLifecycle>(300000);
    
	private Thread thread = null;
	
    private volatile boolean doRun = true;

    public void stop() {
        doRun = false;
    }
    
	public void start()
	{
		thread = new Thread(this, name);
    	thread.start();
    	logger.trace(thread.toString() + " started");
	}

    public Publisher(String name) {
        this.name = name;
        
        this.start();
        
    }

    public void run() {
    	
    	logger.debug("New publisher " + name + " is now running");
        try {
        	while (doRun || ! outboundQueue.isEmpty())
        	{
        		StateLifecycle stateLifecycle = outboundQueue.take();
        		if (logger.isTraceEnabled())
        			logger.trace("Got: " + stateLifecycle.toString());
        		dispatch(stateLifecycle);
        	}
        	
        } catch (InterruptedException e) {
        	logger.error("InterruptedException", e);
        }
    }
    
    synchronized private void dispatch(StateLifecycle stateLifecycle) {

    	for (Subscriber subscriber :  subscribers)
    	{
    		try {
				subscriber.store(stateLifecycle);
        		if (logger.isDebugEnabled())
        			logger.debug("Store to " + subscriber.getName());
			} catch (InterruptedException e) {
				logger.error("Unable to post to " + subscriber.getName(), e);
			}
    	}
	}

	public void publish(StateLifecycle stateLifecycle) throws InterruptedException
    {
    	if ( ! subscribers.isEmpty() )
    		outboundQueue.put(stateLifecycle);
    	else
    		if (logger.isTraceEnabled())
    			logger.trace("No subscribers for: " + stateLifecycle);
    }

	synchronized public void subscribe(Subscriber subscriber) {
		
		subscribers.add(subscriber);
		if (logger.isDebugEnabled())
			logger.debug("Add subsciber:[" + subscriber.getName() + "]");
		
	}

	synchronized public void unsubscribe(Subscriber subscriber) {

		subscribers.remove(subscriber);
		if (logger.isDebugEnabled())
			logger.debug("Remove subsciber:[" + subscriber.getName() + "]");
	}
}
