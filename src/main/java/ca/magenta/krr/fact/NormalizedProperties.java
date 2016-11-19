package ca.magenta.krr.fact;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.tools.Utils;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-06-08
 */
public abstract class NormalizedProperties implements Fact{




	private static Logger logger = Logger.getLogger(NormalizedProperties.class);
	
	public static final String TIMESTAMP_LABEL = "timestamp";
	public static final String CAUSES_LABEL = "causes";
	public static final String CAUSED_BY_LABEL = "causedBy";
	public static final String AGGREGATEDBY_LABEL = "aggregatedBy";
	public static final String AGGREGATES_LABEL = "aggregates";
	
	public static final String AGGREGATOR_STATE_DESCR = "IsAggregating";
	public boolean isAggregator() {
		return stateDescr.equals(AGGREGATOR_STATE_DESCR);
	}

	public NormalizedProperties() {
		super();
	}
	
	public NormalizedProperties(NormalizedProperties normalizedProperties) {
		
		this();
		
		id = normalizedProperties.id;
		linkKey = normalizedProperties.linkKey;
		source = normalizedProperties.source;
		sourceType = normalizedProperties.sourceType;
		managedEntityChain = normalizedProperties.managedEntityChain;
		managedNodeChain = normalizedProperties.managedNodeChain;
		cleared = normalizedProperties.cleared;
		severity = normalizedProperties.severity;
		stateDescr = normalizedProperties.stateDescr;
		shortDescr = normalizedProperties.shortDescr;
		descr = normalizedProperties.descr;
		count = normalizedProperties.count;
		categories = normalizedProperties.categories;

		meLastUpdateTime = normalizedProperties.meLastUpdateTime;    
		meFirstRaiseTime = normalizedProperties.meFirstRaiseTime;
		meLastRaiseTime = normalizedProperties.meLastRaiseTime;
		meLastClearTime = normalizedProperties.meLastClearTime;
		lastUpdateTime = normalizedProperties.lastUpdateTime;    
		firstRaiseTime = normalizedProperties.firstRaiseTime;
		lastRaiseTime = normalizedProperties.lastRaiseTime;
		lastClearTime = normalizedProperties.lastClearTime;
		
		timestamp = normalizedProperties.timestamp;	
		
		//aggregatedBy = normalizedProperties.aggregatedBy;
		//aggregates = normalizedProperties.aggregates;
		
		//causedByLocal = normalizedProperties.causedByLocal;
		//causesLocal = normalizedProperties.causesLocal;
		//causedByExtern = normalizedProperties.causedByExtern;
		//causesExtern = normalizedProperties.causesExtern;
		
		isConsumerView = normalizedProperties.isConsumerView;
		isProviderView = normalizedProperties.isProviderView;
		
		specificProperties = normalizedProperties.specificProperties;

		
	}
	


	public HashMap<String, String> getSpecificProperties() {
		return specificProperties;
	}



	protected String id = null;
	protected String linkKey = null;
	protected String source = null;
	//protected String sourceName = null;
	protected String sourceType = null;
	protected transient Chain<ManagedEntity> managedEntityChain = new Chain<ManagedEntity>();
	protected transient Chain<ManagedNode> managedNodeChain = null;

	protected boolean cleared = true;
	protected Severity severity = Severity.OK;
	protected String stateDescr = null;
	protected String shortDescr = null;
	protected String descr = null;
	protected long count = 0;
	protected HashSet<String> categories = null;

	protected long meLastUpdateTime = 0;    
	protected long meFirstRaiseTime = 0;
	protected long meLastRaiseTime = 0;
	protected long meLastClearTime = 0;
	protected long lastUpdateTime = 0;    
	protected long firstRaiseTime = 0;
	protected long lastRaiseTime = 0;
	protected long lastClearTime = 0;
	
	protected long timestamp = 0;
	
	//protected transient Set<FactHandle> aggregatedBy = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());
	//protected transient Set<FactHandle> aggregates = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());


	
	//protected transient Set<FactHandle> causedByLocal = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());
	//protected transient Set<FactHandle> causesLocal = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());
	//protected transient Set<FactHandle> causedByExtern = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());
	//protected transient Set<FactHandle> causesExtern = Collections.newSetFromMap(new ConcurrentHashMap<FactHandle, Boolean>());
	
	protected boolean isConsumerView = false;
	protected boolean isProviderView = false;
	
	protected HashMap<String, String> specificProperties = new HashMap<String, String>();



	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLinkKey() {
		return linkKey;
	}

	public void setLinkKey(String linkKey) {
		this.linkKey = linkKey;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public static String forgeSourceName(String sourceType, String source){
		return sourceType + "::" + source;
	}

	public String getSourceName() {
		return forgeSourceName(sourceType,source);
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	
	public Chain<ManagedEntity> getManagedEntityChain() {
		return managedEntityChain;
	}

	public void setManagedEntityChain(Chain<ManagedEntity> managedEntityChain) {
		this.managedEntityChain = managedEntityChain;
	}

	public ManagedNode getMostSpecificManagedNode() {
		return managedNodeChain.getMostSpecific();
	}

	public ManagedNode getGroundManagedNode() {
		return managedNodeChain.getGround();
	}

	public Chain<ManagedNode> getManagedNodeChain() {
		return managedNodeChain;
	}

	public void setManagedNodeChain(Chain<ManagedNode> managedNodeChain) {
		this.managedNodeChain = managedNodeChain;
	}

	public boolean isCleared() {
		return cleared;
	}

	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getStateDescr() {
		return stateDescr;
	}

	public void setStateDescr(String stateDescr) {
		this.stateDescr = stateDescr;
	}

	public String getShortDescr() {
		return shortDescr;
	}

	public void setShortDescr(String shortDescr) {
		this.shortDescr = shortDescr;
	}
	
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
	public HashSet<String> getCategories() {
		return categories;
	}

	public void setCategories(HashSet<String> categories) {
		this.categories = categories;
	}
	
	public long getMeLastUpdateTime() {
		return meLastUpdateTime;
	}

	public void setMeLastUpdateTime(long meLastUpdateTime) {
		this.meLastUpdateTime = meLastUpdateTime;
	}

	public long getMeFirstRaiseTime() {
		return meFirstRaiseTime;
	}

	public void setMeFirstRaiseTime(long meFirstRaiseTime) {
		this.meFirstRaiseTime = meFirstRaiseTime;
	}

	public long getMeLastRaiseTime() {
		return meLastRaiseTime;
	}

	public void setMeLastRaiseTime(long meLastRaiseTime) {
		this.meLastRaiseTime = meLastRaiseTime;
	}

	public long getMeLastClearTime() {
		return meLastClearTime;
	}

	public void setMeLastClearTime(long meLastClearTime) {
		this.meLastClearTime = meLastClearTime;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public long getFirstRaiseTime() {
		return firstRaiseTime;
	}

	public void setFirstRaiseTime(long firstRaiseTime) {
		this.firstRaiseTime = firstRaiseTime;
	}

	public long getLastRaiseTime() {
		return lastRaiseTime;
	}

	public void setLastRaiseTime(long lastRaiseTime) {
		this.lastRaiseTime = lastRaiseTime;
	}

	public long getLastClearTime() {
		return lastClearTime;
	}

	public void setLastClearTime(long lastClearTime) {
		this.lastClearTime = lastClearTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
//	public Set<FactHandle> getAggregatedBy() {
//		return aggregatedBy;
//	}
//
//	public Set<FactHandle> getAggregates() {
//		return aggregates;
//	}

	public boolean isConsumerView() {
		return isConsumerView;
	}
	
	public void setConsumerView(boolean isConsumerView) {
		this.isConsumerView = isConsumerView;
	}

	public boolean isProviderView() {
		return isProviderView;
	}

	public void setProviderView(boolean isProviderView) {
		this.isProviderView = isProviderView;
	}

	protected HashSet<String> getChanges(NormalizedProperties other) {
		HashSet<String> changes = new HashSet<String>(); 
		Field[] fields = NormalizedProperties.class.getDeclaredFields();
		for (Field f : fields) {
			Object tHis;
			Object oTher;
			try {
				//logger.debug("Field:" + f.getName());
				if (! Modifier.isStatic(f.getModifiers()) ) {
					if (other != null)
					{
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
					else
					{
						changes.add(f.getName());
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Field:" + f.getName(), e);
			}
		}
		return changes;
	}
	
	public String toString(boolean pretty)		
	{
		return  Utils.toJsonE(this, this.getClass(), pretty);
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}
}
