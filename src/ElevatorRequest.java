/**
 * @purpose          - The shared memory between the elevator and scheduler used for communication between
 *   				   the two threads.
 * @param destFloor  - The destination floor
 * @param floorNum   - The floor number the request is coming from
 * @param arrived    - Flag indicating whether the elevator has arrived
 * @param accepteingElevatorRequestions - Flag indicating whether the elevator is accepting requests
 * @param bufferFull - Flag indicating that there is an elevator request to be served 
 * 
*/

public class ElevatorRequest{
	private int destFloor;
	private int floorNum;
	private boolean arrived;
	private boolean acceptingElevatorRequests;
	private boolean bufferFull;


	public ElevatorRequest() {
		this.acceptingElevatorRequests = true;
		this.bufferFull = false;
		this.arrived = false;
	}
	
	
	/*
	 * @purpose  notifies the elevator of an elevator request
	 * @param f - the floor number of the request
	 * @param d - the destination floor of the request
	 * 
	 */
	public synchronized void notifyElevatorRequest(int f, int d) {
		while(!this.acceptingElevatorRequests) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.floorNum = f;
		
		this.destFloor = d;
		
		this.acceptingElevatorRequests = false;
		
		this.bufferFull = true;
		
		this.arrived = false;
		
		notifyAll();
		
	}
	
	/*
	 * @purpose  the elevator updates the scheduler with its current position
	 * @param pos - the current position of of the elevator
	 * @param receivedPassengers - whether the elevator picked up the passengers for the request
	 * 
	 */
	public synchronized void updatedPosition(int pos, boolean receivedPassengers) {
		while(!this.bufferFull) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}
		
		System.out.println("Current Pos of Elevator: "+ pos);
		
		if(receivedPassengers && pos == this.destFloor) {
			this.arrived = true;
			this.bufferFull = false;
		}
		
		notifyAll();
	}
	
	
	/*
	 * @purpose  Notifies the scheduler that the elevator request was served 
	 * 
	 */
	public synchronized void requestServed() {
		
		while(!this.arrived) {
			try {
				wait();
			}catch (InterruptedException e) {
				System.err.println(e);
			}
		}

		this.acceptingElevatorRequests = true;
				
		notifyAll();
	}
	

	public int getFloorNum() { return this.floorNum; }
	
	public int getDestFloor() { return this.destFloor; }
		
	public boolean hasArrived() {return this.arrived;}
	
	public synchronized  boolean hasRequest() {return this.bufferFull;}
	
	public String toString() {
		return "Making elevator request to: " + this.destFloor;
	}
}