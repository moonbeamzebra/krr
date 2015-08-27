package ca.magenta.krr.common;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-16
 */
public class Severity {
	
	@Override
	public String toString() {
		
		String string =  null;
		
		 switch (severityIntValue) {
         case CRITICAL_VAL: string = CRITICAL_LABEL;
                  break;
         case MAJOR_VAL: string = MAJOR_LABEL;
                  break;
         case MINOR_VAL: string = MINOR_LABEL;
                  break;
         case WARNING_VAL: string = WARNING_LABEL;
                  break;
         case INDETERMINATE_VAL: string = INDETERMINATE_LABEL;
                  break;
         case OK_VAL: string = OK_LABEL;
                  break;
         default: string = null;
                  break;
		 }
		 
		 return string;
	}
	
	public Severity(String severityLabel) throws IllegalArgumentException, NullPointerException 
	{
		super();
		this.severityIntValue = parseSeverityImpl(severityLabel);
	}
	
	public Severity increaseSeverity()
	{
		if (severityIntValue > MOST_SEVERE_VAL)
		{
			return new Severity(severityIntValue-1);
		}
		else
		{
			return MOST_SEVERE;
		}
	}

	public Severity decreaseSeverity()
	{
		if (severityIntValue < LESS_SEVERE_VAL)
		{
			return new Severity(severityIntValue+1);
		}
		else
		{
			return LESS_SEVERE;
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + severityIntValue;
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
		Severity other = (Severity) obj;
		if (severityIntValue != other.severityIntValue)
			return false;
		return true;
	}


	private int severityIntValue = OK_VAL;

	
	private static final int CRITICAL_VAL = 1;		// Red
	private static final int MAJOR_VAL = 2;			// Orange
	private static final int MINOR_VAL = 3;			// Light orange
	private static final int WARNING_VAL = 4;		// Yellow
	private static final int INDETERMINATE_VAL = 5;	// Blue
	private static final int OK_VAL = 6;				// Green
	private static final int MOST_SEVERE_VAL = CRITICAL_VAL;
	private static final int LESS_SEVERE_VAL = OK_VAL;
	
	public static final Severity CRITICAL = new Severity(CRITICAL_VAL);			// Red
	public static final Severity MAJOR = new Severity(MAJOR_VAL);					// Orange
	public static final Severity MINOR = new Severity(MINOR_VAL);					// Light orange
	public static final Severity WARNING = new Severity(WARNING_VAL);				// Yellow
	public static final Severity INDETERMINATE = new Severity(INDETERMINATE_VAL);	// Blue
	public static final Severity OK = new Severity(OK_VAL);						// Green
	public static final Severity MOST_SEVERE = new Severity(MOST_SEVERE_VAL);
	public static final Severity LESS_SEVERE = new Severity(LESS_SEVERE_VAL);

    
    private static final String CRITICAL_LABEL = "CRITICAL";
    private static final String MAJOR_LABEL = "MAJOR";
    private static final String MINOR_LABEL = "MINOR";
    private static final String WARNING_LABEL = "WARNING";
    private static final String INDETERMINATE_LABEL = "INDETERMINATE";
    private static final String OK_LABEL = "OK";
    
	private static int parseSeverityImpl(String s) throws IllegalArgumentException, NullPointerException
	{
		int severityIntValue =  0;
		
		if (s != null)
		{
			s = s.trim();
			
			if (s.toUpperCase().equals(CRITICAL_LABEL)) 
			{
				severityIntValue = CRITICAL_VAL;
			}
			else if (s.toUpperCase().equals(MAJOR_LABEL)) 
			{
				severityIntValue = MAJOR_VAL;
			}
			else if (s.toUpperCase().equals(MINOR_LABEL)) 
			{
				severityIntValue = MINOR_VAL;
			}			
			else if (s.toUpperCase().equals(WARNING_LABEL)) 
			{
				severityIntValue = WARNING_VAL;
			}
			else if (s.toUpperCase().equals(INDETERMINATE_LABEL)) 
			{
				severityIntValue = INDETERMINATE_VAL;
			}
			else if (s.toUpperCase().equals(OK_LABEL)) 
			{
				severityIntValue = OK_VAL;
			}
			else
			{
				throw new IllegalArgumentException("Error parsing [" + s + "]");
			}
		}
		else
		{
			throw new NullPointerException();
		}
		 
		 return severityIntValue;
	}
	
	public Severity() {
		super();
	}

	private Severity(int severityIntValue) {
		super();
		this.severityIntValue = severityIntValue;
	}
}
