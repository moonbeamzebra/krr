package ca.magenta.krr.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.magenta.krr.fact.Message;
import ca.magenta.krr.tools.SendMessage;
import ca.magenta.utils.HashMapVector;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-10-28
 */
public class TestToolKit {

	private static Logger logger = Logger.getLogger(TestToolKit.class);

	private static Statement dbStatement = null;
	private static PrintWriter messageServerPrintWriter = null;
	private static HashMapVector messageFileData = null;
	
	// CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[MGTAWeb2::Impacted MGTAWeb1::Impacted],
	//		Causes=[www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Losing resource]
	private static final String DUMP_STRING_PATTERN = "^(.*),(.*),(.*),isRoot=(.*),CausedBy=(.*),Causes=(.*),AggregatedBy=(.*),Aggregates=(.*),\\[(.*)\\]$";
	private static Pattern dumpString_Pattern = Pattern.compile(DUMP_STRING_PATTERN);


	public static boolean testEachExistOnceNotCleared(Vector<String> expectedResults) throws SQLException {

		return testEachExistOnceNotCleared(expectedResults, -1 /* Don't check count */);
	
	}

	public static boolean testEachExistOnceNotCleared(Vector<String> dumpStrings, int count) throws SQLException {
		
		boolean ok = false;
		
		for (String dumpString : dumpStrings)
		{
			ok = testForOnlyOneNotClearedImplementation(dumpString);
			if (ok)
			{				
				logger.info("SubTest : SUCCESS");
			}
			else
			{
				logger.info("SubTest : FAIL");
				break;
			}
				
		}
		
		if (ok) {
			if (count >= 0)
				ok = TestToolKit.testForTotalCount_NOT_CLEARED(count /* expectedCount */);
		}
			
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");

		
		return ok;
		
	}

	public static boolean testForOnlyOneNotCleared(String dumpString) throws SQLException {
		boolean ok = testForOnlyOneNotClearedImplementation(dumpString);
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");

		
		return ok;
		
	}

		private static boolean testForOnlyOneNotClearedImplementation(String dumpString) throws SQLException {

	// CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[MGTAWeb2::Impacted MGTAWeb1::Impacted],
	//		Causes=[www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Losing resource]
		
		boolean ok = false;
		
		Matcher matcher_dumpString_Pattern = dumpString_Pattern.matcher(dumpString);
		if (matcher_dumpString_Pattern.find()) {
			String iLinkKey = matcher_dumpString_Pattern.group(1);
			String iSeverity = matcher_dumpString_Pattern.group(2);
			String iCategoriesStr = matcher_dumpString_Pattern.group(3);
			String iIsRoot = matcher_dumpString_Pattern.group(4);
			String iCausedByStr = matcher_dumpString_Pattern.group(5); 
			String iCausesStr = matcher_dumpString_Pattern.group(6);
			String iAggregatedByStr = matcher_dumpString_Pattern.group(7);
			String iAggregatesStr = matcher_dumpString_Pattern.group(8); 
			String iShortDescr = matcher_dumpString_Pattern.group(9);
			
			logger.info("SubTesting -->");
			logger.info("  LinkKey=[" + iLinkKey + "]");
			logger.info("  Severity=[" + iSeverity + "]");
			logger.info("  Categories=" + iCategoriesStr);
			logger.info("  IsRoot=[" + iIsRoot + "]");
			logger.info("  CausedBy=" + iCausedByStr);
			logger.info("  Causes=" + iCausesStr);
			logger.info("  AggregatedBy=" + iAggregatedByStr);
			logger.info("  Aggregates=" + iAggregatesStr);
			logger.info("  ShortDescr=[" + iShortDescr + "]");
			
			
			String query = "SELECT linkKey, causes, causedBy, aggregates, aggregatedBy, categories from STATE where " + 
					"linkKey='" + iLinkKey + "' and " +
					"severity='" + iSeverity + "' and " + 
					"shortDescr='" + iShortDescr + "' and " + 
					"isRoot=" + iIsRoot + " and " +
					"cleared=false";
			
			logger.trace(query);


			ResultSet rs = null;
			rs = dbStatement.executeQuery(query);

			if (rs.next()) {
				HashSet<String> iCategories = getHashSet(iCategoriesStr);
				HashSet<String> iCauses = getHashSet(iCausesStr);
				HashSet<String> iCausedBy = getHashSet(iCausedByStr);
				HashSet<String> iAggregates = getHashSet(iAggregatesStr);
				HashSet<String> iAggregatedBy = getHashSet(iAggregatedByStr);

				HashSet<String> oCauses = getHashSet(rs.getString(2));
				HashSet<String> oCausedBy = getHashSet(rs.getString(3));
				HashSet<String> oAggregates = getHashSet(rs.getString(4));
				HashSet<String> oAggregatedBy = getHashSet(rs.getString(5));
				HashSet<String> oCategories = getHashSet(rs.getString(6));

				if ((iCauses.equals(oCauses)) && (iCausedBy.equals(oCausedBy)) && (iAggregates.equals(oAggregates)) && (iAggregatedBy.equals(oAggregatedBy))
						&& (iCategories.equals(oCategories))) {
					ok = true;
				}
			}

			if (rs.next()) {
				ok = false; // Should have only one
			}

			
		}
		else
		{
			throw new IllegalArgumentException("Bad line dumpString: [" + dumpString + "]");
		}


		return ok;
	}
	
	private static HashSet<String> getHashSet(String toHash) {
		if (toHash.startsWith("["))
			toHash = toHash.substring(1);
		if (toHash.endsWith("]"))
			toHash = toHash.substring(0, toHash.length() - 1);
		toHash = toHash.trim();
		HashSet<String> r_hashSet = new HashSet<String>();
		if (!toHash.isEmpty()) {
			String items[] = toHash.split("\\s+");
			for (String item : items) {
				r_hashSet.add(item);
			}
		}

		return r_hashSet;

	}

	public static boolean testAreCleared(Vector<String> linkKeys) throws SQLException {
		
		boolean ok = false;
		
		for (String linkKey : linkKeys)
		{
			ok = testClearStateOrNotExistImplementation(linkKey, true, false /* don't test if NotExist */);
			if (ok)
			{
				logger.info("SubTest : SUCCESS");
			}
			else
			{
				logger.info("SubTest : FAIL");
				break;
			}
				
		}
		
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");

		
		return ok;
	}

	public static boolean testIsClearedOrNotExist(String linkKey) throws SQLException {

		boolean ok = testClearStateOrNotExistImplementation(linkKey, true, true /* test if NotExist also*/);
		
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");


		return ok;

	}

	public static boolean testIsCleared(String linkKey) throws SQLException {

		return testClearState(linkKey, true);
	}

	public static boolean testIsNotCleared(String linkKey) throws SQLException {

		return testClearState(linkKey, false);
	}
	
	private static boolean testClearState(String linkKey, boolean cleared) throws SQLException {

		boolean ok = testClearStateOrNotExistImplementation(linkKey, cleared, false /* don't test if NotExist */);
		
		
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");


		return ok;
	}

	private static boolean testClearStateImplementation2(String linkKey, boolean cleared) throws SQLException {

		boolean ok = false;
		
		String clear = "CLEARED";
		if ( ! cleared )
			clear = "NOT CLEARED";
		logger.info("-->> Test " + linkKey + " is " + clear);

		String query = "SELECT LinkKey from STATE where LinkKey='" + linkKey + "' and Cleared=" + cleared;

		logger.trace(query);

		ResultSet rs = null;
		rs = dbStatement.executeQuery(query);

		if (rs.next()) {
			ok = true;
		}

		return ok;
	}

	private static boolean testClearStateOrNotExistImplementation(String linkKey, boolean iCleared, boolean clearedOrNotExist) throws SQLException {

		boolean ok = false;
		
		String askFor = "NOT CLEARED";
		if ( clearedOrNotExist )
			askFor = "CLEARED OR NOT EXISTING";
		else if ( iCleared )
			askFor = "CLEARED";
		logger.info("-->> Test " + linkKey + " is " + askFor);

		String query = "SELECT LinkKey, Cleared from STATE where LinkKey='" + linkKey + "'";

		logger.trace(query);

		ResultSet rs = null;
		rs = dbStatement.executeQuery(query);

		if (rs.next()) {
			boolean oCleared= rs.getBoolean(2);
			if (iCleared == oCleared)
			{
				ok = true;
			}
		}
		else if (clearedOrNotExist)
		{
			ok = true;
		}

		return ok;
	}
	
	private static boolean testForTotalCount(int expectedCount, boolean cleared) throws SQLException {

		boolean ok = false;
		
		String clear = "CLEARED";
		if ( ! cleared )
			clear = "NOT CLEARED";
		
		logger.info("-->> Test if " + expectedCount + " " + clear + " states exist");


		String query = "SELECT count(linkKey) from STATE where Cleared=" + cleared;

		logger.trace(query);

		ResultSet rs = null;
		rs = dbStatement.executeQuery(query);

		if (rs.next()) {
			long count = rs.getLong(1);
			logger.debug("count:" + count);
			if (count == expectedCount)
				ok = true;
			else
				ok = false;
		}
		
		if (ok)
			logger.info("SUCCESS");
		else
			logger.error("FAILED");


		return ok;
	}

	public static boolean testForTotalCount_NOT_CLEARED(int expectedCount) throws SQLException {

		return testForTotalCount(expectedCount, false);
	}

	public static boolean testForTotalCount_CLEARED(int expectedCount) throws SQLException {

		return testForTotalCount(expectedCount, true);
	}

	public static void simpleFormatClearAllSates() throws UnknownHostException,
			IOException {
		int msgCount = messageFileData.size() + 1;

		logger.debug("msgCount=" + msgCount);

		logger.info("-->> Clearing all current raised States");
		for (int line = 2; line <= msgCount; line++) {
			SendMessage.sendMessage_simpleFormat(messageServerPrintWriter, messageFileData, line, true /* isClearing */);
		}
	}

	public static void smartsFormatClearAllSates(PrintWriter messageServerPrintWriter, HashMapVector messageFileData) throws UnknownHostException,
			IOException {
		int msgCount = messageFileData.size() + 1;

		logger.debug("msgCount=" + msgCount);

		for (int line = 2; line <= msgCount; line++) {
			SendMessage.sendMessage_smartsFormat(messageServerPrintWriter, messageFileData, line, false /* NOTactive Only */);
		}
	}
	
	public static void sendMessage_simpleFormat(int line, boolean isClearing) throws UnknownHostException,
			IOException {

		String clear = "CLEAR";
		if (!isClearing)
			clear = "RAISE";

		HashMap<String, String> rowData = messageFileData.get(line - 2);
		String causedBy = rowData.get("causedBy");
		if (causedBy.equals("0.0"))
			causedBy = "";
		String causes = rowData.get("causes");
		if (causes.equals("0.0"))
			causes = "";
		
		String toPrint = "-->> Sending " + clear + " " + rowData.get("managedElement") + " " + rowData.get("stateDescr");
		if (!isClearing)
			toPrint += " causedBy=[" + causedBy	+ "] causes=[" + causes + "]";
		logger.info(toPrint);
		
		SendMessage.sendMessage_simpleFormat(messageServerPrintWriter, messageFileData, line, isClearing);
	}

	public static boolean testForOnlyOne(String linkKey, boolean cleared, String severity, String stateDescr, String iCategoriesStr, String iCausesStr,
			String iCausedByStr, String iAggregatesStr, String iAggregatedByStr) throws SQLException {

		boolean ok = false;

		String query = "SELECT linkKey, Causes, CausedBy, Aggregates, AggregatedBy, Categories from STATE where " + "LinkKey='" + linkKey + "' and "
				+ "Severity='" + severity + "' and " + "Cleared=" + cleared + " and " + "StateDescr='" + stateDescr + "'";

		logger.trace(query);

		ResultSet rs = null;
		rs = dbStatement.executeQuery(query);

		if (rs.next()) {
			HashSet<String> iCategories = getHashSet(iCategoriesStr);
			HashSet<String> iCauses = getHashSet(iCausesStr);
			HashSet<String> iCausedBy = getHashSet(iCausedByStr);
			HashSet<String> iAggregates = getHashSet(iAggregatesStr);
			HashSet<String> iAggregatedBy = getHashSet(iAggregatedByStr);

			HashSet<String> oCauses = getHashSet(rs.getString(2));
			HashSet<String> oCausedBy = getHashSet(rs.getString(3));
			HashSet<String> oAggregates = getHashSet(rs.getString(4));
			HashSet<String> oAggregatedBy = getHashSet(rs.getString(5));
			HashSet<String> oCategories = getHashSet(rs.getString(6));

			if ((iCauses.equals(oCauses)) && (iCausedBy.equals(oCausedBy)) && (iAggregates.equals(oAggregates)) && (iAggregatedBy.equals(oAggregatedBy))
					&& (iCategories.equals(oCategories))) {
				ok = true;
			}
		}

		if (rs.next()) {
			ok = false; // Should have only one
		}

		return ok;
	}

	public static void init(Statement dbStatement, PrintWriter messageServerPrintWriter, HashMapVector messageFileData) {
		TestToolKit.dbStatement = dbStatement;
		TestToolKit.messageServerPrintWriter = messageServerPrintWriter;
		TestToolKit.messageFileData = messageFileData;
	}


}
