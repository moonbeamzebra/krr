package ca.magenta.krr.fact;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class State extends NormalizedProperties {

	private static Logger logger = Logger.getLogger(State.class);
	
	public static final boolean IS_CLEARED = true;
	public static final boolean IS_RAISED = ! IS_CLEARED;
	
	synchronized public boolean isRoot() {
		return ((causedByLocal.size() == 0) && (causedByExtern.size() == 0));
	}
	
	synchronized public boolean isTop() {
		return ((causesLocal.size() == 0) && (causesExtern.size() == 0));
	}
	
	public long raisedDuration()
	{
		long lLastClearTime = lastClearTime;
		if ( (lLastClearTime == 0) || (isCleared() == State.IS_RAISED) )
			lLastClearTime = System.currentTimeMillis();
		long duration = lLastClearTime - lastRaiseTime;
		logger.debug("Duration:[" + duration + "]" + this.getLinkKey());
		return duration;
	}
	
	synchronized public boolean hasAggregate() {
		boolean hasAggregate = (aggregates.size() > 0);
		logger.trace("in hasAggregate:[" + hasAggregate + "];" + aggregates.size() + ":" + getLinkKey());
		return hasAggregate;
	}

	@Override
	public boolean isAggregator() {
		return super.isAggregator();
	}

	
	synchronized public boolean isAggregated() {
		boolean isAggregated = (aggregatedBy.size() > 0);
		logger.trace("in isAggregated:[" + isAggregated + "];" + aggregatedBy.size() + ":" + getLinkKey());
		return isAggregated;
	}
	
	synchronized public boolean addCausedBy_local(FactHandle factHandle) {
		boolean changed = false;

		if (factHandle != null) {
			if (!causedByLocal.contains(factHandle)) {
				causedByLocal.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}
	
	synchronized public boolean addAggregate(FactHandle factHandle) {
		boolean changed = false;

		if (factHandle != null) {
			if (!aggregates.contains(factHandle)) {
				aggregates.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized private boolean removeAggregate(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (aggregates.contains(factHandle)) {
				aggregates.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized private boolean removeAggregatedBy(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (aggregatedBy.contains(factHandle)) {
				aggregatedBy.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}
	
	synchronized public boolean addAggregatedBy(FactHandle factHandle) {
		boolean changed = false;

		if (factHandle != null) {
			if (!aggregatedBy.contains(factHandle)) {
				aggregatedBy.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	
	synchronized public boolean addCauses_local(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (!causesLocal.contains(factHandle)) {
				causesLocal.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized public boolean removeCausedBy_local(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (causedByLocal.contains(factHandle)) {
				causedByLocal.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized public boolean removeCauses_local(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (causesLocal.contains(factHandle)) {
				causesLocal.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}
	
	synchronized public boolean causedByContains(FactHandle factHandle) {
		boolean contains = false;

		if (factHandle != null) {
			contains = causedByLocal.contains(factHandle);
		}

		return contains;
	}

	synchronized public boolean causesContains(FactHandle factHandle) {
		boolean contains = false;

		if (factHandle != null) {
			contains = causesLocal.contains(factHandle);
		}

		return contains;
	}

	
	synchronized public boolean addCausedBy_extern(FactHandle factHandle) {
		boolean changed = false;

		if (factHandle != null) {
			if (!causedByExtern.contains(factHandle)) {
				causedByExtern.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized public boolean addCauses_extern(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (!causesExtern.contains(factHandle)) {
				causesExtern.add(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized public boolean removeCausedBy_extern(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (causedByExtern.contains(factHandle)) {
				causedByExtern.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}

	synchronized public boolean removeCauses_extern(FactHandle factHandle) {
		boolean changed = false;
		if (factHandle != null) {
			if (causesExtern.contains(factHandle)) {
				causesExtern.remove(factHandle);
				changed = true;
			}
		}

		return changed;
	}


	synchronized public static void insertNew(Signal signal) {
		long now = System.currentTimeMillis();

		State newState = new State(signal);

		Engine.getStreamKS().delete(signal);

		newState.count++;

		newState.firstRaiseTime = now;
		newState.lastRaiseTime = now;
		newState.lastUpdateTime = now;
		newState.lastClearTime = 0;

		FactHandle newStateFactHandle = Engine.getStreamKS().insert(newState);
		ManagedNode managedNode = newState.getMostSpecificManagedNode();

		if (!newState.isCleared()) {
			Engine.registerState(newStateFactHandle, managedNode, newState.getLinkKey());
		}

		if (!newState.isCleared()) {
			newState = State.updateCausedByAndCauses(newState, newStateFactHandle, signal.getCausedByStrs(), signal.getCausedByHdles(), signal.getCausesStrs(),
					signal.getCausesHdles());
			newState = State.addAggregatesToAggregator(newState, newStateFactHandle, signal.getAggregateHdles());
		}
		
		Engine.getStreamKS().update(newStateFactHandle, newState);
		StateNew.insertInWM(newState, null, true /* veryNew */);

		if (newState.isCleared()) {
			StateClear.insertInWM(newState, null, true /* firstEnteredCleared */);
		}

	}
	


	synchronized public void addCausedBy(State causingState)
	{
		State.addCausedBy(this, causingState);
	}
	
	synchronized private static void addCausedBy(State impactedState, State impactingState) {
		
		FactHandle impactedStateFactHandle = Engine.getStreamKS().getFactHandle(impactedState);
		FactHandle impactingStateFactHandle = Engine.getStreamKS().getFactHandle(impactingState);
		
		if (impactingStateFactHandle != null)
		{
			boolean changed = impactedState.addCausedBy_local(impactingStateFactHandle);
			if (changed) {
				HashSet<String> changes = new HashSet<String>();
				changes.add(State.CAUSED_BY_LABEL);
				Engine.getStreamKS().update(impactedStateFactHandle, impactedState);
				StateUpdate.insertInWM(impactedState, changes);
			}
		}
		
		if (impactedStateFactHandle != null)
		{
			boolean changed = impactingState.addCauses_local(impactedStateFactHandle);
			if (changed) {
				HashSet<String> changes = new HashSet<String>();
				changes.add(State.CAUSES_LABEL);
				Engine.getStreamKS().update(impactingStateFactHandle, impactingState);
				StateUpdate.insertInWM(impactingState, changes);
			}
		}
	}

	/*
	public synchronized static State removeAggregateFromAggregators(State aggregate,
																	FactHandle aggregateFactHandle,
																	boolean updateAggregateInWM) {

		HashSet<String> aggregatedbyChanges = new HashSet<String>();
		aggregatedbyChanges.add(State.AGGREGATES_LABEL);
		
		logger.debug("In removeAggregateFromAggregators; linkKey:" + aggregate.getLinkKey());
		
		boolean anyChanges = false;
		
		for (FactHandle aggregatedByHdle : aggregate.aggregatedBy)
		{
			State aggregatedByState = Engine.getState(aggregatedByHdle);
			if (aggregatedByState != null)
			{
				boolean changed = aggregatedByState.removeAggregate(aggregateFactHandle);
				if (changed)
				{
					Engine.getStreamKS().update(aggregatedByHdle, aggregatedByState);
					StateUpdate.insertInWM(aggregatedByState, aggregatedbyChanges);
					anyChanges = true;
				}
				aggregate.removeAggregatedBy(aggregatedByHdle);
			}
		}
		
		if (updateAggregateInWM && anyChanges)
		{
			HashSet<String> aggregateChanges = new HashSet<String>();
			aggregateChanges.add(State.AGGREGATEDBY_LABEL);
			Engine.getStreamKS().update(aggregateFactHandle, aggregate);
			StateUpdate.insertInWM(aggregate, aggregateChanges);
		}

		return aggregate;
	} */
	
	public synchronized static State updateAggregatedAndAggregatesOnClear(
			State clearedState, FactHandle clearedStateFactHandle,
			boolean updateAggregateInWM) {

		HashSet<String> aggregatedbyChanges = new HashSet<String>();
		aggregatedbyChanges.add(State.AGGREGATES_LABEL);
		HashSet<String> aggregateChanges = new HashSet<String>();
		aggregateChanges.add(State.AGGREGATEDBY_LABEL);

		logger.debug("In updateAggregatedAndAggregatesOnClear; linkKey:"
				+ clearedState.getLinkKey());

		boolean anyChanges = false;

		for (FactHandle aggregatedByHdle : clearedState.aggregatedBy) {
			State aggregatedByState = Engine.getState(aggregatedByHdle);
			if (aggregatedByState != null) {
				boolean changed = aggregatedByState
						.removeAggregate(clearedStateFactHandle);
				if (changed) {
					Engine.getStreamKS().update(aggregatedByHdle,
							aggregatedByState);
					StateUpdate.insertInWM(aggregatedByState,
							aggregatedbyChanges);
					anyChanges = true;
				}
				clearedState.removeAggregatedBy(aggregatedByHdle);
			}
		}
		
		for (FactHandle aggregateHdle : clearedState.aggregates) {
			State aggregateState = Engine.getState(aggregateHdle);
			if (aggregateState != null) {
				boolean changed = aggregateState.removeAggregatedBy(clearedStateFactHandle);
				if (changed) {
					Engine.getStreamKS().update(aggregateHdle,
							aggregateState);
					StateUpdate.insertInWM(aggregateState,
							aggregateChanges);
					anyChanges = true;
				}
				clearedState.removeAggregate(aggregateHdle);
			}
		}

		if (updateAggregateInWM && anyChanges) {
			Engine.getStreamKS().update(clearedStateFactHandle, clearedState);
			StateUpdate.insertInWM(clearedState, aggregateChanges);
		}

		return clearedState;
	}
	

	synchronized private static State addAggregatesToAggregator(	State aggregator,
														FactHandle stateFactHandle,
														HashSet<FactHandle> aggregateHdles) {

		HashSet<String> changes = new HashSet<String>();
		changes.add(State.AGGREGATEDBY_LABEL);
		Vector<SimpleImmutableEntry<FactHandle, State>> toUpdate = new Vector<SimpleImmutableEntry<FactHandle, State>>();
		if (aggregateHdles != null) {
			for (FactHandle  aggregateHdle : aggregateHdles) {
				State aggregateState = Engine.getState(aggregateHdle);
				if (aggregateState != null) {
					aggregator.addAggregate(aggregateHdle);
					boolean changed = aggregateState.addAggregatedBy(stateFactHandle);
					if (changed) {
						SimpleImmutableEntry<FactHandle, State> entry = new SimpleImmutableEntry<FactHandle, State>(aggregateHdle,aggregateState);
						toUpdate.add(entry);
					}
				}
			}
			
			for (SimpleImmutableEntry<FactHandle, State> aggregate : toUpdate )
			{
				Engine.getStreamKS().update(aggregate.getKey(), aggregate.getValue());
				StateUpdate.insertInWM(aggregate.getValue(), changes);
			}
		}
		
		return aggregator;
	}

	// This function is static and synchronized
	// This transaction must be complete in total before changing any
	// CausedByAndCauses of any other States
	synchronized private static State updateCausedByAndCauses(	State state, 
																FactHandle stateFactHandle, 
																HashSet<String> causedByStrs, 
																HashSet<FactHandle> causedByHdles,
																HashSet<String> causesStrs, 
																HashSet<FactHandle> causesHdles) {

		if (causedByHdles != null) {
			for (FactHandle causedByHdle : causedByHdles) {
				State causedByState = Engine.getState(causedByHdle);
				if (causedByState != null) {
					state.addCausedBy_local(causedByHdle);
					boolean changed = causedByState.addCauses_local(stateFactHandle);
					if (changed) {
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSES_LABEL);
						Engine.getStreamKS().update(causedByHdle, causedByState);
						StateUpdate.insertInWM(causedByState, changes);
					}
				}
			}
		}

		if (causesHdles != null) {
			for (FactHandle causesHdle : causesHdles) {
				State causesState = Engine.getState(causesHdle);
				if (causesState != null) {
					state.addCauses_local(causesHdle);
					boolean changed = causesState.addCausedBy_local(stateFactHandle);
					if (changed) {
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSED_BY_LABEL);
						Engine.getStreamKS().update(causesHdle, causesState);
						StateUpdate.insertInWM(causesState, changes);
					}
				}
			}
		}
		
		for (String causedByStr : causedByStrs)
		{
			logger.debug("causedByStr:" + causedByStr);
			FactHandle causedByHdle = Engine.getStateByLinkKey(causedByStr);
			if (causedByHdle != null)
			{
				logger.debug("Found");
				State causedByState = Engine.getState(causedByHdle);
				if (causedByState != null) {
					state.addCausedBy_extern(causedByHdle);
					boolean changed = causedByState.addCauses_extern(stateFactHandle);
					if (changed) {
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSES_LABEL);
						Engine.getStreamKS().update(causedByHdle, causedByState);
						StateUpdate.insertInWM(causedByState, changes);
					}
				}
			}
		}
		
		for (String causesStr : causesStrs)
		{
			logger.debug("causesStr:" + causesStr);
			FactHandle causesHdle = Engine.getStateByLinkKey(causesStr);
			if (causesHdle != null)
			{
				logger.debug("Found");
				State causesState = Engine.getState(causesHdle);
				if (causesState != null) {
					state.addCauses_extern(causesHdle);
					boolean changed = causesState.addCausedBy_extern(stateFactHandle);
					if (changed) {
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSED_BY_LABEL);
						Engine.getStreamKS().update(causesHdle, causesState);
						StateUpdate.insertInWM(causesState, changes);
					}
				}
			}
		}

		return state;
	}
	
	synchronized public boolean areSharingSameCategory(State comparedState)
	{
		boolean areSharing = false;
		
		logger.trace("areSharingSameCategory is called for :" + this.getLinkKey());
		
		HashSet<String> thisCategories = this.getCategories();
		HashSet<String> comparedCategories = comparedState.getCategories();
		
		for (String thisCat : thisCategories)
		{
			for (String compareCat : comparedCategories)
			{
				if (thisCat.equals(compareCat))
				{
					areSharing = true;
					break;
				}
			}			
			if (areSharing) break;
		}

		logger.trace("areSharing : " + areSharing);
		
		return areSharing;
		
	}

	// This function is static and synchronized
	// This transaction must be complete in total before changing any
	// CausedByAndCauses of any other States
	synchronized private static State updateCausedByAndCauses_goingClear(State state,
			FactHandle stateFactHandle) {

		if (state.isCleared()) {

			HashSet<String> stateChangeList = new HashSet<String>();

			logger.trace("In updateCausedByAndCauses CLEAR");
			synchronized (state.causedByLocal) {
				synchronized (state.causesLocal) {

					if (state.causedByLocal != null) {

						for (FactHandle causedByHdle : state.causedByLocal) {
							State causedByState = Engine.getState(causedByHdle);
							if (causedByState != null) {
								boolean changed = causedByState
										.removeCauses_local(stateFactHandle);
								if (changed) {
									HashSet<String> changes = new HashSet<String>();
									changes.add(State.CAUSES_LABEL);
									Engine.getStreamKS().update(causedByHdle, causedByState);
									StateUpdate.insertInWM(causedByState,
											changes);
								}
							}
							boolean changed = state
									.removeCausedBy_local(causedByHdle);
							if (changed) {
								stateChangeList.add(State.CAUSED_BY_LABEL);
							}
						}
					}

					if (state.causesLocal != null) {

						for (FactHandle causesHdle : state.causesLocal) {
							State causesState = Engine.getState(causesHdle);
							if (causesState != null) {

								boolean changed = causesState
										.removeCausedBy_local(stateFactHandle);
								if (changed) {
									HashSet<String> changes = new HashSet<String>();
									changes.add(State.CAUSED_BY_LABEL);
									Engine.getStreamKS().update(causesHdle, causesState);
									StateUpdate
											.insertInWM(causesState, changes);
								}
							}
							boolean changed = state
									.removeCauses_local(causesHdle);
							if (changed) {
								stateChangeList.add(State.CAUSES_LABEL);
							}
						}
					}
				}
			}

			if (stateChangeList.size() > 0) {
				Engine.getStreamKS().update(stateFactHandle, state);
				StateUpdate.insertInWM(state, stateChangeList);
			}
		}
		return state;
	}

	synchronized public static void updateExisting(Signal newSignal, State currentState) {
		long now = System.currentTimeMillis();

		State updatedState = new State(newSignal);
		Engine.getStreamKS().delete(newSignal);
		FactHandle currentStateFactHandle = Engine.getStreamKS().getFactHandle(currentState);
		updatedState.count = currentState.count;
		if (updatedState.causedByLocal == null)
			updatedState.causedByLocal = currentState.causedByLocal;
		if (updatedState.causesLocal == null)
			updatedState.causesLocal = currentState.causesLocal;
		
		updatedState.aggregates = currentState.aggregates;
		updatedState.aggregatedBy = currentState.aggregatedBy;

		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastRaiseTime = currentState.lastRaiseTime;
		updatedState.lastUpdateTime = now;

		if (!updatedState.isCleared()) {
			updatedState = State.updateCausedByAndCauses(updatedState, currentStateFactHandle, newSignal.getCausedByStrs(), newSignal.getCausedByHdles(),
					newSignal.getCausesStrs(), newSignal.getCausesHdles());
			updatedState = State.addAggregatesToAggregator(updatedState, currentStateFactHandle, newSignal.getAggregateHdles());

		}

		Engine.getStreamKS().update(currentStateFactHandle, updatedState);
		StateUpdate.insertInWM(updatedState, currentState);
	}

	synchronized public static void updateGoingCleared(Signal newSignal, State currentState) {
		long now = System.currentTimeMillis();

		State updatedState = new State(newSignal);
		Engine.getStreamKS().delete(newSignal);
		FactHandle currentStateFactHandle = Engine.getStreamKS().getFactHandle(currentState);
		updatedState.count = currentState.count;
		updatedState.causedByLocal = currentState.causedByLocal;
		updatedState.causesLocal = currentState.causesLocal;
		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastRaiseTime = currentState.lastRaiseTime;
		updatedState.aggregates = currentState.aggregates;
		updatedState.aggregatedBy = currentState.aggregatedBy;
		updatedState.lastClearTime = now;
		updatedState.lastUpdateTime = now;

		Engine.unregisterState(currentStateFactHandle, updatedState.getMostSpecificManagedNode(), updatedState.getLinkKey());
		
		if (updatedState.isCleared()) {
			updatedState = State.updateCausedByAndCauses_goingClear(updatedState, currentStateFactHandle);
			updatedState = updateAggregatedAndAggregatesOnClear(updatedState, currentStateFactHandle, false /* NO updateAggregateInWM*/);
		}

		Engine.getStreamKS().update(currentStateFactHandle, updatedState);

		StateClear.insertInWM(updatedState, currentState, false /*
																 * not
																 * firstEnteredCleared
																 */);
	}

	synchronized public static void updateGoingNotCleared(Signal newSignal, State currentState) {
		long now = System.currentTimeMillis();

		State updatedState = new State(newSignal);
		Engine.getStreamKS().delete(newSignal);
		FactHandle currentStateFactHandle = Engine.getStreamKS().getFactHandle(currentState);
		updatedState.count = currentState.count;
		updatedState.count++;
		updatedState.causedByLocal = currentState.causedByLocal;
		updatedState.causesLocal = currentState.causesLocal;
		updatedState.aggregates = currentState.aggregates;
		updatedState.aggregatedBy = currentState.aggregatedBy;
		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastClearTime = currentState.lastClearTime;
		updatedState.lastRaiseTime = now;
		updatedState.lastUpdateTime = now;

		if (!updatedState.isCleared()) {
			updatedState = State.updateCausedByAndCauses(updatedState, currentStateFactHandle, newSignal.getCausedByStrs(), newSignal.getCausedByHdles(),
					newSignal.getCausesStrs(), newSignal.getCausesHdles());
			updatedState = State.addAggregatesToAggregator(updatedState, currentStateFactHandle, newSignal.getAggregateHdles());

		}

		Engine.getStreamKS().update(currentStateFactHandle, updatedState);

		Engine.registerState(currentStateFactHandle, updatedState.getMostSpecificManagedNode(), updatedState.getLinkKey());

		StateNew.insertInWM(updatedState, currentState, false /* not veryNew */);
	}

	public State(Signal as) {
		super(as);
	}

	synchronized protected HashSet<String> getChanges(State other) {
		HashSet<String> changes = super.getChanges(other);
		Field[] fields = State.class.getDeclaredFields();
		for (Field f : fields) {
			Object tHis;
			Object oTher;
			try {
				logger.debug("Field:" + f.getName());
				if (!Modifier.isStatic(f.getModifiers())) {
					tHis = f.get(this);
					oTher = f.get(other);
					if ((tHis == null) && (oTher == null)) {
						; // No diff, both null
					} else if ((tHis == null) || (oTher == null)) {
						// One is null the other not : they are diff
						changes.add(f.getName());
					} else // Both not null
					{
						if (!tHis.equals(oTher)) {
							changes.add(f.getName());
						}
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Field:" + f.getName(), e);
			}
		}
		return changes;
	}

	synchronized public String causedByToString() {
		String causedByString = "";

		for (FactHandle causeByHdle : getCausedBy()) {
			Fact fact = Engine.getStreamKS().getFact(causeByHdle);
			if ((fact != null) && fact instanceof State) {
				State state = (State) fact;
				causedByString += state.getMostSpecificManagedNode().getFqdName() + "::" + state.getStateDescr() + " ";
			}
		}

		return "[" + causedByString.trim() + "]";
	}

	synchronized public String categoriesToString() {
		
		String categoriesString = "";

		for (String cat : this.categories) {
			if (cat != null) {
				categoriesString += cat + " ";
			}
		}

		return "[" + categoriesString.trim() + "]";
	}


	synchronized private HashSet<FactHandle> getCausedBy() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		for (FactHandle handle : causedByLocal) {
			total.add(handle);
		}

		for (FactHandle handle : causedByExtern) {
			total.add(handle);
		}

		return total;
	}

	synchronized public HashSet<FactHandle> getCauses() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		for (FactHandle handle : causesLocal) {
			total.add(handle);
		}

		for (FactHandle handle : causesExtern) {
			total.add(handle);
		}

		return total;
	}

	synchronized public String causesToString() {
		String causesString = "";

		for (FactHandle causeHdle : this.getCauses()) {
			Fact fact = Engine.getStreamKS().getFact(causeHdle);
			if ((fact != null) && fact instanceof State) {
				State state = (State) fact;
				causesString += state.getMostSpecificManagedNode().getFqdName() + "::" + state.getStateDescr() + " ";
			}
		}

		return "[" + causesString.trim() + "]";
	}
	
	synchronized public String aggregatedByToString() {
		String aggregatedByString = "";

		for (FactHandle aggregatedByHdle : this.getAggregatedBy()) {
			Fact fact = Engine.getStreamKS().getFact(aggregatedByHdle);
			if ((fact != null) && fact instanceof State) {
				State state = (State) fact;
				aggregatedByString += state.getMostSpecificManagedNode().getFqdName() + "::" + state.getStateDescr() + " ";
			}
		}

		return "[" + aggregatedByString.trim() + "]";
	}

	synchronized public String aggregatesToString() {
		String aggregatesString = "";

		for (FactHandle aggregateHdle : this.getAggregates()) {
			Fact fact = Engine.getStreamKS().getFact(aggregateHdle);
			if ((fact != null) && fact instanceof State) {
				State state = (State) fact;
				aggregatesString += state.getMostSpecificManagedNode().getFqdName() + "::" + state.getStateDescr() + " ";
			}
		}

		return "[" + aggregatesString.trim() + "]";
	}

	
	synchronized public void updatDBRow() {
		try {
			ResultSet result = null;

			if (Engine.getDB() != null) {
				result = Engine.getDB().executeQuery("select linkKey from STATE where linkKey='" + this.linkKey + "';");

				if (!result.isBeforeFirst()) {
					Engine.getDB()
							.executeUpdate(
									"insert into STATE (id,linkKey,sourceName,sourceType,managedEntityChain,managedNodeChain,cleared,severity,stateDescr,shortDescr,descr,count,categories,isRoot,causedBy,causes,isConsumerView,isProviderView,aggregatedBy,aggregates,meLastUpdateTime,meFirstRaiseTime,meLastRaiseTime,meLastClearTime,lastUpdateTime,firstRaiseTime,lastRaiseTime,lastClearTime,specificProperties,timestamp) VALUES ("
											+ "'"
											+ this.getId()
											+ "',"
											+ "'"
											+ this.linkKey
											+ "',"
											+ "'"
											+ this.sourceName
											+ "',"
											+ "'"
											+ this.sourceType
											+ "',"
											+ "'"
											+ this.managedEntityChain
											+ "',"
											+ "'"
											+ this.managedNodeChain
											+ "',"
											+ this.cleared
											+ ","
											+ "'"
											+ this.severity
											+ "',"
											+ "'"
											+ this.stateDescr
											+ "',"
											+ "'"
											+ this.shortDescr
											+ "',"
											+ "'"
											+ this.descr
											+ "',"
											+ "'"
											+ this.count
											+ "',"
											+ "'"
											+ this.categories
											+ "',"
											+ this.isRoot()
											+ ",'"
											+ this.causedByToString()
											+ "',"
											+ "'"
											+ this.causesToString()
											+ "',"
											+ this.isConsumerView
											+ ","
											+ this.isProviderView
											+ ",'"
											+ this.aggregatedByToString()
											+ "',"
											+ "'"
											+ this.aggregatesToString()
											+ "'"
											+ ","
											+ this.meLastUpdateTime
											+ ","
											+ this.meFirstRaiseTime
											+ ","
											+ this.meLastRaiseTime
											+ ","
											+ this.meLastClearTime
											+ ","
											+ this.lastUpdateTime
											+ ","
											+ this.firstRaiseTime
											+ ","
											+ this.lastRaiseTime
											+ ","
											+ this.lastClearTime
											+ ",'"
											+ this.specificProperties + "'," + this.getTimestamp() + ");");
				} else {

					Engine.getDB().executeUpdate(
							"update STATE " + "set " + "id = '" + this.getId() + "', " + "linkKey = '" + this.linkKey + "', " + "sourceName = '"
									+ this.sourceName + "', " + "sourceType = '" + this.sourceType + "', " + "managedEntityChain = '"
									+ this.managedEntityChain + "', " + "managedNodeChain = '" + this.managedNodeChain + "', " + "cleared = "
									+ this.cleared + ", " + "severity = '" + this.severity + "', " + "stateDescr = '" + this.stateDescr + "', "
									+ "shortDescr = '" + this.shortDescr + "', " + "descr = '" + this.descr + "', " + "count = " + this.count + ", "
									+ "categories = '" + this.categories + "', " + "isRoot = " + this.isRoot() + "," + "causedBy = '"
									+ this.causedByToString() + "', " + "causes = '" + this.causesToString() + "', " + "isConsumerView = "
									+ this.isConsumerView + ", " + "isProviderView = " + this.isProviderView + ", " + "aggregatedBy = '"
									+ this.aggregatedByToString() + "', " + "aggregates = '" + this.aggregatesToString() + "', " + "meLastUpdateTime = "
									+ this.meLastUpdateTime + ", " + "meFirstRaiseTime = " + this.meFirstRaiseTime + ", " + "meLastRaiseTime = "
									+ this.meLastRaiseTime + ", " + "meLastClearTime = " + this.meLastClearTime + ", " + "lastUpdateTime = "
									+ this.lastUpdateTime + ", " + "firstRaiseTime = " + this.firstRaiseTime + ", " + "lastRaiseTime = "
									+ this.lastRaiseTime + ", " + "lastClearTime = " + this.lastClearTime + ", " + "specificProperties = '"
									+ this.specificProperties + "', " + "timestamp = " + this.getTimestamp() + " " + "where linkKey='" + this.linkKey
									+ "';");
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
