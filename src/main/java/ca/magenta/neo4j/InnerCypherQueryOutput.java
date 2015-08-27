package ca.magenta.neo4j;

import java.util.HashMap;
import java.util.List;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-27
 */
public class InnerCypherQueryOutput {

	public int id;
	public List<HashMap<String, Object>> body;
	public String from;

}
