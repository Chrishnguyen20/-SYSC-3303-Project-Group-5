/**
 * 
 */


/**
 *
 * Main method that initializes and starts the threads
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
//		
		ElevatorRequest elevatorRequest = new ElevatorRequest();
		FloorRequest floorRequest = new FloorRequest();
		
		Thread scheduler = new Thread(new Scheduler(floorRequest, elevatorRequest), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(elevatorRequest,1), "Elevator Thread");
		Thread floor = new Thread(new Floor(floorRequest), "Floor Thread");
//		
		
		TraceFile.init();
		
		scheduler.start();
		floor.start();
		elevator.start();
	}
}
