package ca.magenta.krr.fact;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.data.StateRelation;
import ca.magenta.krr.data.StateRelations;
import ca.magenta.krr.data.StateRelations.ModifyMode;
import ca.magenta.krr.engine.Engine;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class State extends NormalizedProperties {

	private static Logger logger = Logger.getLogger(State.class);
	
	private static StateRelations localAggregateRelation = new StateRelations();
	private static StateRelations externCauseEffectRelation = new StateRelations();
	private static StateRelations localCauseEffectRelation = new StateRelations();
	
	public static final boolean IS_CLEARED = true;
	public static final boolean IS_RAISED = ! IS_CLEARED;

	private FactHandle selfFactHandle = null;
	
	synchronized public boolean isRoot() {
		boolean rIsRoot = true;
		if (this.getSelfFactHandle() != null)
		{
			rIsRoot = ( !localCauseEffectRelation.upperContains(this.selfFactHandle)  && !externCauseEffectRelation.upperContains(this.selfFactHandle));
		}
		return rIsRoot;
	}
	
	synchronized public boolean isTop() {
		boolean rIsRoot = true;
		if (this.getSelfFactHandle() != null)
		{
			rIsRoot = ( !localCauseEffectRelation.lowerContains(this.selfFactHandle)  && !externCauseEffectRelation.lowerContains(this.selfFactHandle));
		}
		return rIsRoot;
	}

	synchronized public boolean hasAggregate() {
		boolean hasAggregate = true;
		if (this.getSelfFactHandle() != null)
		{
			hasAggregate = ( localAggregateRelation.upperContains(this.selfFactHandle) );
		}
		return hasAggregate;
	}

	synchronized public boolean isAggregated() {
		boolean isAggregated = true;
		if (this.getSelfFactHandle() != null)
		{
			isAggregated = ( localAggregateRelation.lowerContains(this.selfFactHandle) );
		}
		return isAggregated;
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

	@Override
	public boolean isAggregator() {
		return super.isAggregator();
	}
	
	synchronized public boolean addAggregate(FactHandle factHandle) {
		return localAggregateRelation.modify(ModifyMode.PUT, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean removeAggregate(FactHandle factHandle) {
		return localAggregateRelation.modify(ModifyMode.REMOVE, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean addAggregatedBy(FactHandle factHandle) {
		return localAggregateRelation.modify(ModifyMode.PUT, factHandle, this.getSelfFactHandle() );
	}

	synchronized public boolean removeAggregatedBy(FactHandle factHandle) {
		return localAggregateRelation.modify(ModifyMode.REMOVE, factHandle, this.getSelfFactHandle() );
	}

	synchronized public boolean addCausedBy_local(FactHandle factHandle) {
		return localCauseEffectRelation.modify(ModifyMode.PUT, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean addCauses_local(FactHandle factHandle) {
		return localCauseEffectRelation.modify(ModifyMode.PUT, factHandle, this.getSelfFactHandle());
	}

	synchronized public boolean removeCausedBy_local(FactHandle factHandle) {
		return localCauseEffectRelation.modify(ModifyMode.REMOVE, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean removeCauses_local(FactHandle factHandle) {
		return localCauseEffectRelation.modify(ModifyMode.REMOVE, factHandle, this.getSelfFactHandle());
	}

	synchronized public boolean addCausedBy_extern(FactHandle factHandle) {
		return externCauseEffectRelation.modify(ModifyMode.PUT, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean addCauses_extern(FactHandle factHandle) {
		return externCauseEffectRelation.modify(ModifyMode.PUT, factHandle, this.getSelfFactHandle());
	}

	synchronized public boolean removeCausedBy_extern(FactHandle factHandle) {
		return externCauseEffectRelation.modify(ModifyMode.REMOVE, this.getSelfFactHandle(), factHandle);
	}

	synchronized public boolean removeCauses_extern(FactHandle factHandle) {
		return externCauseEffectRelation.modify(ModifyMode.REMOVE, factHandle, this.getSelfFactHandle());
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
			StateAndChanges stateAndChanges = State.updateCausedByAndCauses(newState, newStateFactHandle, signal.getCausedByStrs(), signal.getCausedByHdles(), signal.getCausesStrs(),
					signal.getCausesHdles());
			newState = stateAndChanges.state;
			newState = State.addAggregatesToAggregator(newState, newStateFactHandle, signal.getAggregateHdles());
		}
		
		Engine.getStreamKS().update(newStateFactHandle, newState);
		StateNew.insertInWM(newStateFactHandle, newState, null, true /* veryNew */);

		if (newState.isCleared()) {
			StateClear.insertInWM(newStateFactHandle, newState, null, true /* firstEnteredCleared */);
		}

	}

	synchronized private FactHandle getSelfFactHandle() {

		if (this.selfFactHandle == null)
			this.selfFactHandle = Engine.getStreamKS().getFactHandle(this);
		
		if (this.selfFactHandle == null)
			logger.warn(String.format("selfFactHandle is [null] for [%s]", this.getLinkKey()));
//		else
//			logger.debug(String.format("selfFactHandle: [%s] for [%s]", this.selfFactHandle, this.getLinkKey()));
			

		return this.selfFactHandle;
	}

	// Called in State.drl; keep public
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
				StateUpdate.insertInWM(impactedStateFactHandle, impactedState, changes);
			}
		}
		
		if (impactedStateFactHandle != null)
		{
			boolean changed = impactingState.addCauses_local(impactedStateFactHandle);
			if (changed) {
				HashSet<String> changes = new HashSet<String>();
				changes.add(State.CAUSES_LABEL);
				Engine.getStreamKS().update(impactingStateFactHandle, impactingState);
				StateUpdate.insertInWM(impactingStateFactHandle, impactingState, changes);
			}
		}
	}
	
	public synchronized static State updateAggregatedAndAggregatesOnClear(
			State clearedState, FactHandle clearedStateFactHandle,
			boolean updateAggregateInWM) {

		logger.debug(String.format("In updateAggregatedAndAggregatesOnClear; cleared: [%s]",
				Boolean.toString(clearedState.isCleared())));

		//if (clearedState.isCleared()) {

			HashSet<String> stateChangeList = new HashSet<String>();

			logger.debug(String.format("In updateAggregatedAndAggregatesOnClear; linkKey: [%s]",
					clearedState.getLinkKey()));

			boolean changed;
			changed = localAggregateRelation.removeAllRelationsWhereUpperIs(clearedStateFactHandle);
			if (changed)
				stateChangeList.add(State.AGGREGATES_LABEL);

			changed = localAggregateRelation.removeAllRelationsWhereLowerIs(clearedStateFactHandle);
			if (changed)
				stateChangeList.add(State.AGGREGATEDBY_LABEL);

			logger.debug(String.format("In updateAggregatedAndAggregatesOnClear; updateAggregateInWM: [%b]",
					updateAggregateInWM));
			logger.debug(String.format("In updateAggregatedAndAggregatesOnClear; updateAggregateInWM: [%d]",
					stateChangeList.size()));

			if (updateAggregateInWM && (stateChangeList.size() > 0)) {
				Engine.getStreamKS().update(clearedStateFactHandle, clearedState);
				StateUpdate.insertInWM(clearedStateFactHandle, clearedState, stateChangeList);
			}
		//}

		return clearedState;
	}

	public synchronized static State updateAggregatedAndAggregatesOnClearOld(
			State clearedState, FactHandle clearedStateFactHandle,
			boolean updateAggregateInWM) {

		HashSet<String> aggregatedbyChanges = new HashSet<String>();
		aggregatedbyChanges.add(State.AGGREGATES_LABEL);
		HashSet<String> aggregateChanges = new HashSet<String>();
		aggregateChanges.add(State.AGGREGATEDBY_LABEL);

		logger.debug("In updateAggregatedAndAggregatesOnClear; linkKey:"
				+ clearedState.getLinkKey());

		boolean anyChanges = false;

		for (FactHandle aggregatedByHdle : clearedState.getAggregatedBy()) {
			State aggregatedByState = State.getState(aggregatedByHdle);
			if (aggregatedByState != null) {
				boolean changed = aggregatedByState
						.removeAggregate(clearedStateFactHandle);
				if (changed) {
					Engine.getStreamKS().update(aggregatedByHdle,
							aggregatedByState);
					StateUpdate.insertInWM(aggregatedByHdle, aggregatedByState,
							aggregatedbyChanges);
					anyChanges = true;
				}
				clearedState.removeAggregatedBy(aggregatedByHdle);
			}
		}

		for (FactHandle aggregateHdle : clearedState.getAggregates()) {
			State aggregateState = State.getState(aggregateHdle);
			if (aggregateState != null) {
				boolean changed = aggregateState.removeAggregatedBy(clearedStateFactHandle);
				if (changed) {
					Engine.getStreamKS().update(aggregateHdle,
							aggregateState);
					StateUpdate.insertInWM(aggregateHdle, aggregateState,
							aggregateChanges);
					anyChanges = true;
				}
				clearedState.removeAggregate(aggregateHdle);
			}
		}

		if (updateAggregateInWM && anyChanges) {
			Engine.getStreamKS().update(clearedStateFactHandle, clearedState);
			StateUpdate.insertInWM(clearedStateFactHandle, clearedState, aggregateChanges);
		}

		return clearedState;
	}


	synchronized private static State addAggregatesToAggregator(	State aggregator,
														FactHandle aggregatorFactHandle,
														HashSet<FactHandle> aggregateHdles) {


		logger.debug(String.format("In addAggregatesToAggregator [%s]", aggregator.getLinkKey()));

		HashSet<String> changes = new HashSet<String>();
		changes.add(State.AGGREGATEDBY_LABEL);
		Vector<SimpleImmutableEntry<FactHandle, State>> toUpdate = new Vector<SimpleImmutableEntry<FactHandle, State>>();
		if (aggregateHdles != null) {
			logger.debug(String.format("aggregateHdles.size() [%d]", aggregateHdles.size()));
			for (FactHandle  aggregateHdle : aggregateHdles) {
				State aggregateState = State.getState(aggregateHdle);
				if (aggregateState != null) {
					boolean changed = aggregator.addAggregate(aggregateHdle);
					//boolean changed = aggregateState.addAggregatedBy(aggregatorFactHandle);
					if (changed) {
						SimpleImmutableEntry<FactHandle, State> entry = new SimpleImmutableEntry<FactHandle, State>(aggregateHdle,aggregateState);
						toUpdate.add(entry);
					}
				}
			}

			for (SimpleImmutableEntry<FactHandle, State> aggregate : toUpdate )
			{
				Engine.getStreamKS().update(aggregate.getKey(), aggregate.getValue());
				StateUpdate.insertInWM(aggregate.getKey(), aggregate.getValue(), changes);
			}
			logger.debug(String.format("aggregator.isAggregator() [%b]", aggregator.isAggregator()));
			logger.debug(String.format("aggregator.hasAggregate() [%b]", aggregator.hasAggregate()));
			logger.debug(String.format("aggregator.getAggregates().size() [%d]", aggregator.getAggregates().size()));
			logger.debug(String.format("aggregator.isAggregated() [%b]", aggregator.isAggregated()));
			logger.debug(String.format("aggregator.getAggregatedBy().size() [%d]", aggregator.getAggregatedBy().size()));


		}
		
		return aggregator;
	}

	static private class StateAndChanges 
	{
		public StateAndChanges(State state, HashSet<String> stateChangeList) {
			this.state = state;
			this.stateChangeList = stateChangeList;
		}
		State state;
		HashSet<String> stateChangeList;
	}
	
	synchronized public void setSameCauseBys(State target)
	{
		boolean changed = false;
		
		FactHandle targetHandle = Engine.getStreamKS().getFactHandle(target);
		if (targetHandle != null)
			changed = State.localCauseEffectRelation.replaceLowerWith(targetHandle, this.getCausedBy());
		
		if (changed)
		{
			HashSet<String> changes = new HashSet<String>();
			changes.add(State.CAUSED_BY_LABEL);
			Engine.getStreamKS().update(targetHandle, target);
			StateUpdate.insertInWM(targetHandle, target, changes);
		}
	}

	// This function is static and synchronized
	// This transaction must be complete in total before changing any
	// CausedByAndCauses of any other States
	synchronized private static StateAndChanges updateCausedByAndCauses(	State state, 
																FactHandle stateFactHandle, 
																HashSet<String> causedByStrs, 
																HashSet<FactHandle> causedByHdles,
																HashSet<String> causesStrs, 
																HashSet<FactHandle> causesHdles) {
		
		boolean stateHasChanged = false;
		HashSet<String> stateChangeList = new HashSet<String>();

		if (causedByHdles != null) {
			logger.debug(String.format("State:[%s]; causedByHdles.size():[%d]", state.getLinkKey(), causedByHdles.size()));
			State.localCauseEffectRelation.replaceLowerWith(stateFactHandle, causedByHdles);
		}

		if (causesHdles != null) {
			logger.debug(String.format("State:[%s]; causedByHdles.size():[%d]", state.getLinkKey(), causesHdles.size()));
			State.localCauseEffectRelation.replaceUpperWith(stateFactHandle, causesHdles);
		}
		
		for (String causedByStr : causedByStrs)
		{
			logger.debug("causedByStr:" + causedByStr);
			FactHandle causedByHdle = Engine.getStateByLinkKey(causedByStr);
			if (causedByHdle != null)
			{
				logger.debug("Found");
				State causedByState = State.getState(causedByHdle);
				if (causedByState != null) {
					stateHasChanged = state.addCausedBy_extern(causedByHdle);
					if (stateHasChanged)
					{
						stateChangeList.add(State.CAUSED_BY_LABEL);
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSES_LABEL);
						Engine.getStreamKS().update(causedByHdle, causedByState);
						StateUpdate.insertInWM(causedByHdle, causedByState, changes);
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
				State causesState = State.getState(causesHdle);
				if (causesState != null) {
					stateHasChanged = state.addCauses_extern(causesHdle);
					if (stateHasChanged)
					{
						stateChangeList.add(State.CAUSES_LABEL);
						HashSet<String> changes = new HashSet<String>();
						changes.add(State.CAUSED_BY_LABEL);
						Engine.getStreamKS().update(causesHdle, causesState);
						StateUpdate.insertInWM(causesHdle, causesState, changes);
					}
				}
			}
		}
		

		return new StateAndChanges(state, stateChangeList);
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

	// Called by CausalityAnalyser.drl
	// 'State ServiceConsumer-ServiceProvider dependency; no more ServiceProvider with same category'
	synchronized public void flushCausedBys()
	{
		HashSet<String> stateChangeList = new HashSet<String>();
		
		FactHandle stateHdle = Engine.getStreamKS().getFactHandle(this);

		boolean changed;
		changed = localCauseEffectRelation.removeAllRelationsWhereUpperIs(stateHdle);
		if (changed)
			stateChangeList.add(State.CAUSED_BY_LABEL);
		
		if (stateChangeList.size() > 0) {
			logger.debug("State ServiceConsumer-ServiceProvider dependency; no more ServiceProvider with same category");
			Engine.getStreamKS().update(stateHdle, this);
			StateUpdate.insertInWM(stateHdle, this, stateChangeList);
		}
	}
	
	// This function is static and synchronized
	// This transaction must be complete in total before changing any
	// CausedByAndCauses of any other States
	synchronized private static State updateCausedByAndCauses_goingClear(State state,
			FactHandle stateFactHandle) {

		if (state.isCleared()) {

			HashSet<String> stateChangeList = new HashSet<String>();

			logger.trace(String.format("In updateCausedByAndCauses CLEAR [%s]", state.getLinkKey()));

			boolean changed;
			changed = localCauseEffectRelation.removeAllRelationsWhereUpperIs(stateFactHandle);
			if (changed)
				stateChangeList.add(State.CAUSED_BY_LABEL);

			changed = localCauseEffectRelation.removeAllRelationsWhereLowerIs(stateFactHandle);
			if (changed)
				stateChangeList.add(State.CAUSES_LABEL);

			changed = externCauseEffectRelation.removeAllRelationsWhereUpperIs(stateFactHandle);
			if (changed)
				stateChangeList.add(State.CAUSED_BY_LABEL);

			changed = externCauseEffectRelation.removeAllRelationsWhereLowerIs(stateFactHandle);
			if (changed)
				stateChangeList.add(State.CAUSES_LABEL);
			
			if (stateChangeList.size() > 0) {
				Engine.getStreamKS().update(stateFactHandle, state);
				StateUpdate.insertInWM(stateFactHandle, state, stateChangeList);
			}
		}

		return state;
	}

	synchronized public static void updateExisting(Signal newSignal, State currentState) {
		long now = System.currentTimeMillis();

		logger.debug(String.format("updateExisting: [%s]", currentState.getLinkKey()));
		
		State updatedState = new State(newSignal);
		Engine.getStreamKS().delete(newSignal);
		FactHandle currentStateFactHandle = Engine.getStreamKS().getFactHandle(currentState);
		updatedState.count = currentState.count;

		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastRaiseTime = currentState.lastRaiseTime;
		updatedState.lastUpdateTime = now;

		// TODO move all next after Engine.getStreamKS().update ... 
		updatedState.selfFactHandle = currentStateFactHandle;
		if (!updatedState.isCleared()) {
			logger.debug(String.format("Before updateCausedByAndCauses: [%s]", currentState.getLinkKey()));
			StateAndChanges stateAndChanges = State.updateCausedByAndCauses(updatedState, currentStateFactHandle, newSignal.getCausedByStrs(), newSignal.getCausedByHdles(),
					newSignal.getCausesStrs(), newSignal.getCausesHdles());
			updatedState = stateAndChanges.state;
			updatedState = State.addAggregatesToAggregator(updatedState, currentStateFactHandle, newSignal.getAggregateHdles());

			Engine.getStreamKS().update(currentStateFactHandle, updatedState);
			StateUpdate.insertInWM(currentStateFactHandle, updatedState, currentState, stateAndChanges.stateChangeList);
		}
		else
		{
			Engine.getStreamKS().update(currentStateFactHandle, updatedState);
			StateUpdate.insertInWM(currentStateFactHandle, updatedState, currentState);
		}
	}

	synchronized public static void updateGoingCleared(Signal newSignal, State currentState) {
		long now = System.currentTimeMillis();

		logger.debug(String.format("updateGoingCleared  : [%s]", currentState.getLinkKey()));

		State updatedState = new State(newSignal);
		Engine.getStreamKS().delete(newSignal);
		FactHandle currentStateFactHandle = Engine.getStreamKS().getFactHandle(currentState);
		updatedState.count = currentState.count;
		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastRaiseTime = currentState.lastRaiseTime;
		updatedState.lastClearTime = now;
		updatedState.lastUpdateTime = now;

		Engine.unregisterState(currentStateFactHandle, updatedState.getMostSpecificManagedNode(), updatedState.getLinkKey());
		
		if (updatedState.isCleared()) {
			updatedState = State.updateCausedByAndCauses_goingClear(updatedState, currentStateFactHandle);
			updatedState = updateAggregatedAndAggregatesOnClear(updatedState, currentStateFactHandle, false /* NO updateAggregateInWM*/);
			//updatedState = updateAggregatedAndAggregatesOnClear(updatedState, currentStateFactHandle, true /* updateAggregateInWM*/);
		}

		Engine.getStreamKS().update(currentStateFactHandle, updatedState);

		StateClear.insertInWM(currentStateFactHandle, updatedState, currentState, false /*
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
		updatedState.firstRaiseTime = currentState.firstRaiseTime;
		updatedState.lastClearTime = currentState.lastClearTime;
		updatedState.lastRaiseTime = now;
		updatedState.lastUpdateTime = now;

		// TODO move all next after Engine.getStreamKS().update ... 
		updatedState.selfFactHandle = currentStateFactHandle;
		StateAndChanges stateAndChanges = null;
		if (!updatedState.isCleared()) {
			stateAndChanges = State.updateCausedByAndCauses(updatedState, currentStateFactHandle, newSignal.getCausedByStrs(), newSignal.getCausedByHdles(),
					newSignal.getCausesStrs(), newSignal.getCausesHdles());
			updatedState = stateAndChanges.state;
			updatedState = State.addAggregatesToAggregator(updatedState, currentStateFactHandle, newSignal.getAggregateHdles());
		}
		// TODO END

		Engine.getStreamKS().update(currentStateFactHandle, updatedState);

		Engine.registerState(currentStateFactHandle, updatedState.getMostSpecificManagedNode(), updatedState.getLinkKey());
		
		
		if (!updatedState.isCleared())
			StateNew.insertInWM(currentStateFactHandle, updatedState, currentState, stateAndChanges.stateChangeList, false /* not veryNew */);
		else
			StateNew.insertInWM(currentStateFactHandle, updatedState, currentState, false /* not veryNew */);
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


	synchronized public HashSet<FactHandle> getAggregatedBy() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		if (this.getSelfFactHandle() != null )
		{
			ConcurrentHashMap<FactHandle, Boolean> aggregatedBys = localAggregateRelation.getUpperStateHdles(this.selfFactHandle);
			if (aggregatedBys != null)
			{
				for (Entry<FactHandle, Boolean> e : aggregatedBys.entrySet()) {
					total.add(e.getKey());
				}
			}
		}

		return total;
	}

	synchronized public HashSet<FactHandle> getAggregates() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		if (this.getSelfFactHandle() != null )
		{
			ConcurrentHashMap<FactHandle, Boolean> aggregates = localAggregateRelation.getLowerStateHdles(this.selfFactHandle);
			if (aggregates != null)
			{
				for (Entry<FactHandle, Boolean> e : aggregates.entrySet()) {
					total.add(e.getKey());
				}
			}
		}

		return total;
	}
	
	synchronized public HashSet<FactHandle> getCausedBy() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		if (this.getSelfFactHandle() != null )
		{
			ConcurrentHashMap<FactHandle, Boolean> causedBys = localCauseEffectRelation.getLowerStateHdles(this.selfFactHandle);
			if (causedBys != null)
			{
				for (Entry<FactHandle, Boolean> e : causedBys.entrySet()) {
					total.add(e.getKey());
				}
			}

			causedBys = externCauseEffectRelation.getLowerStateHdles(this.selfFactHandle);
			if (causedBys != null)
			{
				for (Entry<FactHandle, Boolean> e : causedBys.entrySet()) {
					total.add(e.getKey());
				}
			}

		}

		return total;
	}

	synchronized public HashSet<FactHandle> getCauses() {

		HashSet<FactHandle> total = new HashSet<FactHandle>();

		if (this.getSelfFactHandle() != null )
		{
			ConcurrentHashMap<FactHandle, Boolean> causes = localCauseEffectRelation.getUpperStateHdles(this.selfFactHandle);
			if (causes != null)
			{
				for (Entry<FactHandle, Boolean> e : causes.entrySet()) {
					total.add(e.getKey());
				}
			}

			causes = externCauseEffectRelation.getUpperStateHdles(this.selfFactHandle);
			if (causes != null)
			{
				for (Entry<FactHandle, Boolean> e : causes.entrySet()) {
					total.add(e.getKey());
				}
			}

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
		//logger.debug(String.format("causesString:[%s]", causesString.trim()));

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
					logger.debug(String.format("DB INSERT:[%s];CLEARED[%s]", this.linkKey, Boolean.toString(this.cleared)));
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
											+ this.getSourceName()
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

					logger.debug(String.format("DB UPDATE:[%s];CLEARED[%s]", this.linkKey, Boolean.toString(this.cleared)));
					Engine.getDB().executeUpdate(
							"update STATE " + "set " + "id = '" + this.getId() + "', " + "linkKey = '" + this.linkKey + "', " + "sourceName = '"
									+ this.getSourceName() + "', " + "sourceType = '" + this.sourceType + "', " + "managedEntityChain = '"
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

	public static State getState(FactHandle causedByHdle) {
		
		State state = null;
		
		Fact fact = Engine.getStreamKS().getFact(causedByHdle);
		if ((fact != null) && fact instanceof State)
		{
			state = (State) fact;
		}
		return state;
	}

	public static HashSet<State> returnEffectiveCausedBys(
			HashSet<State> causedBys) {
		
		HashSet<State> effectiveCausedBys = null;
		
		if (causedBys != null) {
			effectiveCausedBys = new HashSet<State>();

			for (State causedBy : causedBys) {

				HashSet<State> underlayEffCausedBys = causedBy.returnEffectiveCausedBys(); 
				
				effectiveCausedBys.addAll(underlayEffCausedBys);
			}
		}
		
		return effectiveCausedBys;
	}


	private static HashSet<State> returnEffectiveCausedBysFromHdles(
			HashSet<FactHandle> causedBys) {

		HashSet<State> effectiveCausedBys = null;
		
		if (causedBys != null) {
			effectiveCausedBys = new HashSet<State>();

			for (FactHandle causedByHdle : causedBys) {
				
				State causedBy = State.getState(causedByHdle);
				if (causedBy != null) {
				
					HashSet<State> underlayEffCausedBys = causedBy.returnEffectiveCausedBys(); 
					
					effectiveCausedBys.addAll(underlayEffCausedBys);
				}
			}
		}
		
		return effectiveCausedBys;
	}

	private HashSet<State> returnEffectiveCausedBys() {

		HashSet<State> effectiveCausedBys = new HashSet<State>();
		
		if ( this.isRoot() )
		{
			effectiveCausedBys.add(this);
		}
		else
		{
			HashSet<State> underlayCausedBys = State.returnEffectiveCausedBysFromHdles(this.getCausedBy());
			
			effectiveCausedBys.addAll(underlayCausedBys);
		}
		
		return effectiveCausedBys;
	}

}
