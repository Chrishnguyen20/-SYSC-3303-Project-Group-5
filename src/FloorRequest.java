import java.time.LocalTime;

/**
 * @purpose           - The shared memory between the floor and scheduler used for communication between
 *   				   the two threads.
 * @param direction   - The direction of travel for the request
 * @param floorNum    - The floor number the request is coming from
 * @param carBut      - The destination floor 
 * @param time        - The time of the request
 * @param acceptingFloorRequests - Flag indicating the scheduler is accepting floor requests
 * @param bufferFull  - Flag indicating that the scheduler already is handling a floor request
 * @param maxRequests - Keeps track of the maximum number of requests;
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
	 * @param newFloorNum - the floor number
	 * @param newTime - the time of the request
	 * @param newDirection - the direction of travel
	 * @param newCarBut - the destination floor number
	 * 
	 */
	public synchronized void add(int newFloorNum, String newTime, String newDirection, int newCarBut) {
		while(!this.acceptingFloorRequests) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.floorNum = newFloorNum;
		this.time = newTime;
		this.direction = newDirection;
		this.carBut = newCarBut;
		
		this.acceptingFloorRequests = false;
		
		this.bufferFull = true;
		
    	LocalTime d = LocalTime.now();
		TraceFile.toTrace("Floor Subsystem: " + toString() + ". Time stamp: " + d.toString() + "\n");
		
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
		
    	LocalTime d = LocalTime.now();
		TraceFile.toTrace("Scheduler Subsystem: Passengers arrived at destination floor! Time Stamp: " + d.toString() + "\n");
		
		
		this.requestsServed++;
		if(this.requestsServed == 3) {
			TraceFile.toTrace("EOF");
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