package ca.magenta.krr.fact;

import java.util.HashSet;

import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.engine.Engine;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class StateClear extends StateLifecycle{

	public static void insertInWM(FactHandle factHandle, State newState, State oldState, boolean firstEnteredCleared)
	{
		StateClear stateClear = new StateClear(factHandle, newState, oldState, firstEnteredCleared);
		
		Engine.getStreamKS().insert(stateClear);
	}

	private StateClear() {
		super();
	}

	private StateClear(FactHandle factHandle, State stateNew, State stateOld, boolean firstEnteredCleared) {
		super();
		

		this.setFactHandleRef(factHandle);
		this.setLinkKeyRef(stateNew.getLinkKey());
		
		if (firstEnteredCleared)
			this.setChanges(new HashSet<String>());
		else
			this.setChanges(stateNew.getChanges(stateOld));
	}
}
