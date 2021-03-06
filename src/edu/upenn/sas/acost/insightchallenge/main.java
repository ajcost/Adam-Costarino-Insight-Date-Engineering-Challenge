package edu.upenn.sas.acost.insightchallenge;

/*********************
 * 
 * @author adamcostarino
 * Main exectuable file: Reads in from log_input/log.txt and intializes the server.
 * 
 * Method Name  - Description : Runtime
 * lineParser   -  parses input string into usable data array   : O(1)
 * logHosts     -  writes top hosts to hosts.txt                : O(1)
 * logResources -  writes top resources to resources.txt        : O(1)
 * logHours     -  writes busiest hours to hours.txt
 * logBlocked   -  writes blocked login requests to blocked.txt : O(1)
 ********************/

import java.io.*;
import java.util.List;
import java.util.Map.Entry;

class Main {
    public static void main(String[] args) {
    	Server server;
    	try {
        	server = new Server();
    		FileInputStream fStream = new FileInputStream("../log_input/log.txt");
    		DataInputStream dStream = new DataInputStream(fStream);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(dStream));
    		String newline;
    		while ((newline = reader.readLine()) != null)   {
    			// Print the content on the console
    			String[] params = lineParser(newline);
    			Request requestParsed = new Request(params[1], params[2], params[3], 
    					params[4], Integer.parseInt(params[5]), newline);
    			server.logRequest(params[0], requestParsed);
    		}
    		//Close the input stream
    		dStream.close();
        	try {
    			logHosts(server);
    			logResources(server);
    			logHours(server);
    			logBlocked(server);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	} catch (Exception e) {//Catch exception if any
    		e.printStackTrace();
    		System.err.println("Error: " + e.getMessage());
    	}
    }

	public static String[] lineParser(String line) {
    	line = line.replace(" ", "|");
    	line = line.replace("[", "|");
    	line = line.replace("]", "|");
    	line = line.replace("-", "0");
    	
    	String[] s = line.split("\\|");
    	String[] splice = new String[6];
    	// Splice Structure - [host, date, command, address, HTTP return code, bytes]
    	splice[0] = s[0];
    	splice[1] = s[4];
    	splice[2] = s[7].replace("\"", "");
    	splice[3] = s[8].trim();
    	splice[4] = s[10].trim();
    	splice[5] = s[11].trim();
    	
    	return splice;
    }
    
    public static void logHosts(Server server) throws IOException {
    	File fout = new File("../log_output/hosts.txt");
    	fout.createNewFile();
    	FileOutputStream fos = new FileOutputStream(fout);
    	OutputStreamWriter osw = new OutputStreamWriter(fos);
    	User[] topUsers = server.getTopTenUserNames();
    	for (int i = 0; i < topUsers.length; i++) {
    		osw.write(topUsers[i].getIP() + "," + topUsers[i].getRequests().size() + "\n");
    	}
    	osw.close();
    }
    
    public static void logResources(Server server) throws IOException {
    	File fout = new File("../log_output/resources.txt");
    	fout.createNewFile();
    	FileOutputStream fos = new FileOutputStream(fout);
    	OutputStreamWriter osw = new OutputStreamWriter(fos);
    	String[] topRequests = server.getTopTenRequests();
    	for (int i = 0; i < topRequests.length; i++) {
    		osw.write(topRequests[i] + "\n");
    	}
    	osw.close();
    }
    
    private static void logHours(Server server) throws IOException{
    	File fout = new File("../log_output/hours.txt");
    	FileOutputStream fos = new FileOutputStream(fout);
    	OutputStreamWriter osw = new OutputStreamWriter(fos);
    	List<Entry<String, Integer>> topHours = server.ripQueueReturn();
    	String date = server.getDateOfRequests();
    	int max = 10;
    	if (topHours.size() < 10) {
    		max = topHours.size();
    	}
    	for (int i = 0; i < max; i++) {
    		String time = topHours.get(i).getKey();
    		int numRequests = topHours.get(i).getValue();
    		osw.write(date + ":" + time + " -400" + "," + numRequests + "\n");
    	}
    	osw.close();
		
	}
    
    public static void logBlocked(Server server) throws IOException {
    	File fout = new File("../log_output/blocked.txt");
    	fout.createNewFile();
    	FileOutputStream fos = new FileOutputStream(fout);
    	OutputStreamWriter osw = new OutputStreamWriter(fos);
    	List<Request> blocked = server.getBlockedRequests();
    	for (int i = 0; i < blocked.size(); i++) {
    		osw.write(blocked.get(i).getOriginalInput() + "\n");
    	}
    	osw.close();
    }
}