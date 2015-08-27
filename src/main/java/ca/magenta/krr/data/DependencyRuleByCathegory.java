package ca.magenta.krr.data;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.magenta.krr.common.Severity;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-18
 */
public class DependencyRuleByCathegory {


	//              Category       Severity    
	//              v              v
	private HashMap<String,HashMap<String,DependencyRule>> dependencyRuleByCathegoryHashMap = new HashMap<String,HashMap<String,DependencyRule>>();
	
	public void addDependencyRule(DependencyRule dependencyRule)
	{
		HashMap<String, DependencyRule> dependencyRuleBySeverity = dependencyRuleByCathegoryHashMap.get(dependencyRule.getDependencyCategory());
		if (dependencyRuleBySeverity == null)
		{
			dependencyRuleBySeverity = new HashMap<String,DependencyRule>();
		}
		
		dependencyRuleBySeverity.put(dependencyRule.getSeverity().toString(), dependencyRule);
		
		dependencyRuleByCathegoryHashMap.put(dependencyRule.getDependencyCategory(), dependencyRuleBySeverity);
	}
	
	public HashMap<String, DependencyRule> getDependencyRuleBySeverityForCategory(String stateCategory) {
		
		return dependencyRuleByCathegoryHashMap.get(stateCategory);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dependencyRuleByCathegoryHashMap == null) ? 0
						: dependencyRuleByCathegoryHashMap.hashCode());
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
		DependencyRuleByCathegory other = (DependencyRuleByCathegory) obj;
		if (dependencyRuleByCathegoryHashMap == null) {
			if (other.dependencyRuleByCathegoryHashMap != null)
				return false;
		} else if (!dependencyRuleByCathegoryHashMap
				.equals(other.dependencyRuleByCathegoryHashMap))
			return false;
		return true;
	}

	public HashMap<String, HashMap<String, DependencyRule>> getDependencyRuleByCathegoryHashMap() {
		return dependencyRuleByCathegoryHashMap;
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

}
