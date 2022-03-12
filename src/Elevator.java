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


/**     
 * @purpose                  - The Elevator class obtains elevator requests from the scheduler
 * 					           and will move until it gets to the destination floor
 * @param currentFloor       - Represents the current floor that the elevator is on 
 * @param nextCarNum         - Static variable used to assign id's to the elevator
 * @param carNum		     - The elevator id number
 * @param time			     - Amount of time the elevator should sleep for to simulate movement, doors opening, and passengers boarding/disembarking
 * @param receivedPassengers - Whether passengers have boarded the elevator 
 * @param elevatorRequest    - the shared memory between the elevator and scheduler.
 */
public class Elevator implements Runnable {
	
	private static final float time = 0.1f;//9.175f;
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private int receivedPassengers;
	private int destFloor;
	private TreeMap<Integer, Integer> destFloors;
	private int passengerFloor;
	private boolean hasRequest;
	private ElevatorState state;
	private DatagramSocket eleSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress localAddr;

	private int portID;

	
	public Elevator (int floornum, int portID) throws SocketException {
		this.currentFloor = floornum;
		this.state = ElevatorState.Initial;
		this.eleSocket = new DatagramSocket(portID);
		this.portID = portID;
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		destFloors = new TreeMap<Integer, Integer>();
	}
	
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
	
	public int getFloorNum() {
		return this.passengerFloor;
	}
	
	public int getObjectiveFloor() {
		if (this.receivedPassengers == 0) {
			return getFloorNum(); 
		}
		return getDestFloor(); 
	}
	
	private void openDoors() {
		// Simulate doors opening
        try {
          	LocalTime t = LocalTime.now();
        	writeToTrace("Elevator#" + this.carNum + ", doors opening. Time stamp: " + t.toString() + "\n");
            Thread.sleep((int) getTime() * 1);
        	writeToTrace("Elevator#" + this.carNum + ", doors closing. Time stamp: " + t.toString() + "\n");
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}
	
	private void simulateFloorMovement() {
		// Simulate movement between floors
        try {
            Thread.sleep((int) getTime() * 1000);
        } catch (InterruptedException e) {
        	System.err.println(e);
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


	}
	
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

	Function: isAcending
	This determines if the elevator should move up or down based on it's position and destination

	 @param 
	No parameters

	 @return bool
	True if the elevator is moving up, else false

	*/
	
	boolean isAcending(int cur, int dest)
	{
	    if (cur < dest){
	        return true;
	    }
	    return false;
	}
	
	boolean isAcending()
	{
	    return isAcending(this.passengerFloor, this.destFloor);
	}

	/*

	Function: isPassengerOnPath
	This determines if the given passenger is on the elevators path to it's destination

	 @param Passenger* passenger
	the passenger which will be determined if is on the elevators path to it's destination

	 @return bool
	True if the given passenger is on the elevators path to it's destination, else false

	*/

	boolean isPassengerOnPath(int start, int dest)
	{
	    if (this.destFloors.isEmpty()){
	        return true;
	    }
	    else if (isAcending()
	             && isAcending(start, dest)
	             && this.currentFloor <= start){
	        return true;
	    }
	    else if (!isAcending()
	             && !isAcending(start, dest)
	             && this.currentFloor >= start){
	        return true;
	    }

	    return false;
	}


	/*

	Function: addPassenger
	This attempts to add a passenger to the list of requests to complete
	it will add only if elevator is idle or if
	the given passenger is on the elevators path to it's destination

	this also determines the value of the current destination and final destination

	 @param Passenger* passenger
	the passenger which will be determined if is on the elevators path to it's destination

	 @return bool
	True if the given passenger is added to the list of requests to complete, else false

	*/

	boolean addPassenger(int start, int dest)
	{
	    if (isPassengerOnPath(start, dest)){
	    	if (this.destFloors.isEmpty()) {
	    		this.passengerFloor = start;
	    	}
	    	
	    	if(this.destFloors.containsKey(dest)) {
	    		this.destFloors.put(dest, this.destFloors.get(dest) + 1);
	    	}
	    	else {
	    		this.destFloors.put(dest, 1);
	    	}
	    	
	    	this.hasRequest = true;
	        
	    	this.destFloor = this.destFloors.lastKey();

	        return true;
	    }
	    else if (start == dest){
	        return true;
	    }

	    return false;
	}
	
	private String getUpdateString(boolean hasArrived) {
		String updateData = 
				String.valueOf(this.carNum) 					//0
				+ "," + String.valueOf(this.portID) 			//1
				+ "," + String.valueOf(this.currentFloor) 		//2
				+ "," + String.valueOf(this.passengerFloor)		//3
				+ "," + String.valueOf(this.destFloor)			//4
				+ "," + String.valueOf(this.destFloors.size())	//5
				+ "," + state.getElevatorState()				//6
				+ "," + (hasArrived ? "hasArrived" : "notArrived");//7
		return updateData;
	}
	
	public void run() {		
		while(true) {
			String currentState = state.getElevatorState();
			switch (currentState) {
			case "Initial":
				this.carNum = nextCarNum++;
				this.receivedPassengers = 0;
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");
				writeToTrace("Elevator#" + this.carNum + " initialize elevator " + this.carNum + "\n");
				break; 
				
			case "NoElevatorRequest":
				// Elevator is waiting for ElevatorRequest
				LocalTime s = LocalTime.now();
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");
				writeToTrace("Elevator#" + this.carNum + " is currently idle and waiting for an ElevatorRequest! Time stamp: " + s.toString() + "\n");
				
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
					if(!(new String(this.receivePacket.getData()).trim().equals("NA"))) {
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
				String[] jobData = new String(this.receivePacket.getData()).split(",");
				int start = Integer.parseInt(jobData[0].trim());
				int dest = Integer.parseInt(jobData[1].trim());
				
				addPassenger(start, dest);
				
				break;
			case "PassengersBoarding":
				// Simulate passengers boarding
				openDoors();
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");

	            this.receivedPassengers++;
            	LocalTime d = LocalTime.now();
            	writeToTrace("Elevator#" + this.carNum + " current Pos of Elevator: "+ currentFloor + ". Time stamp: " + d.toString() + "\n");
            	writeToTrace("Elevator#" + this.carNum + " passengers boarded on floor: " + currentFloor + ". Time stamp: " + d.toString() + "\n");
            	writeToTrace("Elevator#" + this.carNum + " passengers currently in elevator: " + receivedPassengers + ". Time stamp: " + d.toString() + "\n");
            	
				break;
			case "MoveToDestination":
				// Simulate movement between floors
            	LocalTime f = LocalTime.now();
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");
            	writeToTrace("Elevator#" + this.carNum + " current Pos: "+ currentFloor + ". Time stamp: " + f.toString() + "\n");

				move();
				//String update = String.valueOf(this.carNum) + "," + String.valueOf(this.currentFloor) + "," + state.getElevatorState();
				
				String updateData = getUpdateString(false);
				
				this.sendPacket = new DatagramPacket(updateData.getBytes(), updateData.length(), this.localAddr, 202);
				try {
					this.eleSocket.send(this.sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				simulateFloorMovement();				
				break;
				
			case "HasArrived":
				// Simulate doors opening
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");
	            openDoors();
            	LocalTime t = LocalTime.now();
            	writeToTrace("Elevator#" + this.carNum + " current Pos: "+ currentFloor + ". Time stamp: " + t.toString() + "\n");

				//String arrived = String.valueOf(this.carNum) + "," + String.valueOf(this.currentFloor) + "," + "HasArrived";
				
				String arrivedData = getUpdateString(true);
				
				this.sendPacket = new DatagramPacket(arrivedData.getBytes(), arrivedData.length(), this.localAddr, 202);
				try {
					this.eleSocket.send(this.sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            this.receivedPassengers--;
	            
	            if (this.destFloors.containsKey(this.currentFloor)) {
	            	this.destFloors.remove(this.currentFloor);
	            }
	            

				break;
			}
						
			state = state.nextState(this);

		}
	}
}