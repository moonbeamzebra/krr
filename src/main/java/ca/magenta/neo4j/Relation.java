package ca.magenta.neo4j;

import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2013-09-10
 */
public class Relation {



	private URI selfURI = null;
	private transient Node start = null;
	private String type = null;
	private transient Node end = null;
	
	public Relation(URI selfURI, Node start, String type, Node end) {
		super();
		this.start = start;
		this.type = type;
		this.end = end;
		
	}
	
	public Relation() {
		super();
	}
	
	public URI getSelfURI() {
		return selfURI;
	}
	
	public Node getStart() {
		return start;
	}

	public String getType() {
		return type;
	}

	public Node getEnd() {
		return end;
	}

	public String toString(boolean pretty)		
	{
		if (pretty)
		{
			return (new GsonBuilder().setPrettyPrinting().create()).toJson(this);
		}
		else
		{
			return (new Gson()).toJson(this);
		}
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((selfURI == null) ? 0 : selfURI.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Relation other = (Relation) obj;
		if (selfURI == null) {
			if (other.selfURI != null)
				return false;
		} else if (!selfURI.equals(other.selfURI))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}
