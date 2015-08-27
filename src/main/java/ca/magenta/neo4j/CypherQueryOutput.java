package ca.magenta.neo4j;

import java.util.HashMap;
import java.util.List;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-01
 */
public class CypherQueryOutput {
	private List<String> columns;
	//private List<List<NodeLocationOutput.Body>> data;
	private List<List<HashMap<String, Object>>> data;

	public List<String> getColumns() {
		return columns;
	}

	public List<List<HashMap<String, Object>>> getData() {
		return data;
	}

//	public List<List<NodeLocationOutput.Body>> getData() {
//		if (data.size() != 0)
//			return data;
//		else
//			return null;
//	}

}
