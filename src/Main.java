/**
 * 
 */


/**
 * @author minhj
 *
 * Main method that initializes and starts the threads
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String args[]) {
//		
		ElevatorRequest er = new ElevatorRequest();
		FloorRequest fr = new FloorRequest();
		
		Thread scheduler = new Thread(new Scheduler(fr,er), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(er,1), "Elevator Thread");
		Thread floor = new Thread(new Floor(fr), "Floor Thread");
//		
		scheduler.start();
		floor.start();
		elevator.start();
	}
}
