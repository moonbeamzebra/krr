package ca.magenta.krr.tools;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.magenta.krr.test.TopologyBasedCorrelationFT;

public class Utils {

	private static Logger logger = Logger.getLogger(TopologyBasedCorrelationFT.class);
	
	public static String toJsonE(Object obj, Class clazz, boolean pretty)
	{
		return "{ERROR}";
	}

	public static String toJsonJ2(Object obj, Class clazz, boolean pretty)
	{
		String json = "ERROR";
		
		//logger.debug(String.format("toJson called for class: [%s]", clazz.toString()));
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			if (pretty)
				json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			else
				json = mapper.writeValueAsString(obj);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.error(String.format("Exception: [%s]", e.getLocation()));
		}

		return json;
	}

	public static String toJsonG(Object obj, Class clazz, boolean pretty)
	{
		String json = "ERROR";
		
		//logger.debug(String.format("toJson called for class: [%s]", clazz.toString()));

		if (pretty)
		{
			json = (new GsonBuilder().setPrettyPrinting().create()).toJson(obj);
		}
		else
		{
			json = (new Gson()).toJson(obj);
		}

		return json;
	}
}
