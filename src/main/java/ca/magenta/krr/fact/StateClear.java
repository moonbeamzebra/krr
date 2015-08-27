package ca.magenta.krr.fact;

import java.util.HashSet;

import ca.magenta.krr.engine.Engine;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class StateClear extends StateLifecycle{
	
	public static void insertInWM(State newState, State oldState, boolean firstEnteredCleared)
	{
		StateClear stateClear = new StateClear(newState, oldState, firstEnteredCleared);
		
		Engine.getStreamKS().insert(stateClear);
	}

	private StateClear() {
		super();
	}

	private StateClear(State stateNew, State stateOld, boolean firstEnteredCleared) {
		super();
		
		this.setStateRef(stateNew);
		
		if (firstEnteredCleared)
			this.setChanges(new HashSet<String>());
		else
			this.setChanges(stateNew.getChanges(stateOld));
	}

}
