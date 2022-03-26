import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collections;


/**     
 * @purpose                  - The Elevator class obtains elevator requests from the scheduler
 * 					           and will move until it gets to the destination floor
 * @param currentFloor       - Represents the current floor that the elevator is on 
 * @param destFloor			 - The destination floor of the elevator
 * @param destFloors         - The destination floors of the elevator when more than 1 passenger is boarded
 * @param hasRequest         - To check if there is a request or not for the elevator
 * @param nextCarNum         - Static variable used to assign id's to the elevator
 * @param carNum		     - The elevator id number
 * @param passengerFloor     - The floor of the passenger
 * @param time			     - Amount of time the elevator should sleep for to simulate movement, doors opening, and passengers boarding/disembarking
 * @param receivedPassengers - Whether passengers have boarded the elevator 
 * @param elevatorRequest    - the shared memory between the elevator and scheduler.
 * @param eleSocket 		 - The datagram socket of the elevator
 * @param sendPacket 		 - The datagram packet that is being sent
 * @param receivePacket 	 - The datagram packet that is being received
 * @param localAddr          - InetAddress
 * @param doorClosed         - When door is open or closed
 * @param state				 - The state of the elevator
 * @param portid			 - The port id of the elevator
 */
public class Elevator implements Runnable {
	
	//private static final float time = 9.175f;
	
	// Short time for debugging
	private static final float time = 0.175f;
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private int receivedPassengers;
	private int destFloor;
	// destFloor#, # of passengers going to floor
	private TreeMap<Integer, Integer> destFloors;
	private TreeSet<Integer> passengerFloors;
	private boolean hasRequest;
	private ElevatorState state;
	private DatagramSocket eleSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress localAddr;
	private boolean doorClosed;
	private int portID;
	private long totalMoveTime;

	
	public Elevator (int floornum, int portID) throws SocketException {
		this.currentFloor = floornum;
		this.state = ElevatorState.Initial;
		this.eleSocket = new DatagramSocket(portID);
		this.portID = portID;
		this.destFloor = 0;
		this.doorClosed = false;
		this.receivedPassengers = 0;
		setCarNum();
		
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		destFloors = new TreeMap<Integer, Integer>();
		passengerFloors = new TreeSet<Integer>();
	}
	
	private synchronized void setCarNum() { this.carNum =  nextCarNum++; }
	
	public int getCarNum() { return this.carNum; }
	
	public int getCurrentFloor() { return this.currentFloor; }
	
	public void setCurrentFloor(int cur) { this.currentFloor = cur; }
	
	public float getTime() { return Elevator.time; }
	
	public int getReceivedPassengers() { return receivedPassengers; }
		
	public String getState() { return state.getElevatorState(); }
	
	public boolean hasRequest() { return this.hasRequest; }
	
	public String getDiretion() {
		return getObjectiveFloor() > this.currentFloor ? "up" : "down";
	}
	
	public int getDestFloor() {
		return this.destFloor;
	}
	
	public int getObjectiveFloor() {
		if (this.receivedPassengers == 0) {
			return getFirstPassengerFloor(); 
		}
		return getDestFloor(); 
	}
	
	public int getFirstDestFloor() {
		if (this.destFloors.isEmpty()) {
			return -1;
		}
		
		int start = this.currentFloor;
		if (!this.passengerFloors.isEmpty()) {
			start = this.passengerFloors.first();
		}
		
		if (start < destFloor) {
			return this.destFloors.firstKey();
		}
    	
		return this.destFloors.lastKey();
	}
	
	public int getFirstPassengerFloor() {
		if (this.passengerFloors.isEmpty()) {
			return -1;
		}
		
		int start = this.passengerFloors.first();
		if (start < destFloor) {
			return start;
		}
    	
		return this.passengerFloors.last();
	}
	
	/*
	 * @purpose - Simulates doors opening
	 * @return void
	 */
	private void openDoors() {
		long startTime = System.nanoTime(); 
        try {
          	LocalTime t = LocalTime.now();
        	writeToTrace(t.toString() + " - Elevator#" + this.carNum + ", doors opening.\n");
			Thread.sleep((long) (time*1000));
        	writeToTrace(t.toString() + " - Elevator#" + this.carNum + ", doors closing.\n");
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
        long endTime = System.nanoTime();
		long openDoorTime = endTime - startTime;
		totalMoveTime += openDoorTime;
	}
	
	
	/*
	 * @purpose - Simulates movement between floors with the time taken
	 * @return void
	 */
	private void simulateFloorMovement() {
		try {
			Thread.sleep((long) (time*1000));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void writeToTrace(String s) {
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("elevator_trace.txt", true));
		    writer.append(s);
		    
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(s);
	}
	
	/*

	Function: move
	This determines if the elevator should move up or down based on it's position and destination 
	and then moves the elevator in that direction

	 @param 
	No parameters

	 @return NULL
	This method is void and has no return value

	*/
	
	public void move() {
		if (this.currentFloor >= 1 && this.currentFloor <= 7) {
			if (this.currentFloor == 1 && getDiretion() == "down") {
				this.currentFloor += 1;
			} 
			else if (this.currentFloor == 7 && getDiretion() == "up") {
				this.currentFloor -= 1;
			}

			this.currentFloor = getDiretion() == "up" ? currentFloor + 1 : currentFloor - 1;
		}
		if (this.currentFloor == getObjectiveFloor()) {
			
			return;
		}
	}

	/*

	Function: addPassenger
	This attempts to add a passenger to the list of requests to complete
	it will add only if elevator is idle or if
	the given passenger is on the elevators path to it's destination

	this also determines the value of the current destination and final destination

	 @param int start, int dest
	the passenger which will be determined if is on the elevators path to it's destination

	 @return bool
	True if the given passenger is added to the list of requests to complete, else false

	*/

	void addPassenger(int start, int dest)
	{
		this.passengerFloors.add(start);
    	
    	if(this.destFloors.containsKey(dest)) {
    		this.destFloors.put(dest, this.destFloors.get(dest) + 1);
    	}
    	else {
    		this.destFloors.put(dest, 1);
    	}
    	
    	this.hasRequest = true;
        
    	if (start < dest) {
    		this.destFloor = this.destFloors.lastKey();
    	}
    	else {
    		this.destFloor = this.destFloors.firstKey();
    	}
	}
	/*
	 * @purpose - To create a string of the elevator data
	 * 
	 * @param boolean hasArrived - if the elevator has arrived at the destination
	 * @return String - data of the elevator
	 */
	private String getUpdateString(boolean hasArrived) {
		String updateData = 
				String.valueOf(this.carNum) 						//0
				+ "," + String.valueOf(this.portID) 				//1
				+ "," + String.valueOf(this.currentFloor) 			//2
				+ "," + String.valueOf(getFirstPassengerFloor())	//3
				+ "," + String.valueOf(this.destFloor)				//4
				+ "," + String.valueOf(this.destFloors.size())		//5
				+ "," + state.getElevatorState()					//6
				+ "," + (hasArrived ? "hasArrived" : "notArrived");	//7
		return updateData;
	}
	
	public void run() {		
		while(true) {
			String currentState = state.getElevatorState();
			
			switch (currentState) {
			case "Initial":
				processInitial();
				break; 
				
			case "NoElevatorRequest":
				processNoElevatorRequest();
				break;
				
			case "PassengersBoarding":
				processPassengersBoarding();
				break;
				
			case "MoveToDestination":
				processMoveToDestination();
				break;
				
			case "HasArrived":
				processHasArrived();
				break;
				
			}
						
			state = state.nextState(this);

		}
	}
	
	private void parseAndAddPassenger() {
		String[] jobData = new String(this.receivePacket.getData()).split(",");
		if (jobData.length < 2) {
			return;
		}
		int start = Integer.parseInt(jobData[0].trim());
		int dest = Integer.parseInt(jobData[1].trim());
		LocalTime s = LocalTime.now();
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " received floor request from floor " + start + ".\n");
		addPassenger(start, dest);
	}
	
	private void processInitial() {
		LocalTime s = LocalTime.now();
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " initialize elevator " + this.carNum + ".\n");
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
	}
	
	private void processNoElevatorRequest() {
		LocalTime s = LocalTime.now();
		
		// Elevator is waiting for ElevatorRequest
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " is currently idle and waiting for an ElevatorRequest!\n");
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
		String initData = getUpdateString(false);
		
		this.sendPacket = new DatagramPacket(initData.getBytes(), initData.length(), this.localAddr, 202);
		this.receivePacket = new DatagramPacket(new byte[21], 21);
		
		boolean receivedWork = false;
		while(!receivedWork) {
			try {
				this.eleSocket.send(this.sendPacket);
				this.eleSocket.receive(this.receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			parseAndAddPassenger();
			
			if (new String(this.receivePacket.getData()).replaceAll("\\P{Print}","").equals("complete")) {
				receivedWork = true;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.hasRequest = true;
    	parseAndAddPassenger();
	}
	
	private void processMoveToDestination() {
		long startTime = System.nanoTime();
		
		LocalTime s = LocalTime.now();
		
		// Simulate movement between floors
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
    	int oldFloor = currentFloor;

		move();
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " moved from floor "+ oldFloor + " to "+currentFloor + ".\n");
		String updateData = getUpdateString(false);
		
		this.sendPacket = new DatagramPacket(updateData.getBytes(), updateData.length(), this.localAddr, 202);
		this.receivePacket = new DatagramPacket(new byte[21], 21);
		
		try {
			this.eleSocket.send(this.sendPacket);
			this.eleSocket.receive(this.receivePacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] jobData = new String(this.receivePacket.getData()).split(",");
		
		if (jobData.length > 1) {
			parseAndAddPassenger();
		}
		
		simulateFloorMovement();
		
		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;
		totalMoveTime += timeElapsed;
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " took this amount of time: "+ timeElapsed + " to move to the next floor");
		
	}
	
	private void processPassengersBoarding() {
		LocalTime s = LocalTime.now();
		
		// Simulate passengers boarding
		openDoors();
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState()  + " on floor: " + this.currentFloor + ".\n");

        this.receivedPassengers++;
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos of Elevator: "+ currentFloor + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " passengers boarded on floor: " + currentFloor + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " passengers currently in elevator: " + receivedPassengers + ".\n");
    	
    	if (this.passengerFloors.contains(this.currentFloor)) {
        	this.passengerFloors.remove(this.currentFloor);
        }
	}
	
	private void processHasArrived() {
		LocalTime s = LocalTime.now();
		// Simulate doors opening
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + " on floor: " + this.currentFloor + ".\n");
        openDoors();
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " took this amount of time to arrive: "+ totalMoveTime);
    	
		String arrivedData = getUpdateString(true);
		
		this.sendPacket = new DatagramPacket(arrivedData.getBytes(), arrivedData.length(), this.localAddr, 202);
		try {
			this.eleSocket.send(this.sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (this.destFloors.containsKey(this.currentFloor)) {
        	this.receivedPassengers -= this.destFloors.get(this.currentFloor);
        	this.destFloors.remove(this.currentFloor);
        }
        
        if (this.destFloors.isEmpty()) {
        	this.hasRequest = false;
        }
	}
	
	public static void main(String args[]) throws SocketException {
		
		Thread elevator1 = new Thread(new Elevator(1, 204), "Elevator1 Thread");
		Thread elevator2 = new Thread(new Elevator(1, 205), "Elevator2 Thread");
		elevator1.start();
		elevator2.start();
	}
}