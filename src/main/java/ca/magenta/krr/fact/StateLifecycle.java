package ca.magenta.krr.fact;


import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public abstract class StateLifecycle implements Fact {

	private HashSet<String> changes = new HashSet<String>();
	private State stateRef = null;
	
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
