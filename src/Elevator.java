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


/**     
 * The Elevator class obtains elevator requests from the scheduler and will move until it gets to the destination floor
 * @param currentFloor        Represents the current floor that the elevator is on 
 * @param destFloor           The destination floor of the elevator
 * @param destFloors          The destination floors of the elevator when more than 1 passenger is boarded
 * @param hasRequest          To check if there is a request or not for the elevator
 * @param nextCarNum          Static variable used to assign id's to the elevator
 * @param carNum              The elevator id number
 * @param passengerFloor      The floor of the passenger
 * @param time                Amount of time the elevator should sleep for to simulate movement, doors opening, and passengers boarding/disembarking
 * @param receivedPassengers  Whether passengers have boarded the elevator 
 * @param elevatorRequest     The shared memory between the elevator and scheduler.
 * @param eleSocket           The datagram socket of the elevator
 * @param sendPacket          The datagram packet that is being sent
 * @param receivePacket       The datagram packet that is being received
 * @param localAddr           The computers IP address
 * @param doorClosed          When door is open or closed
 * @param state               The state of the elevator
 * @param portid              The port id of the elevator
 * @param hasFaulted          A boolean value to identify if the elevator has faulted or not
 * @param isOn                A boolean value to identify if the elevator is running
 * @param doorDelay           The delay of the door
 * @param floorDelay          The delay of the floor
 * @param faultNum            The number of occurrences before the fault occurs
 * @param faultType           The type of fault of the elevator 
 * 
 */
public class Elevator implements Runnable {
	
	private static final float floortime = 9.5f;
	private static final float doortime = 9.175f;
	private static final int topFloor = 22;
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private int destFloor;
	// destFloor#, # of passengers going to floor
	private TreeMap<Integer, Integer> destFloors;
	private TreeSet<Integer> passengerFloors;
	private ElevatorState state;
	private DatagramSocket eleSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress localAddr;
	private int doorDelay = 1000;
	private int floorDelay = 1000;
	private String faultType;
	private int faultNum;
	private int floorCount; 
	private int doorCount;
	
	private int portID;
	
	private boolean hasFaulted;
	private boolean isOn;
	
	public Elevator (int floornum, int portID) throws SocketException {
		this.currentFloor = floornum;
		this.state = ElevatorState.Initial;
		this.eleSocket = new DatagramSocket(portID);
		this.portID = portID;
		this.destFloor = -1;
		this.floorCount = 0;
		this.doorCount = 0;
		this.faultNum = -1;
		this.hasFaulted = false;
		this.faultType = "None";
		this.isOn = true;
		setCarNum();
		
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		destFloors = new TreeMap<Integer, Integer>();
		passengerFloors = new TreeSet<Integer>();
		
		try {
			FileWriter elevatorTrace = new FileWriter("elevator"+this.carNum+"_trace.txt", false);
		} catch (IOException e) {
			e.printStackTrace();
		} //overwrites file
	}
	
	/**
	 * Set the elevator car number
	 * @return void 
	 */
	private synchronized void setCarNum() { this.carNum =  nextCarNum++; }
	
	/**
	 * Get the elevator car number
	 * @return int - the elevator car number
	 */
	public int getCarNum() { return this.carNum; }
	
	/**
	 * Get the Elevators current floor
	 * @return int - the elevator's current floor
	 */
	public int getCurrentFloor() { return this.currentFloor; }
	
	/**
	 * Sets the elevators floor number
	 * @param cur - integer representing the current floor
	 * @return void 
	 */
	public void setCurrentFloor(int cur) { this.currentFloor = cur; }
	
	/**
	 * Gets the time needed to operator the elevators door
	 * @return int - a length of time
	 */
	public float getTime() { return Elevator.doortime; }
	
	/**
	 * Gets the number of received passengers
	 * @return int - the number of received passengers
	 */
	public int getReceivedPassengers() { 
		int receivedPassengers = 0;
		for (Integer passengers : destFloors.values()) 
			receivedPassengers += passengers;
		return receivedPassengers; 
	}
	
	/**
	 * Gets the elevator's state
	 * @return string - the elevator's current state
	 */
	public String getState() { return state.getElevatorState(); }
	
	/**
	 * Checks whether the elevator has a request or not
	 * @return true - if the elevator has a request, false otherwise
	 */
	public boolean hasRequest() { return !this.destFloors.isEmpty(); }
	
	/**
	 * Checks whether the elevator has faulted
	 * @return true - if the elevator has faulted, false otherwise
	 */
	public boolean checkFaulted() { return this.hasFaulted; }
	
	/**
	 * Gets the elevator's current direction of travel
	 * @return string - representing the elevators direction of travel
	 */
	public String getDiretion() {
		return getObjectiveFloor() > this.currentFloor ? "up" : "down";
	}
	
	/**
	 * Gets the elevator's current destination floor
	 * @return int - the destination floor
	 */
	public int getDestFloor() {
		return this.destFloor;
	}
	
	/**
	 * Elevator gets the objective floor
	 * @return int - the floor number
	 */
	public int getObjectiveFloor() {
		if (this.getFirstPassengerFloor() != -1) {
			return getFirstPassengerFloor(); 
		}
		return getDestFloor(); 
	}
	
	/**
	 * Elevator goes to the first destination floor
	 * @return int - the first destination floor
	 */
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
	
	/**
	 * Elevator picks the first passenger
	 * @return int - the first passenger floor
	 */
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
	
	/**
	 * Simulates doors opening
	 * @return boolean - determine whether the doors properly closed or not
	 */
	private boolean openDoors() {
		this.doorCount++;
		if(this.doorCount == this.faultNum && this.faultType.contains("Door")) {
			this.doorDelay = 500;
		}else {
			this.doorDelay = 1000;
		}
		//start timer
		long startTime = System.nanoTime();
        try {
          	LocalTime r = LocalTime.now();
        	writeToTrace(r.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
        	writeToTrace(r.toString() + " - Elevator#" + this.carNum + ", doors opening.\n");
			Thread.sleep((long) (doortime*doorDelay));
        	writeToTrace(r.toString() + " - Elevator#" + this.carNum + ", doors closing.\n");
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
        
		//stop timer 
		long endTime = System.nanoTime();

		//take difference in time in seconds
		long timeElapsed = (endTime - startTime)/1000000000;
		if((timeElapsed < 9 || timeElapsed > 10) && !this.faultType.contains("None")) {
			this.hasFaulted = true;
			return false;
		}
		return true;
	}
	
	
	/**
	 * Simulates movement between floors with the time taken
	 * @return boolean - Did the elevator arrive on time
	 */
	private boolean simulateFloorMovement() {
		//start timer
		long startTime = System.nanoTime();
				
		try {
			Thread.sleep((long) (floortime*floorDelay));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//stop timer 
		long endTime = System.nanoTime();

		//take difference in time in seconds
		long timeElapsed = (endTime - startTime)/1000000000;
		
		if((timeElapsed < 9 || timeElapsed > 10) && !this.faultType.contains("None")) {
			this.hasFaulted = true;
			return false;
		}
		return true;
	}
	
	/**
	 * Writes the string to the appropriate trace file
	 * @param s - the string to be printed to the trace file
	 * @return void 
	 */
	public void writeToTrace(String s) {
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("elevator_trace.txt", true));
		    writer.append(s);
		    writer.close();
		    
			writer = new BufferedWriter(new FileWriter("elevator" + this.carNum + "_trace.txt", true));
		    writer.append(s);
		    writer.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(s);
	}
	
	/**
	This determines if the elevator should move up or down based on it's position and destination and then moves the elevator in that direction
	@return void 
	*/
	
	public void move() {
		if (this.currentFloor >= 1 && this.currentFloor <= topFloor) {
			if (this.currentFloor == 1 && getDiretion() == "down") {
				this.currentFloor += 1;
			} 
			else if (this.currentFloor == topFloor && getDiretion() == "up") {
				this.currentFloor -= 1;
			}
			this.currentFloor = getDiretion() == "up" ? this.currentFloor + 1 : this.currentFloor - 1;
			
			this.floorCount++;
		}

		if(this.floorCount == this.faultNum && this.faultType.contains("Floor")) {
			this.floorDelay = 500;
		}
	}

	/**
	This attempts to add a passenger to the list of requests to complete it will add only if elevator is idle or if the given passenger is on the elevators path to it's destination this also determines the value of the current destination and final destination
	@param start -int, the starting floor of the passenger
	@param dest - int, the passengers destination floor
	@return void 
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
    	
    	if (start < dest) {
    		this.destFloor = this.destFloors.lastKey();
    	}
    	else {
    		this.destFloor = this.destFloors.firstKey();
    	}
    	
    	LocalTime s = LocalTime.now();
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " added a request from floor: "+start+" to destination floor: "+dest+"\n");
	}
	/**
	 * To create a string of the elevator data
	 * @param hasArrived - boolean, if the elevator has arrived at the destination
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
				+ "," + (hasArrived ? "hasArrived" : "notArrived")	//7
				+ "," + this.faultType								//8
				+ "," + getDiretion()								//9
				+ "," + this.isOn;									//10
		return updateData;
	}
	
	public void run() {		
		while(this.isOn) {
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
			case "handleFaults":
				handleFault();
				break;
			}
					
			state = state.nextState(this);
		}
	}
	/**
	 * To handle the faults with the elevator
	 * @return void
	 */
	private void handleFault() {
		LocalTime s = LocalTime.now();
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " notifying " + this.faultType + " fault occurred.\n");

		if(faultType.contains("Door")){
			String updateData = getUpdateString(false);
			
			this.sendPacket = new DatagramPacket(updateData.getBytes(), updateData.length(), this.localAddr, 202);
			this.receivePacket = new DatagramPacket(new byte[30], 30);
			
			try {
				this.eleSocket.send(this.sendPacket);
				this.eleSocket.receive(this.receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			writeToTrace(s.toString() + " - Elevator#" + this.carNum + " reset doors.\n");
			this.hasFaulted = false;
		}
		else if(faultType.contains("Floor")){
			String updateData = getUpdateString(false);
			
			this.sendPacket = new DatagramPacket(updateData.getBytes(), updateData.length(), this.localAddr, 202);
			this.receivePacket = new DatagramPacket(new byte[30], 30);
			
			try {
				this.eleSocket.send(this.sendPacket);
				this.eleSocket.receive(this.receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			writeToTrace(s.toString() + " - Elevator#" + this.carNum + " shutting down.\n");
			this.isOn = false;
		}
	}
	
	/**
	 * To get the data of the request and add the passenger to the elevator
	 * @return void
	 */
	private void parseAndAddPassenger() {
		String[] jobData = new String(this.receivePacket.getData()).split(",");
		if (jobData.length < 2) {
			return;
		}
		int start = Integer.parseInt(jobData[0].trim());
		int dest = Integer.parseInt(jobData[1].trim());
		
		if((this.faultNum < 0 && !jobData[3].trim().contains("None")) || jobData[3].trim().contains("Floor")) {
			this.faultType = jobData[3].trim();		
			this.faultNum =  Integer.parseInt(jobData[2].trim());
			LocalTime s = LocalTime.now();
			writeToTrace(s.toString() + " - Elevator#" + this.carNum + " faultType: " + faultType + ".\n");
		}
		
		LocalTime s = LocalTime.now();
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " received floor request from floor " + start + ".\n");
		addPassenger(start, dest);
	}
	
	/**
	 * Prints the initial state of the elevators
	 * @return void
	 */
	private void processInitial() {
		LocalTime s = LocalTime.now();
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " initialize elevator " + this.carNum + ".\n");
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
	}
	
	/**
	 * Waits to receive a request and processes the request
	 * @return void
	 */
	private void processNoElevatorRequest() {
		LocalTime s = LocalTime.now();
		long startTime = System.nanoTime();		
		// Elevator is waiting for ElevatorRequest
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " is currently idle and waiting for an ElevatorRequest!\n");
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
		String initData = getUpdateString(false);
		
		this.sendPacket = new DatagramPacket(initData.getBytes(), initData.length(), this.localAddr, 202);
		this.receivePacket = new DatagramPacket(new byte[30], 30);
		
		boolean receivedWork = false;
		while(!receivedWork) {
			//stop timer 
			long endTime = System.nanoTime();

			//take difference in time in seconds
			long timeElapsed = (endTime - startTime)/1000000000;
			if(timeElapsed > 10) {
				this.isOn = false;
				try {
					this.eleSocket.send(this.sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				writeToTrace(s.toString() + " - Elevator#" + this.carNum + " no more work from scheduler, shutting down\n");
				break;
			}
			try {
				this.eleSocket.send(this.sendPacket);
				this.eleSocket.receive(this.receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			parseAndAddPassenger();
			
			if (new String(this.receivePacket.getData()).replaceAll("\\P{Print}","").contains("complete")) {
				
				writeToTrace(s.toString() + " - Elevator#" + this.carNum + " receiving 'complete' package\n");
				receivedWork = true;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Process the movement of the elevator to its destination
	 * @return void
	 */
	private void processMoveToDestination() {
		LocalTime s = LocalTime.now();
		
		// Simulate movement between floors
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + ".\n");
    	int oldFloor = currentFloor;

		move();
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " moved from floor " + oldFloor + " to "+currentFloor + ".\n");
		
		writeToTrace(s.toString() + " - Elevator#" + this.carNum 
				+ " final destination: "+destFloor 
				+ ", objective floor: "+ this.getObjectiveFloor() 
				+ ", receivedPassengers: "+ this.getReceivedPassengers()
				+ ".\n");

		simulateFloorMovement();

		String updateData = getUpdateString(false);
		
		this.sendPacket = new DatagramPacket(updateData.getBytes(), updateData.length(), this.localAddr, 202);
		this.receivePacket = new DatagramPacket(new byte[30], 30);
		
		try {
			this.eleSocket.send(this.sendPacket);
			this.eleSocket.receive(this.receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] jobData = new String(this.receivePacket.getData()).split(",");
		
		if (jobData.length > 1) {
			parseAndAddPassenger();
		}
	}
	
	/**
	 * Processes the passengers boarding the elevator
	 * @return void
	 */
	private void processPassengersBoarding() {
		LocalTime s = LocalTime.now();
		
		// Simulate passengers boarding
		if(!openDoors()) {
			return;
		}
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState()  + " on floor: " + this.currentFloor + ".\n");

		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos of Elevator: "+ currentFloor + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " passengers boarded on floor: " + currentFloor + ".\n");
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " passengers currently in elevator: " + this.getReceivedPassengers() + ".\n");
    	
    	if (this.passengerFloors.contains(this.currentFloor)) {
        	this.passengerFloors.remove(this.currentFloor);
        }
	}
	
	/**
	 * The show that the elevator has arrived to its destination floor
	 * @return void
	 */
	private void processHasArrived() {
		LocalTime s = LocalTime.now();
		// Simulate doors opening
		writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current state - " + state.getElevatorState() + " on floor: " + this.currentFloor + ".\n");
		if(!openDoors()) {
			return;
		}
    	writeToTrace(s.toString() + " - Elevator#" + this.carNum + " current Pos: "+ currentFloor + ".\n");
		
		String arrivedData = getUpdateString(true);
		
		this.sendPacket = new DatagramPacket(arrivedData.getBytes(), arrivedData.length(), this.localAddr, 202);
		try {
			this.eleSocket.send(this.sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        if (this.destFloors.containsKey(this.currentFloor)) {
        	this.destFloors.remove(this.currentFloor);
        }
        
        writeToTrace(s.toString() + " - Elevator#" + this.carNum + " remaining requests: "+ this.destFloors.size() + ".\n");
	}
	
	public static void main(String args[]) throws SocketException {
		
		Thread elevator1 = new Thread(new Elevator(1, 204),  "Elevator1 Thread");
		Thread elevator2 = new Thread(new Elevator(22, 205), "Elevator2 Thread");
		Thread elevator3 = new Thread(new Elevator(14, 206), "Elevator3 Thread");
		Thread elevator4 = new Thread(new Elevator(4, 207),  "Elevator4 Thread");
		elevator1.start();
		elevator2.start();
		elevator3.start();
		elevator4.start();
	}
}