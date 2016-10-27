package ca.magenta.krr.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.kie.api.runtime.rule.FactHandle;


import ca.magenta.krr.api.APIServer;
import ca.magenta.krr.common.KS;
import ca.magenta.krr.connector.common.Publisher;
import ca.magenta.krr.connector.common.Subscriber;
import ca.magenta.krr.data.EventCategory;
import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.fact.Fact;
import ca.magenta.krr.fact.State;
import ca.magenta.krr.ruleEngin.CausalityAnalyser;
import ca.magenta.neo4j.Neo4jManager;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class Engine {

	public static Logger logger = Logger.getLogger(Engine.class);

	private static Neo4jManager graphDB = null;
	
	private static Publisher stateChangePublisher = new Publisher("StateChangePublisher");
	private static APIServer apiServer = null;

	
	private static KS streamKS = null;

	private static ManagedNodeCache managedNodeCache = new ManagedNodeCache();

	private static HashMap<ManagedNode, HashSet<FactHandle>> statesByNode = new HashMap<ManagedNode, HashSet<FactHandle>>();
	private static HashMap<String, FactHandle> statesByLinkKey = new HashMap<String, FactHandle>();

	private static HashMap<String, EventCategory> eventCategoryMappings = null;

	private static Connection connection = null;
	private static Statement statement = null;

	private static MessageServer messageServer = null;

	public static void startMessageServer() {

		try {

			if ((messageServer == null) || (!messageServer.getRunner().isAlive())) {
				messageServer = new MessageServer(9292,"MsgSrvr");
				logger.info("Starting MessageServer...");
				messageServer.startServer();
				logger.info("MessageServer started");
			}
		} catch (IOException e) {

			logger.error("", e);
		}
	}

	public static void stopMessageServer() {
		try {
			messageServer.stopServer();
		} catch (Exception ex) {
		}
	}

	public static void startAPIServer() {

		try {

			if ((apiServer == null) || ( ! apiServer.getRunner().isAlive())) {
				apiServer = new APIServer(Globals.API_SERVER_PORT,"APISrvr");
				logger.info("Starting APIServer...");
				apiServer.startServer();
				logger.info("APIServer started");
			}
		} catch (IOException e) {

			logger.error("", e);
		}
	}

	public static void stopAPIServer() {
		try {
			apiServer.stopServer();
		} catch (Exception ex) {
		}
	}

	
	
	public synchronized static void registerState(FactHandle factHandle, ManagedNode managedNode, String linkKey) {
		HashSet<FactHandle> factHandleHashSet = statesByNode.get(managedNode);
		if (factHandleHashSet == null) {
			factHandleHashSet = new HashSet<FactHandle>();
		}

		factHandleHashSet.add(factHandle);

		statesByNode.put(managedNode, factHandleHashSet);
		
		statesByLinkKey.put(linkKey, factHandle);
	}

	public synchronized static void unregisterState(FactHandle factHandle, ManagedNode managedNode, String linkKey) {
		HashSet<FactHandle> factHandleHashSet = statesByNode.get(managedNode);
		if (factHandleHashSet != null) {
			if (factHandleHashSet.contains(factHandle)) {
				factHandleHashSet.remove(factHandle);
			}
		}
		
		statesByLinkKey.remove(linkKey);
	}

	public static void neo4jRegister(String hostname, int port, String user, String password) {
		graphDB = Neo4jManager.getInstance(hostname, port, user, password);
	}
	
	public static void dbConnect(String hostname, int port, boolean dbTest) throws SQLException {
		
		String url = "jdbc:h2:tcp://" + hostname + ":" + port + "/./krr";
		if (dbTest)
			url += "Test";
		connection = DriverManager.getConnection(url, "sa", "");
		
		
		//connection = DriverManager.getConnection("jdbc:h2:tcp://centos1/~/test", "sa", "");
		//connection = DriverManager.getConnection("jdbc:h2:tcp://centos1:8085/ia", "sa", "");
		// connection = DriverManager.getConnection("jdbc:h2:~/test");
		statement = connection.createStatement();
	}

	public static void dbDisconnect() throws SQLException {
		statement.close();
		connection.close();
	}
	
	public static Neo4jManager getGraphDB() {
		return graphDB;
	}

	public static Statement getDB() {
		return statement;
	}

	public static KS getStreamKS() {
		return streamKS;
	}
	
	public static void setStreamKS(KS streamKS) {
		Engine.streamKS = streamKS;
	}
	
	public static ManagedNode getManagedNode(ManagedEntity managedEntity) {

		return managedNodeCache.getManagedNode(managedEntity, graphDB);
	}	

	public static ManagedNode getManagedNode(String fqdName) {
		return managedNodeCache.getManagedNode(fqdName);
	}

	public static ManagedNode getManagedNode(URI selfURI) {
		return managedNodeCache.getManagedNode(selfURI, graphDB);
	}

	public static void startImpactAnalyser() {

		CausalityAnalyser.start(4, 100 /* millis */, 10 /* millis */);

	}

	public static void stopImpactAnalyser() {

		try {
			CausalityAnalyser.stop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	synchronized public static Vector<State> getStateByNodeByCategory(ManagedNode mn, String stateCategory) {

		// TODO Use getStateByNode
		Vector<State> states = new Vector<State>();

		HashSet<FactHandle> allStateHandlesForANode = statesByNode.get(mn);

		if (allStateHandlesForANode != null) {
			for (FactHandle handle : allStateHandlesForANode) {
				Fact fact = streamKS.getFact(handle);
				if (fact instanceof State) {
					State state = (State) fact;

					HashSet<String> categories = state.getCategories();

					if (categories.contains(stateCategory)) {
						states.add(state);
					}
				}
			}
		}
		return states;
	}

	synchronized public static Vector<State> getStateByNode(ManagedNode mn) {

		Vector<State> states = new Vector<State>();

		HashSet<FactHandle> allStateHandlesForANode = statesByNode.get(mn);

		if (allStateHandlesForANode != null) {
			for (FactHandle handle : allStateHandlesForANode) {
				Fact fact = streamKS.getFact(handle);
				if (fact instanceof State) {
					State state = (State) fact;
					states.add(state);
				}
			}
		}
		return states;
	}
	
	public static FactHandle getStateByLinkKey(String linkKey) {

		return statesByLinkKey.get(linkKey);
	}
	
	public static void setEventCategoryMappings(HashMap<String, EventCategory> a_eventCategoryMappings) {
		eventCategoryMappings = a_eventCategoryMappings;
	}

	public static EventCategory getEventCategoryByCategorySignature(String categorySignature) {
		return eventCategoryMappings.get(categorySignature);
	}

	public static void dumpStates() {
		
		try {
			PrintStream out = new PrintStream(new FileOutputStream("dump.txt", false /* no append */), true /* autoflush */);
			//out.println("ID,Severity,Category,IsRoot,CausedBy,Causes,AggregatedBy,Aggregates");


			for (Entry<ManagedNode, HashSet<FactHandle>> entry : statesByNode.entrySet()) {
				HashSet<FactHandle> handles = entry.getValue();
				for (FactHandle factHandle : handles) {
					Fact fact = streamKS.getFact(factHandle);
	
					if (fact instanceof State) {
						State state = (State) fact;
//						logger.info(state.getLinkKey() + "," + state.getSeverity().toString() + "," + state.isRoot() + "," + state.getShortDescr() + ","
//								+ (new Gson()).toJson(state.getCategories()));
//						logger.info("  Caused by: " + state.causedByToString());
//						logger.info("  Impacts: " + state.causesToString());
//						logger.info("  AggregatedByr: " + state.aggregatedByToString());
//						logger.info("  Aggregates: " + state.aggregatesToString());
						
						out.println(state.getLinkKey() + "," + 
								state.getSeverity().toString() + "," +
								state.categoriesToString() + 
								",isRoot=" + state.isRoot() + 
								",CausedBy=" + state.causedByToString() +
								",Causes=" + state.causesToString() +
								",AggregatedBy=" + state.aggregatedByToString() +
								",Aggregates=" + state.aggregatesToString() +
								",[" + state.getShortDescr() + "]");
					}
				}
			}
			
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	


	public static void dbEmpty() {
		try {
			ResultSet result = null;
		
			getDB().executeUpdate("delete FROM STATE;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Publisher stateChangePublisher() {
		
		return stateChangePublisher;
	}
}
