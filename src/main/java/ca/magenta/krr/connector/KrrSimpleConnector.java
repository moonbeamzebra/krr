package ca.magenta.krr.connector;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.Chain;
import ca.magenta.krr.data.EventCategory;
import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.Message;
import ca.magenta.krr.fact.Signal;

import com.google.gson.Gson;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-11-07
 */
public class KrrSimpleConnector {

	private static Logger logger = Logger.getLogger(KrrSimpleConnector.class);

	public static void insertAsSignalInWM(Message message) {

		try {
			HashMap<String, String> atts = message.getAttributes();

			logger.trace("atts:" + (new Gson()).toJson(atts));

			String sourceType = atts.get("sourceType");
			String source = atts.get("source");
			String sourceName = sourceType + "::" + source;

			String managedElement = atts.get("managedElement");
			Chain<ManagedEntity> managedEntityChain = ManagedEntity.parseToManagedEntityChain(managedElement);
			Chain<ManagedNode> managedNodeChain = ManagedNode.transposeToManagedNodeChain(managedEntityChain);

			Severity severity = new Severity(atts.get("severity"));

			String stateDescr = atts.get("stateDescr").replaceAll("\\s+", "_");

			String categorySignature = sourceType + "::" + stateDescr;

			HashSet<String> categories = null;
			logger.debug("categorySignature:" + categorySignature);
			EventCategory eventCategory = Engine.getEventCategoryByCategorySignature(categorySignature);
			if (eventCategory == null) {
				eventCategory = new EventCategory();
			}
			categories = eventCategory.getCategories();
			logger.debug("categories:" + categories);

			long meFirstRaiseTime = (long) (Float.parseFloat(atts.get("firstNotifiedAt")) * 1000);
			long meLastRaiseTime = (long) (Float.parseFloat(atts.get("lastNotifiedAt")) * 1000);
			long meLastClearTime = (long) (Float.parseFloat(atts.get("lastClearedAt")) * 1000);
			long meLastUpdateTime = (long) (Float.parseFloat(atts.get("lastChangedAt")) * 1000);

			ManagedNode mn = managedNodeChain.getMostSpecific();

			String linkKey = sourceName + ":::" + mn.toString() + "::" + stateDescr;

			String identifier = linkKey + ":::" + Long.toString(meLastRaiseTime);

			boolean cleared = Boolean.parseBoolean(atts.get("cleared"));

			Signal as = new Signal();

			as.setId(identifier);
			as.setLinkKey(linkKey);
			as.setSourceName(sourceName);
			as.setSourceType(sourceType);
			as.setManagedEntityChain(managedEntityChain);
			as.setCleared(cleared);
			as.setSeverity(severity);
			as.setStateDescr(stateDescr);
			as.setShortDescr(stateDescr);
			as.setCategories(categories);
			as.setConsumerView(eventCategory.isConsumerView());
			as.setProviderView(eventCategory.isProviderView());

			as.setMeLastUpdateTime(meLastUpdateTime);
			as.setMeFirstRaiseTime(meFirstRaiseTime);
			as.setMeLastRaiseTime(meLastRaiseTime);
			as.setMeLastClearTime(meLastClearTime);

			String causedByStr = atts.get("causedBy");
			if (causedByStr != null) {
				String[] causedBys = causedByStr.split("\\s*,\\s*");
				for (String causedBy : causedBys) {
					String causedByString = sourceName + ":::" + causedBy;
					as.getCausedByStrs().add(causedByString);
					logger.debug("as.causedByStrs=" + causedByString);
				}
			}
			String causeStr = atts.get("causes");
			if (causeStr != null) {
				String[] causes = causeStr.split("\\s*,\\s*");
				for (String cause : causes) {
					String causeString = sourceName + ":::" + cause;
					as.getCausesStrs().add(causeString);
					logger.debug("as.causesStrs=" + causeString);
				}
			}

			as.setManagedNodeChain(managedNodeChain);

			Engine.getStreamKS().insert(as);
		} catch (Throwable e) {
			logger.error("Problem with KrrSimple message: [" + message.toString() + "]", e);
		}
	}
}
