package ca.magenta.krr.data;

import java.util.HashMap;

import ca.magenta.krr.common.LogicalOperator;
import ca.magenta.krr.common.Severity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class DependencyRule {
	




	String nodeFqdName = null;
	String dependencyCategory = null;
	Severity severity = null;
	private LogicalOperator operator = null;
	private float value;
	private boolean inPercent = false;
	String stateCategory = null;

	public DependencyRule(HashMap<String, String> hashMap)
	{
		this.nodeFqdName = hashMap.get("nodeFqdName");
		
		this.dependencyCategory = hashMap.get("dependencyCategory");
		
		String valueStr = hashMap.get("value");
		
		if ((valueStr != null ) && (!valueStr.isEmpty()))
		{
			valueStr = valueStr.trim();
			if (valueStr.endsWith("%"))
			{
				this.inPercent = true;
				valueStr = valueStr.substring(0, valueStr.length()-1);
			}
			float value = Float.parseFloat(valueStr);
			if (this.inPercent)
				this.setValue(value/100);
			else
				this.setValue(value);
		}
		
		this.severity = new Severity(hashMap.get("severity"));

		String operatorStr = hashMap.get("operator");
		
		if ((operatorStr != null) && (!operatorStr.isEmpty()))
		{
			this.setOperator(new LogicalOperator(operatorStr));
		}
		
		this.stateCategory = hashMap.get("stateCategory");
	}
	
	public String getNodeFqdName() {
		return nodeFqdName;
	}
	
	public String getDependencyCategory() {
		return dependencyCategory;
	}

	public boolean isInPercent() {
		return inPercent;
	}
	
	public Severity getSeverity() {
		return severity;
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

	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	public float getValue() {
		return value;
	}

	public String getStateCategory() {
		return stateCategory;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	public DependencyRule(String nodeFqdName, String dependencyCategory, Severity severity, LogicalOperator operator, float value, boolean inPercent,
			String stateCategory) {
		super();
		this.nodeFqdName = nodeFqdName;
		this.dependencyCategory = dependencyCategory;
		this.severity = severity;
		this.operator = operator;
		this.value = value;
		this.inPercent = inPercent;
		this.stateCategory = stateCategory;
	}

	
}
