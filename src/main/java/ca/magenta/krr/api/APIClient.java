package ca.magenta.krr.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import ca.magenta.utils.TCPClient;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-11-25
 */
public class APIClient extends TCPClient {

	private static Logger logger = Logger.getLogger(APIClient.class);

	public APIClient() throws IOException {
		super();

	}

	public void run(BufferedReader in, PrintWriter out) {
		try {

			String inputLine;

			logger.debug("InterruptClient is starting");

			while (!shouldStop && (inputLine = in.readLine()) != null) {
				logger.debug("From server side: " + inputLine);

				try {
					//WiringPiInterruptHandlerMsg msg = (new Gson()).fromJson(inputLine, WiringPiInterruptHandlerMsg.class);

					//GpioInterruptNonNative.pinStateChangeCallback(msg.getPin(), msg.getState());
				} catch (JsonParseException e) {
					if (inputLine.equals("BYE")) {

						break;
					}
				}
			}
			logger.debug("Out of while");

			out.close();
			in.close();
			// Process the data socket here.
		} catch (Exception e) {
		}
	}

}
