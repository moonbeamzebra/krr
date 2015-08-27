package ca.magenta.neo4j;

import java.util.HashMap;

import ca.magenta.krr.common.Severity;
import ca.magenta.krr.data.DependencyRule;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-30
 */
public class ManagedNodeCypherQueryOutput {
	public HashMap<String,HashMap<Severity,DependencyRule>> dependencyRuleByCathegory;
	private String fqdName;	
	private String type;

}
