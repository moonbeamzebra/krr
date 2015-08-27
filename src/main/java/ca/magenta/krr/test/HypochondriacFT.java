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
 * @since 2014-11-11
 */
public class HypochondriacFT {

	private static String version = "0.1 (2014-11-11)";

	private static Logger logger = Logger.getLogger(HypochondriacFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running HypochondriacFT version " + version);

		try {

			Vector<String> expectedResults = new Vector<String>();
			boolean resultOK;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);
			
			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);
			
			logger.info("->> Test specific correlation");
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(0 /* expectedCount */);
			if (resultOK) {

				// 5	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::pied	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(5, Globals.RAISING);
				// 6	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::tete	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.RAISING);
				// 7	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::ventre	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.RAISING);

				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				resultOK = TestToolKit.testForOnlyOneNotCleared("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");
				
			}
			
			if (resultOK)
			{
				// 6	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::tete	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.CLEARING);
				// 7	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::ventre	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.CLEARING);

				
				logger.info("Sleep ...");
				Thread.sleep(2000);
				
				resultOK = TestToolKit.testIsCleared("CausalityAnalyser::local:::Human::jpl::Hypochondriac" /* linkKey */);

			}
			
			if (resultOK)
			{
				// 7	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::ventre	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(7, Globals.RAISING);
				// 6	KrrSimple	Nimsoft	Nim01	:::Host::server01:::Application::tete	AvoirMal	MAJOR
				TestToolKit.sendMessage_simpleFormat(6, Globals.RAISING);
				
				logger.info("Sleep ...");
				Thread.sleep(2000);

				resultOK = TestToolKit.testForOnlyOneNotCleared("CausalityAnalyser::local:::Human::jpl::Hypochondriac,MINOR,[],isRoot=true,CausedBy=[],Causes=[ventre::AvoirMal pied::AvoirMal tete::AvoirMal],AggregatedBy=[],Aggregates=[],[Very Hypochondriac]");

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
