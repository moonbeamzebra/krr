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
public class ConsumerProviderFT {

	private static String version = "0.1 (2015-07-20)";

	private static Logger logger = Logger.getLogger(ConsumerProviderFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running " + ConsumerProviderFT.class.getSimpleName() + " version " + version);

		try {

			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);

			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);

			logger.info("->> Test " + ConsumerProviderFT.class.getSimpleName());
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);

			if (resultOK) {

				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();								
				
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[switch07::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);
				
			}

			if (resultOK) {

				// 45	KrrSimple	Pinger	Pinger01	:::Host::server12	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(45, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
						.add("CausalityAnalyser::local:::Host::server12::Impacted,MAJOR,[HiAvailability],isRoot=false,CausedBy=[switch07::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Losing resource]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);


			}

			if (resultOK) {
				// 44	KrrSimple	Pinger	Pinger01	:::Switch::switch08	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(44, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch07::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Switch::switch08::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[server12::Impacted],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("Pinger::Pinger01:::Host::server12::Unresponsive,MAJOR,[Availability],isRoot=false,CausedBy=[server12::Impacted],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
				expectedResults
					.add("CausalityAnalyser::local:::Host::server12::Impacted,CRITICAL,[Availability],isRoot=false,CausedBy=[switch07::Unresponsive switch08::Unresponsive],Causes=[server12::Unresponsive],AggregatedBy=[],Aggregates=[],[Losing resource]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);


			}

			if (resultOK) {
				// 43	KrrSimple	Pinger	Pinger01	:::Switch::switch07	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(43, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				System.exit(0);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal pied::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				logger.info("Test if aggregates go into root cause situation they leave aggregator");
				// 7 KrrSimple Nimsoft Nim01 :::Host::server01:::Application::ventre AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				// 6 KrrSimple Nimsoft Nim01 :::Host::server01:::Application::tete AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::ventre::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[pied::AvoirMal ventre::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				// 2 KrrSimple Nimsoft DrMaman :::Human::jpl:::Region::Dos Courbatureux MAJOR
				TestToolKit.sendMessage_simpleFormat(2, Globals.RAISING);
				// 3 KrrSimple Nimsoft DrMaman :::Human::jpl:::Region::Nez Congestionneux MAJOR
				TestToolKit.sendMessage_simpleFormat(3, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::ventre::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Congestionneux]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[Nez::Congestionneux Dos::Courbatureux pied::AvoirMal ventre::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				// 6 KrrSimple Nimsoft Nim01 :::Human::jpl:::Application::tete AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");

				expectedResults
						.add("Nimsoft::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Congestionneux]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[Nez::Congestionneux Dos::Courbatureux],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				// 4	KrrSimple	Nimsoft	DrMaman	:::Human::jpl:::Region::Yeux	Fievreux	MAJOR
				TestToolKit.sendMessage_simpleFormat(4, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");

				expectedResults
						.add("Nimsoft::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Congestionneux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Fievreux]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Grippeux,MINOR,[],isRoot=true,CausedBy=[],Causes=[Nez::Congestionneux Yeux::Fievreux Dos::Courbatureux],AggregatedBy=[],Aggregates=[],[Very Grippeux]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			
			if (resultOK) {

				// 7 KrrSimple Nimsoft Nim01 :::Host::server01:::Application::ventre AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal pied::AvoirMal],[It Is Aggregation]");

				expectedResults
						.add("Nimsoft::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Congestionneux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Fievreux]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Grippeux,MINOR,[],isRoot=true,CausedBy=[],Causes=[Nez::Congestionneux Yeux::Fievreux Dos::Courbatureux],AggregatedBy=[],Aggregates=[],[Very Grippeux]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

			}

			if (resultOK) {

				// 3	KrrSimple	Nimsoft	DrMaman	:::Human::jpl:::Region::Nez	Congestionneux	MAJOR
				TestToolKit.sendMessage_simpleFormat(3, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Fievreux]");
				expectedResults
						.add("Nimsoft::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Nimsoft::Nim01:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal Yeux::Fievreux Dos::Courbatureux pied::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);

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
