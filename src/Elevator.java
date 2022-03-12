import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;


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
	
	private static final float time = 9.175f;
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private int receivedPassengers;
	private ArrayList<Integer> destFloor;
	private int passengerFloor;
	private boolean hasRequest;
	private ElevatorState state;
	private DatagramSocket eleSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress localAddr;
	private boolean doorClosed;

	private int portID;

	
	public Elevator (int floornum, int portID) throws SocketException {
		this.currentFloor = floornum;
		this.state = ElevatorState.Initial;
		this.eleSocket = new DatagramSocket(portID);
		this.portID = portID;
		this.destFloor = new ArrayList<Integer>();
		this.doorClosed = false;
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public ArrayList<Integer> getDestFloor() {
		return this.destFloor;
	}
	
	public int getFloorNum() {
		return this.passengerFloor;
	}
	
	public int getObjectiveFloor() {
		if (this.receivedPassengers == 0) {
			return getFloorNum(); 
		}
		return getDestFloor().get(getDestFloor().size()-1); 
	}
	
	/*
	 * @purpose - simulates doors opening
	 * 
	 * @return void
	 */
	private void openDoors() {
        try {
          	LocalTime t = LocalTime.now();
        	writeToTrace("Elevator#" + this.carNum + ", doors opening. Time stamp: " + t.toString() + "\n");
            Thread.sleep((int) getTime() * 1);
        	writeToTrace("Elevator#" + this.carNum + ", doors closing. Time stamp: " + t.toString() + "\n");
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}
	
	
	/*
	 * @purpose - simulates movement between floors with the time taken
	 * 
	 * @return void
	 */
	private void simulateFloorMovement() {
        try {
            Thread.sleep((int) getTime() * 1000);
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}
	
	/*
	 * @purpose - writes to the trace file
	 * 
	 * @param s - elevator data
	 * @return void
	 */
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
	/*
	 * @purpose - Moves the elevator to the destination floor of the request
	 * 
	 * @param s - elevator data
	 * @return void
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
				String data = String.valueOf(this.carNum) + "," + String.valueOf(this.currentFloor) + "," + state.getElevatorState();
				this.sendPacket = new DatagramPacket(data.getBytes(), data.length(), this.localAddr, 202);
				this.receivePacket = new DatagramPacket(new byte[21], 21);
				boolean receivedWork = false;
				while(!receivedWork) {
					try {
						this.eleSocket.send(this.sendPacket);
						eleSocket.receive(this.receivePacket);
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
				this.passengerFloor = Integer.parseInt(jobData[0].trim());
				this.destFloor.add(Integer.parseInt(jobData[1].trim()));
				Collections.sort(destFloor);
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
				String update = String.valueOf(this.carNum) + "," + String.valueOf(this.currentFloor) + "," + state.getElevatorState();
				this.sendPacket = new DatagramPacket(update.getBytes(), update.length(), this.localAddr, 202);
				try {
					this.eleSocket.send(this.sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				simulateFloorMovement();
				try {
					Thread.sleep((long) (time*1000));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				
			case "HasArrived":
				// Simulate doors opening
				writeToTrace("Elevator#" + this.carNum + " current state - " + state.getElevatorState() + "\n");
	            openDoors();
            	LocalTime t = LocalTime.now();
            	writeToTrace("Elevator#" + this.carNum + " current Pos: "+ currentFloor + ". Time stamp: " + t.toString() + "\n");

				String arrived = String.valueOf(this.carNum) + "," + String.valueOf(this.currentFloor) + "," + "HasArrived";
				this.sendPacket = new DatagramPacket(arrived.getBytes(), arrived.length(), this.localAddr, 202);
				try {
					this.eleSocket.send(this.sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            this.receivedPassengers--;
				break;
			}
						
			state = state.nextState(this);

		}
	}
}