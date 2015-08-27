package ca.magenta.neo4j;


/**
 * Generic Neo4j exception.
 *
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-21
 */
public class Neo4jException extends RuntimeException {

 	private static final long serialVersionUID = -8868471708076821359L;

	public Neo4jException(String problemDetail) {
        super(problemDetail.toString());
    }
	
	   public Neo4jException(String problemDetail, Throwable cause) {
	        super(problemDetail.toString(), cause);
	    }

}
