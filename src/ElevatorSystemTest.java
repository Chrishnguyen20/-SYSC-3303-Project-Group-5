import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ElevatorSystemTest {


	Scheduler scheduler;
	Elevator elevator;
	Floor floor;
	
	/*
	 * @purpose checks if an event (string) was captured in the trace file
	 * @param s - a string representing some event
	 */
	private boolean existsInTrace(String s) {
		boolean flag = true;
		while(flag) {
			FileReader fileReader;
			BufferedReader reader = null;
			try {
				//Read the in-file and store it in a readable buffer
				fileReader = new FileReader("trace.txt");
				reader = new BufferedReader(fileReader);
			} catch (FileNotFoundException e) {
				//a read error occurred 
				e.printStackTrace();
			}
			
			String line;
			try {
				while ((line = reader.readLine()) != null){
					if(line.equals(s)) {
						return true;
					}else if(line.equals("EOF")) {
						return false;
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
	
	/*
	 * @purpose checks if a subsystem (sub) is in its final state (s)
	 * @param sub - the subsystem (string) being tested ('e' for elevator or 's' for scheduler)
	 * @param s   - the state (string) being tested 
	 * 
	 */
	private boolean finalState(String sub, String s) {
		boolean flag = true;
		while(flag) {
			FileReader fileReader;
			BufferedReader reader = null;
			try {
				//Read the in-file and store it in a readable buffer
				fileReader = new FileReader("trace.txt");
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
					}
				}
				reader.close();
			} catch (IOException e) {
				//an I/O error occurred 
				e.printStackTrace();
			}
		}
		
		
		if(sub.equals("e")) {
			if(this.elevator.getState().equals(s)) {
				return true;
			}
		}else if(sub.equals("s")) {
			if(this.scheduler.getCurrentState().equals(s)) {
				return true;
			}
		}
		return false;
	}

	
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@ValueSource(strings = {"EOF", "Queuing request for floor 6 at 04:55:20.524 going down to floor 5", 
			"Current Pos of Elevator: 1", "Current Pos of Elevator: 6"})
	
	void iteration_one_tests(String event) {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		this.scheduler = new Scheduler(floorRequest, elevatorRequest);
		this.elevator = new Elevator(elevatorRequest,1);
		this.floor = new Floor(floorRequest);

		Thread s = new Thread(this.scheduler, "Scheduler Thread");
		Thread e = new Thread(this.elevator, "Elevator Thread");
		Thread f = new Thread(this.floor, "Floor Thread");

		TraceFile.init();
		
		s.start();
		f.start();
		e.start();
		
		assert(existsInTrace(event));
		
	}
	
	
	//@purpose checks the initial state of the elevator
	@Test
	void elevatorInitialState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		this.elevator = new Elevator(elevatorRequest,1);
		assert(this.elevator.getState().equals("Initial"));
		
	}
	
	//@purpose checks the initial state of the scheduler
	@Test
	void schedulerInitialState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		this.scheduler = new Scheduler(floorRequest, elevatorRequest);
		assert(this.scheduler.getCurrentState().equals("Initial"));
		
	}
	
	//@purpose checks the final state of the elevator
	@Test
	void elevatorHasArrivedState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		this.scheduler = new Scheduler(floorRequest, elevatorRequest);
		this.elevator = new Elevator(elevatorRequest,1);
		this.floor = new Floor(floorRequest);

		Thread s = new Thread(this.scheduler, "Scheduler Thread");
		Thread e = new Thread(this.elevator, "Elevator Thread");
		Thread f = new Thread(this.floor, "Floor Thread");

		TraceFile.init();

		s.start();
		f.start();
		e.start();
		assert(finalState("e", "HasArrived"));
		
	}
	
	//@purpose checks the final state of the scheduler
	@Test
	void schedulerDone() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		this.scheduler = new Scheduler(floorRequest, elevatorRequest);
		this.elevator = new Elevator(elevatorRequest,1);
		this.floor = new Floor(floorRequest);

		Thread s = new Thread(this.scheduler, "Scheduler Thread");
		Thread e = new Thread(this.elevator, "Elevator Thread");
		Thread f = new Thread(this.floor, "Floor Thread");

		TraceFile.init();

		s.start();
		f.start();
		e.start();
		assert(finalState("s", "Request removed"));
		
	}
}
	
