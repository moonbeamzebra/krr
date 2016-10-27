package ca.magenta.krr.data;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2016-10-16
 */

import org.kie.api.runtime.rule.FactHandle;

public class StateRelation {

	private transient FactHandle upperValue = null;
	private transient FactHandle lowerValue = null;

	public StateRelation(FactHandle upperValue, FactHandle lowerValue) {
		super();
		this.upperValue = upperValue;
		this.lowerValue = lowerValue;
	}
	public FactHandle getUpperValue() {
		return upperValue;
	}
	public FactHandle getLowerValue() {
		return lowerValue;
	}
	
}
