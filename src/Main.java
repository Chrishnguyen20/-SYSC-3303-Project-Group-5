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
		
		Scheduler scheduler = new Scheduler();
		
		Thread elevator = new Thread(new Elevator(scheduler, 1, "up"));
		Thread floor = new Thread(new Floor(scheduler), "Floor Thread");
	
		floor.start();
	}
}
