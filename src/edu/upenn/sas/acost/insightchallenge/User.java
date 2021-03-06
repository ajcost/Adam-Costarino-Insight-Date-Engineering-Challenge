package edu.upenn.sas.acost.insightchallenge;

/******************************
 * 
 * @author adamcostarino
 * User class
 * 
 * Method Name  -   Description : Runtime
 * getIP               - gets User ip address                         : O(1)
 * isBlocked()         - returns true if blocked and false otherwise  : O(1)
 * getRequests         - gets list of requests user made              : O(1)         
 * getRequestsAtIndex  - gets specific request user made              : O(1)
 * setBlocked          - sets value of blocked boolean                : O(1)
 * addRequest          - adds request to request list                 : O(1)
 * resetFailedLogins   - private helper that resets failed login array: O(1)
 * moniterFailedUserLogins - private helped that helps moniter failed login attempts : O(1)
 ******************************/

import java.util.ArrayList;
import java.util.List;

public class User {
	private String ip;
	private boolean blocked;
	private Request[] mostRecentlyFailedLogins;
	private int failedLogins;
	private List<Request> requests;

	public User(String ip) {
		this.ip = ip;
		blocked = false;
		this.mostRecentlyFailedLogins = new Request[3];
		this.failedLogins = 0;
		requests = new ArrayList<Request>();
	}
	
	public String getIP() {
		return ip;
	}
	
	public boolean isBlocked() {
		return blocked;
	}
	
	public List<Request> getRequests() {
		return requests;
	}
	
	public Request getRequestAtIndex(int i) {
		return requests.get(i);
	}
	
	private void setBlocked(boolean val) {
		blocked = val;
	}
	
	public void addRequest(Request newRequest) {
		if (blocked) {
			if (newRequest.overTime(mostRecentlyFailedLogins[2], "fiveminutes")) {
				setBlocked(false);
				resetFailedLogins();
			}
		}
		if (newRequest.getAddress().equals("/login") && newRequest.getHTTPcode().equals("401") && !blocked) {
			moniterFailedUserLogins(newRequest);
		} else if (newRequest.getAddress().equals("/login") && !blocked) {
			resetFailedLogins();
		}
		requests.add(newRequest);
	}
	
	private void resetFailedLogins() {
		mostRecentlyFailedLogins = new Request[3];
		failedLogins = 0;
	}
	
	private void moniterFailedUserLogins(Request newRequest) {
		if (mostRecentlyFailedLogins[0] == null) {
			mostRecentlyFailedLogins[0] = newRequest;
			failedLogins++;
		} else if (!newRequest.overTime(mostRecentlyFailedLogins[0], "twentyseconds")) {
			mostRecentlyFailedLogins[failedLogins] = newRequest;
			failedLogins++;
			if (failedLogins >= 3) {
				setBlocked(true);
			}
		} else {
			resetFailedLogins();
			mostRecentlyFailedLogins[0] = newRequest;
			failedLogins = 1;
		}
	}
}
