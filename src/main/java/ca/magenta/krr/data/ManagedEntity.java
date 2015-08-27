package ca.magenta.krr.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.magenta.krr.fact.Signal;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-02-17
 */
public class ManagedEntity implements FqdNamed{
	
	private static Logger logger = Logger.getLogger(ManagedEntity.class);

	private static final String ME_SEPARATOR = "::";

	//private static final String ME_SEPARATOR_PATTERN_STR = "(:[:g]:)([^:]*).*";
	private static final String ME_SEPARATOR_PATTERN_STR = "(:[:g]:)(.*)";
	private static final String ME_TYPE2NAME_SEPARATOR_PATTERN_STR = "([^:]*)::([^:]*)";

	private static final Pattern ME_SEPARATOR_PATTERN = Pattern.compile(ME_SEPARATOR_PATTERN_STR);
	private static final Pattern ME_TYPE2NAME_SEPARATOR_PATTERN = Pattern.compile(ME_TYPE2NAME_SEPARATOR_PATTERN_STR);


	public ManagedEntity() {
		super();
	}
	
	public ManagedEntity(String clazz, String fqdName) {
		super();
		this.fqdName = fqdName;
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return clazz + ME_SEPARATOR + fqdName;
	}

	public String getFqdName() {
		return fqdName;
	}
	public void setFqdName(String fqdName) {
		this.fqdName = fqdName;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
	private String fqdName = null;
	private String clazz = null;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((fqdName == null) ? 0 : fqdName.hashCode());
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
		ManagedEntity other = (ManagedEntity) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (fqdName == null) {
			if (other.fqdName != null)
				return false;
		} else if (!fqdName.equals(other.fqdName))
			return false;
		return true;
	}
	
	public static Chain<ManagedEntity> parseToManagedEntityChain(String managedEntityChainStr)
	{
		// Goal: parse
		// :::Host::server04:::Application::nimRobot
		// :g:Host::server04:::Application::nimRobot
		
		Chain<ManagedEntity> managedEntityChain = new Chain<ManagedEntity>();

		managedEntityChainStr = managedEntityChainStr.trim();
		
		int groundCount = 0;
		
		if ( ! managedEntityChainStr.isEmpty() )
		{
			String toAnalyse = managedEntityChainStr;
			while ( ! toAnalyse.isEmpty() )
			{
				Matcher meSeparatorMatcher = ME_SEPARATOR_PATTERN.matcher(toAnalyse);
				if (meSeparatorMatcher.find()) {
					String separatorLook = meSeparatorMatcher.group(1);
					toAnalyse = meSeparatorMatcher.group(2);
					String managedEntityPart = toAnalyse;
					meSeparatorMatcher = ME_SEPARATOR_PATTERN.matcher(toAnalyse);
					if (meSeparatorMatcher.find()) {
						managedEntityPart = toAnalyse.substring(0, meSeparatorMatcher.start());
						toAnalyse = toAnalyse.substring(meSeparatorMatcher.start());
					}
					else
					{
						toAnalyse = "";	
					}
					Matcher meType2NameSeparatorMatcher = ME_TYPE2NAME_SEPARATOR_PATTERN.matcher(managedEntityPart);
					if (meType2NameSeparatorMatcher.find()) {
						String clazz = meType2NameSeparatorMatcher.group(1);
						String fqdName = meType2NameSeparatorMatcher.group(2);
						ManagedEntity managedEntity = new ManagedEntity(clazz, fqdName);
						if (separatorLook.equals(Chain.GROUND_INDICATOR) )
						{
							managedEntityChain.addMostSpecificAndSetAsGround(managedEntity);
							groundCount++;
							if (groundCount > 1)
								throw new IllegalArgumentException("Too many ground indicator: [" + managedEntityChainStr +"]");
						}
						else
							managedEntityChain.addMostSpecific(managedEntity);
					}
					else
					{
						throw new IllegalArgumentException("Bad argument format: [" + managedEntityChainStr +"]");
					}
				}
				if ( managedEntityChain.isEmpty() )
				{
					throw new IllegalArgumentException("Bad argument format: [" + managedEntityChainStr +"]");
				}
			}
		}
		
		logger.debug("In:[" + managedEntityChainStr + "]; out:[" + managedEntityChain.toTypedString() + "]");
		
		return managedEntityChain;
	}
	
	
}
