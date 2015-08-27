package ca.magenta.neo4j;

import java.util.List;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2013-10-27
 */
public class NodeOutputFormatCypher {

	private String columns;
	private Body body;
	private int id;

	public String getColumns() {
		return columns;
	}

	public Body getBody() {
		return body;
	}

	public int getId() {
		return id;
	}

	public static class Body {
		private List<String> columns;
		private List<List<NodeLocationOutput.Body>> data;
		public List<String> getColumns() {
			return columns;
		}
		public List<List<NodeLocationOutput.Body>> getData() {
			if(data.size() != 0)
			return data;
			else
				return null;
		}
		
	}

}
