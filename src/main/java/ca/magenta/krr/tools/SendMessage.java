package ca.magenta.krr.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.magenta.krr.fact.Message;
import ca.magenta.utils.HashMapVector;
import ca.magenta.utils.XLSXFile;

/**
 * @author jean-paul.laberge <jplaberge@magenta.ca>
 * @version 0.1
 * @since 2014-04-12
 */
public class SendMessage {

	private static String version = "0.1 (2014-04-12)";

	private static Logger logger = Logger.getLogger(SendMessage.class);

	private static Boolean defaulActionIsClear = false;
	private static Format format_opt = null;
	private static String msgServerAddr_opt = null;
	private static int msgServerPort_opt = -1;
	private static String messageFile_opt = null;
	private static Vector<LineAndAction> lines_opt = new Vector<LineAndAction>();

	public enum Format {
		SMARTS, SIMPLE
	}

	public static final void main(String[] args) {

		int rc = 0;

		logger.info("");
		logger.info("Running SendMessage version " + version);

		rc = parseParam(args);

		try {

			Socket client = new Socket(msgServerAddr_opt, msgServerPort_opt);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);

			HashMapVector eventFileData = getMessageFileData(messageFile_opt);

			if (rc == 0) {

				if (format_opt == Format.SMARTS)
					sendMessages_smartsFormat(out, eventFileData, lines_opt);
				else
					sendMessages_simpleFormat(out, eventFileData, lines_opt, false);

			}
			out.close();
			client.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static boolean sendMessages_simpleFormat(PrintWriter out, HashMapVector eventFileData, Vector<LineAndAction> lines, boolean isClearing)
			throws UnknownHostException, IOException {

		for (LineAndAction line : lines) {
			sendMessage_simpleFormat(out, eventFileData, line.line, line.clearing);
		}

		return true;

	}

	public static boolean sendMessages_smartsFormat(PrintWriter out, HashMapVector eventFileData, Vector<LineAndAction> lines)
			throws UnknownHostException, IOException {

		for (LineAndAction line : lines) {
			sendMessage_smartsFormat(out, eventFileData, line.line);
		}

		return true;

	}

	public static void sendMessage_smartsFormat(PrintWriter out, HashMapVector eventFileData, int line) throws UnknownHostException, IOException {
		sendMessage_smartsFormat(out, eventFileData, line, null /* all: active + not active */);
	}

	public static void sendMessage_smartsFormat(PrintWriter out, HashMapVector eventFileData, int line, Boolean activeOnly) throws UnknownHostException,
			IOException {
		HashMap<String, String> rowData = eventFileData.get(line - 2);

		String type = rowData.get("type");
		boolean active = Boolean.parseBoolean(rowData.get("active"));

		if ((activeOnly == null) || ((activeOnly) && (active)) || ((!activeOnly) && (!active))) {
			Message message = new Message(UUID.randomUUID().toString(), type, System.currentTimeMillis(), rowData);

			String msgString = (new Gson()).toJson(message);
			out.println(msgString);

			logger.info(line + " : " + message);
		}
	}

	public static void sendMessage_simpleFormat(PrintWriter out, HashMapVector eventFileData, int line, boolean isClearing) throws UnknownHostException,
			IOException {

		HashMap<String, String> rowData = eventFileData.get(line - 2);

		String type = rowData.get("type");

		rowData.put("cleared", Boolean.toString(isClearing));

		Message message = new Message(UUID.randomUUID().toString(), type, System.currentTimeMillis(), rowData);

		String msgString = (new Gson()).toJson(message);
		out.println(msgString);

		logger.trace(line + " : " + message);
	}

	public static HashMapVector getMessageFileData(String messageFile) {
		return new HashMapVector(new XLSXFile(messageFile));
	}

	private static int parseParam(String a_sArgs[]) {
		int rc = 0;

		if (a_sArgs.length > 0) {
			for (int i = 0; i < a_sArgs.length; i++) {
				if (a_sArgs[i].startsWith("-msgServerAddr=")) {
					msgServerAddr_opt = a_sArgs[i].substring(15);
					logger.info("msgServerAddr: [" + msgServerAddr_opt + "]");
				} else if (a_sArgs[i].startsWith("-msgServerPort=")) {
					String msgServerPortStr = a_sArgs[i].substring(15);
					try {
						msgServerPort_opt = Integer.parseInt(msgServerPortStr);
						logger.info("msgServerPort: [" + msgServerPort_opt + "]");

					} catch (NumberFormatException e) {
						logger.error("Bad msgServerPort: [" + msgServerPortStr + "]");
						rc = 1;
					}
				} else if (a_sArgs[i].startsWith("-defaultAction=")) {
					String defaultAction = a_sArgs[i].substring(15);
					if (defaultAction.toLowerCase().equals("raise"))
					{
						defaulActionIsClear = false;
					}
					else if (defaultAction.toLowerCase().equals("clear"))
					{
						defaulActionIsClear = true;
					}
					else
						rc = 1;
					
				} else if (a_sArgs[i].startsWith("-eventFile=")) {
					messageFile_opt = a_sArgs[i].substring(11);
					logger.info("eventFile: [" + messageFile_opt + "]");
				} else if (a_sArgs[i].equals("-simpleFormat")) {
					format_opt = Format.SIMPLE;
					logger.info("inputFormat:[" + format_opt + "]");
				} else if (a_sArgs[i].equals("-smartsFormat")) {
					format_opt = Format.SMARTS;
					logger.info("inputFormat:[" + format_opt + "]");
				} else if (a_sArgs[i].startsWith("-")) {
					rc = 1;
				} else {
					String linesInput = a_sArgs[i];
					String[] linesStr = linesInput.split(",");
					for (String lineStr : linesStr) {
						String[] linesStr2 = lineStr.split("-");
						if (linesStr2.length > 2) {
							logger.error("Bad line list: [" + linesInput + "]");
							rc = 1;
							break;
						} else if (linesStr2.length == 2) {
							try {
								LineAndAction lineAndAction = new LineAndAction(linesStr2[0]);
								int line2 = Integer.parseInt(linesStr2[1]);
								for (int i2 = lineAndAction.line; i2 <= line2; i2++) {
									lines_opt.add(new LineAndAction(i2, lineAndAction.clearing));
								}
							} catch (IllegalArgumentException e) {
								logger.error("Bad line list: [" + linesInput + "]");
								rc = 1;
								break;
							}

						} else if (linesStr2.length == 1) {
							try {
								LineAndAction lineAndAction = new LineAndAction(linesStr2[0]);
								lines_opt.add(lineAndAction);
							} catch (IllegalArgumentException e) {
								logger.error("Bad line list: [" + linesInput + "]");
								rc = 1;
								break;
							}
						}
					}
					if (rc == 0) {
						logger.info("lines: [" + linesInput + "]");
					}
				}
			}
		}

		if ((msgServerAddr_opt == null) || (msgServerPort_opt == -1) || (messageFile_opt == null) || (format_opt == null) || (lines_opt.size() < 1)
				|| (rc != 0)) {
			System.err
					.println("Usage: SendMessage [-smartsFormat|-simpleFormat] [-defaultAction=[raise|clear]] -msgServerAddr=msgServerAddr -msgServerPort=msgServerPort -eventFile=eventFile lineList");
			System.err
					.println("       Line could be : number{:clear|raise} or startNumber{:clear|raise}-endNumeber");
			
			System.err.println("Ex:    SendMessage -simpleFormat -msgServerAddr=127.0.0.1 -msgServerPort=9292 -eventFile=eventFile 2,4,7");
			System.err.println("       SendMessage -simpleFormat -msgServerAddr=127.0.0.1 -msgServerPort=9292 -eventFile=eventFile 2,4,7-10");
			System.err.println("       SendMessage -simpleFormat -msgServerAddr=127.0.0.1 -msgServerPort=9292 -eventFile=eventFile 2:clear,4:raise,7:clear-10");

			rc = 1;
		}
		else
			logger.info("defaulActionIsClear: [" + defaulActionIsClear + "]");

		return rc;
	}

	static class LineAndAction {
		public LineAndAction(Integer line, Boolean cleared) {
			super();
			this.line = line;
			this.clearing = cleared;
		}

		public LineAndAction(String string) {
			String[] parts = string.split(":");

			clearing = defaulActionIsClear;
			if (parts.length > 2)
				throw new IllegalArgumentException("Bad line and action: [" + string + "]");
			else if (parts.length == 2) {
				String clearingStr = parts[1].toLowerCase();

				line = Integer.parseInt(parts[0]);
				if (clearingStr.equals("clear"))
					clearing = true;
				else if (clearingStr.equals("raise")) {
					clearing = false;
				} else
					throw new IllegalArgumentException("Bad line and action: [" + string + "]");
			} else if (parts.length == 1) {
				line = Integer.parseInt(parts[0]);
			}
		}

		Integer line = null;
		Boolean clearing = null;
	}

}
