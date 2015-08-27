package ca.magenta.krr.api;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.magenta.krr.common.LogicalOperator;
import ca.magenta.krr.common.Severity;
import ca.magenta.krr.connector.common.Subscriber;
import ca.magenta.krr.data.DependencyRule;
import ca.magenta.krr.data.DependencyRuleByCathegory;
import ca.magenta.krr.data.FqdNamed;
import ca.magenta.krr.data.ManagedEntity;
import ca.magenta.krr.data.ManagedNode;
import ca.magenta.krr.fact.Fact;
import ca.magenta.krr.fact.InheritanceAdapter;
import ca.magenta.krr.fact.StateClear;
import ca.magenta.krr.fact.StateNew;
import ca.magenta.krr.fact.StateUpdate;
import ca.magenta.neo4j.Node;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-12-07
 */
public class SubscriptionForwarder extends Subscriber {
	

	public static Logger logger = Logger.getLogger(SubscriptionForwarder.class);
	
	private PrintWriter client = null;

    public SubscriptionForwarder(String name, PrintWriter client) {
    	super(name);
    	this.client = client;
    }

	@Override
	protected void forward(Fact fact) {
		String type = "Unknown";
		
		if (fact instanceof StateNew)
			type = "StateNew";
		else if (fact instanceof StateClear)
			type = "StateClear";
		else if (fact instanceof StateUpdate)
			type = "StateUpdate";
		
		
		
		
		if (logger.isTraceEnabled())
			logger.trace("Got: " + type + ";" + fact.toString(true));

			Gson gsonExt = null;
	        {
	            GsonBuilder builder = new GsonBuilder();
	            builder.registerTypeAdapter(Fact.class, new InheritanceAdapter<Fact>());
	            gsonExt = builder.create();
	        }
//			Gson gsonExtP = null;
//	        {
//	            GsonBuilder builderP = new GsonBuilder();
//	            builderP.registerTypeAdapter(Fact.class, new InheritanceAdapter<Fact>());
//	            //builderP.registerTypeAdapter(DependencyRuleByCathegory.class, new DependencyRuleByCathegoryAdapter());
//	            //builderP.registerTypeAdapter(ManagedEntity.class, new InheritanceAdapter<ManagedEntity>());
//	            //builderP.registerTypeAdapter(ManagedNode.class, new InheritanceAdapter<ManagedNode>());
//	            gsonExtP = builderP.setPrettyPrinting().create();
//	        }
//	        
//	        //DependencyRule dependencyRule = new  DependencyRule("wwwMgntaCaHome","Availability", Severity.MAJOR, LogicalOperator.LESS_THAN, 1, false,"Availability");
//	        
//	        DependencyRule dependencyRule = new  DependencyRule("wwwMgntaCaHome","Availability", Severity.MAJOR, null, 1, false,"Availability");
//	        DependencyRuleByCathegory dependencyRuleByCathegory = new DependencyRuleByCathegory();
//	        dependencyRuleByCathegory.addDependencyRule(dependencyRule);
//	        String dependencyRuleByCathegoryJSonP = gsonExtP.toJson(dependencyRuleByCathegory, DependencyRuleByCathegory.class);
//            logger.debug("Cat in JSON [" + dependencyRuleByCathegoryJSonP + "]");
//            DependencyRuleByCathegory dependencyRuleByCathegory2 =  gsonExtP.fromJson(dependencyRuleByCathegoryJSonP, DependencyRuleByCathegory.class);
//			logger.trace("Cat Got2: " + dependencyRuleByCathegory2.toString(true));
//            
//	        

            String factInJSon = gsonExt.toJson(fact, Fact.class);
            //logger.debug("Forward JSON [" + factInJSon + "]");
//            String factInJSonP = gsonExtP.toJson(fact, Fact.class);
//            logger.debug("Forward JSON [" + factInJSonP + "]");
//            Fact fact2 = gsonExtP.fromJson(factInJSonP, Fact.class);
//			logger.trace("Got2: " + type + ";" + fact2.toString(true));
            //System.out.println("serialized with the custom serializer:"  +animalJson);
            //IAnimal animal2 = gsonExt.fromJson(animalJson, IAnimal.class);
            //System.out.println(animal2.sound());
		
		
//		String factInJSon = (new Gson()).toJson(fact);
	//	logger.debug("Forward JSON [" + factInJSon + "]");

		if (client != null) {
			logger.debug("Out to client");
			client.println(factInJSon);
		}
	}

}
