package edu.upenn.sas.acost.insightchallenge;
/*********************** 
 * @author adamcostarino
 * Active Hosts Object keeps track of the total hosts that have accessed the site
 * and the number of requests each host has made
 * 
 * Implement with HashMap data structure: get, set, contain, and put methods
 * run in guaranteed constant time. The total formation for the data structure will
 * run n time for n requests from the log.txt file.
 */

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
	private Deque<Request> busyQueue;
	private TreeMap<Integer, Request> busiestTimes;
	private List<Request> blockedRequests;
	
	public Server() {
		 this.users = new HashMap<User, Integer>(); 
		 this.bandwidthTracker = new HashMap<String, Integer>();
		 this.ipToUser = new HashMap<String, User>();
		 this.busyQueue = new ArrayDeque<Request>();
		 this.busiestTimes = new TreeMap<Integer, Request>(Collections.reverseOrder());
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

		if (cur.isBlocked()) {
			blockedRequests.add(newRequest);
		}
		cur.addRequest(newRequest);
		busyQueueUpdate(newRequest);
	}
	
	private void busyQueueUpdate(Request newRequest) {
		busyQueue.offer(newRequest);
		while (newRequest.overTime(busyQueue.peek(), "hour") && busyQueue.size() > 1) {
			busyQueue.pollFirst();
		}
		updateBusiestTimes();
	}
	
	private void updateBusiestTimes() {
		// Break before accidental replacement
		if (busiestTimes.size() < 10 && !busiestTimes.containsValue(busyQueue.peek())) {
			busiestTimes.put(busyQueue.size(), busyQueue.peek());
		} else if (busyQueue.size() > busiestTimes.lastKey()) {
			busiestTimes.put(busyQueue.size(), busyQueue.peek());
			busiestTimes.remove(busiestTimes.lastKey());
		}
	}
	
	public TreeMap<Integer, Request> ripQueueReturn() {
		if (busyQueue.size() > 0) {
			busyQueue.pollFirst();
			updateBusiestTimes();
		}
		return busiestTimes;
	}
	
	public User[] getTopTenUserNames() {
        Set<Entry<User, Integer>> userSet = users.entrySet();
        List<Entry<User, Integer>> userList = new ArrayList<Entry<User, Integer>>(userSet);
		Collections.sort(userList, new Comparator<Map.Entry<User, Integer>>() {
            public int compare(Map.Entry<User, Integer> hOne, Map.Entry<User, Integer> hTwo) {
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
	
	public List<Request> getBlockedRequests() {
		return blockedRequests;
	}
}