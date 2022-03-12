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
import java.util.ArrayList;
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
 */

public class Scheduler implements Runnable {
	private boolean isClient;
	

	private String states = "Initial";
	private DatagramSocket receiveSocket;
	private InetAddress localAddr;
	private DatagramPacket receivedFloorPacket;
	private DatagramPacket receivedElevatorPacket;
	private DatagramPacket sendElevatorPacket;
	private static LinkedBlockingQueue<String[]> floorQueue;
	private static ArrayList<String[]> currentRequests;
	private ArrayList<String[]> activeElevators;
	
	private static int numEventsQueued = 0;
	private static int numEventsServed = -1;

	public Scheduler(boolean b) {
		this.isClient = b;
		this.activeElevators = new ArrayList<String[]>();
		Scheduler.currentRequests = new ArrayList<String[]>();
		Scheduler.floorQueue = new LinkedBlockingQueue<String[]>();
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

	
	public enum schedulerState {
		WaitRequest {
			public schedulerState nextState() {
				return NotifyElevator;
			}

			public int Current() {
				return 1;
			}
		},
		NotifyElevator {
			public schedulerState nextState() {
				return Served;
			}

			public int Current() {
				return 2;
			}
		},
		Served {
			public schedulerState nextState() {
				return Removed;
			}

			public int Current() {
				return 3;
			}
		},
		Removed {
			public schedulerState nextState() {
				return WaitRequest;
			}

			public int Current() {
				return 4;
			}
		};

		public abstract schedulerState nextState();

		public abstract int Current();
	}
	
	public String getCurrentState() {
		return states;
	}
	
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

	}
	
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
		
		schedulerState state = schedulerState.WaitRequest;
		while (true) {
			if(isClient) {
				try {
					this.receivedFloorPacket = new DatagramPacket(new byte[21], 21);
					this.receiveSocket.receive(receivedFloorPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] floorReq = new String(receivedFloorPacket.getData(), StandardCharsets.UTF_8).split(",");
				for(int i = 0; i < floorReq.length; i++) {
					floorReq[i] = floorReq[i].trim();
				}
				writeToFloorTrace("Scheduler Subsystem: Queueing event from floor subsystem\n");
				Scheduler.numEventsQueued++;
				floorQueue.offer(floorReq);
				try {
					receivedFloorPacket.setPort(200);
					writeToFloorTrace("Scheduler Subsystem: Sending floor acknowledgement\n");
					this.receiveSocket.send(receivedFloorPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				if (!Scheduler.floorQueue.isEmpty() || !Scheduler.currentRequests.isEmpty()) {
					switch (state.Current()) {
					case 1:
						writeToElevatorTrace("Scheduler Subsystem: current state - " + states + "\n");
						state = state.nextState();
						states = "Has request";
						this.receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
						writeToElevatorTrace("Scheduler Subsystem: waiting for elevator\n");
						try {
							this.receiveSocket.receive(receivedElevatorPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String[] data = new String(this.receivedElevatorPacket.getData()).split(",");
						this.activeElevators.add(data);
						writeToElevatorTrace("Scheduler Subsystem: added event\n");
						break;
					case 2:
						writeToElevatorTrace("Scheduler Subsystem: current state - " + states + "\n");
						writeToElevatorTrace("Scheduler Subsystem: Send data to an active elevator\n");
						String elevatorData = String.valueOf(Scheduler.floorQueue.peek()[1]) + "," + String.valueOf(Scheduler.floorQueue.peek()[3]);
						Scheduler.currentRequests.add(Scheduler.floorQueue.poll());
						this.sendElevatorPacket = new DatagramPacket(elevatorData.getBytes(), elevatorData.getBytes().length, localAddr, receivedElevatorPacket.getPort());
						try { 
							this.receiveSocket.send(sendElevatorPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						state = state.nextState();
						states = "Notified elevator";
						break;
					case 3:
						writeToElevatorTrace("Scheduler Subsystem: current state - " + states + "\n");
						writeToElevatorTrace("Scheduler Subsystem: Waiting for an elevator to update their status\n");
						receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
						try {
							this.receiveSocket.receive(receivedElevatorPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String[] updateData = new String(receivedElevatorPacket.getData()).split(",");
						for(int i = 0; i < activeElevators.size(); i++) {
							if(activeElevators.get(i)[0].equals(updateData[0])) {
								activeElevators.get(i)[1] = updateData[1];
							}
						}						
						writeToElevatorTrace("Scheduler Subsystem: got update from elevator#" + updateData[0] + " -- " + updateData[2] + "\n");
						if(updateData[2].replaceAll("\\P{Print}","").equals("HasArrived")) {
							writeToElevatorTrace("Scheduler Subsystem: service floor " + updateData[1] + "\n");
							state = state.nextState();
							Scheduler.currentRequests.remove(0);
							if(Scheduler.numEventsServed < 0) {
								Scheduler.numEventsServed = 0;
							}
							Scheduler.numEventsServed++;
						}
						states = "Request served";
						break;
					case 4:
						writeToElevatorTrace("Scheduler Subsystem: current state - " + states + "\n");
						state = state.nextState();
						states = "Request removed";
						break;
					}
					if(Scheduler.numEventsQueued == Scheduler.numEventsServed) {
						writeToElevatorTrace("EOF");
						Scheduler.numEventsServed++;
					}
				}
			}
		}
	}
}