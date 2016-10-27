package ca.magenta.krr.fact;

import java.util.HashMap;

import org.apache.log4j.Logger;


import ca.magenta.krr.tools.Utils;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-16
 */
public class Message implements  Fact{
	
	private static Logger logger = Logger.getLogger(Message.class);

	public Message() {
		super();
	}

	public Message(String id, String type, long timestamp, HashMap<String, String> attributes) {
		this();
		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
		this.attributes = attributes;
	}

	public void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String identifier) {
		this.id = identifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}


	private String id = null;
	private String type = null;
	private long timestamp = 0;
	
	private HashMap<String, String> attributes = new HashMap<String, String>();
	
	public String toString(boolean pretty)		
	{
		return  Utils.toJsonG(this, this.getClass(), pretty);
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}
	
}
