/**
 * @purpose          - A derivative of the Request class, the FloorRequest class 
 *                     stores all the information needed by the scheduler to handle
 *                     events generated by floors. 
 * @param direction  - The direction the calling customer would like to travel. 'u' for
 *       			   for and 'd' for down.
 */

public class FloorRequest{
	private String direction; 
	private String time;
	private int floorNum;
	private int carBut;
	
	
	
	private boolean acceptingFloorRequests;
	private boolean bufferFull;
	
	
	public FloorRequest() {
		this.acceptingFloorRequests = true;
		this.bufferFull = false;
	}
	
	
	public synchronized void add(int n, String t, String d, int c) {
		while(!this.acceptingFloorRequests) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.floorNum = n;
		this.direction = d;
		this.time = t;
		this.carBut = c;
		
		this.acceptingFloorRequests = false;
		
		this.bufferFull = true;
		
		System.out.println(toString());
		notifyAll();
		
	}
	
	public synchronized void remove() {
		while(!this.bufferFull) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		this.acceptingFloorRequests = true;
		
		this.bufferFull = false;		
		System.out.println("Passengers arrived at destination floor!");
		
		notifyAll();
	

	}
		
	public String getDirection() { return this.direction; }
	
	public int getCarBut() { return this.carBut; }
	
	public int getFloorNum() { return this.floorNum; }
	
	public synchronized boolean hasRequest() {return this.bufferFull;}
	
	public String toString() {
		return "Queuing request for floor " + this.floorNum + " at " + this.time + " going " + this.direction + " to floor " + this.carBut;
	}
	
}
