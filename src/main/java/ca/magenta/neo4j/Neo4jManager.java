package ca.magenta.neo4j;

/**
 * @author jplaberge@magenta.ca
 * 
 * Use code from How to use the REST API from Java (The Neo4j Manual v2.0.2)
 * @version 0.1
 * @since 2014-04-21
 */
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.ws.rs.core.MediaType;

import ca.magenta.krr.data.CategorizedRelation;
import ca.magenta.krr.data.ManagedNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class Neo4jManager {
	
	public static Logger logger = Logger.getLogger(Neo4jManager.class);
	
	private final String SERVER_ROOT_URI;
	private final String HOST;
	
	private static volatile Neo4jManager instance = null;

	private static String user = null; 
	private static String password = null;

	private Neo4jManager(	String host, 
							int port,
							String user, 
							String password) {
		super();
		this.user = user; 
		this.password = password;
		HOST = "http://" + host + ":" + port;
		SERVER_ROOT_URI = HOST + "/db/data/";
		logger.info("Neo4j host:" + HOST);
		logger.info("Neo4j root uri:" + SERVER_ROOT_URI);
		
	}

	public static Neo4jManager getInstance(	String host, 
											int port, 
											String user, 
											String password) {
		if (instance == null) {
			synchronized (Neo4jManager.class) {
				// Double check
				if (instance == null) {
					instance = new Neo4jManager(host, port, user, password);
				}
			}
		}
		return instance;
	}
	
	public URI createNode(ManagedNode mn)
    {
		HashMap<String, String> properties = new HashMap<String, String>();
        URI node = createNode(properties);
        
        String[] labels = { "ManagedNode", mn.getType() };
        addLabel( node, labels );
        
        addProperty( node, "type", mn.getType() );
        addProperty( node, "fqdName", mn.getFqdName() );
        


        
        return node;
    }
	
	
	
    private URI createNode(HashMap<String, String> properties)
    {
        final String nodeEntryPointUri = SERVER_ROOT_URI + "node";
        // http://localhost:7474/db/data/node
        
        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(properties);
        
        //logger.debug(entity);

        WebResource resource = Client.create()
                .resource( nodeEntryPointUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( "{}" )
                .post( ClientResponse.class );
        
        if (response.getStatus() != 201) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}

        final URI location = response.getLocation();
//        logger.trace(String.format(
//                "POST to [%s], status code [%d], location header [%s]",
//                nodeEntryPointUri, response.getStatus(), location.toString() ));
        response.close();

        return location;
        // END SNIPPET: createNode
    }

    public CypherQueryOutput doCypherQuery(String query)
    {
    	 HashMap<String, String> params = new  HashMap<String, String>();
    	
		HashMap<String, Object> body = new HashMap<String, Object>();
		body.put("query", query);
		body.put("params", params);

    	return doCypherQuery(body);
    }
    
    public CypherQueryOutput doCypherQuery(String query,  HashMap<String, String> params)
    {
		HashMap<String, Object> body = new HashMap<String, Object>();
		body.put("query", query);
		body.put("params", params);

    	return doCypherQuery(body);
    }
    
    private CypherQueryOutput doCypherQuery(HashMap<String, Object> body)
    {
        final String cypherUri = SERVER_ROOT_URI + "cypher";
        // POST http://localhost:7474/db/data/cypher
        
        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(body);
        
        //logger.debug(entity);

        WebResource resource = Client.create()
                .resource( cypherUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
//        logger.debug(String.format(
//              "NEO4J user [%s], password [%s]",
//              user, password ));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( entity )
                .post( ClientResponse.class );
        
        if (response.getStatus() != 200) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}
        
        String entityResponse = response.getEntity(String.class);
    	
    	//logger.trace(entityResponse);
    	
    	CypherQueryOutput cypherQueryOutput = (new Gson()).fromJson(entityResponse, CypherQueryOutput.class);

        //final URI location = response.getLocation();
//        logger.trace(String.format(
//                "POST to [%s], status code [%d]",
//                cypherUri, response.getStatus() ));
        response.close();
        
        return cypherQueryOutput;
    }
    
    public void addProperty( URI nodeUri, String propertyName,
            String propertyValue )
    {
        // START SNIPPET: addProp
        String propertyUri = nodeUri.toString() + "/properties/" + propertyName;
        // http://localhost:7474/db/data/node/{node_id}/properties/{property_name}

        //logger.debug( String.format( "propertyValue: [%s]",propertyValue ));
        
        WebResource resource = Client.create()
                .resource( propertyUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( "\"" + propertyValue + "\"" )
                .put( ClientResponse.class );
        
//        logger.trace( String.format( "PUT to [%s], status code [%d]",
//                propertyUri, response.getStatus() ) );

        if (response.getStatus() != 204) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}

        response.close();
        // END SNIPPET: addProp
    }

    private void addLabel( URI nodeUri, String[] labels )
    {
        
        String labelUri = nodeUri.toString() + "/labels";
        // http://localhost:7474/db/data/node/{node_id}/labels

        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(labels);
        
        //logger.debug(entity);
        
        WebResource resource = Client.create()
                .resource( labelUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( entity)
                .post( ClientResponse.class );
        
        if (response.getStatus() != 204) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}

//        logger.trace(String.format(
//                "POST to [%s], status code [%d]",
//                labelUri, response.getStatus() ));
        
        response.close();

    }
    
    public HashSet<String> listIndexesForALabel( String label)
    {
    	HashSet<String> r_list = new HashSet<String>();
    	
    	final String listIndexesForALabelUri = SERVER_ROOT_URI + "schema/index/" + label;
        // http://localhost:7474/db/data/schema/index/{label}
        
        WebResource resource = Client.create()
                .resource( listIndexesForALabelUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .get( ClientResponse.class );
        
        if (response.getStatus() != 200) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}

        String entity = response.getEntity(String.class);
    	
		// [ {
		//	  "property_keys" : [ "name" ],
		//	  "label" : "user"
		// } ]
    	
    	//logger.trace(entity);
    	
    	Type vectorHashType = new TypeToken<Vector<HashMap<String,Object>>>(){}.getType();
    	Vector<HashMap<String,Object>> vectorHash = (new Gson()).fromJson(entity, vectorHashType);
    	
    	if ((vectorHash != null) &&
        		(vectorHash.size() > 0) &&
        		(vectorHash.get(0) != null) &&
        		(vectorHash.get(0).get("property_keys") != null))
    	{
    		String keys = vectorHash.get(0).get("property_keys").toString();
    		Type hashSetType = new TypeToken<HashSet<String>>(){}.getType();
    		r_list = (new Gson()).fromJson(keys, hashSetType);
    	}
    	
//    	logger.trace(String.format(
//                "GET to [%s], status code [%d], entity [%s], response [%s]",
//                listIndexesForALabelUri, response.getStatus(), entity.toString(), response.toString() ));
        
        response.close();
        
        return r_list;

    }
    
    
    public boolean isSpecificUniquenessConstraint(String label, String key)
    {
    	boolean found = false;
    	
    	final String getConstraintUri = SERVER_ROOT_URI + "schema/constraint/" + label +"/uniqueness/" + key;
        // GET http://localhost:7474/db/data/schema/constraint/{label}/uniqueness/{key}
        
        WebResource resource = Client.create()
                .resource( getConstraintUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .get( ClientResponse.class );
        
        if (response.getStatus() == 200)
        {
        	//HashMap<String, Object> entity = new HashMap<String, Object>();
        	String entity = response.getEntity(String.class);
        	
        	//logger.trace(entity);
        	
        	Type vectorHashType = new TypeToken<Vector<HashMap<String,Object>>>(){}.getType();
        	Vector<HashMap<String,Object>> vectorHash = (new Gson()).fromJson(entity, vectorHashType);
        	
        	if ((vectorHash != null) &&
        		(vectorHash.get(0) != null) &&
        		(vectorHash.get(0).get("property_keys") != null))
        	{
        		String keys = vectorHash.get(0).get("property_keys").toString();
        		Type hashSetType = new TypeToken<HashSet<String>>(){}.getType();
        		HashSet<String> hashSet = (new Gson()).fromJson(keys, hashSetType);
        		if (hashSet.contains(key))
        		{
        			logger.trace("UniquenessConstraint FOUND!");
        			found = true;
        		}
        	}
        	
        	logger.trace(String.format(
                    "GET to [%s], status code [%d], entity [%s], response [%s]",
                    getConstraintUri, response.getStatus(), entity.toString(), response.toString() ));
        }
        
        response.close();
        
        return found;
    }

    public void createIndex(String label, String[] keys)
    {
        final String createIndexUri = SERVER_ROOT_URI + "schema/index/" + label;
        // http://localhost:7474/db/data/schema/index/{label}
        
        HashMap<String,Object> property_keys = new HashMap<String,Object>();
        
        property_keys.put("property_keys", keys);
        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(property_keys);
        
        //logger.debug(entity);
        
        WebResource resource = Client.create()
                .resource( createIndexUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( entity)
                .post( ClientResponse.class );
        
        if (response.getStatus() != 200) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}
        
        logger.trace(String.format(
                "POST to [%s], status code [%d]",
                createIndexUri, response.getStatus() ));
        response.close();
    }
    
    
    public void createUniquenessConstraint(String label, String[] keys)
    {
        final String createUniquenessConstraintUri = SERVER_ROOT_URI + "schema/constraint/" + label + "/uniqueness/";
        // http://localhost:7474/db/data/schema/constraint/{label}/uniqueness/
        
        HashMap<String,Object> property_keys = new HashMap<String,Object>();
        
        property_keys.put("property_keys", keys);
        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(property_keys);
        
        //logger.debug(entity);
        
        WebResource resource = Client.create()
                .resource( createUniquenessConstraintUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( entity)
                .post( ClientResponse.class );
        
        if (response.getStatus() != 200) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}
        
        logger.trace(String.format(
                "POST to [%s], status code [%d]",
                createUniquenessConstraintUri, response.getStatus() ));
        response.close();
    }

    public void createRelationship(ManagedNode mnStart, ManagedNode mnEnd, String relationType, HashMap<String,Object> properties)
    {
        final String createRelationshipUri = mnStart.getSelfURI().toString() + "/relationships";
        // POST http://localhost:7474/db/data/node/{nodeID}/relationships
        
        
        // Content
        // {
        //		"to" : "http://localhost:7474/db/data/node/0",
        //		"type" : "LOVES"
        //		"data" : {
        //		"foo" : "bar"
        //		 }
        // }
        
//        HashMap<String,Object> data = new HashMap<String,Object>();
//        data.put("category", relation.getCategory());
//        data.put("owner", relation.getOwner());
//        data.put("weight", relation.getWeight());
        HashMap<String,Object> content = new HashMap<String,Object>();
        content.put("to", mnEnd.getSelfURI().toString());
        content.put("type", relationType);
        content.put("data", properties);
        
        String entity = (new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()).toJson(content);
        
        //logger.debug(entity);
        
        WebResource resource = Client.create().resource( createRelationshipUri );
        resource.addFilter(new HTTPBasicAuthFilter(user, password));
        // POST {} to the node entry point URI
        ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
                .type( MediaType.APPLICATION_JSON )
                .entity( entity)
                .post( ClientResponse.class );
        
        if (response.getStatus() != 201) {
		    Neo4jException neo4jException = new Neo4jException("Failed : HTTP error code : " + response.getStatus());
			logger.error("", neo4jException);
			throw neo4jException;
		}
        
        logger.trace(String.format(
                "POST to [%s], status code [%d]",
                createRelationshipUri, response.getStatus() ));
        response.close();
    }

	public void createIndexIfNotExist(String label, String[] keys) {
		
		Vector<String> keysToDo = new Vector<String>();
		
		HashSet<String> list  = listIndexesForALabel(label);
		
		for (String key : keys)
		{
			if ( !list.contains(key) )
			{
				keysToDo.add(key);
			}
		}
		
		if (keysToDo.size() > 0)
		{
			logger.debug("Keys: [" + keysToDo + "]");
			String listKeys[] = new String[keysToDo.size()];
			int row = 0;
			for (String todo : keysToDo)
			{
				listKeys[row] = todo;
				row++;
			}
			createIndex(label, listKeys);
		}
	}

    
}
