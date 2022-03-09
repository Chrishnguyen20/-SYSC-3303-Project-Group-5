import java.net.SocketException;

/**
 *
 * Main method that initializes and starts the threads
 * 
 */
public class Main {

	/**
	 * @param args
	 * @throws SocketException 
	 */
	public static void main(String args[]) throws SocketException {
				
		Thread scheduler_server = new Thread(new Scheduler(true), "Scheduler Thread");
		Thread scheduler_client = new Thread(new Scheduler(false), "Scheduler Thread");
		Thread elevator = new Thread(new Elevator(1, 204), "Elevator Thread");
		Thread floor = new Thread(new Floor(), "Floor Thread");
		
		scheduler_server.start();
		scheduler_client.start();
		floor.start();
		elevator.start();
	}
}
