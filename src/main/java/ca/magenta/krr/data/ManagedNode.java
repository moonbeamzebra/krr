package ca.magenta.krr.data;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.engine.Engine;
import ca.magenta.neo4j.CypherQueryOutput;
import ca.magenta.neo4j.Neo4jManager;
import ca.magenta.neo4j.Node;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class ManagedNode extends Node implements FqdNamed {

	public static Logger logger = Logger.getLogger(ManagedNode.class);

	public static HashMap<String, DependencyRuleByCathegory> dependencyRuleByCathegoryByNode = new HashMap<String, DependencyRuleByCathegory>();

	public static final String DEFAULT_INDEX = "ManagedNode";

	private String type = null;
	private Vector<CategorizedRelation> startingRelations = null;
	private Vector<CategorizedRelation> endingRelations = null;

	private static final String NODE_SELF_PATTERN = "^.*/(\\d+)$";
	private static Pattern nodeSelf_Pattern = Pattern.compile(NODE_SELF_PATTERN);

	private String fqdName = null;

	private DependencyRuleByCathegory dependencyRuleByCathegory = new DependencyRuleByCathegory();

	public static ManagedNode getInstance(URI self, Neo4jManager graphDB) {
		ManagedNode r_managedNode = new ManagedNode();

		r_managedNode.updateFromGraphDB(self, graphDB);

		return r_managedNode;
	}

	public static ManagedNode getInstance(HashMap<String, String> hashMap, Neo4jManager neo4jManager) {
		ManagedNode r_managedNode = new ManagedNode(hashMap);

		r_managedNode.updateSelfFromGraphDB(neo4jManager);

		return r_managedNode;
	}

	public static ManagedNode getInstance(String type, String fqdName, Neo4jManager neo4jManager) {
		ManagedNode r_managedNode = new ManagedNode(type, fqdName);

		r_managedNode.updateSelfFromGraphDB(neo4jManager);

		return r_managedNode;
	}

	public static ManagedNode getInstance(String fqdName, Neo4jManager neo4jManager) {
		return getInstance(null, fqdName, neo4jManager);
	}

	private void updateSelfFromGraphDB(Neo4jManager neo4jManager) {
		// BODY:
		// {
		// "query" : "MATCH (mn:Switch { fqdName: {fqdName} }) RETURN mn",
		// "params" : {
		// "fqdName" : "switch03"
		// }
		// }

		String indexHint = this.getType();
		if (indexHint == null) {
			indexHint = DEFAULT_INDEX;
		}

		// MATCH (mn:Switch { fqdName: "switch03" }) RETURN mn;
		StringBuilder query = new StringBuilder();
		query.append("MATCH (mn:");
		query.append(indexHint);
		query.append(" { fqdName: {fqdName} }) RETURN mn");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fqdName", fqdName);

		CypherQueryOutput cypherQueryOutput = neo4jManager.doCypherQuery(query.toString(), params);

		if ((cypherQueryOutput.getData() != null) && (cypherQueryOutput.getData().size() > 0) && (cypherQueryOutput.getData().get(0) != null)
				&& (cypherQueryOutput.getData().get(0).get(0) != null)) {
			HashMap<String, Object> inBody = cypherQueryOutput.getData().get(0).get(0);

			String self = (String) inBody.get("self");

			if (self != null) {
				try {
					this.setSelfURI(new URI(self));
					logger.trace(String.format("Self: [%s]", self));
				} catch (URISyntaxException e) {
					logger.error(String.format("Bad URI: [%s]", self), e);
					this.setSelfURI(null);
				}
			}

			Object data = inBody.get("data");
			this.setPropertiesFromJsonData(data);
/* Commented out by JPL 11 nov 2014
			if (data != null) {
				// {fqdName=server01, type=Host}
				logger.debug(String.format("HASH DATA: [%s]", data.toString()));
				Type hashMapType = new TypeToken<HashMap<String, String>>() {
				}.getType();
				HashMap<String, String> hashMap = (new Gson()).fromJson(data.toString(), hashMapType);

				String type = hashMap.get("type");
				if (type != null) {
					this.setType(type);
					logger.trace(String.format("Type: [%s]", type));
				}
			} */
		}

	}

	private void updateFromGraphDB(URI a_self, Neo4jManager neo4jManager) {

		// self = http://127.0.0.1:7474/db/data/node/133395

		String nodeIDStr = null;
		Matcher matcher_nodeSelf_Pattern = nodeSelf_Pattern.matcher(a_self.toString());
		if (matcher_nodeSelf_Pattern.find()) {
			nodeIDStr = matcher_nodeSelf_Pattern.group(1);

			logger.trace(String.format("nodeIDStr: [%s]", nodeIDStr));

			long nodeID = Long.parseLong(nodeIDStr);

			// BODY:
			// {
			// "query" : "START mn=node({nodeID}) RETURN mn",
			// "params" : {
			// "nodeID" : "133395"
			// }
			// }

			// START mn=node({nodeID}) RETURN mn;
			String query = "START mn=node(" + nodeID + ") RETURN mn";

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("nodeID", Long.toString(nodeID));

			CypherQueryOutput cypherQueryOutput = neo4jManager.doCypherQuery(query);

			if ((cypherQueryOutput.getData() != null) && (cypherQueryOutput.getData().get(0) != null)
					&& (cypherQueryOutput.getData().get(0).get(0) != null)) {
				HashMap<String, Object> inBody = cypherQueryOutput.getData().get(0).get(0);

				String self = (String) inBody.get("self");
				logger.trace(String.format("Relation Self: [%s]", self));

				if (self != null) {
					try {
						this.setSelfURI(new URI(self));
						logger.trace(String.format("Self: [%s]", self));
					} catch (URISyntaxException e) {
						logger.error(String.format("Bad URI: [%s]", self), e);
						this.setSelfURI(null);
					}
				}

				Object data = inBody.get("data");
				this.setPropertiesFromJsonData(data);
				// if (data != null)
				// {
				// // {fqdName=server01, type=Host}
				// logger.debug(String.format( "HASH DATA: [%s]", data.toString() ));
				// Type hashMapType = new TypeToken<HashMap<String,Object>>(){}.getType();
				// HashMap<String,Object> hashMap = (new Gson()).fromJson(data.toString(), hashMapType);
				//
				// String type = (String) hashMap.get("type");
				// if ( type != null )
				// {
				// this.setType(type);
				// logger.trace(String.format( "Type: [%s]", type ));
				// }
				//
				// String fqdName = (String) hashMap.get("fqdName");
				// if ( fqdName != null )
				// {
				// this.setFqdName(fqdName);
				// logger.trace(String.format( "fqdName: [%s]", fqdName ));
				// }
				//
				// Object dependencyRuleByCathegoryObj = hashMap.get("dependencyRuleByCathegory");
				// if ( dependencyRuleByCathegoryObj != null )
				// {
				// logger.trace(String.format( "dependencyRuleByCathegoryObj: [%s]", dependencyRuleByCathegoryObj.toString() ));
				// this.setDependencyRuleByCathegoryFromJSON(dependencyRuleByCathegoryObj.toString());
				// logger.trace(String.format( "dependencyRuleByCathegory: [%s]", dependencyRuleByCathegory ));
				// }
				// }
			}

		}

	}

	public void updateRelationshipsFromGraphDB(Neo4jManager neo4jManager) {
		// MATCH (s:Service { fqdName: 'MECWeb1' })-[r]-(e) RETURN r;

		// BODY:
		// {
		// "query" : "MATCH (s:Switch { fqdName: {fqdName} })-[r]-(e) RETURN r",
		// "params" : {
		// "fqdName" : "switch03"
		// }
		// }

		String indexHint = this.getType();
		if (indexHint == null) {
			indexHint = DEFAULT_INDEX;
		}

		// MATCH (mn:Switch { fqdName: "switch03" }) RETURN mn;
		StringBuilder query = new StringBuilder();
		query.append("MATCH (s:");
		query.append(indexHint);
		query.append(" { fqdName: {fqdName} })-[r]-(e) RETURN r");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("fqdName", fqdName);

		CypherQueryOutput cypherQueryOutput = neo4jManager.doCypherQuery(query.toString(), params);

		logger.debug(String.format("cypherQueryOutput: [%s]", cypherQueryOutput));

		if ((cypherQueryOutput.getData() != null)) {
			startingRelations = new Vector<CategorizedRelation>();
			endingRelations = new Vector<CategorizedRelation>();
			for (List<HashMap<String, Object>> line : cypherQueryOutput.getData()) {
				HashMap<String, Object> relation = line.get(0);

				logger.debug(String.format("Relation: [%s]", relation));

				try {
					String rStart = (String) relation.get("start");
					logger.trace(String.format("Start MN: [%s]", rStart));
					URI rStartURI = new URI(rStart);
					ManagedNode rStartMn = Engine.getManagedNode(rStartURI);
					logger.trace(String.format("Start MN: [%s]", rStartMn.toString()));

					String rSelf = (String) relation.get("self");
					logger.trace(String.format("Rel Self: [%s]", rSelf));
					URI rSelfURI = new URI(rStart);

					String rType = (String) relation.get("type");
					logger.trace(String.format("Type: [%s]", rType));

					String rEnd = (String) relation.get("end");
					URI rEndURI = new URI(rEnd);
					ManagedNode rEndMn = Engine.getManagedNode(rEndURI);
					logger.trace(String.format("End MN: [%s]", rEndMn.toString()));

					Object data = relation.get("data");
					if (data != null) {
						// "category" : "Availability",
						// "weight" : 1.0,
						// "owner" : "IA"
						logger.debug(String.format("HASH DATA: [%s]", data.toString()));
						Type hashMapType = new TypeToken<HashMap<String, String>>() {
						}.getType();
						HashMap<String, String> hashMap = (new Gson()).fromJson(data.toString(), hashMapType);

						String category = hashMap.get("category");
						if (category == null)
							category = "";
						String weightStr = hashMap.get("weight");
						float weight = 0;
						if (weightStr != null) {
							try {
								weight = Float.parseFloat(weightStr);
							} catch (NumberFormatException e) {
								logger.error(String.format("Bad weight : [%s]", weightStr), e);
							}
						}

						String owner = hashMap.get("owner");
						if (owner == null)
							owner = "";

						CategorizedRelation categorizedRelation = new CategorizedRelation(rSelfURI, rStartMn, rType, rEndMn, weight, category, owner);

						if (rStartURI.equals(this.getSelfURI())) {
							startingRelations.add(categorizedRelation);
						} else {
							endingRelations.add(categorizedRelation);
						}

					}

				} catch (URISyntaxException e) {
					logger.error("Bad URI", e);
				}

				// relation.
				//
				// CategorizedRelation cr = new CategorizedRelation(Node start, String type, Node end, float weight, String category, String owner);
				//

				{

				}
			}

		}
	}

	public ManagedNode(String type, String fqdName) {
		super();
		this.type = type;
		this.fqdName = fqdName;
	}

	private ManagedNode() {
		super();
	}

	public ManagedNode(HashMap<String, String> hashMap) {
		super();
		this.type = hashMap.get("Type");
		this.fqdName = hashMap.get("FqdName");
	}

	public String getDependencyRuleByCathegoryToJSON() {
		// String toJsonRaw = (new GsonBuilder().disableHtmlEscaping().create()).toJson(dependencyRuleByCathegory);
		String toJsonReplaced = (new GsonBuilder().disableHtmlEscaping().create()).toJson(dependencyRuleByCathegory).replace("\"", "'");

		// logger.debug(String.format( "toJsonRaw: [%s]", toJsonRaw ));
		logger.debug(String.format("toJsonReplaced: [%s]", toJsonReplaced));

		// Type dependencyRuleByCathegoryType = new TypeToken<HashMap<String,HashMap<Severity,DependencyRule>>>(){}.getType();
		// DependencyRuleByCathegory dependencyRuleByCathegory2 = (new Gson()).fromJson(toJsonRaw, DependencyRuleByCathegory.class);

		// DependencyRuleByCathegory dependencyRuleByCathegory3 = (new Gson()).fromJson(toJsonReplaced, DependencyRuleByCathegory.class);

		return toJsonReplaced;
	}

	public void setDependencyRuleByCathegoryFromJSON(Object dependencyRuleByCathegoryObj) {
		Type dependencyRuleByCathegoryType = new TypeToken<HashMap<String, HashMap<Severity, DependencyRule>>>() {
		}.getType();
		dependencyRuleByCathegory = (new Gson()).fromJson(dependencyRuleByCathegoryObj.toString(), dependencyRuleByCathegoryType);
	}

	private void setPropertiesFromJsonData(Object data) {
		if (data != null) {
			// {fqdName=server01, type=Host}
			logger.debug(String.format("HASH DATA: [%s]", data.toString()));

			// ManagedNodeCypherQueryOutput managedNodeCypherQueryOutput = (new Gson()).fromJson( data.toString(), ManagedNodeCypherQueryOutput.class);

			String dataStr = data.toString().replace("'", "\"");
			logger.debug(String.format("HASH DATA STR: [%s]", dataStr));
			Type hashMapType = new TypeToken<HashMap<String, Object>>() {
			}.getType();
			HashMap<String, Object> hashMap = (new Gson()).fromJson(dataStr, hashMapType);
			
			logger.debug("HashMap:" + hashMap);

			String type = (String) hashMap.get("type");
			if (type != null) {
				this.setType(type);
				logger.trace(String.format("Type: [%s]", type));
			}

			String fqdName = (String) hashMap.get("fqdName");
			if (fqdName != null) {
				logger.trace(String.format("fqdName: [%s]", fqdName));
				this.setFqdName(fqdName);

				DependencyRuleByCathegory dependencyRuleByCathegoryTmp = dependencyRuleByCathegoryByNode.get(fqdName);

				if (dependencyRuleByCathegoryTmp != null) {
					dependencyRuleByCathegory = dependencyRuleByCathegoryTmp;
					logger.debug("Got dependencyRuleByCathegory for [" + fqdName + "]");
				}
				else
				{
					logger.debug("NO dependencyRuleByCathegory for [" + fqdName + "]");
				}
				
			}
		}
	}

	// public static void addDependencyRule(DependencyRule dependencyRule, HashMap<String,HashMap<Severity,DependencyRule>> a_dependencyRuleByCathegory)
	// {
	//
	// HashMap<Severity, DependencyRule> dependencyRuleBySeverity = a_dependencyRuleByCathegory.get(dependencyRule.getDependencyCategory());
	// if (dependencyRuleBySeverity == null)
	// {
	// dependencyRuleBySeverity = new HashMap<Severity,DependencyRule>();
	// }
	//
	// dependencyRuleBySeverity.put(dependencyRule.getSeverity(), dependencyRule);
	//
	// a_dependencyRuleByCathegory.put(dependencyRule.getDependencyCategory(), dependencyRuleBySeverity);
	// }

	public String getFqdName() {
		return fqdName;
	}

	public void setFqdName(String fqdName) {
		this.fqdName = fqdName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	synchronized public Vector<CategorizedRelation> getStartingRelations() {
		if (startingRelations == null) {
			this.updateRelationshipsFromGraphDB(Engine.getGraphDB());
		}
		return startingRelations;
	}

	synchronized public Vector<CategorizedRelation> getEndingRelations() {
		if (endingRelations == null) {
			this.updateRelationshipsFromGraphDB(Engine.getGraphDB());
		}
		return endingRelations;
	}

	synchronized private void addEndingRelation(CategorizedRelation relation) {
		endingRelations.add(relation);
	}

	synchronized private void addStartingRelation(CategorizedRelation relation) {
		startingRelations.add(relation);
	}

	@Override
	public String toString() {
		// return toString(false);
		return type + "::" + fqdName;
	}

	public void addDependencyRule(DependencyRule dependencyRule) {
		dependencyRuleByCathegory.addDependencyRule(dependencyRule);
	}

	public HashMap<String, DependencyRule> getDependencyRuleBySeverityForCategory(String stateCategory) {
		return dependencyRuleByCathegory.getDependencyRuleBySeverityForCategory(stateCategory);
	}

	public static void setDependencyRuleByCathegoryByNode(HashMap<String, DependencyRuleByCathegory> dependencyRuleByCathegoryByNode) {
		ManagedNode.dependencyRuleByCathegoryByNode = dependencyRuleByCathegoryByNode;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fqdName == null) ? 0 : fqdName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManagedNode other = (ManagedNode) obj;
		if (fqdName == null) {
			if (other.fqdName != null)
				return false;
		} else if (!fqdName.equals(other.fqdName))
			return false;
		return true;
	}

	public static Chain<ManagedNode> transposeToManagedNodeChain(Chain<ManagedEntity> managedEntityChain) {

		Chain<ManagedNode> r_managedNodeChain = new Chain<ManagedNode>();

		ManagedEntity ground = managedEntityChain.getGround();

		Iterator<ManagedEntity> itr = managedEntityChain.iterator();
		while (itr.hasNext()) {
			ManagedEntity managedEntity = (ManagedEntity) itr.next();
			if (managedEntity == ground) {
				r_managedNodeChain.addMostSpecificAndSetAsGround(Engine.getManagedNode(managedEntity));
			} else {
				r_managedNodeChain.addMostSpecific(Engine.getManagedNode(managedEntity));
			}
		}

		return r_managedNodeChain;
	}

}
