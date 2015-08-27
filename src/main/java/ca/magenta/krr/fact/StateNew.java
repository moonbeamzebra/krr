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
		StateNew stateNew = new StateNew(newState, oldState, veryNew);
		
		Engine.getStreamKS().insert(stateNew);
	}

	private StateNew() {
		super();
	}

	private StateNew(State stateNew, State stateOld, boolean veryNew) {
		super();
		
		this.setStateRef(stateNew);
		
		if (veryNew)
			this.setChanges(new HashSet<String>());
		else
			this.setChanges(stateNew.getChanges(stateOld));
	}
}
