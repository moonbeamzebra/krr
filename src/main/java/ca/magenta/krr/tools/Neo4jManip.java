package ca.magenta.krr.tools;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import ca.magenta.krr.data.DependencyRule;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.neo4j.Neo4jManager;
import ca.magenta.utils.HashMapVector;
import ca.magenta.utils.XLSXFile;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-18
 */
public class Neo4jManip {
	
	private static String version = "0.1 (2014-04-18)";

	private static Logger logger = Logger.getLogger(Neo4jManip.class);
	
	private static Neo4jManager graphDB = null;
	
	private static String neo4jHost = null;
	private static int neo4jPort = -1;
	private static Vector<String> managedNodeFiles = new Vector<String>();
	private static String relationFile = null;
	private static boolean cleanNodesRels = false;
	
	private static HashMap<String, ManagedNode> nodes = new HashMap<String, ManagedNode>(); 

	public static final void main(String[] args) {
		
		int rc = 0;
		
		logger.info("");
        logger.info("Running Neo4jManip version " + version);

        logger.info("PWD: " + System.getProperty("user.dir"));
        
        rc = parseParam(args);
        
        
        
        if (rc == 0) {
        	
			try {
				graphDB = Neo4jManager.getInstance(neo4jHost, neo4jPort);
				
				if (cleanNodesRels)
				{
					graphDB.doCypherQuery("start r=relationship(*) delete r");
					graphDB.doCypherQuery("start n=node(*) delete n");
				}
				
				
				// TODO determine if index already exist
		        //listIndexes( "ManagedNode");
				String key = "fqdName";
				String keys[] = {key};
				if (!graphDB.isSpecificUniquenessConstraint(ManagedNode.DEFAULT_INDEX, key))
				{
			        graphDB.createUniquenessConstraint(ManagedNode.DEFAULT_INDEX, keys);
			        
				}

				for (String managedNodeFile : managedNodeFiles)
				{
					if (managedNodeFile != null)
					{
						HashSet<String> types = new HashSet<String>();
						HashMapVector v = new HashMapVector(new XLSXFile(managedNodeFile));
						for (HashMap<String, String> rowData : v) {
							String enable = rowData.get("Enable");
							if ((enable != null) && enable.toUpperCase().equals("YES"))
							{
								ManagedNode m = new ManagedNode(rowData);
								ManagedNode mn = getManagedNode(m.getType(), m.getFqdName());
								//ManagedNode mn = ManagedNode.getInstance(rowData, graphDB);
								
								if (!mn.existsInGraphDB())
						        {
									graphDB.createNode(mn);
									types.add(mn.getType());
						        }
								else
								{
						        	logger.error(String.format("[%s] already exist", mn.toString()));
						        }
							}
						}
					    
				        for (String type : types)
				        {
				        	graphDB.createIndexIfNotExist(type, keys);
				        }
					}
				}

				if (relationFile != null)
				{
					HashMapVector v = new HashMapVector(new XLSXFile(relationFile));
					for (HashMap<String, String> rowData : v) {
						String enable = rowData.get("Enable");
						if ((enable != null) && enable.toUpperCase().equals("YES"))
						{
							String start = rowData.get("start");
							if (start != null)
							{
					        	logger.info(String.format("%s", start));
								ManagedNode mnStart = getManagedNode(null, start);
					        	if (mnStart.existsInGraphDB())
					        	{
					        		String end = rowData.get("end");
					        		logger.info(String.format("%s", end));
									ManagedNode mnEnd = getManagedNode(null,end);
					        		//ManagedNode mnEnd = ManagedNode.getInstance(r.getEnd(), graphDB);
					        		if (mnEnd.existsInGraphDB())
						        	{
					        			String type = rowData.get("type");
					        			if (type != null)
					        			{
						        			HashMap<String, Object> properties = new HashMap<String, Object>(); 
						        			properties.put("weight", Float.valueOf(rowData.get("weight")));
						        			properties.put("category", rowData.get("category"));
						        			properties.put("owner", rowData.get("owner"));
							        		logger.info(String.format("%s", mnEnd.toString()));
							        		graphDB.createRelationship(mnStart, mnEnd,type, properties);
					        			}
						        	}
					        	}
							}
						}
					}
				}

///////////////////////////////////////////////////////
//				Hold on DependencyRule.xlsx in Neo4j
///////////////////////////////////////////////////////
//				HashSet<String> nodesToUpdate = new HashSet<String>(); 
//				HashMapVector v = new HashMapVector(new XLSXFile("./DependencyRule.xlsx"));
//				for (HashMap<String, String> rowData : v) {
//					String enable = rowData.get("enable");
//					if ((enable != null) && enable.toUpperCase().equals("YES"))
//					{
//						DependencyRule dependencyRule = new DependencyRule(rowData);
//						logger.trace(dependencyRule.toString());
//						ManagedNode node = getManagedNode(null, dependencyRule.getNodeFqdName());
//						//ManagedNode node = ManagedNode.getInstance(dependencyRule.getNodeFqdName(), graphDB);
//						node.addDependencyRule(dependencyRule);
//						nodesToUpdate.add(node.getFqdName());
//					}				
//
//				}
//				for ( String fqdName  : nodesToUpdate)
//				{
//					ManagedNode node = getManagedNode(null, fqdName);
//					logger.trace("Update node:" + fqdName );
//					//graphDB.addProperty( node.getSelfURI(), "dependencyRuleByCathegory", node.getDependencyRuleByCathegoryJSON().replace("\"", "'"));
//					graphDB.addProperty( node.getSelfURI(), "dependencyRuleByCathegory", node.getDependencyRuleByCathegoryToJSON());
//					
//				}
				
				graphDB.doCypherQuery("MATCH (s:Service { fqdName:'MECWeb1' })-[r]-(n) RETURN s, r, n");
			} catch (Throwable t) {
				t.printStackTrace();
			}
        }
	}
	
	public static ManagedNode getManagedNode(String type, String fqdName) {
		ManagedNode managedNode =  nodes.get(fqdName);
		
		if (managedNode == null)
		{
			managedNode = ManagedNode.getInstance(type, fqdName, graphDB);
			
			if (managedNode.existsInGraphDB())
			{
				nodes.put(fqdName, managedNode);
			}
		}
		
		return managedNode;
	}


	
    private static int parseParam(String a_sArgs[]) {
        int rc = 0;

        if (a_sArgs.length > 0) {
            for (int i = 0; i < a_sArgs.length; i++) {
                if (a_sArgs[i].startsWith("-neo4jHost=")) {
                	neo4jHost = a_sArgs[i].substring(11);
                    logger.info("neo4jHost: [" + neo4jHost + "]");
                } else if (a_sArgs[i].startsWith("-neo4jPort=")) {
                	String msgServerPortStr = a_sArgs[i].substring(11);
                	try {
                		neo4jPort = Integer.parseInt(msgServerPortStr);
                		logger.info("msgServerPort: [" + neo4jPort + "]");
                		
                	}
                	catch (NumberFormatException e)
                	{
                		logger.error("Bad msgServerPort: [" + neo4jPort + "]");
                		rc = 1;
                	}
                } else if (a_sArgs[i].startsWith("-cleanNodesRels")) {
                	cleanNodesRels = true;
                    logger.info("cleanNodesRels: [" + cleanNodesRels + "]");
                } else if (a_sArgs[i].startsWith("-relationFile=")) {
                	relationFile = a_sArgs[i].substring(14);
                    logger.info("relationFile: [" + relationFile + "]");
                } else if (a_sArgs[i].startsWith("-managedNodeFile=")) {
                	String managedNodeFile = a_sArgs[i].substring(17);
                    logger.info("managedNodeFile: [" + managedNodeFile + "]");
                    managedNodeFiles.add(managedNodeFile);
                } else if (a_sArgs[i].startsWith("-")) {
                    rc = 1;
                } else {
                	rc = 1;
                }
            }
        }

        if ((neo4jHost == null) || (neo4jPort == -1)
               || (rc != 0)) {
            System.err
                    .println("Usage: Neo4jManip [-cleanNodesRels] -neo4jHost=neo4jHost -neo4jPort=neo4jPort { [-managedNodeFile=managedNodeFile] [-relationFile=relationFile] }");
            System.err
                    .println("Ex:    Neo4jManip -neo4jHost=127.0.0.1 -neo4jPort=7474 -managedNodeFile=AutoManagedNodes.xlsx -relationFile=ManRelations.xlsx");

            rc = 1;
        }

        return rc;
    }

}
