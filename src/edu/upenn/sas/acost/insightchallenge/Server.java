package edu.upenn.sas.acost.insightchallenge;

/********************
 * @author adamcostarino
 * 
 * Server Object keeps track of:
 * All requests made to the server
 * All Users who have made requests to the server
 * Most frequent users
 * Resources using the most bandwidth
 * Most popular hours of access
 * Logs blocked attempts to login
 * 
 * Method Name - Description - Runtime
 * logRequest          - logs request made to the server and updates         : O(1)
 *                       relevant data structures
 * updateLinearTimeMap - private method that creates a HashMap that          : O(m), where m is the number of seconds between first and last request
 *                       holds all times between first and last requests
 *                       and maps to requests, if any, that happened
 *                       during that specific period
 * moreThanHour        - private method that evaluates the distance between  : O(1)
 *                       two time strings, it returns true if over and hour 
 *                       and false otherwise
 * iterateThroughLinearTimeMap - private method that iterates through the    : O(m), where m is the number of seconds between first and last request
 *                               linear time map keeping track of the busiest
 *                               times
 * getTopTenUserNames  - uses optimal algorithm (merge sort) to sort the     : O(n*logn) where n is the total number of users
 *                       users hashmap by values then returns the top ten.
 *                       ties are determined lexicographically
 * 
 * getTopTenRequests   - rses optimal algorithm (merge sort) to sort the     : O(n*logn) where n is the total number of users
 *                       bandwidthTracker hashmap by values then returns 
 *                       the top ten. ties are determined lexicographically
 * ripQueueReturn      - runs through the busyQueue to find any other times  : O(n*logn) where m is the total number of busy times logged
 *                       that were not logged initially. uses merge sort to
 *                       sort the busiestTimes map by values
 * getBlockedRequest   - returns the blockedRequest List which is formed     : O(1)
 *                       during each log request. the process of forming the
 *                       list will take n time, where n is the number of
 *                       requests. however the method only takes constant to
 *                       return.
 ********************/

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.*;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


class Server {
	private Map<User, Integer> users;
	private Map<String, Integer> bandwidthTracker;
	private Map<String, User> ipToUser;
	private Deque<String> busyQueue;
	private SortedMap<String, Request> linearTimeMap;
	private Map<String, Integer> busiestTimes;
	private List<Request> blockedRequests;
	private String date;
	
	public Server() {
		 this.users = new HashMap<User, Integer>(); 
		 this.bandwidthTracker = new HashMap<String, Integer>();
		 this.ipToUser = new HashMap<String, User>();
		 this.busyQueue = new ArrayDeque<String>();
		 this.linearTimeMap = new TreeMap<String, Request>();
		 this.busiestTimes = new HashMap<String, Integer>();
		 this.blockedRequests = new ArrayList<Request>();
	}
	
	public void logRequest(String ip, Request newRequest) {
		int val = 0;
		User cur;
		// Check if new User
		if (ipToUser.containsKey(ip)) {
			cur = ipToUser.get(ip);
			val = users.get(cur);
		} else {
			cur = new User(ip);	
			ipToUser.put(ip, cur);
		}
		// Check if new Request
		if (bandwidthTracker.containsKey(newRequest.getAddress())) {
			bandwidthTracker.put(newRequest.getAddress(), 
					bandwidthTracker.get(newRequest.getAddress()) + newRequest.getBytes());
		} else {
			bandwidthTracker.put(newRequest.getAddress(), newRequest.getBytes());
		}
		users.put(cur, val + 1);
		
		boolean justBlocked = false;
		if (cur.isBlocked()) {
			blockedRequests.add(newRequest);
			justBlocked = true;
		}
		cur.addRequest(newRequest);
		if (!cur.isBlocked() && justBlocked) {
			blockedRequests.remove(blockedRequests.size() - 1);
		}
		if (linearTimeMap.size() == 0) {
			linearTimeMap.put(newRequest.getClockString(), newRequest);
			date = newRequest.getDateString();
		} else {
			updateLinearTimeMap(newRequest);
		}
	}
	
	private void updateLinearTimeMap(Request newRequest) {
		String lastTime = linearTimeMap.lastKey();
		String nextRequestTime = newRequest.getClockString();
		while (!lastTime.equals(nextRequestTime)) {
			String [] hms = lastTime.split(":");
			if (hms[2].equals("59")) {
				if (hms[1].equals("59")) {
					int hs = Integer.parseInt(hms[0]);
					hs++;
					String hsString = "" + hs;
					if (hs < 10) {
						hsString = "0" + hs;
					}
					lastTime = hsString + ":" + "00" + ":" + "00";
				} else {
					int mins = Integer.parseInt(hms[1]);
					mins++;
					String minsString = "" + mins;
					if (mins < 10) {
						minsString = "0" + mins;
					}
					lastTime = hms[0] + ":" + minsString + ":" + "00";
				}
			} else {
				int secs = Integer.parseInt(hms[2]);
				secs++;
				String secsString = "" + secs;
				if (secs < 10) {
					secsString = "0" + secs;
				}
				lastTime = hms[0] + ":" + hms[1] + ":" + secsString;
			}
			linearTimeMap.put(lastTime, new Request("0/0/0:00:00:00", "none", 
					"none", "none", 0, "none"));
		}
		linearTimeMap.put(lastTime, newRequest);
	}
	
	private boolean moreThanHour(String timeOne, String timeTwo) {
		if (timeOne == null || timeTwo == null) {
			return false;
		}
		String[] timeOneArr = timeOne.split(":");
		String[] timeTwoArr = timeTwo.split(":");
		if (timeOneArr[1].equals(timeTwoArr[1]) && timeOneArr[2].equals(timeTwoArr[2])) {
			int hourOne = Integer.parseInt(timeOneArr[0]);
			int hourTwo = Integer.parseInt(timeTwoArr[0]);
			return hourOne > hourTwo;
		}
		return false;
	}
	
	private int iterateThroughLinearTimeMap() {
		int numRequests = 1;
		for (Map.Entry<String, Request> entry : linearTimeMap.entrySet()) {
			busyQueue.offerLast(entry.getKey());
			if (!linearTimeMap.get(entry.getKey()).getAddress().equals("none")) {
				numRequests++;
			}
			if (moreThanHour(entry.getKey(), busyQueue.peekFirst())) {
				String remove = busyQueue.removeFirst();
				if (!linearTimeMap.get(remove).getAddress().equals("none")) {
					numRequests--;
				}
			}
			if (!busiestTimes.containsKey(busyQueue.peekFirst()) || 
					busiestTimes.get(busyQueue.peekFirst()) < numRequests) {
				busiestTimes.put(busyQueue.peekFirst(), numRequests);
			}
		}
		return numRequests;
	}
	
	public User[] getTopTenUserNames() {
        Set<Entry<User, Integer>> userSet = users.entrySet();
        List<Entry<User, Integer>> userList = new ArrayList<Entry<User, Integer>>(userSet);
		Collections.sort(userList, new Comparator<Map.Entry<User, Integer>>() {
            public int compare(Map.Entry<User, Integer> hOne, Map.Entry<User, Integer> hTwo) {
            	// Lexicographic comparison if the values are equal
            	if (hTwo.getValue() == hOne.getValue()) {
            		return hOne.getKey().getIP().compareTo(hTwo.getKey().getIP());
            	}
                return (hTwo.getValue()).compareTo(hOne.getValue());
            }
        });
		// Check if less than ten users
		int tenSize = 10;
		if (tenSize > users.size()) {
			tenSize = users.size();
		}
		User[] ten = new User[tenSize];
        for (int i = 0; i < ten.length; i++) {
        	User p = userList.get(i).getKey();
        	ten[i] = p;
        }
        return ten;
	}
	
	public String[] getTopTenRequests() {
        Set<Entry<String, Integer>> bandwidthSet = bandwidthTracker.entrySet();
        List<Entry<String, Integer>> bandwidthList = new ArrayList<Entry<String, Integer>>(bandwidthSet);
		Collections.sort(bandwidthList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> hOne, Map.Entry<String, Integer> hTwo) {
            	// Lexicographic comparison if the values are equal
            	if (hTwo.getValue() == hOne.getValue()) {
            		return hOne.getValue().compareTo(hTwo.getValue());
            	}
                return (hTwo.getValue()).compareTo(hOne.getValue());
            }
        });
		// Check if less than ten resources
		int tenSize = 10;
		if (tenSize > bandwidthTracker.size()) {
			tenSize = bandwidthTracker.size();
		}
		String[] ten = new String[tenSize];
		for (int i = 0; i < ten.length; i++) {
			String r = bandwidthList.get(i).getKey();
			ten[i] = r;
		}
		return ten;
	}
	
	public List<Entry<String, Integer>> ripQueueReturn() {
		int numRequests = iterateThroughLinearTimeMap();
		while (busyQueue.size() > 0) {
			String key = busyQueue.removeFirst();
			if (!linearTimeMap.get(key).getAddress().equals("none")) {
				numRequests--;
			}
			if (!busiestTimes.containsKey(key) || 
					busiestTimes.get(key) < numRequests) {
				busiestTimes.put(key, numRequests);
			}
		}
        Set<Entry<String, Integer>> timeSet = busiestTimes.entrySet();
        List<Entry<String, Integer>> timeList = new ArrayList<Entry<String, Integer>>(timeSet);
		Collections.sort(timeList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> hOne, Map.Entry<String, Integer> hTwo) {
            	if (hTwo.getValue() == hOne.getValue()) {
            		return hOne.getKey().compareTo(hTwo.getKey());
            	}
                return (hTwo.getValue()).compareTo(hOne.getValue());
            }
        });
		return timeList;
	}
	
	public List<Request> getBlockedRequests() {
		return blockedRequests;
	}

	public String getDateOfRequests() {
		return date;
	}
}