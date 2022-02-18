/**
 * @purpose          - The shared memory between the floor and scheduler used for communication between
 *   				   the two threads.
 * @param direction  - The direction of travel for the request
 * @param floorNum   - The floor number the request is coming from
 * @param carBut     - The destination floor 
 * @param time       - The time of the request
 * @param acceptingFloorRequests - Flag indicating the scheduler is accepting floor requests
 * @param bufferFull - Flag indicating that the scheduler already is handling a floor request
 * 
 */

public class FloorRequest{
	private String direction; 
	private String time;
	private int floorNum;
	private int carBut;
	private int requestsServed;
	
	
	
	private boolean acceptingFloorRequests;
	private boolean bufferFull;
	
	
	public FloorRequest() {
		this.acceptingFloorRequests = true;
		this.bufferFull = false;
		this.requestsServed = 0;
	}
	
	
	
	/*
	 * @purpose  adds the floor request
	 * @param n - the floor number
	 * @param t - the time of the request
	 * @param d - the direction of travel
	 * @param c - the destination floor number
	 * 
	 */
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
		
		TraceFile.toTrace(toString() + "\n");
		notifyAll();
		
	}
	
	/*
	 * @purpose  removes the floor request from the buffer, allowing for new floor requests
	 * 
	 */
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
		TraceFile.toTrace("Passengers arrived at destination floor!\n");
		this.requestsServed++;
		if(this.requestsServed == 3) {
			TraceFile.toTrace("EOD");
			TraceFile.closeTrace();
		}
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
