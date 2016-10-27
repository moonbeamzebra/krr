package ca.magenta.krr.fact;

import java.util.HashSet;

import ca.magenta.krr.engine.Engine;


/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class StateUpdate extends StateLifecycle{
	
	public static void insertInWM(State newState, State oldState)
	{
		StateUpdate stateUpdate = new StateUpdate(newState, oldState);
		
		Engine.getStreamKS().insert(stateUpdate);
	}

	public static void insertInWM(State newState,  HashSet<String> changes)
	{
		StateUpdate stateUpdate = new StateUpdate(newState, changes);
		
		Engine.getStreamKS().insert(stateUpdate);
	}
	
	private StateUpdate() {
		super();
	}
	
	private StateUpdate(State stateNew, HashSet<String> changes) {
		super(changes);
		
		this.setStateRef(stateNew);
	}

	private StateUpdate(State stateNew, State stateOld) {
		this(stateNew, stateNew.getChanges(stateOld));
	}

	public static void insertInWM(State stateNew, State stateOld, HashSet<String> stateChangeList) {
		stateChangeList.addAll(stateNew.getChanges(stateOld));
		insertInWM(stateNew, stateChangeList);
	}
}
