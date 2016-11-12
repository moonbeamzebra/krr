package ca.magenta.krr.data;

import ca.magenta.krr.tools.Utils;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-02
 */
public class SeverityRule {

	private String type = null;
	
	public SeverityRule(String type) {
		super();
		this.type = type;
	}
	
	public SeverityRule() {
		super();
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	
}
