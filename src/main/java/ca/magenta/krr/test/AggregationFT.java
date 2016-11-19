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
public class AggregationFT {

	private static String version = "0.1 (2014-12-07)";

	private static Logger logger = Logger.getLogger(AggregationFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running " + AggregationFT.class.getSimpleName() + " version " + version);

		try {

			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);

			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);

			logger.info("->> Test aggregation");
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);

			if (resultOK) {

				// 5	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::pied	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(5, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				

				expectedResults.clear();
				
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());
						

			}

			if (resultOK) {

				// 6	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::tete	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal pied::AvoirMal],[It Is Aggregation]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {
				// 5	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::pied	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(5, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());


			}

			if (resultOK) {
				// 5	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::pied	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(5, Globals.RAISING);
				
				
				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();

				expectedResults
				.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal pied::AvoirMal],[It Is Aggregation]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				logger.info("Test if aggregates go into root cause situation they leave aggregator");
				// 7	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::ventre	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				//System.exit(0);
				

				expectedResults.clear();
				expectedResults
						.add("SweetHome::DrMaman:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 6 KrrSimple SweetHome DrMaman :::Human::jpl:::Application::tete AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);


				expectedResults.clear();
				expectedResults
						.add("SweetHome::DrMaman:::Application::ventre::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[pied::AvoirMal ventre::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 2 KrrSimple SweetHome DrMaman :::Human::jpl:::Region::Dos Courbatureux MAJOR
				TestToolKit.sendMessage_simpleFormat(2, Globals.RAISING);
				// 3 KrrSimple SweetHome DrMaman :::Human::jpl:::Region::Nez Congestionneux MAJOR
				TestToolKit.sendMessage_simpleFormat(3, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);


				
				expectedResults.clear();
				expectedResults
						.add("SweetHome::DrMaman:::Application::ventre::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
						.add("SweetHome::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
						.add("SweetHome::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Congestionneux]");
				expectedResults
						.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[Nez::Congestionneux Dos::Courbatureux pied::AvoirMal ventre::AvoirMal],[It Is Aggregation]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 6 KrrSimple SweetHome DrMaman :::Human::jpl:::Application::tete AvoirMal MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				expectedResults
				.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[Dos::Courbatureux Nez::Congestionneux],[It Is Aggregation]");
				expectedResults
				.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[tete::AvoirMal ventre::AvoirMal pied::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Congestionneux]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 4	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Region::Yeux	Fievreux	MAJOR
				TestToolKit.sendMessage_simpleFormat(4, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				
				expectedResults
				.add("CausalityAnalyser::local:::Human::jpl::Grippeux,MINOR,[],isRoot=true,CausedBy=[],Causes=[Dos::Courbatureux Nez::Congestionneux Yeux::Fievreux],AggregatedBy=[],Aggregates=[],[Very Grippeux]");
				expectedResults
				.add("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[tete::AvoirMal ventre::AvoirMal pied::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::ventre::AvoirMal,MAJOR,[],isRoot=false,CausedBy=[jpl::Hypochondriac],Causes=[],AggregatedBy=[],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Courbatureux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Congestionneux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Fievreux]");
				
				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			if (resultOK) {

				// 7	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::ventre	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();
				
				expectedResults
				.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal pied::AvoirMal],[It Is Aggregation]");
				expectedResults
				.add("CausalityAnalyser::local:::Human::jpl::Grippeux,MINOR,[],isRoot=true,CausedBy=[],Causes=[Dos::Courbatureux Nez::Congestionneux Yeux::Fievreux],AggregatedBy=[],Aggregates=[],[Very Grippeux]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Courbatureux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Nez::Congestionneux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Congestionneux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=false,CausedBy=[jpl::Grippeux],Causes=[],AggregatedBy=[],Aggregates=[],[Fievreux]");
				

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			if (resultOK) {

				// 3	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Region::Nez	Congestionneux	MAJOR
				TestToolKit.sendMessage_simpleFormat(3, Globals.CLEARING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				expectedResults.clear();

				expectedResults
				.add("Aggregator::local:::Human::jpl::IsAggregating,CRITICAL,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[tete::AvoirMal Dos::Courbatureux Yeux::Fievreux pied::AvoirMal],[It Is Aggregation]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::pied::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Application::tete::AvoirMal,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[AvoirMal]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Dos::Courbatureux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Courbatureux]");
				expectedResults
				.add("SweetHome::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[jpl::IsAggregating],Aggregates=[],[Fievreux]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}


			if (resultOK) {

				// 2 KrrSimple SweetHome DrMaman :::Human::jpl:::Region::Dos Courbatureux MAJOR
				TestToolKit.sendMessage_simpleFormat(2, Globals.CLEARING);
				// 5	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::pied	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(5, Globals.CLEARING);
				// 6	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Application::tete	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.CLEARING);


				logger.info("Sleep ...");
				Thread.sleep(2000);

				expectedResults.clear();
				
				expectedResults
				.add("SweetHome::DrMaman:::Region::Yeux::Fievreux,MAJOR,[],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Fievreux]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults, expectedResults.size());

			}

			
			if (resultOK) {

				// 4	KrrSimple	SweetHome	DrMaman	:::Human::jpl:::Region::Yeux	Fievreux	MAJOR
				TestToolKit.sendMessage_simpleFormat(4, Globals.CLEARING);

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
