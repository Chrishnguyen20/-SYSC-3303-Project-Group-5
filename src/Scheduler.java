import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * @purpose 						- The scheduler class coordinates how made by the floors are served by the elevator.
 * @param	isClient    			- is the current scheduler client (floor) facing
 * @param   states     				- the state of the scheduler
 * @param   receiveSocket  			- the socket used to receive data
 * @param   localAddr  	   			- the computers IP address
 * @param   receivedFloorPacket     - the data packet received from the floor
 * @param   receivedElevatorPacket  - the data packet received from the elevator
 * @param   sendElevatorPacket      - the packet sent to the floor/elevator
 * @param   floorQueue    			- a thread safe queue of floor requests
 * @param   currentRequests    		- all requests currently being handled 
 * @param   activeElevators    		- list of all active elevators
 * @param   numEventsQueued     	- static int representing the number of events queued by the floor
 * @param   numEventsServed     	- static int representing the number of requests served by the elevator
 * @param   elevatorCount           - The number of elevators
 * @param 	elevatorFaults			- Map between an elevator car num and a string array. Element 0 of string array is fauly type
 * 									  and element 1 of string array is elevator port ID.
 */

public class Scheduler implements Runnable {
	private boolean isClient;
	

	private String states;
	SchedulerState state;
	private DatagramSocket receiveSocket;
	private InetAddress localAddr;
	private DatagramPacket receivedFloorPacket;
	private DatagramPacket receivedElevatorPacket;
	private DatagramPacket sendElevatorPacket;
	private static ArrayList<String[]> requestList;
	private static ArrayList<String[]> currentRequests;
	private ArrayList<String[]> activeElevators;
	private int elevatorCount;
	private Map<Integer, String[]> elevatorFaults;
	
	private static int numEventsQueued = 0;
	private static int numEventsServed = -1;

	public Scheduler(boolean b, int c) {
		this.isClient = b;
		this.elevatorCount = c;
		
		states = "Initial";
		state = SchedulerState.WaitRequest;
		
		this.activeElevators = new ArrayList<String[]>();
		this.elevatorFaults = new HashMap<Integer, String[]>();
		Scheduler.requestList = new ArrayList<String[]>();
		Scheduler.currentRequests = new ArrayList<String[]>();
		
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isClient) {
			try {
				FileWriter elevatorTrace = new FileWriter("elevator_trace.txt", false);
				FileWriter floorTrace = new FileWriter("floor_trace.txt", false);
			} catch (IOException e) {
				e.printStackTrace();
			} //overwrites file
		}
	}

	/*
	 * @purpose - The states of the scheduler 
	 */
	
	public String getCurrentState() {
		return states;
	}
	
	/*
	 * @purpose - writes to the elevator_trace.txt file
	 * @return void
	 */
	public void writeToElevatorTrace(String s) {
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
	 * @purpose - writes to the floor_trace.txt file
	 * @return void
	 */
	public void writeToFloorTrace(String s) {
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("floor_trace.txt", true));
		    writer.append(s);
		    
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(s);
	}
	
	/*

	Function: isAcending
	This determines if the elevator should move up or down based on it's position and destination

	 @param int cur, int dest

	 @return bool
	True if the elevator is moving up, else false

	*/
	
	private boolean isAcending(int cur, int dest)
	{
	    if (cur < dest){
	        return true;
	    }
	    return false;
	}
	
	/*

	Function: isPassengerOnPath
	This determines if the given passenger is on the elevators path to it's destination

	 @param int requestStart, int requestDest, int eStart, int eDest, int eCurrentFloor
	the passenger which will be determined if is on the elevators path to it's destination

	 @return bool
	True if the given passenger is on the elevators path to it's destination, else false

	*/

	private boolean isPassengerOnPath(int requestStart, int requestDest, int eStart, int eDest, int eCurrentFloor)
	{
	    if (isAcending(eStart, eDest)
	             && isAcending(requestStart, requestDest)
	             && eCurrentFloor <= requestStart){
	        return true;
	    }
	    else if (!isAcending(eStart, eDest)
	             && !isAcending(requestStart, requestDest)
	             && eCurrentFloor >= requestStart){
	        return true;
	    }

	    return false;
	}
	
	/*
	 * @purpose to get the data of the active elevators
	 * 
	 * @param String[] data - data of the elevator
	 * @param int index - the index of the elevator
	 */
	
	private void updateActiveElevator(String[] data, int index) {
		activeElevators.get(index)[0] = data[0];
		activeElevators.get(index)[1] = data[1];
		activeElevators.get(index)[2] = data[2];
		activeElevators.get(index)[3] = data[3];
		activeElevators.get(index)[4] = data[4];
		activeElevators.get(index)[5] = data[5];
		activeElevators.get(index)[6] = data[6];
		activeElevators.get(index)[7] = data[7];
	}
	
	/*
	 * @purpose - To get an available elevator
	 * 
	 * @param int requestStart - Starting floor of the request
	 * @param int requestDest - Destination floor of the request
	 */
	private int getAvailableElevator(int requestStart, int requestDest) {
		if (activeElevators.isEmpty()) {
			return receivedElevatorPacket.getPort();
		}
		
		int canidateIndex = 0;
		int minimumDist = Math.abs(Integer.parseInt(this.activeElevators.get(canidateIndex)[2]) - requestStart);
		boolean isIdle = false;
		this.activeElevators.get(canidateIndex)[5] = String.valueOf(Integer.parseInt(this.activeElevators.get(canidateIndex)[5]) + 1);
		
		for (int i = 1; i < this.activeElevators.size(); ++i) {
			String[] elevator = this.activeElevators.get(i);
			
			int currentFloor = Integer.parseInt(elevator[2]);
			int passengerFloor = Integer.parseInt(elevator[3]);
			int effectiveFloor = passengerFloor;
			if ((isAcending(passengerFloor, Integer.parseInt(elevator[4])) && currentFloor < passengerFloor)
					|| (!isAcending(passengerFloor, Integer.parseInt(elevator[4])) && currentFloor > passengerFloor)) {
				effectiveFloor = currentFloor;
			}
			
			if (Integer.parseInt(elevator[5]) == 0
					&& (minimumDist > Math.abs(effectiveFloor - requestStart) || !isIdle)) {
				elevator[5] = "1";
				minimumDist = Math.abs(effectiveFloor - requestStart);
				isIdle = true;
				this.activeElevators.get(canidateIndex)[5] = String.valueOf(Integer.parseInt(this.activeElevators.get(canidateIndex)[5]) - 1);
				canidateIndex = i;
			}
			else if (isPassengerOnPath(requestStart, requestDest, passengerFloor, Integer.parseInt(elevator[4]), currentFloor)
					&& minimumDist > Math.abs(effectiveFloor - requestStart)
					&& !isIdle) {
				elevator[5] = String.valueOf(Integer.parseInt(elevator[5]) + 1);
				minimumDist = Math.abs(effectiveFloor - requestStart);
				this.activeElevators.get(canidateIndex)[5] = String.valueOf(Integer.parseInt(this.activeElevators.get(canidateIndex)[5]) - 1);
				canidateIndex = i;
			}
		}
		return canidateIndex;
	}
	
	public void run() {
		try {
			if(isClient) {
				this.receiveSocket  = new DatagramSocket(201);
			}else {
				this.receiveSocket  = new DatagramSocket(202);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			LocalTime s = LocalTime.now();
			if(isClient) {
				processClient();
			}
<<<<<<< HEAD
			else if (!Scheduler.requestList.isEmpty() || !Scheduler.currentRequests.isEmpty()) {
				switch (state.Current()) {
				case "WaitRequest":
					processWaitRequest();
					break;
					
				case "NotifyElevator":
					processNotifyElevator();
					break;
					
				case "Served":
					processServed();
					break;
					
				case "Removed":
					processRemoved();
					break;
					
				}
				if(Scheduler.numEventsQueued == Scheduler.numEventsServed) {
					writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: EOF.\n");
					Scheduler.numEventsServed++;
=======
			else {
				if (!Scheduler.requestList.isEmpty() || !Scheduler.currentRequests.isEmpty()) {
					switch (state.Current()) {
					case "WaitRequest":
						processWaitRequest();
						break;
						
					case "NotifyElevator":
						processNotifyElevator();
						break;
						
					case "GetElevatorUpdate":
						processServed();
						break;
						
					case "Removed":
						processRemoved();
						break;
						
					case "HandleFault":
						handleFaults();
						break;
					}
					if(Scheduler.numEventsQueued == Scheduler.numEventsServed) {
						writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: EOF.\n");
						return;
					}
>>>>>>> branch 'main' of https://github.com/leoxu321/SYSC-3303-Project-Group-5
				}
			}
		}
	}
	
	public void processClient() {
		LocalTime s = LocalTime.now();
		
		try {
			this.receivedFloorPacket = new DatagramPacket(new byte[30], 30);
			this.receiveSocket.receive(receivedFloorPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] floorReq = new String(receivedFloorPacket.getData(), StandardCharsets.UTF_8).split(",");
		for(int i = 0; i < floorReq.length; i++) {
			floorReq[i] = floorReq[i].trim();
		}
		writeToFloorTrace(s.toString() + " - Scheduler Subsystem (floor): Queueing event from floor subsystem.\n");

		requestList.add(floorReq);
		Scheduler.numEventsQueued++;
		try {
			receivedFloorPacket.setPort(200);
			writeToFloorTrace(s.toString() + " - Scheduler Subsystem (floor): Sending floor acknowledgement.\n");
			this.receiveSocket.send(receivedFloorPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processWaitRequest() {
		LocalTime s = LocalTime.now();
		
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
		states = "Has request";
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
		while(activeElevators.size() < this.elevatorCount) {
			this.receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): waiting for elevator.\n");
			try {
				this.receiveSocket.receive(receivedElevatorPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String[] data = new String(this.receivedElevatorPacket.getData()).split(",");
			
			boolean newElevator = true;
			for (int i = 0; i < this.activeElevators.size(); ++i) {
				if (this.activeElevators.get(i)[1].equals(data[1])) {
					newElevator = false;
				}
			}
			if (newElevator) {
				this.activeElevators.add(data);
				writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): added elevator#" + data[0] + " to active elevators list\n");
			}
		}
		state = state.nextState();
	}
	
	public void processNotifyElevator() {
		LocalTime s = LocalTime.now();
		
		states = "Notified elevator";
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): Send data to an active elevator.\n");
		
		for (int i = Scheduler.requestList.size() - 1; i >= 0; --i) {
			String[] request = Scheduler.requestList.get(i);
			int elevatorIndex = getAvailableElevator(Integer.parseInt(request[1]), Integer.parseInt(request[3]));
			
			if (elevatorIndex == -1) {
				continue;
			}
			
			String elevatorData = String.valueOf(request[1]) + "," + String.valueOf(request[3]) + "," + request[4] + "," + request[5];
			int port = Integer.parseInt(this.activeElevators.get(elevatorIndex)[1]);
			Scheduler.requestList.remove(i);
			currentRequests.add(request);
			
			this.sendElevatorPacket = new DatagramPacket(elevatorData.getBytes(), elevatorData.getBytes().length, localAddr, port);
			try { 
				this.receiveSocket.send(sendElevatorPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): sent elevator #" + this.activeElevators.get(elevatorIndex)[0] + " a request"
					+ " from floor: "+request[1]+" to "+request[3]+"\n");
		}
		
		byte completeBytes[] = "complete".getBytes();
		
		for(int i = 0; i < activeElevators.size(); i++) {
			this.sendElevatorPacket = new DatagramPacket(completeBytes, completeBytes.length, localAddr, 
					Integer.parseInt(activeElevators.get(i)[1]));
			try { 
				this.receiveSocket.send(sendElevatorPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		state = state.nextState();
	}
	
	public void processServed() {
		LocalTime s = LocalTime.now();
		
		states = "Serving Requests";
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): Waiting for an elevator to update their status.\n");
		
		receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
		boolean hasArrived = false;
		try {
			this.receiveSocket.receive(receivedElevatorPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] updateData = new String(receivedElevatorPacket.getData()).split(",");
		
		for(int i = 0; i < activeElevators.size(); i++) {
			if(activeElevators.get(i)[0].equals(updateData[0])) {
				updateActiveElevator(updateData, i);
			}
		}
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): got update from elevator#" + updateData[0] + " -- " + updateData[6] +  ".\n");
		if(updateData[6].replaceAll("\\P{Print}","").equals("HasArrived")) {
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): service floor " + updateData[2] + ".\n");
			state = state.nextState();
			Scheduler.currentRequests.remove(0);
			if(Scheduler.numEventsServed < 0) {
				Scheduler.numEventsServed = 0;
			}
			if(Scheduler.currentRequests.isEmpty()) {
				states = "Request Served";
				writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
			}
			Scheduler.numEventsServed++;
			hasArrived = true;
			
			return;
		}else if(updateData[6].replaceAll("\\P{Print}","").equals("handleFaults")) {
			//elevator experienced a fault
			String[] faultData = new String[3];
			faultData[0] = updateData[8];	//get fault code
			faultData[1] = updateData[1]; 	//get elevator index
			faultData[2] = updateData[9];	//get number of events attributed to this elevator 
			this.elevatorFaults.put(Integer.parseInt(updateData[0]), faultData);
			state = SchedulerState.HandleFault;
			return;
		}
		//go to handle fault state
		//	return
		//be smart and pick up any passengers on this floor going in the direction 
		if (!Scheduler.requestList.isEmpty() && !hasArrived) {
			state = SchedulerState.NotifyElevator;
			writeToElevatorTrace("Switching to State: SchedulerState.NotifyElevator\n");
			return;
		}
		
		byte updateBytes[] = "update".getBytes();
		this.sendElevatorPacket = new DatagramPacket(updateBytes, updateBytes.length, localAddr, receivedElevatorPacket.getPort());
		
		try { 
			this.receiveSocket.send(sendElevatorPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processRemoved() {
		LocalTime s = LocalTime.now();
		
		byte arrivedBytes[] = "arrived".getBytes();
		this.sendElevatorPacket = new DatagramPacket(arrivedBytes, arrivedBytes.length, localAddr, receivedElevatorPacket.getPort());
		
		try { 
			this.receiveSocket.send(sendElevatorPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		states = "Request Served";
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + states + ".\n");
		if(!Scheduler.currentRequests.isEmpty()) {
			state = SchedulerState.GetElevatorUpdate;
		}
	}
	
	public void handleFaults() {
		LocalTime s = LocalTime.now();
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: Switching to State: HandleFault\n");
		
		for(Map.Entry<Integer, String[]> entry : this.elevatorFaults.entrySet()) {
			writeToElevatorTrace(s.toString() + " - Handling " + entry.getValue()[0] + " fault from elevator#" + entry.getKey() + ".\n");
			if(entry.getValue()[0].equals("Door")) {
				byte updateBytes[] = "ResetDoors".getBytes();
				DatagramPacket faultPacket = new DatagramPacket(updateBytes, updateBytes.length, localAddr, Integer.parseInt(entry.getValue()[1]));
				try { 
					this.receiveSocket.send(faultPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//remove fault
				this.elevatorFaults.remove(entry.getKey());
			}else if(entry.getValue()[0].equals("Floor")) {
				byte updateBytes[] = "ShutDownElevator".getBytes();
				DatagramPacket faultPacket = new DatagramPacket(updateBytes, updateBytes.length, localAddr, Integer.parseInt(entry.getValue()[1]));
				try { 
					this.receiveSocket.send(faultPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(Scheduler.numEventsServed < 0) {
					Scheduler.numEventsServed = 0;
				}
				Scheduler.numEventsServed+=Integer.parseInt(entry.getValue()[2].replaceAll("\\P{Print}",""));
				this.activeElevators.remove(entry.getKey());
				//remove fault
				this.elevatorFaults.remove(entry.getKey());
			}
		}
		
		state = state.nextState();
	}
	
	public static void main(String args[]) throws SocketException {
		
		final int elevatorCount = 2;
				
		Thread scheduler_server = new Thread(new Scheduler(true, elevatorCount), "Floor Scheduler Thread");
		Thread scheduler_client = new Thread(new Scheduler(false, elevatorCount), "Elevator Scheduler Thread");
		scheduler_client.start();
		scheduler_server.start();
	}
}