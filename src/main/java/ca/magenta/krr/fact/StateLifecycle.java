package ca.magenta.krr.fact;


import java.util.HashSet;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.magenta.krr.test.TopologyBasedCorrelationFT;
import ca.magenta.krr.tools.Utils;
import org.kie.api.runtime.rule.FactHandle;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public abstract class StateLifecycle implements Fact {
	
	private static Logger logger = Logger.getLogger(TopologyBasedCorrelationFT.class);

	private HashSet<String> changes = new HashSet<String>();
	private String linkKeyRef = null;
	private transient FactHandle factHandleRef= null; 
	
	

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
		return State.getState(factHandleRef);
	}

	public String toString(boolean pretty)		
	{
		String string = linkKeyRef + ":" + Utils.toJsonJ2(changes, changes.getClass(), pretty) ;
		
		return string;
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}

	public String getLinkKeyRef() {
		return linkKeyRef;
	}

	public void setLinkKeyRef(String linkKeyRef) {
		this.linkKeyRef = linkKeyRef;
	}
	
	public void setFactHandleRef(FactHandle factHandleRef) {
		this.factHandleRef = factHandleRef;
	}

}
