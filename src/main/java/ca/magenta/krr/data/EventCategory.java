package ca.magenta.krr.data;


import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-02
 */
public class EventCategory {
	
	private static Logger logger = Logger.getLogger(EventCategory.class);
	
	private String sourceName = null;
	private String sourceType = null;
	private String clazz = null;
	private String veryShortDescr = null;
	private HashSet<String> categories = new HashSet<String>(); 
	private boolean isConsumerView = false;
	private boolean isProviderView = false;
	
	public EventCategory(HashMap<String, String> hashMap)
	{
		// sourceName	sourceType	class	veryShortDescr	category	isProviderView	isConsumerView
		this.sourceName = hashMap.get("sourceName");
		this.sourceType = hashMap.get("sourceType");
		this.clazz = hashMap.get("class");
		this.veryShortDescr = hashMap.get("veryShortDescr");
		
		String values[] = hashMap.get("category").split(",");
		for (String cat : values)
		{
			categories.add(cat.trim());
		}
		
		String view = hashMap.get("isConsumerView");
		if ((view != null) &&
				view.trim().toUpperCase().equals("YES")
				)
		{
			this.isConsumerView = true;
		}

		view = hashMap.get("isProviderView");
		if ((view != null) &&
				view.trim().toUpperCase().equals("YES")
				)
		{
			this.isProviderView = true;
		}
		
		if ( this.isConsumerView && this.isProviderView)
		{
			logger.error("Bad EventCategory config: isConsumerView and isProviderView could not both be set to YES" );
			this.isProviderView = false;
		}
		
	}

	public EventCategory() {
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getClazz() {
		return clazz;
	}

	public String getVeryShortDescr() {
		return veryShortDescr;
	}

	public HashSet<String> getCategories() {
		return categories;
	}

	public boolean isConsumerView() {
		return isConsumerView;
	}

	public boolean isProviderView() {
		return isProviderView;
	}
		
	public String toString(boolean pretty) {
		if (pretty) {
			return (new GsonBuilder().setPrettyPrinting().create())
					.toJson(this);
		} else {
			return (new Gson()).toJson(this);
		}
	}

	@Override
	public String toString() {
		return toString(false);
	}

}
