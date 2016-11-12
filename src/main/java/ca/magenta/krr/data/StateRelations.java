package ca.magenta.krr.data;

import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2016-10-16
 */

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.State;
import ca.magenta.krr.fact.StateUpdate;

public class StateRelations {
	
	private static Logger logger = Logger.getLogger(StateRelations.class);

	
	private transient ConcurrentHashMap<FactHandle,ConcurrentHashMap<FactHandle, Boolean>> upperIndex = new ConcurrentHashMap<FactHandle,ConcurrentHashMap<FactHandle, Boolean>>(); 
	private transient ConcurrentHashMap<FactHandle,ConcurrentHashMap<FactHandle, Boolean>> lowerIndex = new ConcurrentHashMap<FactHandle,ConcurrentHashMap<FactHandle, Boolean>>();
	
	public enum ModifyMode {
	    PUT, REMOVE 
	}
	
	synchronized public boolean put(StateRelation stateRelation)
	{
		boolean changed = false;
		
		
		logger.debug("In StateRelations.put");
		
		FactHandle upperValue = stateRelation.getUpperValue();
		FactHandle lowerValue = stateRelation.getLowerValue();
		
		ConcurrentHashMap<FactHandle, Boolean> lowerValues = upperIndex.get(upperValue);
		if (lowerValues == null)
		{
			changed = true;
			lowerValues = new ConcurrentHashMap<FactHandle, Boolean>();
		}
		if (!lowerValues.containsKey(lowerValue))
		{
			changed = true;
			lowerValues.put(lowerValue, Boolean.TRUE);
			upperIndex.put(upperValue, lowerValues);
		}
		
		ConcurrentHashMap<FactHandle, Boolean> upperValues = lowerIndex.get(lowerValue);
		if (upperValues == null)
		{
			changed = true;
			upperValues = new ConcurrentHashMap<FactHandle, Boolean>();
		}
		if (!upperValues.containsKey(upperValue))
		{
			changed = true;
			upperValues.put(upperValue, Boolean.TRUE);
			lowerIndex.put(lowerValue, upperValues);
		}

		logger.debug(String.format("Returns: [%s]", Boolean.toString(changed)));

		return changed;
	}

	synchronized public boolean remove(StateRelation stateRelation)
	{
		boolean changed = false;
		
		logger.debug("In StateRelations.remove");

		FactHandle upperValue = stateRelation.getUpperValue();
		FactHandle lowerValue = stateRelation.getLowerValue();
		
		ConcurrentHashMap<FactHandle, Boolean> lowerValues = upperIndex.get(upperValue);
		if (lowerValues != null)
		{
			if (lowerValues.containsKey(lowerValue))
			{
				lowerValues.remove(lowerValue);
				changed = true;
				if (lowerValues.size() >  0)
					upperIndex.put(upperValue, lowerValues);
				else
					upperIndex.remove(upperValue);
			}
		}

		ConcurrentHashMap<FactHandle, Boolean> upperValues = lowerIndex.get(lowerValue);
		if (upperValues != null)
		{
			if (upperValues.containsKey(upperValue))
			{
				upperValues.remove(upperValue);
				changed = true;
				if (upperValues.size() > 0)
					lowerIndex.put(lowerValue, upperValues);
				else
					lowerIndex.remove(lowerValue);
			}
		}

		logger.debug(String.format("Returns: [%s]", Boolean.toString(changed)));

		return changed;
	}

	synchronized public boolean contains(StateRelation stateRelation)
	{
		boolean contains = false;

		FactHandle upperValue = stateRelation.getUpperValue();
		FactHandle lowerValue = stateRelation.getLowerValue();
		
		ConcurrentHashMap<FactHandle, Boolean> lowerValues = upperIndex.get(upperValue);
		if (lowerValues != null)
		{
			if (lowerValues.containsKey(lowerValue))
			{
				ConcurrentHashMap<FactHandle, Boolean> upperValues = lowerIndex.get(lowerValue);
				if (upperValues != null)
				{
					if (upperValues.containsKey(upperValue))
					{
						contains = true;
					}
					else
					{
						// TODO Raise an exception
						// Means corruption
						;
					}
				}
			}
		}

		return contains;
	}
	
	
	
	synchronized public ConcurrentHashMap<FactHandle, Boolean> getLowerStateHdles(FactHandle upperStateHdle) {

		return upperIndex.get(upperStateHdle);
	}

	synchronized public ConcurrentHashMap<FactHandle, Boolean> getUpperStateHdles(FactHandle lowerStateHdle) {

		return lowerIndex.get(lowerStateHdle);
	}

	synchronized public boolean upperContains(FactHandle upperStateHdle) {

		return upperIndex.containsKey(upperStateHdle);
	}

	synchronized public boolean lowerContains(FactHandle lowerStateHdle) {

		return lowerIndex.containsKey(lowerStateHdle);
	}

	synchronized public boolean modify(ModifyMode ModifyMode, FactHandle upperFactHandle, FactHandle lowerFactHandle) {
		boolean changed = false;

		if ( (upperFactHandle != null) && (lowerFactHandle != null) ) {
			
			StateRelation stateRelation = new StateRelation(upperFactHandle, lowerFactHandle);
			if (ModifyMode == ModifyMode.PUT)
				changed = this.put(stateRelation);
			else if (ModifyMode == ModifyMode.REMOVE.PUT)
				changed = this.remove(stateRelation);
		}

		return changed;
	}

	public boolean removeAllRelationsWhereUpperIs(FactHandle upperFactHdle) {
		
		boolean upperChanged = false;
		
		ConcurrentHashMap<FactHandle, Boolean> lowerFactHdles = upperIndex.get(upperFactHdle);
		if (lowerFactHdles != null)
		{
			synchronized(lowerFactHdles)
			{
				boolean lowerChanged = false;
				for (Entry<FactHandle, Boolean> lowerFactHdleE: lowerFactHdles.entrySet())
				{
					FactHandle lowerFactHdle = lowerFactHdleE.getKey();
					lowerChanged = remove(new StateRelation(upperFactHdle, lowerFactHdle));
					if (lowerChanged) {
						upperChanged = true;
						sendUpdate(lowerFactHdle, State.CAUSES_LABEL);
					}
				}
			}
		}
		
		return upperChanged;
	}
	

	public boolean removeAllRelationsWhereLowerIs(FactHandle lowerFactHdle) {
		
		boolean lowerChanged = false;
		
		ConcurrentHashMap<FactHandle, Boolean> upperFactHdles = lowerIndex.get(lowerFactHdle);
		if (upperFactHdles != null)
		{
			synchronized(upperFactHdles)
			{
				boolean upperChanged = false;
				for (Entry<FactHandle, Boolean> upperFactHdleE: upperFactHdles.entrySet())
				{
					FactHandle upperFactHdle = upperFactHdleE.getKey();
					upperChanged = remove(new StateRelation(upperFactHdle, lowerFactHdle));
					if (upperChanged) {
						lowerChanged = true;
						sendUpdate(upperFactHdle, State.CAUSED_BY_LABEL);
					}
				}
			}
		}
		
		return lowerChanged;
	}

	synchronized public boolean replaceLowerWith(FactHandle upperValue, HashSet<FactHandle> newLowerValues) {
		
		boolean upperChanged = false;
		boolean lowerChanged = false;
		
		ConcurrentHashMap<FactHandle, Boolean> previousLowerValues = upperIndex.get(upperValue);
		if (previousLowerValues != null)
		{
			for (Entry<FactHandle, Boolean> previousLowerValueE : previousLowerValues.entrySet())
			{
				lowerChanged = false;
				FactHandle previousLowerValue = previousLowerValueE.getKey();
				if ( ! newLowerValues.contains(previousLowerValue) )
				{
					// Not needed anymore : remove
					lowerChanged = remove(new StateRelation(upperValue, previousLowerValue));
					if (lowerChanged)
					{
						sendUpdate(previousLowerValue, State.CAUSES_LABEL);
						upperChanged = true;
					}
				}
				else
				{
					// Already there: No need to remove or add
					newLowerValues.remove(previousLowerValue);
				}
			}
		}

		for (FactHandle newLowerValue : newLowerValues)
		{
			// Not there yet : add it
			lowerChanged = put(new StateRelation(upperValue, newLowerValue));
			if (lowerChanged)
			{
				sendUpdate(newLowerValue, State.CAUSES_LABEL);
				upperChanged = true;
			}
		}
		return upperChanged;
	}

	synchronized public boolean replaceUpperWith(FactHandle lowerValue, HashSet<FactHandle> newUpperValues) {
		
		boolean lowerChanged = false;
		boolean upperChanged = false;
		
		ConcurrentHashMap<FactHandle, Boolean> previousUpperValues = lowerIndex.get(lowerValue);
		if (previousUpperValues != null)
		{
			for (Entry<FactHandle, Boolean> previousUpperValueE : previousUpperValues.entrySet())
			{
				upperChanged = false;
				FactHandle previousUpperValue = previousUpperValueE.getKey();
				if ( ! newUpperValues.contains(previousUpperValue) )
				{
					// Not needed anymore : remove
					upperChanged = remove(new StateRelation(previousUpperValue, lowerValue));
					if (upperChanged)
					{
						sendUpdate(previousUpperValue, State.CAUSED_BY_LABEL);
						lowerChanged = true;
					}
				}
				else
				{
					// Already there: No need to remove or add
					newUpperValues.remove(previousUpperValue);
				}
			}
		}
		for (FactHandle newUpperValue : newUpperValues)
		{
			// Not there yet : add it
			upperChanged = put(new StateRelation(newUpperValue, lowerValue));
			if (upperChanged)
			{
				sendUpdate(newUpperValue, State.CAUSED_BY_LABEL);
				lowerChanged = true;
			}
		}
		return lowerChanged;
	}

	private void sendUpdate(FactHandle factHandle, String label)
	{
		
		State state = State.getState(factHandle);
		if (state != null)
		{
			HashSet<String> changes = new HashSet<String>();
			changes.add(label);
			
			Engine.getStreamKS().update(factHandle , state);
			StateUpdate.insertInWM(factHandle, state,changes);
		}
	}

}
