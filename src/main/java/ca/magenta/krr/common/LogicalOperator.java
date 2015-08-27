package ca.magenta.krr.common;

import org.apache.log4j.Logger;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-03-10
 */
public class LogicalOperator {
	
	private static Logger logger = Logger.getLogger(LogicalOperator.class);
	
	/*
	==      equal to
	!=      not equal to
	>       greater than
	>=      greater than or equal to
	<       less than
	<=      less than or equal to
	*/
	
	public boolean isTrue(double leftHand, double rightHand)
	{
		logger.debug("In test(" + leftHand +"," +rightHand + ")"); 
		
		boolean isTrue = false;
		
		 switch (operatorIntValue) {
         case EQUAL_VAL: isTrue = (leftHand == rightHand);
         	break;
         case NOT_EQUAL_VAL:  isTrue = (leftHand != rightHand);
         	break;
         case GREATER_THAN_VAL: isTrue = (leftHand > rightHand);
      		break;
         case GREATER_EQUAL_THAN_VAL:  isTrue = (leftHand >= rightHand);
      		break;
         case LESS_THAN_VAL:  isTrue = (leftHand < rightHand);
      		break;
         case LESS_EQUAL_THAN_VAL:  isTrue = (leftHand <= rightHand);
      		break;
         default: throw new RuntimeException("Bad operatorIntValue [" + operatorIntValue + "]");
		 }
		 
		 return isTrue;
	}

	public boolean isTrue(long leftHand, long rightHand)
	{
		//logger.debug("In test(long leftHand, long rightHand)"); 

		boolean isTrue = false;
		
		 switch (operatorIntValue) {
         case EQUAL_VAL: isTrue = (leftHand == rightHand);
         	break;
         case NOT_EQUAL_VAL:  isTrue = (leftHand != rightHand);
         	break;
         case GREATER_THAN_VAL: isTrue = (leftHand > rightHand);
      		break;
         case GREATER_EQUAL_THAN_VAL:  isTrue = (leftHand >= rightHand);
      		break;
         case LESS_THAN_VAL:  isTrue = (leftHand < rightHand);
      		break;
         case LESS_EQUAL_THAN_VAL:  isTrue = (leftHand <= rightHand);
      		break;
         default: throw new RuntimeException("Bad operatorIntValue [" + operatorIntValue + "]");
		 }
		 
		 return isTrue;
	}

	

	@Override
	public String toString() {
		
		String string =  null;
		
		 switch (operatorIntValue) {
         case EQUAL_VAL: string = EQUAL_LABEL;
         	break;
         case NOT_EQUAL_VAL: string = NOT_EQUAL_LABEL;
         	break;
         case GREATER_THAN_VAL: string = GREATER_THAN_LABEL;
      		break;
         case GREATER_EQUAL_THAN_VAL: string = GREATER_EQUAL_THAN_LABEL;
      		break;
         case LESS_THAN_VAL: string = LESS_THAN_LABEL;
      		break;
         case LESS_EQUAL_THAN_VAL: string = LESS_EQUAL_THAN_LABEL;
      		break;
         default: throw new RuntimeException("Bad operatorIntValue [" + operatorIntValue + "]");
		 }
		 
		 return string;
	}

	public LogicalOperator(String operatorLabel) throws IllegalArgumentException, NullPointerException 
	{
		super();
		this.operatorIntValue = parseOperatorImpl(operatorLabel);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + operatorIntValue;
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
		LogicalOperator other = (LogicalOperator) obj;
		if (operatorIntValue != other.operatorIntValue)
			return false;
		return true;
	}
	
	
	private int operatorIntValue = EQUAL_VAL;

	private static final int EQUAL_VAL = 1;
	private static final int NOT_EQUAL_VAL = 2;
	private static final int GREATER_THAN_VAL = 3;
	private static final int GREATER_EQUAL_THAN_VAL = 4;
	private static final int LESS_THAN_VAL = 5;
	private static final int LESS_EQUAL_THAN_VAL = 6;
	
	public static final LogicalOperator EQUAL = new LogicalOperator(EQUAL_VAL);
	public static final LogicalOperator NOT_EQUAL = new LogicalOperator(NOT_EQUAL_VAL);
	public static final LogicalOperator GREATER_THAN = new LogicalOperator(GREATER_THAN_VAL);
	public static final LogicalOperator GREATER_EQUAL_THAN = new LogicalOperator(GREATER_EQUAL_THAN_VAL);
	public static final LogicalOperator LESS_THAN = new LogicalOperator(LESS_THAN_VAL);
	public static final LogicalOperator LESS_EQUAL_THAN = new LogicalOperator(LESS_EQUAL_THAN_VAL);

    
    private static final String EQUAL_LABEL = "==";
    private static final String NOT_EQUAL_LABEL = "!=";
    private static final String GREATER_THAN_LABEL = ">";
    private static final String GREATER_EQUAL_THAN_LABEL = ">=";
    private static final String LESS_THAN_LABEL = "<";
    private static final String LESS_EQUAL_THAN_LABEL = "<=";

	private static int parseOperatorImpl(String s) throws IllegalArgumentException, NullPointerException
	{
		int operatorIntValue =  0;
		
		if (s != null)
		{
			s = s.trim();
			
			if (s.equals(EQUAL_LABEL)) 
			{
				operatorIntValue = EQUAL_VAL;
			}
			else if (s.equals(NOT_EQUAL_LABEL)) 
			{
				operatorIntValue = NOT_EQUAL_VAL;
			}
			else if (s.equals(GREATER_THAN_LABEL)) 
			{
				operatorIntValue = GREATER_THAN_VAL;
			}
			else if (s.equals(GREATER_EQUAL_THAN_LABEL)) 
			{
				operatorIntValue = GREATER_EQUAL_THAN_VAL;
			}
			else if (s.equals(LESS_THAN_LABEL)) 
			{
				operatorIntValue = LESS_THAN_VAL;
			}
			else if (s.equals(LESS_EQUAL_THAN_LABEL)) 
			{
				operatorIntValue = LESS_EQUAL_THAN_VAL;
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
		 
		 return operatorIntValue;
	}
    
	private LogicalOperator() {
		super();
	}
	
	private  LogicalOperator(int operatorIntValue) {
		this.operatorIntValue = operatorIntValue;
	}

	/*
	==      equal to
	!=      not equal to
	>       greater than
	>=      greater than or equal to
	<       less than
	<=      less than or equal to
	*/
	
	public static final void main(String[] args) {
		
	long lSeven = 7;
	long lNine = 9;
	int iSeven = 7;
	int iNine = 9;
	double dSeven = 7.0;
	double dNine = 9.0;
	float fSeven = 7;
	float fNine = 9;
	
	logger.trace("== " + (EQUAL.equals(new LogicalOperator("=="))));
	logger.trace("!= == ==" + (NOT_EQUAL.equals(new LogicalOperator("=="))));
	logger.trace("!= " + (NOT_EQUAL.equals(new LogicalOperator("!="))));
	logger.trace(">" + (GREATER_THAN.equals(new LogicalOperator(">"))));
	logger.trace(">=" + (GREATER_EQUAL_THAN.equals(new LogicalOperator(">="))));
	logger.trace("<" + (LESS_THAN.equals(new LogicalOperator("<"))));
	logger.trace("<=" + (LESS_EQUAL_THAN.equals(new LogicalOperator("<="))));
	
	
	logger.trace("7.0 == 9.0 " + EQUAL.isTrue(dSeven, dNine)); 
	logger.trace("7.0 != 9.0 " + NOT_EQUAL.isTrue(dSeven, dNine)); 
	logger.trace("7.0 > 9.0 " + GREATER_THAN.isTrue(dSeven, dNine)); 
	logger.trace("7.0 >= 7.0 " + GREATER_EQUAL_THAN.isTrue(dSeven, dSeven)); 
	logger.trace("7.0 < 9.0 " + LESS_THAN.isTrue(dSeven, dNine)); 
	logger.trace("7.0 <= 7.0 " + LESS_EQUAL_THAN.isTrue(dSeven, dSeven)); 

	logger.trace("7long == 9long " + EQUAL.isTrue(lSeven, lNine)); 
	
	logger.trace("7int == 9int " + EQUAL.isTrue(iSeven, iNine)); 

	logger.trace(fSeven + "(float) == " + fNine + "(float) " + EQUAL.isTrue(fSeven, fNine)); 

	logger.trace(iSeven + "(int) == " + dNine + "(double) " + EQUAL.isTrue(iSeven, dNine)); 
	
	
	
	}
}
