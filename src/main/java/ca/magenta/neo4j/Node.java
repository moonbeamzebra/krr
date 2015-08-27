package ca.magenta.neo4j;

import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-19
 */
public class Node {



	private URI selfURI = null;
	
	public Node() {
	}
	
	public boolean existsInGraphDB() {
		return (selfURI != null);
	}

	public URI getSelfURI() {
		return selfURI;
	}
	
	public void setSelfURI(URI selfURI) {
		this.selfURI = selfURI;
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
		Node other = (Node) obj;
		if (selfURI == null) {
			if (other.selfURI != null)
				return false;
		} else if (!selfURI.equals(other.selfURI))
			return false;
		return true;
	}
}
