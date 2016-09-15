package ca.magenta.krr.fact;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import com.google.gson.Gson;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.EventCategory;
import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.engine.Globals;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
final public class Signal extends NormalizedProperties {

	private static Logger logger = Logger.getLogger(Signal.class);
	
	public static final boolean FALLING_EDGE = Globals.CLEARING;
	public static final boolean RAISING_EDGE = Globals.RAISING;

	public static final boolean ADD = true;
	public static final boolean REPLACE = ! ADD;
	
	
	private HashSet<String> causedByStrs = new HashSet<String>();
	private transient HashSet<FactHandle> causedByHdles = null;
	private HashSet<String> causeStrs = new HashSet<String>();
	private transient HashSet<FactHandle> causesHdles = null;

	private transient HashSet<FactHandle> aggregateHdles = null;

	public void addCausedByHdles(HashSet<State> causedBy) {

		for (State state : causedBy) {
			addCausedByHdle(state);
		}
	}

	public void addCausedByHdle(State state) {
		if (causedByHdles == null)
			causedByHdles = new HashSet<FactHandle>();

		FactHandle factHandle = Engine.getStreamKS().getFactHandle(state);
		if (factHandle != null) {
			causedByHdles.add(factHandle);
		}
	}

	public void addCausesHdles(HashSet<State> impacts) {

		for (State state : impacts) {
			addCausesHdle(state);
		}
	}

	public void addCausesHdle(State state) {
		if (causesHdles == null)
			causesHdles = new HashSet<FactHandle>();

		FactHandle factHandle = Engine.getStreamKS().getFactHandle(state);
		if (factHandle != null) {
			causesHdles.add(factHandle);
		}
	}

	public Signal() {
		super();
		setTimestamp(System.currentTimeMillis());
	}
	
	public Signal(State state) {
		super(state);
	}

	public static void insertInWMFromWorksheet(Message message, String sourceTypeStr) {
		HashMap<String, String> atts = message.getAttributes();
		
		logger.debug("atts:" + (new Gson()).toJson(atts));

		String sourceType = sourceTypeStr;
		String source = atts.get("Source");
		String sourceName = NormalizedProperties.forgeSourceName(sourceType,source);

		ManagedEntity managedEntityGround = new ManagedEntity(atts.get("ElementClassName"), atts.get("ElementName"));
		ManagedEntity managedEntityMostSpecific = new ManagedEntity(atts.get("ClassName"), atts.get("InstanceName"));
		Chain<ManagedEntity> managedEntityChain = new Chain<ManagedEntity>();
		Chain<ManagedNode> managedNodeChain = new Chain<ManagedNode>();
		managedEntityChain.insertGround(managedEntityGround);
		managedNodeChain.insertGround(Engine.getManagedNode(managedEntityGround));
		managedEntityChain.addMostSpecific(managedEntityMostSpecific);
		managedNodeChain.addMostSpecific(Engine.getManagedNode(managedEntityMostSpecific));


		String severityStr = atts.get("Severity");
		float severityFloat = Float.valueOf(severityStr);

		int severityInt = (int) severityFloat;

		Severity severity = null;
		switch (severityInt) {
		case 1:
			severity = Severity.CRITICAL;
			break;
		case 2:
			severity = Severity.MAJOR;
			break;
		case 3:
			severity = Severity.MINOR;
			break;
		case 4:
			severity = Severity.WARNING;
			break;
		case 5:
			severity = Severity.OK;
			break;
		default:
			severity = Severity.INDETERMINATE;
			break;
		}

		String stateDescr = atts.get("Name").replaceAll("\\s+", "_");

		long meFirstRaiseTime = (long) (Float.parseFloat(atts.get("FirstNotifiedAt")) * 1000);
		long meLastRaiseTime = (long) (Float.parseFloat(atts.get("LastNotifiedAt")) * 1000);
		long meLastClearTime = (long) (Float.parseFloat(atts.get("LastClearedAt")) * 1000);
		long meLastUpdateTime = (long) (Float.parseFloat(atts.get("LastChangedAt")) * 1000);

		String categorySignature = sourceType + "::" + stateDescr;

		HashSet<String> categories = null;
		logger.debug("categorySignature:" + categorySignature);
		EventCategory eventCategory = Engine.getEventCategoryByCategorySignature(categorySignature);
		if (eventCategory == null)
		{
			eventCategory = new EventCategory();
		}
		categories = eventCategory.getCategories();
		logger.debug("categories:" + categories);


		String linkKey = sourceName + ":::" + atts.get("ClassName") + "::" + atts.get("InstanceName") + "::" + stateDescr;

		String identifier = linkKey + ":::" + Long.toString(meLastRaiseTime);

		boolean active = Boolean.parseBoolean(atts.get("active"));
		
		
		

		boolean cleared = !active;

		Signal as = new Signal();


		
		as.setId(identifier);
		as.linkKey = linkKey;
		as.source = source;
		as.sourceType = sourceType;
		as.managedEntityChain = managedEntityChain;
		as.cleared = cleared;
		as.severity = severity;
		as.stateDescr = stateDescr;
		as.shortDescr = stateDescr;
		as.categories = categories;
		as.isConsumerView = eventCategory.isConsumerView();
		as.isProviderView = eventCategory.isProviderView();


		as.meLastUpdateTime = meLastUpdateTime;
		as.meFirstRaiseTime = meFirstRaiseTime;
		as.meLastRaiseTime = meLastRaiseTime;
		as.meLastClearTime = meLastClearTime;

		String causedByStr = atts.get("CauseBy");
		if (causedByStr != null)
		{
			String[] causedBys = causedByStr.split("\\s*,\\s*");
			for (String causedBy : causedBys)
			{
				String causedByString = sourceName + ":::" + causedBy;
				as.causedByStrs.add(causedByString);
				logger.debug("as.causedByStrs:" + causedByString);
			}
		}
		String causeStr = atts.get("Causes");
		if (causeStr != null)
		{
			String[] causes = causeStr.split("\\s*,\\s*");
			for (String cause : causes)
			{
				String causeString = sourceName + ":::" + cause;
				as.causeStrs.add(causeString);
				logger.debug("as.impactStrs:" + causeString);
			}
		}
		
		as.managedNodeChain = managedNodeChain;

		Engine.getStreamKS().insert(as);
	}
	
	public static void insertInWM_Clear(State state) {
		
		Signal as = new Signal(state);
		
		as.setCleared(FALLING_EDGE);
		
		Engine.getStreamKS().insert(as);
		
	}
	
	public static void raising(	State state, 
								HashSet<State> newCausedBys) {
		
		raising(state.sourceType,
				state.source, 
				state.managedNodeChain,
				state.stateDescr,
				state.severity, 
				state.shortDescr, 
				state.descr, 
				state.categories,
				state.isConsumerView, 
				state.isProviderView,
				newCausedBys,
				null /*causes*/,
				null /*aggregates*/,
				state.specificProperties
				);		
	}
	

	public static void raising(String sourceType,
			String source, 
			Chain<ManagedNode> managedNodeChain,
			String stateDescr,
			Severity severity, 
			String shortDescr, 
			String descr, 
			HashSet<String> categories,
			boolean isConsumerView, 
			boolean isProviderView,
			HashSet<State> causedBy,
			HashSet<State> causes,
			HashSet<State> aggregates,
			HashMap<String, String> specificProperties
			) {
		insertInWM(sourceType,
				source, 
				managedNodeChain,
				Signal.RAISING_EDGE,
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
				specificProperties
				);
	}

	public static void updating(String sourceType,
			String source, 
			Chain<ManagedNode> managedNodeChain,
			String stateDescr,
			Severity severity, 
			String shortDescr, 
			String descr, 
			HashSet<String> categories,
			boolean isConsumerView, 
			boolean isProviderView,
			HashSet<State> causedBy,
			HashSet<State> causes,
			HashSet<State> aggregates,
			HashMap<String, String> specificProperties
			) {
		raising(sourceType,
				source, 
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
				specificProperties
				);
	}

	
	public static void falling(String sourceType,
			String source, 
			Chain<ManagedNode> managedNodeChain,
			String stateDescr,
			Severity severity, 
			String shortDescr, 
			String descr, 
			HashSet<String> categories,
			boolean isConsumerView, 
			boolean isProviderView,
			HashSet<State> causedBy,
			HashSet<State> causes,
			HashSet<State> aggregates,
			HashMap<String, String> specificProperties
			) {
		insertInWM(sourceType,
				source, 
				managedNodeChain,
				Signal.FALLING_EDGE,
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
				specificProperties
				);
	}

	public static void insertInWM(String sourceType,
										String source, 
										Chain<ManagedNode> managedNodeChain,
										boolean clearing,
										String stateDescr,
										Severity severity, 
										String shortDescr, 
										String descr, 
										HashSet<String> categories,
										boolean isConsumerView, 
										boolean isProviderView,
										HashSet<State> causedBy,
										HashSet<State> causes,
										HashSet<State> aggregates,
										HashMap<String, String> specificProperties
										) {

		String sourceName = NormalizedProperties.forgeSourceName(sourceType,source);
		
		ManagedNode mn = managedNodeChain.getMostSpecific();
		
		ManagedEntity managedEntity = null;
		Chain<ManagedEntity> managedEntityChain = new Chain<ManagedEntity>();
		managedEntity = new ManagedEntity(mn.getType(), mn.getFqdName());
		managedEntityChain.addMostSpecific(managedEntity);

		String linkKey = sourceName + ":::" + mn.getType() + "::"
				+ mn.getFqdName() + "::" + stateDescr;

		String identifier = linkKey + ":::" + System.currentTimeMillis();

		Signal as = new Signal();

		as.setId(identifier);
		as.linkKey = linkKey;
		as.source = source;
		as.sourceType = sourceType;
		//as.managedEntityChain = managedEntityChain;
		as.cleared = clearing;
		as.severity = severity;
		as.stateDescr = stateDescr;
		as.shortDescr = shortDescr;
		as.descr = descr;
		
		if (categories == null)
			categories = new HashSet<String>();
		as.categories = categories;
		
		if (causedBy != null)
		{
			logger.debug("CausedByHashSet.size():" + causedBy.size());
			as.addCausedByHdles(causedBy);
		}
		if (causes != null)
		{
			logger.debug("CausesHashSet.size():" + causes.size());
			as.addCausesHdles(causes);
		}
		if (aggregates != null)
		{
			logger.debug("AggregatesHashSet.size():" + aggregates.size());
			as.addAggregateHdles(aggregates);
		}

		as.isConsumerView = isConsumerView;
		as.isProviderView = isProviderView;

		as.meLastUpdateTime = 0;
		as.meFirstRaiseTime = 0;
		as.meLastRaiseTime = 0;
		as.meLastClearTime = 0;

		as.managedNodeChain = managedNodeChain;
		
		if (specificProperties == null)
		{
			specificProperties = new HashMap<String, String>();
		}
		as.specificProperties = specificProperties;

		Engine.getStreamKS().insert(as);
	}

	private void addAggregateHdles(HashSet<State> aggregates) {

		for (State aggregate : aggregates) {
			addAggregateHdle(aggregate);
		}
	}

	private void addAggregateHdle(State aggregate) {
		if (aggregateHdles == null)
			aggregateHdles = new HashSet<FactHandle>();

		FactHandle factHandle = Engine.getStreamKS().getFactHandle(aggregate);
		if (factHandle != null) {
			aggregateHdles.add(factHandle);
		}
	}

	public HashSet<FactHandle> getCausedByHdles() {
		return causedByHdles;
	}

	public HashSet<FactHandle> getCausesHdles() {
		return causesHdles;
	}

	public HashSet<FactHandle> getAggregateHdles() {
		return aggregateHdles;
	}

	public HashSet<String> getCausedByStrs() {
		return causedByStrs;
	}
	
	public HashSet<String> getCausesStrs() {
		return causeStrs;
	}



}
