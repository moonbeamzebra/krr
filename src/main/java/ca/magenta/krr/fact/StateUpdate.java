package ca.magenta.krr.fact;

import java.util.HashSet;

import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.engine.Engine;


/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class StateUpdate extends StateLifecycle{
	
	public static void insertInWM(FactHandle factHandle, State newState, State oldState)
	{
		StateUpdate stateUpdate = new StateUpdate(factHandle, newState, oldState);
		
		Engine.getStreamKS().insert(stateUpdate);
	}

	public static void insertInWM(FactHandle factHandle, State newState,  HashSet<String> changes)
	{
		StateUpdate stateUpdate = new StateUpdate(factHandle, newState, changes);
		
		Engine.getStreamKS().insert(stateUpdate);
	}
	
	private StateUpdate() {
		super();
	}
	
	private StateUpdate(FactHandle factHandle, State stateNew, HashSet<String> changes) {
		super(changes);
		
		this.setFactHandleRef(factHandle);
		this.setLinkKeyRef(stateNew.getLinkKey());
	}

	private StateUpdate(FactHandle factHandle, State stateNew, State stateOld) {
		this(factHandle, stateNew, stateNew.getChanges(stateOld));
	}

	public static void insertInWM(FactHandle factHandle, State stateNew, State stateOld, HashSet<String> stateChangeList) {
		stateChangeList.addAll(stateNew.getChanges(stateOld));
		insertInWM(factHandle, stateNew, stateChangeList);
	}
}
