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
 * @since 2014-11-02
 */
public class TopologyBasedCorrelationFT {

	private static String version = "0.1 (2014-10-20)";

	private static Logger logger = Logger.getLogger(TopologyBasedCorrelationFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running TopologyBasedCorrelationUT version " + version);

		try {
			Vector<String> linkKeyList = new Vector<String>();
			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK = false;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);

			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);

			logger.info("->> Test Topology Based Correlation");
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);
			
			if (resultOK) {

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);
				// 15 KrrSimple Smarts MGTA-AM-PM :::Host::server02 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(15, Globals.RAISING);
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.RAISING);
				// 25 KrrSimple Smarts MGTA-AM-PM :::Host::server07 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(25, Globals.RAISING);
				
				logger.info("Sleep ...");
				Thread.sleep(3000);
				
				
				expectedResults.clear();
				
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
	
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
				
			if (resultOK) {
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,WARNING,[Performance],isRoot=false,"
						+ "CausedBy=[server02::Unresponsive server01::Unresponsive server07::Unresponsive server03::Unresponsive "
						+ "server08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
						+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {
				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				resultOK = TestToolKit.testIsCleared("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted" /* linkKey */);
				
				if (resultOK) {
				
					expectedResults.clear();
					
					expectedResults
					.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
					expectedResults
					.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
					expectedResults
					.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
					expectedResults
					.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
							+ "CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
	
					resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
				}

			}

			if (resultOK) {
				// 29 KrrSimple Smarts MGTA-AM-PM :::Host::server09 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(29, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				
				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,WARNING,[Performance],isRoot=false,"
								+ "CausedBy=[server03::Unresponsive server09::Unresponsive server07::Unresponsive server02::Unresponsive "
								+ "server08::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}

			if (resultOK) {
				// 31 KrrSimple Smarts MGTA-AM-PM :::Host::server10 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(31, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MINOR,[Performance],isRoot=false,"
								+ "CausedBy=[server03::Unresponsive server09::Unresponsive server07::Unresponsive server02::Unresponsive "
								+ "server08::Unresponsive server10::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}


			
			if (resultOK) {
				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,"
								+ "CausedBy=[server01::Unresponsive server03::Unresponsive server09::Unresponsive server07::Unresponsive "
								+ "server02::Unresponsive server08::Unresponsive server10::Unresponsive],"
								+ "Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			if (resultOK) {
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MINOR,[Performance],isRoot=false,"
								+ "CausedBy=[server01::Unresponsive server09::Unresponsive server07::Unresponsive server02::Unresponsive "
								+ "server08::Unresponsive server10::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}


			
			if (resultOK) {
				// 33 KrrSimple Smarts MGTA-AM-PM :::Host::server11 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(33, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,"
								+ "CausedBy=[server11::Unresponsive server01::Unresponsive server09::Unresponsive server07::Unresponsive "
								+ "server02::Unresponsive server08::Unresponsive server10::Unresponsive],"
								+ "Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			
			if (resultOK) {
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[www.magenta.ca|MyProfile::Impacted MGTAWeb::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

		
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			if (resultOK) {
				// 19 KrrSimple Smarts MGTA-AM-PM :::Host::server04 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(19, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,"
								+ "CausedBy=[server03::Unresponsive server09::Unresponsive server02::Unresponsive server10::Unresponsive "
								+ "server07::Unresponsive server11::Unresponsive server01::Unresponsive],"
								+ "Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,MINOR,[Performance],isRoot=false,"
								+ "CausedBy=[server04::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");

				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			if (resultOK) {
				// 23 KrrSimple Smarts MGTA-AM-PM :::Host::server06 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(23, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,"
								+ "CausedBy=[server03::Unresponsive server09::Unresponsive server02::Unresponsive server10::Unresponsive "
								+ "server07::Unresponsive server11::Unresponsive server01::Unresponsive],"
								+ "Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
						.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,MAJOR,[HiAvailability],isRoot=false,"
								+ "CausedBy=[server04::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");

				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,"
								+ "CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			if (resultOK) {
				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				

				expectedResults.clear();
				
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|order::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server05::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			if (resultOK) {
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|order::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server10::Unresponsive server02::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server10::Unresponsive server02::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server05::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|krr::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server10::Unresponsive server02::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server10::Unresponsive server02::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|home::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server10::Unresponsive server02::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::Impacted MGTAWeb2::Impacted www.magenta.ca|order::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");				

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			if (resultOK) {
				// 48	KrrSimple	Nimsoft	Nim01	:::URL::www.magenta.ca|order	NoAnswer	MAJOR
				TestToolKit.sendMessage_simpleFormat(48, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server05::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted www.magenta.ca|krr::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|home::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|krr::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|order::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");						

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			if (resultOK) {

				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.CLEARING);				

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server04::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server07::Unresponsive server10::Unresponsive server02::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server10::Unresponsive server02::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");						

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {
				
				
				// 10 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|krr NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(10, Globals.RAISING);
				// 11 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|MyProfile NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(11, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|krr::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb2::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted MGTAWeb1::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server04::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|MyProfile::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.RAISING);				

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server05::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|home::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|home::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|krr::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|order::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|MyProfile::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|krr::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server03::Unresponsive server11::Unresponsive server06::Unresponsive server01::Unresponsive server09::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			
			if (resultOK) {

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.CLEARING);
				// 15 KrrSimple Smarts MGTA-AM-PM :::Host::server02 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(15, Globals.CLEARING);
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.CLEARING);
				// 19 KrrSimple Smarts MGTA-AM-PM :::Host::server04 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(19, Globals.CLEARING);
				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.CLEARING);
				// 23 KrrSimple Smarts MGTA-AM-PM :::Host::server06 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(23, Globals.CLEARING);
				// 25 KrrSimple Smarts MGTA-AM-PM :::Host::server07 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(25, Globals.CLEARING);
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.CLEARING);
				// 29 KrrSimple Smarts MGTA-AM-PM :::Host::server09 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(29, Globals.CLEARING);
				// 31 KrrSimple Smarts MGTA-AM-PM :::Host::server10 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(31, Globals.CLEARING);
				// 33 KrrSimple Smarts MGTA-AM-PM :::Host::server11 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(33, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(3000);
				
				expectedResults.clear();
				
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|MyProfile::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|krr::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
			}

			if (resultOK) {
				// 10 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|krr NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(10, Globals.CLEARING);
				// 11 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|MyProfile NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(11, Globals.CLEARING);
				// 48	KrrSimple	Nimsoft	Nim01	:::URL::www.magenta.ca|order	NoAnswer	MAJOR
				TestToolKit.sendMessage_simpleFormat(48, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);

			}
			
			
			if (resultOK) {
				// 9 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|home NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(9, Globals.RAISING);
				// 10 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|krr NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(10, Globals.RAISING);
				// 11 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|MyProfile NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(11, Globals.RAISING);
				// 48	KrrSimple	Nimsoft	Nim01	:::URL::www.magenta.ca|order	NoAnswer	MAJOR
				TestToolKit.sendMessage_simpleFormat(48, Globals.RAISING);

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);
				// 15 KrrSimple Smarts MGTA-AM-PM :::Host::server02 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(15, Globals.RAISING);
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.RAISING);
				// 19 KrrSimple Smarts MGTA-AM-PM :::Host::server04 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(19, Globals.RAISING);
				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.RAISING);
				// 23 KrrSimple Smarts MGTA-AM-PM :::Host::server06 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(23, Globals.RAISING);
				// 25 KrrSimple Smarts MGTA-AM-PM :::Host::server07 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(25, Globals.RAISING);
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.RAISING);
				// 29 KrrSimple Smarts MGTA-AM-PM :::Host::server09 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(29, Globals.RAISING);
				// 31 KrrSimple Smarts MGTA-AM-PM :::Host::server10 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(31, Globals.RAISING);
				// 33 KrrSimple Smarts MGTA-AM-PM :::Host::server11 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(33, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				
				
				expectedResults.clear();
				
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server02::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server03::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server04::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server05::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server06::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|order::NoAnswer www.magenta.ca|krr::NoAnswer MGTAWeb2::Impacted www.magenta.ca|order::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server09::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server10::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
				.add("Smarts::MGTA-AM-PM:::Host::server11::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[MGTAWeb::Impacted www.magenta.ca|home::Impacted www.magenta.ca|MyProfile::NoAnswer www.magenta.ca|MyProfile::Impacted www.magenta.ca|krr::NoAnswer MGTAWeb1::Impacted www.magenta.ca|krr::Impacted www.magenta.ca|home::NoAnswer],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb1::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb2::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::Service::MGTAWeb::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|MyProfile::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|home::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|krr::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("CausalityAnalyser::local:::URL::www.magenta.ca|order::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|MyProfile::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server07::Unresponsive server02::Unresponsive server10::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|home::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|krr::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server07::Unresponsive server02::Unresponsive server10::Unresponsive server05::Unresponsive server08::Unresponsive server11::Unresponsive server03::Unresponsive server06::Unresponsive server09::Unresponsive server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");
				expectedResults
				.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=false,CausedBy=[server04::Unresponsive server05::Unresponsive server06::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			
			if (resultOK) {

				// 9 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|home NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(9, Globals.CLEARING);
				// 10 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|krr NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(10, Globals.CLEARING);
				// 11 KrrSimple Nimsoft Nim01 :::URL::www.magenta.ca|MyProfile NoAnswer MAJOR
				TestToolKit.sendMessage_simpleFormat(11, Globals.CLEARING);

				// 13 KrrSimple Smarts MGTA-AM-PM :::Host::server01 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.CLEARING);
				// 15 KrrSimple Smarts MGTA-AM-PM :::Host::server02 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(15, Globals.CLEARING);
				// 17 KrrSimple Smarts MGTA-AM-PM :::Host::server03 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(17, Globals.CLEARING);
				// 19 KrrSimple Smarts MGTA-AM-PM :::Host::server04 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(19, Globals.CLEARING);
				// 21 KrrSimple Smarts MGTA-AM-PM :::Host::server05 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(21, Globals.CLEARING);
				// 23 KrrSimple Smarts MGTA-AM-PM :::Host::server06 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(23, Globals.CLEARING);
				// 25 KrrSimple Smarts MGTA-AM-PM :::Host::server07 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(25, Globals.CLEARING);
				// 27 KrrSimple Smarts MGTA-AM-PM :::Host::server08 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(27, Globals.CLEARING);
				// 29 KrrSimple Smarts MGTA-AM-PM :::Host::server09 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(29, Globals.CLEARING);
				// 31 KrrSimple Smarts MGTA-AM-PM :::Host::server10 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(31, Globals.CLEARING);
				// 33 KrrSimple Smarts MGTA-AM-PM :::Host::server11 Unresponsive MAJOR
				TestToolKit.sendMessage_simpleFormat(33, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults.add("Nimsoft::Nim01:::URL::www.magenta.ca|order::NoAnswer,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[NoAnswer]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}
			// ///////////////
			// END of testing
			// ///////////////

			messageServerPrintWriter.close();
			client.close();

			if (resultOK) {
				logger.info("->> COMPLETE ALL TESTS SUCCESSFULLY");
				System.exit(0);
			} else {
				logger.info("->> PART OF TEST FAILED");
				System.exit(1);
			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
