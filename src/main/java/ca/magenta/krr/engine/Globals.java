package ca.magenta.krr.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import ca.magenta.krr.KRR;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-12-12
 */
public class Globals {


	public static Logger logger = Logger.getLogger(Globals.class);


    public static final String KRR_PROPERTY_FILE_NAME = "KRR.properties";
	public static final boolean CLEARING = true;
	public static final boolean RAISING = ! CLEARING;
	public static final boolean UPDATING = RAISING;
	public static final int API_SERVER_PORT = 9595;

	public static final String DEFAULT_NEO4J_HOST = "127.0.0.1";
	public static final String DEFAULT_NEO4J_PORT = "7474";
	public static final String DEFAULT_DB_HOST = null;
	public static final String DEFAULT_DB_PORT = "-1";
	public static final String DEFAULT_DB_TEST_VALUE = "false";


}
