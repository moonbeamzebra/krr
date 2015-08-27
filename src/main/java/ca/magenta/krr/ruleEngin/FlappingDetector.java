package ca.magenta.krr.ruleEngin;

import java.util.HashMap;

import org.apache.log4j.Logger;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Globals;
import ca.magenta.krr.fact.Signal;
import ca.magenta.krr.fact.State;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-06-03
 */
public class FlappingDetector {
	
	private static Logger logger = Logger.getLogger(FlappingDetector.class);

	
	public static final String SOURCE_TYPE = "FlappingDetector";
	public static final String SOURCE = "local";	
	public static final String STATE_DESCR = "Flapping";
	public static final String EPISODIC_LABEL = "Episodic";
	public static final String INTERMITTENT_LABEL = "Intermittent";
	public static final String CONSTANT_LABEL = "Constant"; 
	private static final String ORIGINAL_LINK_KEY_PROPERTY_POSFIX = "OriginalLinkKey";
	private static final String ORIGINAL_STATE_DESCR_PROPERTY_POSFIX = "OriginalStateDescr";
	private static final String PROPERTY_PREFIX = "fp";
	public static final String FLAPPING_TYPE_PROPERTY = PROPERTY_PREFIX + "Type";
	public static final String ORIGINAL_LINK_KEY_PROPERTY = PROPERTY_PREFIX + ORIGINAL_LINK_KEY_PROPERTY_POSFIX;
	public static final String ORIGINAL_STATE_DESCR_PROPERTY = PROPERTY_PREFIX + ORIGINAL_STATE_DESCR_PROPERTY_POSFIX;

	public static void raiseEpesodicFlapping( Chain<ManagedNode> managedNodeChainOfFlapper,
												String linkKeyOfFlapper,
												String stateDescrOfFlapper,
												Severity configuredSeverity)
	{
		
		
		
        HashMap<String, String> specificProperties = new HashMap<String, String>();
        specificProperties.put(FLAPPING_TYPE_PROPERTY, EPISODIC_LABEL); 
        specificProperties.put(ORIGINAL_LINK_KEY_PROPERTY, linkKeyOfFlapper);
        specificProperties.put(ORIGINAL_STATE_DESCR_PROPERTY, stateDescrOfFlapper);
        
        String stateDescr = FlappingDetector.buildSateDescr(stateDescrOfFlapper);

        raiseUpdateClearFlapping(Globals.RAISING,
    			managedNodeChainOfFlapper,
    			linkKeyOfFlapper, 
    			stateDescr,
    			EPISODIC_LABEL + " " + stateDescr,
    			EPISODIC_LABEL + " " + stateDescr,
    			configuredSeverity,
    			specificProperties);
	}
	
	public static void promoteToConstantFlapping(State flapping, Severity configuredSeverity) {

		HashMap<String, String> specificProperties = flapping.getSpecificProperties();
		specificProperties.put(FLAPPING_TYPE_PROPERTY, CONSTANT_LABEL);
	
//		FlappingDetector.clearEpesodicFlapping(
//				flapping);

		raiseUpdateClearFlapping(Globals.RAISING, 
				flapping.getManagedNodeChain(),
				flapping.getLinkKey(),
				flapping.getStateDescr(),
				CONSTANT_LABEL + " " + flapping.getStateDescr(),
				CONSTANT_LABEL + " " + flapping.getStateDescr(),
				configuredSeverity, 
				specificProperties);
	}
	
	public static void clearEpesodicFlapping(State episodicFlapping) {
		
		raiseUpdateClearFlapping(Globals.CLEARING, 
				episodicFlapping.getManagedNodeChain(),
				episodicFlapping.getLinkKey(), 
				episodicFlapping.getStateDescr(),
    			EPISODIC_LABEL + " " + episodicFlapping.getStateDescr(),
    			EPISODIC_LABEL + " " + episodicFlapping.getStateDescr(),
				episodicFlapping.getSeverity(),
				null /*specificProperties*/);
	}
	
	
	private static void raiseUpdateClearFlapping(boolean isCleared,
			Chain<ManagedNode> managedNodeChain,
			String linkKey, 
			String stateDescr,
			String shortDescr,
			String descr,
			Severity configuredSeverity,
			HashMap<String, String> specificProperties) {

		Signal.insertInWM(FlappingDetector.SOURCE_TYPE,
				FlappingDetector.SOURCE, 
				managedNodeChain,
				isCleared,
				stateDescr,
				configuredSeverity, 
				shortDescr,
				descr, 
				null /* categories */,
				false /* isConsumerView */, 
				false /* isProviderView */,
				null /* causedBy */, 
				null /* causes */, 
				null /* aggregates */,
				specificProperties);
	}
	
	public static String buildSateDescr(String flappingStateDescr)
	{
		return  STATE_DESCR + "-" + flappingStateDescr ;
	}

}
