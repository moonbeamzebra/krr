package ca.magenta.krr.test;

import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.engine.Globals;
import ca.magenta.krr.tools.SendMessage;
import ca.magenta.utils.HashMapVector;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-12-07
 */
public class NetworkTopoNoToolFT {

	private static String version = "0.1 (2016-03-20)";

	private static Logger logger = Logger.getLogger(NetworkTopoNoToolFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running " + NetworkTopoNoToolFT.class.getSimpleName() + " version " + version);

		try {

			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);

			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);

			logger.info("->> Test " + NetworkTopoNoToolFT.class.getSimpleName());
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);
			
			
			if (resultOK) {

				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.RAISING);
				// 44	KrrSimple	Pinger	Pinger01	:::Switch::switch08	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(44, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();								
				
				expectedResults
				.add("CausalityAnalyser::local:::Host::server12::Impacted,CRITICAL,[Availability],isRoot=false,"
						+ "CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch08::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				
			}

			if (resultOK) {

				// 45	KrrSimple	Pinger	Pinger01	:::Host::server12	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(45, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				
				expectedResults.clear();		
				
				expectedResults
				.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=false,"
						+ "CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Host::server12::Impacted,CRITICAL,[Availability],isRoot=false,"
						+ "CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Unresponsive server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch08::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Unresponsive server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");				
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				
			}

			if (resultOK) {

				// 44	KrrSimple	Pinger	Pinger01	:::Switch::switch08	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(44, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();	
				
				expectedResults
				.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,"
						+ "CausedBy=[switch07::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				
			}

			if (resultOK) {

				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();	
				
				expectedResults
				.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				
			}
			
			if (resultOK) {

				// 45	KrrSimple	Pinger	Pinger01	:::Host::server12	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(45, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);
				
			}			
			
			/////////////////////////////////////////////////
			// Reverse the order now
			//////////////////////////////////////////////////

			
			
			if (resultOK) {

				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();								
				
				expectedResults
				.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,"
						+ "CausedBy=[switch07::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				
			}

			if (resultOK) {

				// 45	KrrSimple	Pinger	Pinger01	:::Host::server12	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(45, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
					.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,"
							+ "CausedBy=[switch07::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
					.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}
			
			
			if (resultOK) {

				// 44	KrrSimple	Pinger	Pinger01	:::Switch::switch08	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(44, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				
				expectedResults
					.add("CausalityAnalyser::local:::Host::server12::Impacted,CRITICAL,[Availability],isRoot=false,"
							+ "CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
					.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=false,"
							+ "CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[server12::Impacted server12::Unresponsive],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch08::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[server12::Impacted server12::Unresponsive],AggregatedBy=[],Aggregates=[],[Unresponsive]");				
				

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}

			if (resultOK) {

				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
	
				expectedResults.clear();
				
				expectedResults
					.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,"
							+ "CausedBy=[switch08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
					.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch08::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}

			if (resultOK) {
				// 44	KrrSimple	Pinger	Pinger01	:::Switch::switch08	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(44, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}

			if (resultOK) {
				// 45	KrrSimple	Pinger	Pinger01	:::Host::server12	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(45, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);

			}
			

			
			
			// ///////////////
			// END of testing
			// ///////////////

			messageServerPrintWriter.close();
			client.close();

			if (resultOK) {
				logger.info("->> COMPLETE TEST SUCCESSFUL");
				System.exit(0);
			} else {
				logger.error("->> PART OF TEST FAILED");
				System.exit(1);
			}

		} catch (Throwable e) {
			logger.fatal(e.getMessage(), e);
		}

	}

}
