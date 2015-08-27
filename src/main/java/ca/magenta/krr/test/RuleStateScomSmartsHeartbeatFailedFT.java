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
public class RuleStateScomSmartsHeartbeatFailedFT {

	private static String version = "0.1 (2014-10-18)";

	private static Logger logger = Logger.getLogger(RuleStateScomSmartsHeartbeatFailedFT.class);

	private static final String MSG_SERVER_ADDR = "127.0.0.1";
	private static final int MSG_SERVER_PORT = 9292;
	private static final String messageFile_opt = "SimpleMessages.xlsx";
	private static final String DB_SERVER_ADDR = "127.0.0.1";
	private static final int DB_SERVER_PORT = 9092;

	public static void main(String[] args) {

		logger.info("");
		logger.info("Running RuleStateScomSmartsHeartbeatFailedUT version " + version);

		try {
			Vector<String> expectedResults = new Vector<String>();  
			boolean resultOK = true;

			Engine.dbConnect(DB_SERVER_ADDR, DB_SERVER_PORT, true /* dbTest */);
			Engine.dbEmpty();

			Socket client = new Socket(MSG_SERVER_ADDR, MSG_SERVER_PORT);
			PrintWriter messageServerPrintWriter = new PrintWriter(client.getOutputStream(), true);

			HashMapVector messageFileData = SendMessage.getMessageFileData(messageFile_opt);
			
			TestToolKit.init(Engine.getDB(), messageServerPrintWriter, messageFileData);

			logger.info("->> Test 'State Scom-Smarts Heartbeat failed' Drools");
			logger.info("=============================================");
			TestToolKit.simpleFormatClearAllSates();

			logger.info("Sleep 5 sec ... ");
			Thread.sleep(5000);

			// 13	KrrSimple	Smarts	MGTA-AM-PM	:::Host::server01	Unresponsive	MAJOR
			TestToolKit.sendMessage_simpleFormat(13, Globals.RAISING);
			// 12	KrrSimple	Scom	SCOM01	:::Host::server01:::Application::scomAgent	Heartbeat failed	MAJOR
			TestToolKit.sendMessage_simpleFormat(12, Globals.RAISING);

			logger.info("Sleep ...");
			Thread.sleep(1000);

			resultOK = TestToolKit.testForTotalCount_NOT_CLEARED(2 /* expectedCount */);

			if (resultOK) {
				expectedResults.add("Scom::SCOM01:::Application::scomAgent::Heartbeat_failed,MAJOR,[],isRoot=false,CausedBy=[server01::Unresponsive],Causes=[],AggregatedBy=[],Aggregates=[],[Heartbeat_failed]");
				expectedResults.add("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[scomAgent::Heartbeat_failed],AggregatedBy=[],Aggregates=[],[Unresponsive]");

				resultOK = TestToolKit.testEachExistOnceNotCleared(expectedResults);
			}

			if (resultOK) {
				// 12	KrrSimple	Scom	SCOM01	:::Host::server01:::Application::scomAgent	Heartbeat failed	MAJOR
				TestToolKit.sendMessage_simpleFormat(12, Globals.CLEARING);
				
				logger.info("Sleep ...");
				Thread.sleep(1000);
				
				resultOK = TestToolKit.testIsCleared("Scom::SCOM01:::Application::scomAgent::Heartbeat_failed");
			}

			if (resultOK) {
				resultOK = TestToolKit.testForOnlyOneNotCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive,MAJOR,[Availability],isRoot=true,CausedBy=[],Causes=[],AggregatedBy=[],Aggregates=[],[Unresponsive]");
			}
				
			if (resultOK) {
				// 13	KrrSimple	Smarts	MGTA-AM-PM	:::Host::server01	Unresponsive	MAJOR
				TestToolKit.sendMessage_simpleFormat(13, Globals.CLEARING);
				
				logger.info("Sleep ...");
				Thread.sleep(1000);
				
				if (resultOK) {
					resultOK = TestToolKit.testIsCleared("Smarts::MGTA-AM-PM:::Host::server01::Unresponsive" /* linkKey */);         
				}
			}

			
			/////////////////
			// END of testing
			/////////////////

			messageServerPrintWriter.close();
			client.close();

			if (resultOK)
			{
				logger.info("->> COMPLETE TEST SUCCESSFUL");
				System.exit(0);
			}
			else
			{
				logger.error("->> PART OF TEST FAILED");
				System.exit(1);
			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
