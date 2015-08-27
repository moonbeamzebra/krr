package ca.magenta.krr.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import ca.magenta.utils.TCPServer;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-11-25
 */
public class APIServer extends TCPServer {

	private static Logger logger = Logger.getLogger(APIServer.class);
	
    private volatile boolean doRun = true;

    public void stop() {
        doRun = false;
    }

	public APIServer(int port, String name) {
		super(port, name);
	}

	public void run(Socket data) {
		try {

			String apiServerName = this.getClass().getSimpleName() + "-" + this.getClientCount();
			String threadName = SubscriptionForwarder.class.getSimpleName() + "-" + this.getClientCount();

			InetAddress clientAddress = data.getInetAddress();
			int port = data.getPort();
			logger.trace(apiServerName + " is now connected to client: " + clientAddress.getHostAddress() + ":" + port);

			PrintWriter toClient = new PrintWriter(data.getOutputStream(), true);
			
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(data.getInputStream()));

			String inputLine;
			
			
			SubscriptionForwarder subscriptionServer = new SubscriptionForwarder(threadName, toClient);
			
			Thread subscritionServerThread = new Thread(subscriptionServer, threadName);
			subscritionServerThread.start();

			while ((inputLine = fromClient.readLine()) != null) {
				//logger.debug("Client: " + inputLine);

			}
			
			logger.trace(apiServerName + " is now disconnected from client: " + clientAddress.getHostAddress() + ":" + port);

			fromClient.close();
			toClient.close();
			data.close();
			
			subscriptionServer.stop();
			subscritionServerThread.interrupt();
			subscritionServerThread.join();
			
			logger.info(threadName + " stopped");
			
			setClientCount(getClientCount() - 1);

			// Process the data socket here.
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
