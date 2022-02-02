/**
 * @author Billal Ghadie
 * @purpose         - Simple "Request" base class. It is used to derive different
 * 			          types of requests. 
 * @param floorNum  - The floor number associated with the request
 * @param timeStamp - The time the request was sent, form of HH:MM:SS.sss
 */

public class Request {
	private int    floorNum;
	private String timeStamp; 
	
	public Request(int n, String t) {
		this.floorNum  = n;
		this.timeStamp = t;
	}
	
	protected int getFloorNum() { return this.floorNum; }
	
	protected String getTimeStamp() { return this.timeStamp; }
	
	
}
