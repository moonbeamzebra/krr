package ca.magenta.krr.common;

import org.apache.log4j.Logger;

import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.Ticker;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-07-08
 */
public class TickerGenerator  implements Runnable {
	
	public static Logger logger = Logger.getLogger(TickerGenerator.class);
	
	private static volatile TickerGenerator instance = null;
	private static volatile Thread thread = null;

	private volatile boolean doRun = true;
    private long period;

	private TickerGenerator(long period) {
		super();
		
		this.period = period;
        
	}

    public void stopIt() {
        doRun = false;
    }
    
	public static void start(long period)
	{
		if (instance == null) {
			synchronized (TickerGenerator.class) {
				// Double check
				if (instance == null) {
					instance = new TickerGenerator(period);

					thread = new Thread(instance);
			    	thread.start();
			    	logger.trace(thread.toString() + " started");
				}
			}
		}
	}
	
	public static void stop() throws InterruptedException
	{
		instance.stopIt();

		thread.join(3*(instance.period));
		logger.trace(thread.toString() + " stopped");
	}

	public void run() {
		
			while (doRun) {

				Engine.getStreamKS().insert(new Ticker());
				
				try {
				Thread.sleep(period);
				} catch (InterruptedException e) {
					logger.error("", e);
					e.printStackTrace();
					doRun = false;
				}
			}
		
	}
}
