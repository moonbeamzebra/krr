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
 * @since 2014-10-28
 */
public class ServerToSwitchFT {

	private static String version = "0.1 (2014-11-11)";

	private static Logger logger = Logger.getLogger(ServerToSwitchFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running ServerToSwitchUT version " + version);

		try {

			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);
			
			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);
			
			logger.info("->> Test external root cause analysis support");
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);
			if (resultOK) {

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);
				// 35 KrrSimple Smarts MGTA-AM-PM :::Switch::switch01 Down CRITICAL 0 Switch::switch01::Unresponsive, Host::server01::Unresponsive,
				TestToolKit.sendMessage_simpleFormat(35, Globals.RAISING);
				// 14 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR Switch::switch01::Down
				TestToolKit.sendMessage_simpleFormat(14, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(3000);

				resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(2 /* expectedCount */);

				if (resultOK) {
					expectedResults
							.add("Smarts::MGTA-AM-PM:::Switch::switch01::Down,CRITICAL,[Availability],isRoot=true,CausedBy=[],Causes=[server01::Unresponsive],AggregatedBy=[],Aggregates=[],[Down]");
					expectedResults
							.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=false,CausedBy=[switch01::Down],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
					resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

				}
			}

			if (resultOK) {
				TestToolKit.sendMessage_simpleFormat(35, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(1000);

				resultOK = TestToolKit.testIsCleared("Smarts::MGTA-AM-PM:::Switch::switch01::Down" /* linkKey */);

				if (resultOK) {

					logger.info("-->> Test CausalityAnalyser did not change causedBy of server01::Unresponsive");
					resultOK = TestToolKit
							.testForOnlyOneNotCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=false,CausedBy=[switch01::Down],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				}
			}

			if (resultOK) {
				logger.info("-->> Simulate update of causedBy field of server01::Unresponsive");
				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(1000);

				logger.info("-->> Test causedBy of server01::Unresponsive is now empty");
				resultOK = TestToolKit
						.testForOnlyOneNotCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");

			}

			if (resultOK) {

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(1000);

				resultOK = TestToolKit.testIsCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive" /* linkKey */);
			}
			
			if (resultOK) {

				logger.info("-->> Test the reversed order");

				// 35 KrrSimple Smarts MGTA-AM-PM :::Switch::switch01 Down CRITICAL 0 Switch::switch01::Unresponsive, Host::server01::Unresponsive,
				TestToolKit.sendMessage_simpleFormat(35, Globals.RAISING);
				// 14 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR Switch::switch01::Down
				TestToolKit.sendMessage_simpleFormat(14, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				if (resultOK) {
					expectedResults
							.add("Smarts::MGTA-AM-PM:::Switch::switch01::Down,CRITICAL,[Availability],isRoot=true,CausedBy=[],Causes=[server01::Unresponsive],AggregatedBy=[],Aggregates=[],[Down]");
					expectedResults
							.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=false,CausedBy=[switch01::Down],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
					resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

				}
			}

			if (resultOK) {

				logger.info("-->> Clear both");

				// 35 KrrSimple Smarts MGTA-AM-PM :::Switch::switch01 Down CRITICAL 0 Switch::switch01::Unresponsive, Host::server01::Unresponsive,
				TestToolKit.sendMessage_simpleFormat(35, Globals.CLEARING);
				// 14 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR Switch::switch01::Down
				TestToolKit.sendMessage_simpleFormat(14, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(1000);

				resultOK = TestToolKit.testIsCleared("Smarts::MGTA-AM-PM:::Switch::switch01::Down" /* linkKey */);

				if (resultOK) {
					resultOK = TestToolKit.testIsCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive" /* linkKey */);
				}
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
