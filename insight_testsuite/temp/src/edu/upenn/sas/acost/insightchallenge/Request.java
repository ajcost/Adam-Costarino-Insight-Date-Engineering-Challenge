package edu.upenn.sas.acost.insightchallenge;
/******************************
 * 
 * @author adamcostarino
 * Description: Class that contains information 
 * about each request to the server
 * 
 * Method Name - Method Description : Runtime
 * getAddress - returns address of URI request was made to : O(1)
 * getTime    - returns time request was made              : O(1)
 * getBytes   - returns bytes of request                   : O(1)
 * monthEval  - private method that converts month string  : O(1)
 *              to integer representation
 * overHour   - returns boolean value true if input time   : O(1)
 *              occurs and hour before time of this request
 ******************************/

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class Request {
	private String time;
	private String cmd;
	private String address;
	private String httpReturnCode;
	private Calendar date;
	private int bytes;
	private String originalInput;
	
	Request(String time, String cmd, String address, String httpReturnCode, 
			int bytes, String originalInput) {
		this.time = time;
		this.cmd = cmd;
		this.address = address;
		this.httpReturnCode = httpReturnCode;
		this.bytes = bytes;
		this.originalInput = originalInput;
		String[] dateArr = time.split("/");
		String[] timeArr = dateArr[2].split(":");
		this.date = new GregorianCalendar(
				Integer.parseInt(timeArr[0]),
				this.monthEval(dateArr[1]),
				Integer.parseInt(dateArr[0]),
				Integer.parseInt(timeArr[1]),
				Integer.parseInt(timeArr[2]),
				Integer.parseInt(timeArr[3]));
	}
	
	public String getTime() {
		return time;
	}
	
	public String getCMD() {
		return cmd;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getHTTPcode() {
		return httpReturnCode;
	}
	
	public int getBytes() {
		return bytes;
	}
	
	public String getOriginalInput() {
		return originalInput;
	}
	
	public Calendar getDate() {
		return date;
	}
	
	private int monthEval(String month) {
        switch (month) {
        case "Jan":  return 1;
        case "Feb":  return 2;
        case "Mar":  return 3;
        case "Apr":  return 4;
        case "May":  return 5;
        case "Jun":  return 6;
        case "Jul":  return 7;
        case "Aug":  return 8;
        case "Sep":  return 9;
        case "Oct":  return 10;
        case "Nov":  return 11;
        case "Dec":  return 12;
        default: return -1;
        }
	}
	
	public boolean overTime(Request compare, String time) {
		long dateMilliSeconds = date.getTimeInMillis();
		long compareMilliSeconds = compare.getDate().getTimeInMillis();
		if (time.equals("hour")) {
			long timeBetween = TimeUnit.MILLISECONDS.toHours(dateMilliSeconds - compareMilliSeconds);
			if (timeBetween < 0) {
				return false;
			} else if (Math.floor(timeBetween) >= 1) {
				return true;
			}
			return false;
		} else if (time.equals("fiveminutes")) {
			long timeBetween = TimeUnit.MILLISECONDS.toMinutes(dateMilliSeconds - compareMilliSeconds);
			if (timeBetween < 0) {
				return false;
			} else if (Math.floor(timeBetween) >= 5) {
				return true;
			}
			return false;
		} else if (time.equals("twentyseconds")) {
			long timeBetween = TimeUnit.MILLISECONDS.toSeconds(dateMilliSeconds - compareMilliSeconds);
			if (timeBetween < 0) {
				return false;
			} else if (Math.floor(timeBetween) >= 20) {
				return true;
			}
			return false;
		} else {
			return false;
		}
	}
}
