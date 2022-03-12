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
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * @param	floorRequest    	- the floor request that acts as the shared memory between the floor and scheduler
 * @param   elevatorRequest     - the elevator request that acts as the shared memory between the elevator and scheduler
 */

public class Scheduler implements Runnable {
	private boolean isClient;
	

	private String states = "Initial";
	private DatagramSocket receiveSocket;
	private InetAddress localAddr;
	private DatagramPacket receivedFloorPacket;
	private DatagramPacket receivedElevatorPacket;
	private DatagramPacket sendElevatorPacket;
	//private static LinkedBlockingQueue<String[]> floorQueue;
	private static ArrayList<String[]> requestList;
	private ArrayList<String[]> activeElevators;
	private int elevatorCount;

	public Scheduler(boolean b, int c) {
		this.isClient = b;
		this.elevatorCount = c;
		this.activeElevators = new ArrayList<String[]>();
		Scheduler.requestList = new ArrayList<String[]>();
		//`Scheduler.floorQueue = new LinkedBlockingQueue<String[]>();
		
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
	
	/*

	Function: isPassengerOnPath
	This determines if the given passenger is on the elevators path to it's destination

	 @param Passenger* passenger
	the passenger which will be determined if is on the elevators path to it's destination

	 @return bool
	True if the given passenger is on the elevators path to it's destination, else false

	*/

	boolean isPassengerOnPath(int requestStart, int requestDest, int eStart, int eDest, int eCurrentFloor)
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
	
	int getAvailableElevator(int requestStart, int requestDest) {
		if (activeElevators.isEmpty()) {
			return receivedElevatorPacket.getPort();
		}
		Random rand = new Random();
		int randNum = rand.nextInt(this.activeElevators.size());
		int canidateIndex = -1;
		int minimumRequests = Integer.parseInt(this.activeElevators.get(randNum)[5]);
		for (int i = 0; i < this.activeElevators.size(); ++i) {
			String[] elevator = this.activeElevators.get(i);
			System.out.println("Scheduler getAvailableElevator: "+i+", "+elevator[1]);
			if (Integer.parseInt(elevator[5]) == 0) {
				elevator[5] = "1";
				return i;
			}
			else if (isPassengerOnPath(requestStart, requestDest, Integer.parseInt(elevator[3]), Integer.parseInt(elevator[4]), Integer.parseInt(elevator[2]))
					&& Integer.parseInt(elevator[5]) < minimumRequests) {
				elevator[5] = String.valueOf(Integer.parseInt(elevator[5]) + 1);
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
				
				System.out.println("Scheduler isClient Port: "+receivedFloorPacket.getPort());
				
				String[] floorReq = new String(receivedFloorPacket.getData(), StandardCharsets.UTF_8).split(",");
				for(int i = 0; i < floorReq.length; i++) {
					floorReq[i] = floorReq[i].trim();
				}
				writeToFloorTrace("Scheduler Subsystem: Queueing event from floor subsystem\n");
				requestList.add(floorReq);
				//floorQueue.offer(floorReq);
				try {
					receivedFloorPacket.setPort(200);
					writeToFloorTrace("Scheduler Subsystem: Sending floor acknowledgement\n");
					this.receiveSocket.send(receivedFloorPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				if (!Scheduler.requestList.isEmpty()) {
					System.out.println("Scheduler loop activeElevators.size(): "+activeElevators.size()+", "+Scheduler.requestList.size());
					switch (state.Current()) {
					case 1:
						while(activeElevators.size() < this.elevatorCount) {
							this.receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
							writeToElevatorTrace("Scheduler Subsystem: waiting for elevator\n");
							try {
								this.receiveSocket.receive(receivedElevatorPacket);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							System.out.println("Scheduler floorQueue.isEmpty Port: "+receivedElevatorPacket.getPort());
							
							String[] data = new String(this.receivedElevatorPacket.getData()).split(",");
							
							this.activeElevators.add(data);
							
							writeToElevatorTrace("Scheduler Subsystem: added event\n");
						}
						state = state.nextState();
						states = "Has request";
						break;
						
					case 2:
						writeToElevatorTrace("Scheduler Subsystem: Send data to an active elevator\n");
						
						for (int i = Scheduler.requestList.size() - 1; i >= 0; --i) {
							String[] request = Scheduler.requestList.get(i);
							int elevatorIndex = getAvailableElevator(Integer.parseInt(request[1]), Integer.parseInt(request[3]));
							
							if (elevatorIndex == -1) {
								continue;
							}
							
							String elevatorData = String.valueOf(request[1]) + "," + String.valueOf(request[3]);
							int port = Integer.parseInt(this.activeElevators.get(elevatorIndex)[1]);
							Scheduler.requestList.remove(i);
							
							this.sendElevatorPacket = new DatagramPacket(elevatorData.getBytes(), elevatorData.getBytes().length, localAddr, port);
							
							System.out.println("Scheduler sendElevatorPacket Port: "+port);
							
							try { 
								this.receiveSocket.send(sendElevatorPacket);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						state = state.nextState();
						states = "Notified elevator";
						break;
						
					case 3:
						writeToElevatorTrace("Scheduler Subsystem: Waiting for an elevator to update their status\n");
						receivedElevatorPacket = new DatagramPacket(new byte[1000], 1000);
						try {
							this.receiveSocket.receive(receivedElevatorPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						System.out.println("Scheduler update Port: "+receivedElevatorPacket.getPort());
						
						String[] updateData = new String(receivedElevatorPacket.getData()).split(",");
						
						//boolean newElevator = true;
						
						for(int i = 0; i < activeElevators.size(); i++) {
							if(activeElevators.get(i)[0].equals(updateData[0])) {
								//activeElevators.get(i)[1] = updateData[1];
								updateActiveElevator(updateData, i);
								//newElevator = false;
							}
						}
						
						//be smart and pick up any passengers on this floor going in the direction 
						
						if (!Scheduler.requestList.isEmpty()) {
							state = schedulerState.NotifyElevator;
							continue;
						}
						
						
						writeToElevatorTrace("Scheduler Subsystem: got update from elevator#" + updateData[0] + "\n");
						System.out.println("Scheduler arrived: "+updateData[7]);
						if(updateData[7].replaceAll("\\P{Print}","").equals("hasArrived")) {
							writeToElevatorTrace("Scheduler Subsystem: service floor " + updateData[1] + "\n");
							state = state.nextState();
						}
						states = "Request served";
						break;
						
					case 4:
						writeToElevatorTrace("Scheduler Subsystem: Removed Request!\n");
						state = state.nextState();
						states = "Request removed";
						break;
						
					}
				}
			}
		}
	}
}