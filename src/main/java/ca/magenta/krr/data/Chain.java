package ca.magenta.krr.data;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-21
 */
public class Chain<T> implements Iterable<T> {
	
	private static final String LINK_SEPARATOR = ":::";
	public static final String GROUND_INDICATOR = ":g:";
	
	@Override
	synchronized public String toString() {
		return toString(false /* NOT typed */);
	}

	synchronized public String toTypedString() {
		return toString(true /* typed */);
	}
	
	synchronized private String toString(boolean typed) {
		String str = "";
		int index = -1;
		for(T t : chain )
		{
			if (t instanceof FqdNamed)
			{
				FqdNamed named = (FqdNamed) t;
				index++;
				if (groundIndex == index)
				{
					str = str + GROUND_INDICATOR;
				}
				else
				{
					str = str + LINK_SEPARATOR;
				}
				if (typed)
					str = str + named.toString();
				else
					str = str + named.getFqdName();
			}
		}
		
		return str;
	}

	private static Logger logger = Logger.getLogger(Chain.class);
	
	private int groundIndex = 0;
	
	private Vector<T> chain = new Vector<T>();
	
	@SuppressWarnings("unchecked")
	synchronized public void insertGround(FqdNamed item)
	{
		chain.insertElementAt((T) item, groundIndex);
	}
	
	synchronized public T getGround()
	{
		if (chain.size() > groundIndex)
		{
			return chain.get(groundIndex);
		}
		
		return null;
	}
	
	synchronized public void addMostSpecificAndSetAsGround(FqdNamed item)
	{
		addMostSpecific(item);
		
		groundIndex = chain.size() - 1;
	}
	
	@SuppressWarnings("unchecked")
	synchronized public void addMostSpecific(FqdNamed item)
	{
		chain.add((T) item);
	}

	@SuppressWarnings("unchecked")
	synchronized public void addMostGeneric(FqdNamed item)
	{
		if (chain.size() > 0)
		{
			groundIndex++;
		}
		chain.insertElementAt((T) item, 0);
		
	}
	
	synchronized public boolean isEmpty()
	{
		return (chain.size() == 0);
	}
	
	synchronized public T getMostGeneric() {
		return chain.get(0);
	}

	synchronized public T getMostSpecific() {
		return chain.get(chain.size()-1);
	}

	@Override
	synchronized public Iterator<T> iterator() {
		return chain.iterator();
	}

	@Override
	synchronized public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chain == null) ? 0 : chain.hashCode());
		result = prime * result + groundIndex;
		return result;
	}

	@Override
	synchronized public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chain other = (Chain) obj;
		if (chain == null) {
			if (other.chain != null)
				return false;
		} else if (!chain.equals(other.chain))
			return false;
		if (groundIndex != other.groundIndex)
			return false;
		return true;
	}

}
