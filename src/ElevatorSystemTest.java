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
	 */
	private boolean existsInTrace(String s) {
		boolean flag = true;
		while(flag) {
			FileReader fileReader;
			BufferedReader reader = null;
			try {
				//Read the in-file and store it sin a readable buffer
				fileReader = new FileReader("trace.txt");
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
			if(this.scheduler_server.getCurrentState().equals(s)) {
				return true;
			}
		}
		return false;
	}

	
	
	//@purpose test that the program runs successfully
	@ParameterizedTest
	@ValueSource(strings = {"EOF", "Floor Subsystem: Queuing request for floor 6 at 04:55:20.524 going down to floor 5", 
			"Elevator Subsystem: Current Pos of Elevator: 1", "Elevator Subsystem: Current Pos of Elevator: 6"})
	
	void iteration_one_tests(String event) {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		
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

		TraceFile.init();
		
		s1.start();
		s2.start();
		f.start();
		e.start();
		
		assert(existsInTrace(event));
		
	}
	
	
	//@purpose checks the initial state of the elevator
	@Test
	void elevatorInitialState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		try {
			this.elevator = new Elevator(1, 206);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(this.elevator.getState().equals("Initial"));
		
	}
	
	//@purpose checks the initial state of the scheduler
	@Test
	void schedulerInitialState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();

		this.scheduler_server = new Scheduler(true);
		assert(this.scheduler_server.getCurrentState().equals("Initial"));
		
	}
	
	//@purpose checks the final state of the elevator
	@Test
	void elevatorHasArrivedState() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		
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

		TraceFile.init();

		s1.start();
		s2.start();
		f.start();
		e.start();
		assert(finalState("e", "HasArrived"));
		
	}
	
	//@purpose checks the final state of the scheduler
	@Test
	void schedulerDone() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		
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

		TraceFile.init();

		s1.start();
		s2.start();
		f.start();
		e.start();
		assert(finalState("s", "Request removed"));
		
	}
}
	
