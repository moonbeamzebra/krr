package ca.magenta.krr.data;

import java.net.URI;
import java.util.HashMap;

import ca.magenta.krr.tools.Utils;
import ca.magenta.neo4j.Node;
import ca.magenta.neo4j.Relation;


/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-19
 */
public class CategorizedRelation extends Relation {


	private HashMap<String, Object> properties = new HashMap<String, Object>(); 

	public CategorizedRelation(URI selfURI, Node start, String type, Node end, float weight, String category, String owner) {
		super(selfURI, start, type, end);
		properties.put("weight", weight);
		properties.put("category", category);
		properties.put("owner", owner);
	}
	
	public float getWeight() {
		float f = 0;
		Object fO = properties.get("weight");
		if ( (fO != null) && (fO instanceof Float) )
		{
			f = (float) fO;
		}
		return f;
	}

	public String getCategory() {
		return (String) properties.get("category");
	}

	public String getOwner() {
		return(String) properties.get("owner");
	}
	
	public String toString(boolean pretty)		
	{
		return  Utils.toJsonG(this, this.getClass(), pretty);
	}

	@Override
	public String toString()		
	{
		return  toString(false);
	}
	
	public HashMap<String, Object> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
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
		CategorizedRelation other = (CategorizedRelation) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
	
	
}
