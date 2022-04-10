import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The scheduler class coordinates how made by the floors are served by the elevator.
 * @param isClient                 Is the current scheduler client (floor) facing
 * @param states                   The state of the scheduler
 * @param receiveSocket            The socket used to receive data
 * @param localAddr                The computers IP address
 * @param receivedFloorPacket      The data packet received from the floor
 * @param receivedElevatorPacket   The data packet received from the elevator
 * @param sendElevatorPacket       The packet sent to the floor/elevator
 * @param floorQueue               A thread safe queue of floor requests
 * @param currentRequests          All requests currently being handled 
 * @param activeElevators          List of all active elevators
 * @param numEventsQueued          Static int representing the number of events queued by the floor
 * @param numEventsServed          Static int representing the number of requests served by the elevator
 * @param elevatorCount            The number of elevators
 * @param elevatorFaults           Map between an elevator car num and a string array. Element 0 of string array is fault type and element 1 of string array is elevator port ID.
 */

public class Scheduler implements Runnable {
	private boolean isClient;
	

	//private String states;
	private SchedulerState state;
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
	private GUI gui;
	
	private static int numEventsQueued = 0;
	private static int numEventsServed = -1;

	public Scheduler(boolean b, int c) {
		this.isClient = b;
		this.elevatorCount = c;
		
		state = SchedulerState.WaitRequest;
		
		this.activeElevators = new ArrayList<String[]>();
		this.elevatorFaults = new HashMap<Integer, String[]>();
		Scheduler.requestList = new ArrayList<String[]>();
		Scheduler.currentRequests = new ArrayList<String[]>();
		
		try {
			this.localAddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(isClient) {
			try {
				FileWriter schedulerTrace = new FileWriter("scheduler_elevator_trace.txt", false);
				FileWriter elevatorTrace = new FileWriter("elevator_trace.txt", false);
				FileWriter floorTrace = new FileWriter("floor_trace.txt", false);
			} catch (IOException e) {
				e.printStackTrace();
			} //overwrites file
		}
		else {
			this.gui = new GUI(4);
			try {
				FileWriter schedulerTrace = new FileWriter("scheduler_floor_trace.txt", false);
			} catch (IOException e) {
				e.printStackTrace();
			} //overwrites file
		}
	}

	/**
	 * Gets the states of the scheduler 
	 * @return void 
	 */
	public String getCurrentState() {
		return this.state.Current();
	}
	
	/**
	 * Writes to the elevator_trace.txt file
	 * @return void 
	 */
	public void writeToElevatorTrace(String s) {
		BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter("scheduler_elevator_trace.txt", true));
			    writer.append(s);
			    writer.close();
			    
			    writer = new BufferedWriter(new FileWriter("elevator_trace.txt", true));
			    writer.append(s);
			    writer.close();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
		System.out.println(s);
	}
	
	/**
	 * Writes to the floor_trace.txt file
	 * @return void
	 */
	public void writeToFloorTrace(String s) {
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("scheduler_floor_trace.txt", true));
		    writer.append(s);
		    writer.close();
		    
		    writer = new BufferedWriter(new FileWriter("floor_trace.txt", true));
		    writer.append(s);
		    writer.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(s);
	}
	
	/**	
	This determines if the elevator should move up or down based on it's position and destination
 	@param cur  - int, the current floor
	@param dest - int, the destination floor
	@return bool - True if the elevator is moving up, else false
	*/
	private boolean isAcending(int cur, int dest)
	{
	    if (cur < dest){
	        return true;
	    }
	    return false;
	}
	
	/**
	This determines if the given passenger is on the elevators path to it's destination
	@param requestStart - int, the starting floor of the request
	@param requestDes - int, the destination floor of the request
	@param eStart - int, the elevators starting floor
	@param eDest - int, the elevators current destination floor
	@param eCurrentFloor - int, the elevators current floor
	
	@return bool - True if the given passenger is on the elevators path to it's destination, else false

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
	
	/**
	 * Gets the data of the active elevators
	 * 
	 * @param data - String[], data of the elevator
	 * @param index - int, the index of the elevator
	 * @return void
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
		activeElevators.get(index)[8] = data[8];
		activeElevators.get(index)[9] = data[9];
		activeElevators.get(index)[10] = data[10];
		
		updateGUI(data);
	}
	
	/**
	 * Updates the GUI display
	 * 
	 * @param String[] data - data of the elevator
	 * @return void
	 */
	private void updateGUI(String[] data) {
		if(Integer.parseInt(data[4]) > 0) {
			int currentFloor = Integer.parseInt(data[2]);
			int passengerFloor = Integer.parseInt(data[3]);
			int destFloor = Integer.parseInt(data[4]);
			
			boolean eisAcending = isAcending(passengerFloor, destFloor);
			
			String dir = data[9].contains("up") ? "UP" : "DOWN";
            String state = data[6];
            
            String fault = data[8];
            if (!data[8].contains("None"))
            	fault += "Fault";
            
            if (state.contains("handleFaults")) {
            	dir = "Stationary";
            	state = data[8].contains("Floor") ? "FloorFaulted" : "DoorFaulted";
            }
            else if (data[7].contains("hasArrived")) {
            	dir = "Stationary";
            	state = "HasArrived";
            }
            else if(currentFloor == passengerFloor) {
	            dir = "Stationary";
	            state = "PassengersBoarding";
            }
            else if(state.contains("NoElevatorRequest")) {
	            dir = "Stationary";
	            state = "NoElevatorRequest";
            }

            int index = Integer.parseInt(data[0]);
            gui.setLabel(index, "Elevator "+index+"|"+data[2]+"| "+dir+" "+state+" | "+fault);
        }
	}
	
	
	
	/**
	 * Gets an available elevator
	 * 
	 * @param int requestStart - Starting floor of the request
	 * @param int requestDest - Destination floor of the request
	 * @return void
	 */
	private int getAvailableElevator(int requestStart, int requestDest) {
		if (activeElevators.isEmpty()) {
			return receivedElevatorPacket.getPort();
		}
		
		TreeMap<Float, Integer> requestTimes = new TreeMap<Float, Integer>();
		
		for (int i = 0; i < this.activeElevators.size(); ++i) {
			String[] elevator = this.activeElevators.get(i);
			
			int currentFloor = Integer.parseInt(elevator[2]);
			int passengerFloor = Integer.parseInt(elevator[3]);
			int destFloor = Integer.parseInt(elevator[4]);
			int effectiveFloor = currentFloor;
			int requestCount = Integer.parseInt(elevator[5]);
			
			if (passengerFloor == -1 
					|| (passengerFloor < destFloor && requestStart < passengerFloor) 
					|| (passengerFloor > destFloor && requestStart > passengerFloor)) {
				passengerFloor = requestStart;
			}
			
			if (requestCount == 0 || destFloor == -1 
					|| (passengerFloor < destFloor && requestDest > destFloor) 
					|| (passengerFloor > destFloor && requestDest < destFloor)) {
				destFloor = requestDest;
			}
			
			if (((passengerFloor < destFloor != requestStart < requestDest) 
					|| !isPassengerOnPath(requestStart, requestDest, passengerFloor, destFloor, currentFloor)) 
					&& requestCount != 0) {
				continue;
			}
			
			float moveTime = 0;
			
			if (passengerFloor != -1
					&& ((passengerFloor < destFloor && currentFloor > passengerFloor)
					|| (passengerFloor > destFloor && currentFloor < passengerFloor))) {
				effectiveFloor = passengerFloor;
				moveTime += Math.abs(currentFloor - passengerFloor) * 9.5f;
			}
			
			moveTime += Math.abs(effectiveFloor - requestDest) * 9.5f;
			
			moveTime += 9.175f * requestCount;
			
			requestTimes.put(moveTime, i);
		}
		
		if (requestTimes.isEmpty())
			return -1;
		
		int index = requestTimes.firstEntry().getValue();
		String[] elevator = this.activeElevators.get(index);
		
		int currentFloor = Integer.parseInt(elevator[2]);
		int passengerFloor = Integer.parseInt(elevator[3]);
		int destFloor = Integer.parseInt(elevator[4]);
		int requestCount = Integer.parseInt(elevator[5]);
		
		boolean eisAcending = passengerFloor < destFloor;

		passengerFloor = passengerFloor != -1 ? passengerFloor : currentFloor;

		if (passengerFloor == -1 || (eisAcending && requestStart < passengerFloor) || (!eisAcending && requestStart > passengerFloor)) {
			elevator[3] = String.valueOf(requestStart);
		}
		if (requestCount == 0 || destFloor == -1 || (eisAcending && requestDest > destFloor) || (!eisAcending && requestDest < destFloor)) {
			elevator[4] = String.valueOf(requestDest);
		}
		
		elevator[5] = String.valueOf(Integer.parseInt(elevator[5]) + 1);

		updateActiveElevator(elevator, index);
		
		return index;
	}
	
	public void run() {
		long startTime = System.nanoTime();		
		
		try {
			if(isClient) {
				this.receiveSocket  = new DatagramSocket(201);
			}else {
				this.receiveSocket  = new DatagramSocket(202);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while (true) {
			LocalTime s = LocalTime.now();
			if(isClient) {
				processClient();
			}
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
						processUpdate();
						break;
						
					case "Removed":
						processRemoved();
						break;
						
					case "HandleFault":
						handleFaults();
						break;
					}
					if(Scheduler.numEventsQueued == Scheduler.numEventsServed) {
						//stop timer 
						long endTime = System.nanoTime();

						//take difference in time in seconds
						long timeElapsed = (endTime - startTime)/1000000000;

						writeToElevatorTrace("Scheduler Subsystem: Elapsed time: " + timeElapsed);
							
						writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: EOF.\n");
						
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Waits to receive the floor request and add it to the request list
	 * 
	 * @return void
	 */
	public void processClient() {
		LocalTime s = LocalTime.now();
		
		try {
			this.receivedFloorPacket = new DatagramPacket(new byte[30], 30);
			this.receiveSocket.receive(receivedFloorPacket);
		} catch (IOException e) {
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
			e.printStackTrace();
		}
	}
	
	/**
	 * Waits to receive the floor request and add it to the request list
	 * 
	 * @return void
	 */
	public void processWaitRequest() {
		LocalTime s = LocalTime.now();

		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + this.state.Current() + ".\n");
		while(activeElevators.size() < this.elevatorCount) {
			this.receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): waiting for elevator.\n");
			try {
				this.receiveSocket.receive(receivedElevatorPacket);
			}
			catch (IOException e) {
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
	
	
	/**
	 * Notifies active elevators of floor requests that need to be served
	 * 
	 * @return void
	 */
	public void processNotifyElevator() {
		LocalTime s = LocalTime.now();
		
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + this.state.Current() + ".\n");
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): Send data to an active elevator.\n");
		
		for (int i = Scheduler.requestList.size() - 1; i >= 0; --i) {
			String[] request = Scheduler.requestList.get(i);
			int elevatorIndex = getAvailableElevator(Integer.parseInt(request[1]), Integer.parseInt(request[3]));
			
			if (elevatorIndex == -1) {
				writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): no available elevators\n");
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
				e.printStackTrace();
			}
			
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): sent elevator #" + this.activeElevators.get(elevatorIndex)[0] + " a request"
					+ " from floor: "+request[1]+" to "+request[3]+"\n");
		}
		
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): sending 'complete' packages\n");
		
		byte completeBytes[] = "complete".getBytes();
		
		for(int i = 0; i < activeElevators.size(); i++) {
			this.sendElevatorPacket = new DatagramPacket(completeBytes, completeBytes.length, localAddr, 
					Integer.parseInt(activeElevators.get(i)[1]));
			try { 
				this.receiveSocket.send(sendElevatorPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		state = state.nextState();
	}
	
	
	/**
	 * Waits to receive update from elevators and processes the update data. If the elevator is on a new floor, it sends an acknowledgement back. If the elevator has arrived at a destination or passenger floor, it sends an acknowledgement back. If a fault is reported it handles the fault
	 * 
	 * @return void
	 */
	public void processUpdate() {
		LocalTime s = LocalTime.now();
		
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + this.state.Current() + ".\n");
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): Waiting for an elevator to update their status.\n");
		
		receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
		boolean hasArrived = false;
		try {
			this.receiveSocket.setSoTimeout(20000);
			this.receiveSocket.receive(receivedElevatorPacket);
		} 
		catch (SocketTimeoutException e) {
            // timeout exception.
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): Timed out.\n");
			state = state.nextState();
			Scheduler.numEventsQueued = Scheduler.numEventsServed;
			return;
        }
		catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] updateData = new String(receivedElevatorPacket.getData()).split(",");
		
		for(int i = 0; i < activeElevators.size(); i++) {
			if(activeElevators.get(i)[0].equals(updateData[0])) {
				updateActiveElevator(updateData, i);
			}
		}
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): got update from elevator#" + updateData[0] + " -- " + updateData[6] +  ".\n");
		if(updateData[10].replaceAll("\\P{Print}","").equals("false")) {
			
			for (int i = 0; i < activeElevators.size(); ++i)
				if (updateData[0].equals(activeElevators.get(i)[0]))
					this.activeElevators.remove(i);
			
			return;
		}
		else if(updateData[6].replaceAll("\\P{Print}","").equals("HasArrived")) {
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): service floor " + updateData[2] + ".\n");
			state = state.nextState();
			Scheduler.currentRequests.remove(0);
			if(Scheduler.numEventsServed < 0) {
				Scheduler.numEventsServed = 0;
			}
			if(Scheduler.currentRequests.isEmpty()) {
				writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + this.state.Current() + ".\n");
			}
			Scheduler.numEventsServed++;
			hasArrived = true;
			
			writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: remaining requests: "+currentRequests.size()+"\n");
			
			return;
		}else if(updateData[6].replaceAll("\\P{Print}","").equals("handleFaults")) {
			//elevator experienced a fault
			String[] faultData = new String[3];
			faultData[0] = updateData[8];	//get fault code
			faultData[1] = updateData[1]; 	//get elevator index
			faultData[2] = updateData[5];	//get number of events attributed to this elevator 
			this.elevatorFaults.put(Integer.parseInt(updateData[0]), faultData);
			state = SchedulerState.HandleFault;
			return;
		}

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
			e.printStackTrace();
		}
	}
	
	/**
	 * Serves the request by removing it from the queue of requests. 
	 * 
	 * @return void
	 */
	public void processRemoved() {
		LocalTime s = LocalTime.now();
		
		byte arrivedBytes[] = "arrived".getBytes();
		this.sendElevatorPacket = new DatagramPacket(arrivedBytes, arrivedBytes.length, localAddr, receivedElevatorPacket.getPort());
		
		try { 
			this.receiveSocket.send(sendElevatorPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem (elevator): current state - " + this.state.Current() + ".\n");
		
		if(!Scheduler.currentRequests.isEmpty()) {
			state = SchedulerState.GetElevatorUpdate;
		}
	}
	
	/**
	 * Handles the faults. If a floor fault occurred shut down the elevator. If a door fault occurs tell the elevator to reset their doors
	 * 
	 * @return void
	 */
	public void handleFaults() {
		LocalTime s = LocalTime.now();
		writeToElevatorTrace(s.toString() + " - Scheduler Subsystem: Switching to State: HandleFault\n");
		
		for(Map.Entry<Integer, String[]> entry : this.elevatorFaults.entrySet()) {
			writeToElevatorTrace(s.toString() + " - Handling " + entry.getValue()[0].trim() + " fault from elevator#" + entry.getKey() + ".\n");
			if(entry.getValue()[0].contains("Door")) {
				byte updateBytes[] = "ResetDoors".getBytes();
				DatagramPacket faultPacket = new DatagramPacket(updateBytes, updateBytes.length, localAddr, Integer.parseInt(entry.getValue()[1]));
				try { 
					this.receiveSocket.send(faultPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//remove fault
				this.elevatorFaults.remove(entry.getKey());
			}else if(entry.getValue()[0].contains("Floor")) {
				byte updateBytes[] = "ShutDownElevator".getBytes();
				DatagramPacket faultPacket = new DatagramPacket(updateBytes, updateBytes.length, localAddr, Integer.parseInt(entry.getValue()[1]));
				try { 
					this.receiveSocket.send(faultPacket);
				} catch (IOException e) {
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
		
		final int elevatorCount = 4;
				
		Thread scheduler_server = new Thread(new Scheduler(true, elevatorCount), "Floor Scheduler Thread");
		Thread scheduler_client = new Thread(new Scheduler(false, elevatorCount), "Elevator Scheduler Thread");
		scheduler_client.start();
		scheduler_server.start();
	}
}