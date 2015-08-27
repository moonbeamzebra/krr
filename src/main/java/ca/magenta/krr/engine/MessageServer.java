package ca.magenta.krr.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import ca.magenta.krr.fact.Message;
import ca.magenta.utils.TCPServer;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-11
 */
public class MessageServer extends TCPServer {

	private static Logger logger = Logger.getLogger(MessageServer.class);

	public MessageServer(int port, String name) {
		super(port, name);

	}

	public void run(Socket data) {
		try {

			InetAddress clientAddress = data.getInetAddress();
			int port = data.getPort();
			logger.debug("Connected to client: " + clientAddress.getHostAddress() + ":" + port);

			BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				logger.trace("Client: " + inputLine);

				try {
					Message msg = (new Gson()).fromJson(inputLine, Message.class);

					Engine.getStreamKS().insert(msg);
					logger.trace("Message inserted WM");
					
				} catch (JsonParseException e) {
					if (inputLine.equals("BYE")) {

						break;
					}
				}
			}

			in.close();
			data.close();

			// Process the data socket here.
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
