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
	private static LinkedBlockingQueue<String[]> floorQueue;
	private ArrayList<String[]> activeElevators;

	public Scheduler(boolean b) {
		this.isClient = b;
		this.activeElevators = new ArrayList<String[]>();
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

	/*
	 * @purpose - The states of the scheduler 
	 */
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
				if (!Scheduler.floorQueue.isEmpty()) {
					switch (state.Current()) {
					case 1:
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
						writeToElevatorTrace("Scheduler Subsystem: Send data to an active elevator\n");
						String elevatorData = String.valueOf(Scheduler.floorQueue.peek()[1]) + "," + String.valueOf(Scheduler.floorQueue.peek()[3]);
						Scheduler.floorQueue.poll();
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
						//be smart and pick up any passengers on this floor going in the direction 
						
						writeToElevatorTrace("Scheduler Subsystem: got update from elevator#" + updateData[0] + "\n");
						if(updateData[2].replaceAll("\\P{Print}","").equals("HasArrived")) {
							writeToElevatorTrace("Scheduler Subsystem: service floor " + updateData[1] + "\n");
							state = state.nextState();
						}
						states = "Request served";
						break;
					case 4:
						state = state.nextState();
						states = "Request removed";
						break;
					}
				}
			}
		}
	}
}