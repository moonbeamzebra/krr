package ca.magenta.krr.fact;

import java.util.HashSet;

import org.apache.log4j.Logger;

import ca.magenta.krr.engine.Engine;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class StateNew extends StateLifecycle{
	
	private static Logger logger = Logger.getLogger(StateNew.class);
	
	public static void insertInWM(State newState, State oldState, boolean veryNew)
	{
		StateNew stateNew = new StateNew(newState, oldState, new HashSet<String>(), veryNew);
		
		Engine.getStreamKS().insert(stateNew);
	}

	private StateNew() {
		super();
	}

	private StateNew(State stateNew, State stateOld, HashSet<String> stateChangeList, boolean veryNew) {
		super();
		
		this.setStateRef(stateNew);
		
		if (veryNew)
			this.setChanges(stateChangeList);
		else
		{
			HashSet<String> changes = stateNew.getChanges(stateOld);
			changes.addAll(stateChangeList);
			this.setChanges(changes);
		}
	}

	public static void insertInWM(State newState, State oldState, HashSet<String> stateChangeList, boolean veryNew) {
		
		StateNew stateNew = new StateNew(newState, oldState, stateChangeList, veryNew);
		
		Engine.getStreamKS().insert(stateNew);
	}
}
