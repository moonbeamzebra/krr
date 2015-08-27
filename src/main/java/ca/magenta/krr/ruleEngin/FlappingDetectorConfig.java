package ca.magenta.krr.ruleEngin;



import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.fact.Fact;
import ca.magenta.krr.fact.Message;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-06-09
 */
public class FlappingDetectorConfig implements  Fact {
	
	private static Logger logger = Logger.getLogger(Message.class);
	
	public FlappingDetectorConfig(
			String flappingType,
			long windowTimeInMilliSec,
			long raisedMaximumTimeInMilliSec, 
			int raisingFlappingCount,
			int fallingFlappingCount,
			Severity severity) {
		super();

		
		this.flappingType = flappingType;
		this.windowTimeInMilliSec = windowTimeInMilliSec;
		this.raisedMaximumTimeInMilliSec = raisedMaximumTimeInMilliSec;
		this.raisingFlappingCount = raisingFlappingCount;
		this.fallingFlappingCount = fallingFlappingCount;
		this.severity = severity;
	}

	private String flappingType;
	private long windowTimeInMilliSec;
	private long raisedMaximumTimeInMilliSec; // maximum time the state is raised to consider flapping candidate  
	private int raisingFlappingCount;
	private int fallingFlappingCount;
	private Severity severity;
	
	public String getFlappingType() {
		return flappingType;
	}
	public long getWindowTimeInMilliSec() {
		return windowTimeInMilliSec;
	}
	public String getWindowTimeInMilliSecStr() {
		return windowTimeInMilliSec + "ms";
	}
	public long getRaisedMaximumTimeInMilliSec() {
		return raisedMaximumTimeInMilliSec;
	}
	public int getRaisingFlappingCount() {
		return raisingFlappingCount;
	}
	public int getFallingFlappingCount() {
		return fallingFlappingCount;
	}

	public Severity getSeverity() {
		return severity;
	}
	
	public String toString(boolean pretty)		
	{
		if (pretty)
		{
			return (new GsonBuilder().setPrettyPrinting().create()).toJson(this);
		}
		else
		{
			return (new Gson()).toJson(this);
		}
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}	
}
