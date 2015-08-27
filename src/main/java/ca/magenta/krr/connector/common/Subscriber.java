package ca.magenta.krr.connector.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.Fact;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-12-04
 */
public abstract class Subscriber  implements Runnable {
	

	public static Logger logger = Logger.getLogger(Subscriber.class);
	
	private String name = null;

    private BlockingQueue<Fact> queue = null;
    
    private volatile boolean doRun = true;

    public void stop() {
        doRun = false;
    }

    public Subscriber(String name) {
    	this.name = name;
        Engine.stateChangePublisher().subscribe(this);
    }

    public void store(Fact fact) throws InterruptedException {
    	queue.put(fact);
    }

    public void run() {
    	
    	logger.debug("New Subscriber " + name + " running");
    	queue = new ArrayBlockingQueue<Fact>(300000);
        try {
        	while (doRun)
        	{
        		Fact fact = queue.take();
        		this.forward(fact);
        		
        	}
        } catch (InterruptedException e) {
        	//logger.error("InterruptedException", e);
        }
        Engine.stateChangePublisher().unsubscribe(this);
    	queue.clear();
    	queue = null;
    	logger.debug("Subscriber " + name + " stops running; queue emptied");
    }
    
	protected abstract void forward(Fact fact);

	public String getName() {
		return name;
	}

}
