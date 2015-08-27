package ca.magenta.krr.engine;

import java.util.HashMap;
import java.net.URI;

import org.apache.log4j.Logger;

import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.neo4j.Neo4jManager;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-29
 */
public class ManagedNodeCache {

	public static Logger logger = Logger.getLogger(ManagedNodeCache.class);

	private HashMap<String, ManagedNode> fqdName2managedNodes = new HashMap<String, ManagedNode>();
	private HashMap<URI, ManagedNode> uri2managedNodes = new HashMap<URI, ManagedNode>();
	
	synchronized public ManagedNode getManagedNode(ManagedEntity managedEntity, Neo4jManager graphDB) {
		ManagedNode managedNode =  fqdName2managedNodes.get(managedEntity.getFqdName());
		
		if (managedNode == null)
		{
			managedNode = ManagedNode.getInstance(managedEntity.getFqdName(), graphDB);
			
			if (managedNode.existsInGraphDB() )
			{
				uri2managedNodes.put(managedNode.getSelfURI(), managedNode);
			}
			else
			{
				managedNode.setType(managedEntity.getClazz());
			}
			
			fqdName2managedNodes.put(managedNode.getFqdName(), managedNode);
		}
		
		return managedNode;
	}

	synchronized public ManagedNode getManagedNode(URI self, Neo4jManager graphDB) {
		ManagedNode managedNode =  uri2managedNodes.get(self);
		
		if (managedNode == null)
		{
			managedNode = ManagedNode.getInstance(self, graphDB);
			
			if (managedNode.existsInGraphDB() )
			{
				uri2managedNodes.put(managedNode.getSelfURI(), managedNode);
			}
			
			fqdName2managedNodes.put(managedNode.getFqdName(), managedNode);
		}
		
		return managedNode;
	}

	synchronized public ManagedNode getManagedNode(String fqdName) {
		return fqdName2managedNodes.get(fqdName);
	}
	synchronized public ManagedNode getManagedNode(URI selfURI) {
		return uri2managedNodes.get(selfURI);
	}

}
