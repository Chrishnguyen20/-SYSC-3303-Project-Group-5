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
		Elevator elevator = new Elevator("Elevator", scheduler);
		Thread floor = new Thread(new Floor("Floor", scheduler), "Floor Thread");

	
		floor.start();
	}
}
