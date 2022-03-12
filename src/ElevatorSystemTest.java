import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;

import org.junit.jupiter.api.Order;
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
						}else if(line.contains("EOF")) {
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
						}else if(line.contains("EOF")) {
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
					if(line.contains("EOF")) {
						flag = false;
					}else if(line.contains(s)) {
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
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@ValueSource(strings = {"EOF", "Floor Subsystem: Queued a request -- request for floor 1 going up",
			 "Scheduler Subsystem: Queueing event from floor subsystem",
			 "Scheduler Subsystem: Sending floor acknowledgement",
			 "Floor Subsystem: Received an acknowledgement",
			 "Floor Subsystem: Queued a request -- request for floor 6 going down"})
	void floor_tests(String event) {		
		assert(existsInTrace(event, false));	
	}
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@Order(3)
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
	
	//@purpose checks the whether multiple elevators are moving throughout the program
	@ParameterizedTest
	@ValueSource(strings = {"Elevator#1 current state - Initial",
			 "Elevator#0 current state - Initial",
			 "Elevator#0 initialize elevator 0",
			 "Elevator#1 initialize elevator 1",
			 "Elevator#0 current Pos: 3",
			 "Elevator#1 current Pos: 2",
			 "Elevator#1 current state - HasArrived",
			 "Elevator#0 current state - HasArrived"})
	void multipleElevators(String event) {		
		assert(existsInTrace(event, true));
	}
	
}
	
