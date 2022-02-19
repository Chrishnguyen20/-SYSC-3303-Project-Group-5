import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

class ElevatorSystemTest {

	
	//@purpose checks if a string exists in the trace file
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


	//@purpose test that the program runs successfully
	@Test
	void programRunsCorrectly() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		Thread scheduler = new Thread(new Scheduler(floorRequest, elevatorRequest), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(elevatorRequest,1), "Elevator Thread");
		Thread floor = new Thread(new Floor(floorRequest), "Floor Thread");

		TraceFile.init();
		
		scheduler.start();
		floor.start();
		elevator.start();
		
		assert(existsInTrace("EOF"));
		
	}
	
	//@purpose The floor class queued the third request
	@Test
	void finalFloorRequestQueues() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		Thread scheduler = new Thread(new Scheduler(floorRequest, elevatorRequest), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(elevatorRequest,1), "Elevator Thread");
		Thread floor = new Thread(new Floor(floorRequest), "Floor Thread");

		TraceFile.init();
		
		scheduler.start();
		floor.start();
		elevator.start();
		
		assert(existsInTrace("Queuing request for floor 6 at 04:55:20.524 going down to floor 5"));
		
	}
	
	//@purpose test The elevator makes it to floor 1 and floor 6
	@Test
	void elevatorArrivesAtFloorOne() {
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		Thread scheduler = new Thread(new Scheduler(floorRequest, elevatorRequest), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(elevatorRequest,1), "Elevator Thread");
		Thread floor = new Thread(new Floor(floorRequest), "Floor Thread");

		TraceFile.init();
		
		scheduler.start();
		floor.start();
		elevator.start();
		
		boolean case1 = existsInTrace("Current Pos of Elevator: 1");
		boolean case2 = existsInTrace("Current Pos of Elevator: 6");
		
		assert(case1 && case2);
		
	}
	

}
