package ca.magenta.krr.ruleEngin;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.NormalizedProperties;
import ca.magenta.krr.fact.Signal;
import ca.magenta.krr.fact.State;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-05-25
 */
public class Aggregator {
	
	private static Logger logger = Logger.getLogger(Aggregator.class);
	
	public static final String SOURCE_TYPE = "Aggregator";
	public static final String SOURCE = "local";	
	
	public static void insertNew(State state1, State state2)
	{
		HashSet<String> categories = new HashSet<String>();
		
		HashSet<State> aggregates = new HashSet<State>();
		aggregates.add(state1);
		aggregates.add(state2);
		
		Chain<ManagedNode> managedNodeChain = new Chain<ManagedNode>();
		managedNodeChain.addMostSpecific(state1.getGroundManagedNode());

		
		// TODO Gives highest severity
		Signal.raising(Aggregator.SOURCE_TYPE,
				Aggregator.SOURCE, 
				managedNodeChain,
				NormalizedProperties.AGGREGATOR_STATE_DESCR,
				Severity.CRITICAL, 
				"It Is Aggregation" /* TODO nice shortDescr */,
				"It Is Aggregation" /* TODO nice descr */, 
				categories, 
				false /* NOT isConsumerView */,
				false /*NOT isProviderView*/,
				null /* causedBy */, 
				null /* causes */, 
				aggregates,
				null /* specificProperties */);
	}
	
	// IN CONSTRUCTION
	public static void aggregate1stTo2nd(State aggregate, State aggregator)
	{
		HashSet<String> categories = new HashSet<String>();
		
		HashSet<State> aggregates = new HashSet<State>();
		aggregates.add(aggregate);
		
		Signal.updating(aggregator.getSourceType(),
				aggregator.getSourceName(), 
				aggregator.getManagedNodeChain(),
				aggregator.getStateDescr(),
				aggregator.getSeverity(), 
				aggregator.getShortDescr(),
				aggregator.getDescr(), 
				aggregator.getCategories(), 
				aggregator.isConsumerView(),
				aggregator.isProviderView(),
				null /* causedBy */, 
				null /* causes */, 
				aggregates,
				null /* specificProperties */);
	}

	public static void addAggregate(State aggregator, State newAggregate)
	{
		HashSet<State> aggregates = new HashSet<State>();
		aggregates.add(newAggregate);
		
		Chain<ManagedNode> managedNodeChain = new Chain<ManagedNode>();
		managedNodeChain.addMostSpecific(aggregator.getGroundManagedNode());
		
		Signal.raising(Aggregator.SOURCE_TYPE,
				Aggregator.SOURCE, 
				managedNodeChain,
				NormalizedProperties.AGGREGATOR_STATE_DESCR,
				aggregator.getSeverity(), 
				aggregator.getShortDescr(),
				aggregator.getShortDescr() /* TODO descr */, 
				aggregator.getCategories(), 
				aggregator.isConsumerView(),
				aggregator.isProviderView(),
				null /* causedBy */, 
				null /* causes */, 
				aggregates,
				null /* specificProperties */);
	}
	
	public static void removeAggregate(State aggregate)
	{
		FactHandle aggregateFactHandle = Engine.getStreamKS().getFactHandle(aggregate);
		
		if (aggregateFactHandle != null)
		{
			State.updateAggregatedAndAggregatesOnClear(aggregate, aggregateFactHandle, true /* updateAggregateInWM*/);
		}
	}

	public static void doClear(State aggregator)
	{
		//HashSet<State> aggregates = new HashSet<State>();
		//Vector<State> aggregates = new Vector<State>();
		
		Chain<ManagedNode> managedNodeChain = new Chain<ManagedNode>();
		managedNodeChain.addMostSpecific(aggregator.getGroundManagedNode());
		
		Signal.insertInWM_Clear(aggregator);
/*		
		Signal.insertInWM(Aggregator.SOURCE_TYPE,
				Aggregator.SOURCE, 
				managedNodeChain,
				Signal.CLEARING,
				NormalizedProperties.AGGREGATOR_STATE_DESCR,
				aggregator.getSeverity(), 
				aggregator.getShortDescr(),
				aggregator.getShortDescr() /* TODO descr /, 
				aggregator.getCategories(), 
				aggregator.isConsumerView(),
				aggregator.isProviderView(),
				null /* causedBy /, 
				null /* causes /, 
				aggregates,
				null /* specificProperties /); */  
	}
	
	public static String getSourceName()
	{
		return SOURCE_TYPE + "::" + SOURCE;
	}
}
