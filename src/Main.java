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
				
		Thread scheduler_server = new Thread(new Scheduler(true), "Floor Scheduler Thread");
		Thread scheduler_client = new Thread(new Scheduler(false), "Elevator Scheduler Thread");
		Thread elevator1 = new Thread(new Elevator(1, 204), "Elevator1 Thread");
		Thread elevator2 = new Thread(new Elevator(1, 205), "Elevator2 Thread");
		Thread floor = new Thread(new Floor(), "Floor Thread");
		
		scheduler_server.start();
		scheduler_client.start();
		floor.start();
		elevator1.start();
		//elevator2.start();
	}
}
