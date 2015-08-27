package ca.magenta.krr.fact;

import java.util.UUID;


/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-07-08
 */
public class Ticker extends Message {
	
	private static final String TYPE = "KRR_TICKER";

	public Ticker() {
		super(UUID.randomUUID().toString(), TYPE, System.currentTimeMillis(), null);
	}
}
