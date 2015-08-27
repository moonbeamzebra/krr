package ca.magenta.neo4j;

import java.util.HashMap;
import java.util.List;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2013-10-25
 */
public class NodeLocationOutput {

	public int id;
	public List<Body> body;
	public String from;

	public String getSelf() {
		if (body.size() != 0)
			return body.get(0).self;
		return "";
	}
	
	public String getStart() {
		if (body.size() != 0)
			return body.get(0).start;
		return "";
	}
	
	public String getEnd() {
		if (body.size() != 0)
			return body.get(0).end;
		return "";
	}
	public List<Body> getBody() {
		return body;
	}
	
	public String getStart(int i) {

		if (body.size()  >=  i)
			return body.get(i).start;
		return "";
	}
	
	public String getEnd(int i) {
		if (body.size() >= i)
			return body.get(i).end;
		return "";
	}
	
	public static class Body {
		public String indexed;
		public String outgoing_relationships;
		public String start;
		public String end;
		public HashMap<String, Object> data;
		public String traverse;
		public String all_typed_relationships;
		public String property;
		public String self;
		public String properties;
		public String outgoing_typed_relationships;
		public String incoming_relationships;
		public Extensions extensions;
		public String create_relationship;
		public String paged_traverse;
		public String all_relationships;
		public String incoming_typed_relationships;

	}

//	public static class Data {
//
//		public String smartsDescription;
//		// public String smartsLocation;
//		public String lastSeen;
//		public String smartsSystemName;
//		public String smartsDisplayClassName;
//		public String smartsCreationClassName;
//		public String type;
//		public String smartsVendor;
//		public String uniqueid;
//		public String source;
//		public String smartsType;
//		public String name;
//		public String smartsIsManaged;
//		public String smartsPrimaryOwnerContact;
//		public String smartsConnectedSystems;
//	
//
//	}

	public static class Extensions {

	}

}
