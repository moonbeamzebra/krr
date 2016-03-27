package ca.magenta.krr.ruleEngin;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.common.LogicalOperator;
import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.CategorizedRelation;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.DependencyRule;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.Signal;
import ca.magenta.krr.fact.State;
import ca.magenta.krr.fact.StateLifecycle;
import ca.magenta.krr.fact.StateUpdate;
import ca.magenta.neo4j.Node;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-30
 */
public class CausalityAnalyser implements Runnable {
	
	private static Logger logger = Logger.getLogger(CausalityAnalyser.class);
	

	
	public static final String SOURCE_TYPE = "CausalityAnalyser";
	public static final String SOURCE = "local";	
	public static final String STATE_DESCR = "Impacted";
	
	private static final String[] CHANGES_OF_INTEREST = { "categories" };


    private volatile boolean doRun = true;

    public void stopIt() {
        doRun = false;
    }
	
	private static int numberOfThreads;
	private static int waitTime;
	private static int randomMax;
	
	private static Vector<HashSet<DirtyNodeInfo>> buckets = new Vector<HashSet<DirtyNodeInfo>>();
	private static Vector<CausalityAnalyser> runners = new Vector<CausalityAnalyser>();
	private static Vector<Thread> threads = new Vector<Thread>();
	
	private int threadNumber;
	
	private CausalityAnalyser(int threadNumber)
	{
		this.threadNumber = threadNumber;
	}
	
	public static String getSourceName()
	{
		return SOURCE_TYPE + "::" + SOURCE;
	}

	
	public static void check4RootCauseAndDispatch(StateLifecycle stateLifecycle)
	{
		
		logger.trace("In check4RootCauseAndDispatch");
		
		boolean isStateUpdateInstance = (stateLifecycle instanceof StateUpdate);
		boolean hasChangesOfInterest = hasChangesOfInterest(stateLifecycle);
		
		State stateRef = stateLifecycle.getStateRef();
		ManagedNode causingMN = stateRef.getMostSpecificManagedNode();
		HashSet<String> cats = stateRef.getCategories();
		for(String cat : cats)
		{
			logger.trace("cat:[" + cat +"]");
		}

		if ( !isStateUpdateInstance || hasChangesOfInterest)
		{
			Vector<CategorizedRelation> endRels = causingMN.getEndingRelations();
			
			for (CategorizedRelation endRel : endRels )
			{
				logger.trace("Relation:[" + endRel +"]");
				Node startEndNode = endRel.getStart();
				if ((startEndNode != null) && (startEndNode instanceof ManagedNode))
				{
					ManagedNode startEN =  (ManagedNode) startEndNode;
				
					//RelationCategorization rc = endRel.getRelationCategorization();
					String category = endRel.getCategory();
					if (cats.contains(category) || hasChangesOfInterest )
					{
												
						CausalityAnalyser.addDirtyNode(new DirtyNodeInfo(startEN, category, DirtyNodeInfo.CHECK_ROOT_CAUSE_AND_SIGNAL_IMPACT));
						logger.trace("In check4RootCauseAndDispatch; addDirtyNode");
					}
					logger.trace("CategorizedRelation:[" + endRel +"]");
				}
			}
			
			logger.trace(stateRef);
		}
		
		for(String cat : cats)
		{
			CausalityAnalyser.addDirtyNode(new DirtyNodeInfo(causingMN, cat, DirtyNodeInfo.CHECK_ROOT_CAUSE_ONLY));
		}

	}

	private static boolean hasChangesOfInterest(StateLifecycle stateLifecycle) {
		
		HashSet<String> changes = stateLifecycle.getChanges();
		
		if ( (changes != null) && (changes.size() > 0) )
		{
			for (String interestingChange : CHANGES_OF_INTEREST)
			{
				if (changes.contains(interestingChange) )
				{
					return true;
				}
			}
		}
		
		return false;
	}

	private void checkImpacts(ManagedNode mn, String stateCategory, boolean signalImpact) {
	 
		logger.trace("In checkImpacts");
		
		Chain<ManagedNode> managedNodeChain = new Chain<ManagedNode>();
		managedNodeChain.addMostSpecific(mn);
		
		logger.debug("Durty MN:[" + mn + "]; cat:[" + stateCategory + "]");
		
		HashMap<String, DependencyRule> dependencyRuleBySeverity = mn.getDependencyRuleBySeverityForCategory(stateCategory);
		
		HashSet<State> causedBys = new HashSet<State>();
		if (dependencyRuleBySeverity != null)
		{
			logger.debug("Has dependency rule:(" + dependencyRuleBySeverity.size() + ")");
			Vector<CategorizedRelation> startRels = mn.getStartingRelations();
			
			int outOf = 0;
			
			float straight = 0;
			int good = 0;
			for (CategorizedRelation startRel : startRels )
			{
				logger.debug("Depend of:[" + startRel.getEnd()+"]");
				ManagedNode endMN = (ManagedNode) startRel.getEnd();
				if (endMN != null)
				{
					//RelationCategorization relationCategorization = startRel.getRelationCategorization();
					float relationWeight = startRel.getWeight();
					if (relationWeight > 0)
					{
						// TODO Should not be outOf = outOf + relationWeight  ?
						outOf++;
						
						boolean foundActiveStateOfCategory = false;
						Vector<State> states = Engine.getStateByNodeByCategory(endMN, stateCategory);
						Iterator<State> iter = states.iterator();
						while (!foundActiveStateOfCategory && iter.hasNext())
						{
							State state = iter.next();
							if ( !state.isCleared() )
							{
								foundActiveStateOfCategory = true;
								causedBys.add(state);
								logger.debug("Active state for:" + state.getCategories().toString());
							}
						}
						
						if (!foundActiveStateOfCategory)
						{
							straight = straight + relationWeight;
							
							// TODO Should not be good = good + relationWeight  ?
							good++;
						}
					}
				}
			}
			logger.debug("Straight:[" + straight+"]");
			logger.debug("Good:[" + good+"]");
			logger.debug("OutOf:[" + outOf+"]");
			
			DependencyRule dependencyRule = null;
			
			Severity testedSeverity = Severity.MOST_SEVERE;
			
			logger.trace("Start Testing:" + testedSeverity.toString());
			
			boolean impactFound = false;
			HashSet<String> toClearCategories = new HashSet<String>();
			while (true)
			{	
				logger.debug("Testing severity:[" + testedSeverity.toString() + "]");
				dependencyRule = dependencyRuleBySeverity.get(testedSeverity.toString());
				
				if (dependencyRule != null)
				{
					toClearCategories.add(dependencyRule.getStateCategory());
					logger.debug("DependencyRule:" + dependencyRule.toString(true /*pretty*/));
					LogicalOperator logicalOperator = dependencyRule.getOperator();
					boolean isInPercent = dependencyRule.isInPercent();
					if (logicalOperator != null)
					{
						if (!isInPercent)
						{
							impactFound = logicalOperator.isTrue(straight, dependencyRule.getValue());
						}
						else
						{
							double math = (double) good / (double) outOf;
							logger.debug("good:" + good + ";outOf:" + outOf + ";math:" + Double.toString(math) + ";value:" + dependencyRule.getValue());
							impactFound = logicalOperator.isTrue(math, dependencyRule.getValue());
						}
						if (impactFound)
						{
							logger.debug("Impact found for:" + testedSeverity.toString());
							
							if (signalImpact)
							{
								HashSet<String> categories = new HashSet<String>();
								categories.add(dependencyRule.getStateCategory());
								logger.debug("Add impact state for:[" + managedNodeChain + "]" + 
										      categories);
								// Add or update
								Signal.raising(CausalityAnalyser.SOURCE_TYPE,
										CausalityAnalyser.SOURCE, 
										managedNodeChain,
										CausalityAnalyser.STATE_DESCR,
										testedSeverity, 
										"Losing resource" /* TODO nice shortDescr */,
										"Losing resource" /* TODO nice descr */, 
										categories, 
										false /* NOT isConsumerView */,
										true /*isProviderView*/,
										causedBys, 
										null /* causes */, 
										null /* aggregates */,
										null /* specificProperties */);
							}
							
							// Apply the same impact to other states of this manage node
							Vector<State> thisNodeStates = Engine.getStateByNodeByCategory(mn, dependencyRule.getStateCategory());
							if (thisNodeStates != null) {
								for (State state : thisNodeStates) {
									if (	! ( state.getStateDescr().equals(STATE_DESCR) && 
											state.getSourceName().equals(CausalityAnalyser.getSourceName()) ) )
									{
										if (logger.isDebugEnabled())
											logger.debug("Other state on same node:[" + state.getLinkKey() + "]-" +  state.getCategories());

										for (State causedBy : causedBys) {
											state.addCausedBy(causedBy);
										}
									}
								}
							}
							
							if (! dependencyRule.getStateCategory().equals(stateCategory)){
								// Remove all causedBy to other states of this manage node
								Vector<State> thisNodeStatesOfTestedCategory = Engine.getStateByNodeByCategory(mn, stateCategory);
								if (thisNodeStatesOfTestedCategory != null) {
									for (State state : thisNodeStatesOfTestedCategory) {
										if (	! ( state.getStateDescr().equals(STATE_DESCR) && 
												state.getSourceName().equals(CausalityAnalyser.getSourceName()) ) )
										{
											if (logger.isDebugEnabled())
												logger.debug("Other state on same node:[" + state.getLinkKey() + "]-" +  state.getCategories());

											state.flushCausedBy();
										}
									}
								}

							}
								
							break;
							
						}
					}
				}
				
				
				if (testedSeverity.equals(Severity.LESS_SEVERE))
				{
					// No impact found for this severity/Categories
					// Clear all States that could have been open before
					Vector<State> allStatesForANode = Engine.getStateByNode(mn);

					if (allStatesForANode != null) {
						for (State state : allStatesForANode) {
							boolean toClear = false;
							if (	state.getStateDescr().equals(STATE_DESCR) && 
									state.getSourceName().equals(CausalityAnalyser.getSourceName()) &&
									signalImpact )
							{
								HashSet<String> cats = state.getCategories();
								for (String cat : cats)
								{
									if (toClearCategories.contains(cat))
									{
										toClear = true;
										break;
									}
								}
								if (toClear)
								{
									if (logger.isDebugEnabled())
										logger.debug("Will clear:[" + state.getLinkKey() + "]-" +  state.getCategories());
									
									Signal.insertInWM_Clear(state);
								}
							}
						}
					}
					
					break;
				}
				testedSeverity = testedSeverity.decreaseSeverity();
				
			} 
		}
		else 
		{
			if (signalImpact)
			{
				// Clear everyone that could have been raised
				clearAllOtherImpactAnalyserStateForThatNode_but(mn, STATE_DESCR, null,null);
			}
		}
		
	}
	
	private void clearAllOtherImpactAnalyserStateForThatNode_but(	ManagedNode targetMN, 
																	String targetStateDescr, 
																	Severity targetSeverity, 
																	HashSet<String> targetCategories) {
		
		Vector<State> allStatesForANode = Engine.getStateByNode(targetMN);

		if (allStatesForANode != null) {
			for (State state : allStatesForANode) {
				if (	state.getSourceName().equals(CausalityAnalyser.getSourceName()) && // is it mine
						state.getStateDescr().equals(targetStateDescr) 
					)
				{
					if ((targetSeverity == null) && (targetCategories == null)) 
					{
						if (logger.isDebugEnabled())
							logger.debug("Will clear:[" + state.getLinkKey() + "]-" +  state.getCategories());

						Signal.insertInWM_Clear(state);
					}
					else if (	!state.getSeverity().equals(targetSeverity) ||
								!state.getCategories().equals(targetCategories) 	)
					{
						if (logger.isDebugEnabled())
							logger.debug("Will clear:[" + state.getLinkKey() + "]-" +  state.getCategories());

						Signal.insertInWM_Clear(state);
					}
				}
			}
		}
	}
	
	public static void insertNew(Chain<ManagedNode> managedNodeChain, 
			String stateDescr,
			Severity severity, 
			String shortDescr, 
			String descr,
			HashSet<String> categories,
			boolean isConsumerView, 
			boolean isProviderView,
			HashSet<State> causedBy,
			HashSet<State> causes,
			HashSet<State> aggregates)
	{
		
		Signal.raising(SOURCE_TYPE,
				SOURCE, 
				managedNodeChain, 
				stateDescr,
				severity, 
				shortDescr, 
				descr,
				categories,
				isConsumerView, 
				isProviderView,
				causedBy,
				causes,
				aggregates,
				null
				);
	}


	synchronized private HashSet<DirtyNodeInfo> giveMeSomeWork(int threadNumber)
	{
		HashSet<DirtyNodeInfo> bucket = buckets.get(threadNumber);
		
		@SuppressWarnings("unchecked")
		HashSet<DirtyNodeInfo> work = (HashSet<DirtyNodeInfo>) bucket.clone();
		bucket.clear();
		
		return work;
		
	}

	synchronized public static void addDirtyNode(DirtyNodeInfo dirtyNodeInfo)
	{
		logger.trace("In addDirtyNode; Node:[" + dirtyNodeInfo.mn + "] Category:[" + dirtyNodeInfo.category + "]");
		
		// With the following trick, a particular nodeName will always be processed
		// by the same thread
		int bucketNumber = Math.abs(dirtyNodeInfo.mn.hashCode()) % numberOfThreads;
		
		HashSet<DirtyNodeInfo> bucket = buckets.get(bucketNumber);
		
		bucket.add(dirtyNodeInfo);
		
	}
	
	public static void start(int pNumberOfThreads, int pWaitTime, int pRandomMax)
	{
		numberOfThreads = pNumberOfThreads;
		waitTime = pWaitTime;
		randomMax = pRandomMax;
		
        for (int i = 0; i < numberOfThreads; i++) {
        	buckets.insertElementAt(new HashSet<DirtyNodeInfo>(), i);
        	CausalityAnalyser dnt = new CausalityAnalyser(i);
        	runners.insertElementAt(dnt, i);
        	Thread t = new Thread(dnt, "CsAnl-" + (i+1));
        	threads.insertElementAt(t, i);
        	t.start();
        	logger.trace(t.toString() + " started");
        }
	}
	
	public static void stop() throws InterruptedException
	{
	
		for (CausalityAnalyser r : runners)
		{
			r.stopIt();
		}

		for (Thread t : threads)
		{
			t.join(3*(waitTime+randomMax));
			logger.trace(t.toString() + " stopped");
		}

	}

	public void run() {
		
			while (doRun) {
				HashSet<DirtyNodeInfo> managedNodesToCheck = giveMeSomeWork(threadNumber);
				for (DirtyNodeInfo fqdNameEntry : managedNodesToCheck)
				{
					logger.trace(fqdNameEntry.mn + "," + fqdNameEntry.category);
					checkImpacts(fqdNameEntry.mn, fqdNameEntry.category,fqdNameEntry.signalImpact);
				}
				try {
				Thread.sleep(waitTime + (new Random()).nextInt(randomMax + 1));
				} catch (InterruptedException e) {
					logger.error("", e);
					e.printStackTrace();
					doRun = false;
				}
			}
		
	}
	
	static class DirtyNodeInfo {
		

		final static boolean CHECK_ROOT_CAUSE_AND_SIGNAL_IMPACT = true;
		final static boolean CHECK_ROOT_CAUSE_ONLY = false;
		
		public DirtyNodeInfo(ManagedNode mn, String category,
				boolean signalImpact) {
			super();
			this.mn = mn;
			this.category = category;
			this.signalImpact = signalImpact;
		}
		
		ManagedNode mn = null; 
		String category = null;
		boolean signalImpact = true;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((category == null) ? 0 : category.hashCode());
			result = prime * result + ((mn == null) ? 0 : mn.hashCode());
			result = prime * result + (signalImpact ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DirtyNodeInfo other = (DirtyNodeInfo) obj;
			if (category == null) {
				if (other.category != null)
					return false;
			} else if (!category.equals(other.category))
				return false;
			if (mn == null) {
				if (other.mn != null)
					return false;
			} else if (!mn.equals(other.mn))
				return false;
			if (signalImpact != other.signalImpact)
				return false;
			return true;
		}
		
		
		
	}


}
