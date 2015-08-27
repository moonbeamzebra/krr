package ca.magenta.krr.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.fact.Fact;
import ca.magenta.krr.fact.InheritanceAdapter;
import ca.magenta.krr.fact.Message;
import ca.magenta.krr.fact.StateLifecycle;
import ca.magenta.utils.HashMapVector;
import ca.magenta.utils.XLSXFile;

/**
 * @author moonbeam <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-12-03
 */
public class ListenStateLifecycle {

	private static String version = "0.1 (2014-12-03)";

	private static Logger logger = Logger.getLogger(ListenStateLifecycle.class);

	private static String apiServerAddr_opt = null;
	private static int apiServerPort_opt = -1;

	public static final void main(String[] args) {

		int rc = 0;

		logger.info("");
		logger.info("Running ListenStateLifecycle version " + version);

		rc = parseParam(args);

		if (rc == 0) {

			try {

				Socket apiServer = new Socket(apiServerAddr_opt, apiServerPort_opt);
				PrintWriter toServer = new PrintWriter(apiServer.getOutputStream(), true);

				BufferedReader fromServer = new BufferedReader(new InputStreamReader(apiServer.getInputStream()));

				String inputLine;

				Gson gsonExt = null;
				{
					GsonBuilder builder = new GsonBuilder();
					builder.registerTypeAdapter(Fact.class, new InheritanceAdapter<Fact>());
					gsonExt = builder.create();
				}

				while ((inputLine = fromServer.readLine()) != null) {
					logger.info(inputLine);

					try {
						Fact fact = gsonExt.fromJson(inputLine, Fact.class);
						logger.trace(fact.toString());
					} catch (JsonParseException e) {
						logger.error("", e);
					}

					// try {
					// StateLifecycle stateLifecycle = (new Gson()).fromJson(inputLine, StateLifecycle.class);
					//
					// logger.trace(stateLifecycle);
					//
					// } catch (JsonParseException e) {
					// logger.error("", e);
					// }
				}

				fromServer.close();
				apiServer.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private static int parseParam(String a_sArgs[]) {
		int rc = 0;

		if (a_sArgs.length > 0) {
			for (int i = 0; i < a_sArgs.length; i++) {
				if (a_sArgs[i].startsWith("-apiServerAddr=")) {
					apiServerAddr_opt = a_sArgs[i].substring(15);
					logger.info("apiServerAddr: [" + apiServerAddr_opt + "]");
				} else if (a_sArgs[i].startsWith("-apiServerPort=")) {
					String apiServerPortStr = a_sArgs[i].substring(15);
					try {
						apiServerPort_opt = Integer.parseInt(apiServerPortStr);
						logger.info("apiServerPort: [" + apiServerPort_opt + "]");

					} catch (NumberFormatException e) {
						logger.error("Bad apiServerPort: [" + apiServerPortStr + "]");
						rc = 1;
					}
				} else if (a_sArgs[i].startsWith("-")) {
					rc = 1;
				} else {
					rc = 1;
				}
			}
		}

		if ((apiServerAddr_opt == null) || (apiServerPort_opt == -1) || (rc != 0)) {
			System.err.println("Usage: ListenStateLifecycle -apiServerAddr=apiServerAddr -apiServerPort=msgServerPort");

			System.err.println("Ex:    ListenStateLifecycle -apiServerAddr=127.0.0.1 -apiServerPort=9595");

			rc = 1;
		}

		return rc;
	}

}
