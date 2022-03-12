import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ElevatorSystemTest {


	Scheduler scheduler_client;
	Scheduler scheduler_server;
	Elevator elevator;
	Floor floor;
	
	
	/*
	 * @purpose checks if an event (string) was captured in the trace file
	 * @param s - a string representing some event
	 * @param elevatorTace - a flag indicating which trace file to read
	 * @return boolean true or false
	 */
	private boolean existsInTrace(String s, boolean elevatorTrace) {
		if(elevatorTrace) {
			boolean flag = true;
			while(flag) {
				FileReader fileReader;
				BufferedReader reader = null;
				try {
					//Read the in-file and store it sin a readable buffer
					fileReader = new FileReader("elevator_trace.txt");
					reader = new BufferedReader(fileReader);
				} catch (FileNotFoundException e) {
					//a read error occurred 
					e.printStackTrace();
				}
				
				String line;
				try {
					while ((line = reader.readLine()) != null){
						if(line.contains(s)) {
							return true;
						}else if(line.equals("EOF")) {
							flag = false;
						}
					}
					reader.close();
				} catch (IOException e) {
					//an I/O error occurred 
					e.printStackTrace();
				}
			}		
			return false;
		}else {
			boolean flag = true;
			while(flag) {
				FileReader fileReader;
				BufferedReader reader = null;
				try {
					//Read the in-file and store it sin a readable buffer
					fileReader = new FileReader("floor_trace.txt");
					reader = new BufferedReader(fileReader);
				} catch (FileNotFoundException e) {
					//a read error occurred 
					e.printStackTrace();
				}
				
				String line;
				try {
					while ((line = reader.readLine()) != null){
						if(line.contains(s)) {
							return true;
						}else if(line.equals("EOF")) {
							flag = false;
						}
					}
					reader.close();
				} catch (IOException e) {
					//an I/O error occurred 
					e.printStackTrace();
				}
			}		
			return false;
		}

	}
	
	/*
	 * @purpose checks if a subsystem is in its final state (s)
	 * @param s   - the state (string) being tested 
	 * @return boolean true or false
	 */
	private boolean checkState(String s) {
		boolean flag = true;
		while(flag) {
			FileReader fileReader;
			BufferedReader reader = null;
			try {
				//Read the in-file and store it in a readable buffer
				fileReader = new FileReader("elevator_trace.txt");
				reader = new BufferedReader(fileReader);
			} catch (FileNotFoundException e) {
				//a read error occurred 
				e.printStackTrace();
			}
			
			String line;
			try {
				while ((line = reader.readLine()) != null){
					if(line.equals("EOF")) {
						flag = false;
					}else if(line.equals(s)) {
						return true;
					}
				}
				reader.close();
			} catch (IOException e) {
				//an I/O error occurred 
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Test
	//@purpose run through the program once, the trace file is then used for unit testing
	private void initTests() {
		this.scheduler_server = new Scheduler(true);
		this.scheduler_client = new Scheduler(false);
		try {
			this.elevator = new Elevator(1, 206);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.floor = new Floor();

		Thread s1 = new Thread(this.scheduler_server, "Scheduler Thread");
		Thread s2 = new Thread(this.scheduler_client, "Scheduler Thread");
		Thread e = new Thread(this.elevator, "Elevator Thread");
		Thread f = new Thread(this.floor, "Floor Thread");


		s1.start();
		s2.start();
		f.start();
		e.start();
		assert(existsInTrace("EOF", false) && existsInTrace("EOF", true));
	}
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@ValueSource(strings = {"EOF", "Floor Subsystem: Queued a request -- 21:22:54.314,1,up,3",
			 "Scheduler Subsystem: Queueing event from floor subsystem",
			 "Scheduler Subsystem: Sending floor acknowledgement",
			 "Floor Subsystem: Queued a request -- 14:05:15.000,2,up,4"})
	void floor_tests(String event) {		
		assert(existsInTrace(event, false));	
	}
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@ValueSource(strings = {"EOF", 
			"Elevator#0 initialize elevator 0", "Elevator#0 is currently idle and waiting for an ElevatorRequest!",
			"Scheduler Subsystem: added event", "Scheduler Subsystem: Send data to an active elevator", "Elevator#0 current Pos: 1",
			"Scheduler Subsystem: service floor 5", "Scheduler Subsystem: service floor 3", "Scheduler Subsystem: service floor 4"})
	
	void elevator_scheduler_tests(String event) {		
		assert(existsInTrace(event, true));	
	}
	
	
	//@purpose checks the states of the elevator and scheduler subsystem
	@ParameterizedTest
	@ValueSource(strings = {"Scheduler Subsystem: current state - Initial",
			 "Elevator#0 current state - NoElevatorRequest",
			 "Scheduler Subsystem: current state - Has request",
			 "Scheduler Subsystem: current state - Notified elevator",
			 "Elevator#0 current state - MoveToDestination",
			 "Scheduler Subsystem: current state - Request served",
			 "Elevator#0 current state - PassengersBoarding",
			 "Elevator#0 current state - HasArrived"})
	void elevatorHasArrivedState(String event) {		
		assert(checkState(event));
	}
	
}
	
