package ca.magenta.krr.fact;


import java.util.HashSet;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.magenta.krr.test.TopologyBasedCorrelationFT;
import ca.magenta.krr.tools.Utils;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public abstract class StateLifecycle implements Fact {
	
	private static Logger logger = Logger.getLogger(TopologyBasedCorrelationFT.class);

	private HashSet<String> changes = new HashSet<String>();
	private transient State stateRef = null;
	
	public StateLifecycle() {
		super();
	}	
	
	public StateLifecycle(HashSet<String> changes) {
		super();
		this.changes = changes;
		
	}
	
	public HashSet<String> getChanges() {
		return changes;
	}

	public void setChanges(HashSet<String> changes) {
		this.changes = changes;
	}

	public State getStateRef() {
		return stateRef;
	}

	public void setStateRef(State stateRef) {
		this.stateRef = stateRef;
	}
	
	public String toString(boolean pretty)		
	{
		String string = stateRef.getLinkKey() + ":" + Utils.toJsonG(this, this.getClass(), pretty) ;
		
		return string;
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}
	
}
