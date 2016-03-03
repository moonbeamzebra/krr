package ca.magenta.krr;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import ca.magenta.krr.common.KS;
import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.DependencyRule;
import ca.magenta.krr.data.DependencyRuleByCathegory;
import ca.magenta.krr.data.EventCategory;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.engine.Engine;
import ca.magenta.krr.engine.Globals;
import ca.magenta.krr.ruleEngin.FlappingDetector;
import ca.magenta.krr.ruleEngin.FlappingDetectorConfig;
import ca.magenta.utils.HashMapVector;
import ca.magenta.utils.XLSXFile;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-01-01
 */

// Knowledge Representation and Reasoning (KKR)

// To compile
// mvn clean install

// Pour partir neo4j:
// cd /Users/jplaberge/softwares/neo4j-community-2.1.2/bin
// ./neo4j start 

/*
// Pour partir H2:
// cd /Users/jplaberge/softwares/h2/bin
// java -cp ./h2-1.3.175.jar org.h2.tools.Server  -tcpAllowOthers
// Interresting command : java -cp ./h2-1.3.175.jar org.h2.tools.Server -?
 * 
 * 

WEB H2:
http://127.0.0.1:8082
BD de test:
jdbc:h2:~/krrTest
*/


/* Comment creer les nodes:

java -classpath \
./target/ca.magenta.krr.common-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
ca.magenta.krr.tools.Neo4jManip \
-cleanNodesRels \
-neo4jHost=127.0.0.1 \
-neo4jPort=7474 \
-managedNodeFile=AutoManagedNodes.xlsx \
-managedNodeFile=ManManagedNodes.xlsx \
-relationFile=ManRelations.xlsx

*/

/* Web of Neo4j
http://localhost:7474/

START mn=node(*) RETURN mn

*/

// Just Neo4j
// java -classpath ./target/ca.magenta.causalityAnalyser-0.0.1-SNAPSHOT-jar-with-dependencies.jar ca.magenta.krr.KRR
// -neo4jHost=127.0.0.1 -neo4jPort=7474 -dbHost=127.0.0.1 -dbPort=9092 -dependencyRuleFile=./DependencyRule.xlsx

// Neo4j + Database
// java -classpath ./target/ca.magenta.causalityAnalyser-0.0.1-SNAPSHOT-jar-with-dependencies.jar ca.magenta.krr.KRR
// -neo4jHost=127.0.0.1 -neo4jPort=7474 -dbHost=127.0.0.1 -dbPort=9092 -dependencyRuleFile=./DependencyRule.xlsx

/* Neo4j + Test Database
java -classpath ./target/ca.magenta.krr.common-0.0.1-SNAPSHOT-jar-with-dependencies.jar  ca.magenta.krr.KRR -neo4jHost=127.0.0.1 -neo4jPort=7474 -dbHost=127.0.0.1 -dbPort=9092 -dbTest -dependencyRuleFile=./DependencyRule.xlsx

java -classpath \
./target/ca.magenta.krr.common-0.0.1-SNAPSHOT-jar-with-dependencies.jar  \
ca.magenta.krr.KRR \
-neo4jHost=127.0.0.1 \
-neo4jPort=7474 \
-dbHost=127.0.0.1 \
-dbPort=9092 \
-dbTest \
-dependencyRuleFile=./DependencyRule.xlsx


*/

public class KRR implements Runnable {


	private static Logger logger = Logger.getLogger(KRR.class);

	public KRR() throws IOException {
		super();
		this.props = loadKRRProperties();
	}

	public KRR(KRRProps options) {
		super();
		this.props = options;
	}

	public final static String VERSION = "0.1 (2014-01-01)";

	private KRR.KRRProps props = null;
	
	private Thread thread = null;
	
    private volatile boolean doRun = true;

    public void stop() {
        doRun = false;
        thread.interrupt();
        try {
			thread.join();
		} catch (InterruptedException e) {
			logger.error("", e);
		}
    }
    
	public void start()
	{
		doRun = true;
		thread = new Thread(this, this.getClass().getSimpleName());
    	thread.start();
    	logger.trace(thread.toString() + " started; version " + VERSION);
	}
	
	public boolean isAlive() {
		return thread.isAlive();
	}

	private static HashMap<String, DependencyRuleByCathegory> dependencyRuleByCathegoryByNode = new HashMap<String, DependencyRuleByCathegory>();


	public void run() {

		try {

			HashMapVector v = new HashMapVector(new XLSXFile(props.dependencyRuleFile));
			for (HashMap<String, String> rowData : v) {
				String enable = rowData.get("enable");
				if ((enable != null) && enable.toUpperCase().equals("YES")) {
					DependencyRule dependencyRule = new DependencyRule(rowData);
					logger.trace(dependencyRule.toString());
					addDependencyRuleByCathegoryByNode(dependencyRule);
					// ManagedNode node = nodes.get(dependencyRule.getNodeFqdName());
					// node.addDependencyRule(dependencyRule);
				}

			}

			ManagedNode.setDependencyRuleByCathegoryByNode(dependencyRuleByCathegoryByNode);

			// ////////////////////////////////

			HashMap<String, EventCategory> eventCategoryMappings = new HashMap<String, EventCategory>();
			HashMapVector eventCategories = new HashMapVector(new XLSXFile("./Event_Category.xlsx"));
			for (HashMap<String, String> rowData : eventCategories) {
				EventCategory eventCategory = new EventCategory(rowData);
				logger.trace("eventCategory:" + eventCategory.toString());
				String key = eventCategory.getSourceType() + "::" + eventCategory.getVeryShortDescr();
				eventCategoryMappings.put(key, eventCategory);
			}
			Engine.setEventCategoryMappings(eventCategoryMappings);

			Engine.neo4jRegister(props.neo4jHost, props.neo4jPort, props.neo4jUser, props.neo4jPassword);

			if (props.dbHost != null) {
				Engine.dbConnect(props.dbHost, props.dbPort, props.dbTest);
			}

			Engine.startImpactAnalyser();
			Engine.setStreamKS(new KS("StreamKS", 250));
			Engine.getStreamKS().insert(new FlappingDetectorConfig(FlappingDetector.EPISODIC_LABEL, 30000, 30000, 1, 2, Severity.WARNING));
			Engine.getStreamKS().insert(new FlappingDetectorConfig(FlappingDetector.INTERMITTENT_LABEL, 3600000, 3600000, 5, 6, Severity.MINOR));
			// Engine.setNodes(nodes);

			Engine.startMessageServer();
			Engine.startAPIServer();

			// TickerGenerator.start(5000); // A ticker every 5 seconds

			while (doRun) {
				logger.trace("Sleep a bit...");
				try
				{
					Thread.sleep(3000);
					Engine.dumpStates();
				}
				catch (InterruptedException e)
				{
					logger.trace("", e);
				}
			}
			
			logger.debug(thread.toString() + " ending");

		} catch (Throwable t) {
			logger.error("", t);
		}

	}

	public static void addDependencyRuleByCathegoryByNode(DependencyRule dependencyRule) {
		DependencyRuleByCathegory dependencyRuleByCathegory = dependencyRuleByCathegoryByNode.get(dependencyRule.getNodeFqdName());

		if (dependencyRuleByCathegory == null) {
			dependencyRuleByCathegory = new DependencyRuleByCathegory();
		}

		dependencyRuleByCathegory.addDependencyRule(dependencyRule);

		dependencyRuleByCathegoryByNode.put(dependencyRule.getNodeFqdName(), dependencyRuleByCathegory);
	}

	public static final void main(String[] args) {

		int rc = 0;

		logger.info("");
		logger.info("Running KRR version " + VERSION);

		logger.info("PWD: " + System.getProperty("user.dir"));

		KRR.KRRProps paramOptions = parseParam(args);

		if (rc == 0) {

			KRR krr = new KRR(paramOptions);

			krr.run();
		}
	}
	
	private static KRRProps parseParam(String a_sArgs[]) {

		KRR.KRRProps paramsOptions = new KRRProps();

		if (a_sArgs.length > 0) {
			for (int i = 0; i < a_sArgs.length; i++) {
				if (a_sArgs[i].startsWith("-neo4jHost=")) {
					paramsOptions.neo4jHost = a_sArgs[i].substring(11);
					logger.info("neo4jHost: [" + paramsOptions.neo4jHost + "]");
				} else if (a_sArgs[i].startsWith("-neo4jPort=")) {
					String neo4jPortStr = a_sArgs[i].substring(11);
					try {
						paramsOptions.neo4jPort = Integer.parseInt(neo4jPortStr);
						logger.info("neo4jPort: [" + paramsOptions.neo4jPort + "]");

					} catch (NumberFormatException e) {
						logger.error("Bad neo4jPort: [" + paramsOptions.neo4jPort + "]");
						paramsOptions = null;
					}
				} else if (a_sArgs[i].startsWith("-dbHost=")) {
					paramsOptions.dbHost = a_sArgs[i].substring(8);
					logger.info("dbHost: [" + paramsOptions.dbHost + "]");
				} else if (a_sArgs[i].startsWith("-dbPort=")) {
					String dbPortStr = a_sArgs[i].substring(8);
					try {
						paramsOptions.dbPort = Integer.parseInt(dbPortStr);
						logger.info("dbPort: [" + paramsOptions.dbPort + "]");

					} catch (NumberFormatException e) {
						logger.error("Bad dbPortStr: [" + dbPortStr + "]");
						paramsOptions = null;
					}
                } else if (a_sArgs[i].startsWith("-neo4jUser=")) {
                	paramsOptions.neo4jUser = a_sArgs[i].substring(11);
                    logger.info("neo4jUser: [" + paramsOptions.neo4jUser + "]");
                } else if (a_sArgs[i].startsWith("-neo4jPassword=")) {
                	paramsOptions.neo4jPassword = a_sArgs[i].substring(15);
                    logger.info("neo4jPassword: [" + paramsOptions.neo4jPassword + "]");
				} else if (a_sArgs[i].startsWith("-dbTest")) {
					paramsOptions.dbTest = true;
					logger.info("dbTest: [" + paramsOptions.dbTest + "]");
				} else if (a_sArgs[i].startsWith("-dependencyRuleFile=")) {
					paramsOptions.dependencyRuleFile = a_sArgs[i].substring(20);
					logger.info("dependencyRuleFile: [" + paramsOptions.dependencyRuleFile + "]");
				} else if (a_sArgs[i].startsWith("-")) {
					paramsOptions = null;
				} else {
					paramsOptions = null;
				}
			}
		}

		if ((paramsOptions == null) || (paramsOptions.neo4jHost == null) || (paramsOptions.neo4jPort == -1) || (paramsOptions.neo4jUser == null) || (paramsOptions.neo4jPassword == null) || (paramsOptions.dependencyRuleFile == null)) {
			System.err
					.println("Usage: KKR -neo4jHost=neo4jHost -neo4jPort=neo4jPort -neo4jUser=neo4jUser -neo4jPassword=neo4jPassword -dependencyRuleFile=dependencyRuleFile [-dbHost=dbHost -dbPort=dbPort [-dbTest]]");
			System.err
					.println("Ex:    KKR -neo4jHost=127.0.0.1 -neo4jPort=7474 -neo4jUser=neo4j -neo4jPassword=lab1 -dbHost=julia -dbPort=9393 -dependencyRuleFile=../ca.magenta.correlation.tools/DependencyRule.xlsx");
			System.err
					.println("Ex:    KKR -neo4jHost=127.0.0.1 -neo4jPort=7474 -neo4jUser=neo4j -neo4jPassword=lab1 -dbHost=127.0.0.1 -dbPort=9092 -dependencyRuleFile=../ca.magenta.correlation.tools/DependencyRule.xlsx");
			System.err
					.println("Ex:    KKR -neo4jHost=127.0.0.1 -neo4jPort=7474 -neo4jUser=neo4j -neo4jPassword=lab1 -dbHost=127.0.0.1 -dbPort=9092 -dbTest -dependencyRuleFile=../ca.magenta.correlation.tools/DependencyRule.xlsx");

			paramsOptions = null;
		}

		return paramsOptions;
	}


	
    private  static KRRProps loadKRRProperties() throws IOException {

    	KRRProps krrProps = null;
    	
        Properties krrProperties = new Properties();

        InputStream propsFile = null;

        propsFile = Globals.class.getClassLoader().getResourceAsStream(Globals.KRR_PROPERTY_FILE_NAME);
        if (propsFile != null) {
        	krrProperties.load(propsFile);
        	
        	krrProps = new KRRProps(krrProperties);
        	
        }
        
        return krrProps;

    }

	private static class KRRProps {

		public KRRProps() {
			
		}

		public KRRProps(Properties properties) {
			
			this();
			
			neo4jHost = properties.getProperty("neo4jHost", Globals.DEFAULT_NEO4J_HOST);
			Globals.logger.info("neo4jHost: [" + neo4jHost + "]");
			String neo4jPortStr = properties.getProperty("neo4jPort", Globals.DEFAULT_NEO4J_PORT);
			try {
				neo4jPort = Integer.parseInt(neo4jPortStr);
				Globals.logger.info("neo4jPort: [" + neo4jPort + "]");
	
			} catch (NumberFormatException e) {
				Globals.logger.error("Bad neo4jPort: [" + neo4jPortStr + "]");
				throw e;
			}
			
			dbHost = properties.getProperty("dbHost", Globals.DEFAULT_DB_HOST);
			String dbPortStr = properties.getProperty("dbPort", Globals.DEFAULT_DB_PORT);
			try {
				dbPort = Integer.parseInt(dbPortStr);
				Globals.logger.info("dbPort: [" + dbPort + "]");
	
			} catch (NumberFormatException e) {
				Globals.logger.error("Bad dbPort: [" + dbPortStr + "]");
				throw e;
			}
			
			String dbTestStr = properties.getProperty("dbTest", Globals.DEFAULT_DB_TEST_VALUE);
			dbTest = Boolean.parseBoolean(dbTestStr);
			Globals.logger.info("dbTest: [" + dbTest + "]");
	
			dependencyRuleFile = properties.getProperty("dependencyRuleFile");
			Globals.logger.info("dependencyRuleFile: [" + dependencyRuleFile + "]");
				
		}
		public String neo4jHost = null;
		public int neo4jPort = -1;
		public String neo4jUser = null;
		public String neo4jPassword = null;
		public String dbHost = null;
		public int dbPort = -1;
		public boolean dbTest = true;
		public String dependencyRuleFile = null;
	
	}

    
}
