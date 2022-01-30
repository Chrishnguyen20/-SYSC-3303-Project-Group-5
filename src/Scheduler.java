import java.util.ArrayList;

/*
 * @param	newFloorRequest  	- a boolean indication whether the system is accepting
 * 								  new floor requests.
 */


public class Scheduler{
	
	private ArrayList<String> queue;
	
	private ArrayList<FloorRequest> fr;
	
	private boolean newFloorRequest;
	private boolean pendingFloorRequest;
	
	public Scheduler() {
		this.queue = new ArrayList<>();
		this.fr = new ArrayList<>();

		this.newFloorRequest     = true;
		this.pendingFloorRequest = false;
	}
	
	/*
	 * @purpose       - adds a new floor request to the queue for the scheduler to process
	 * @param request - a new floor request object
	 */
	public synchronized void floorRequest(FloorRequest request) {
		while(!this.newFloorRequest) {
			try {
				wait();
			}catch( InterruptedException e) {
				System.err.println(e);
			}
		}
		
		this.fr.add(request);
			
		System.out.println(request.toString());
		//TODO: process the event in some way
		this.pendingFloorRequest = true;
		notifyAll();
	}
	
	
	public synchronized void recieveUpdate(String info) {
		queue.add(info);
	}
	
	public synchronized String updateElevator() {
		try {
			String element = queue.remove(0);
			return element;
		} catch (NullPointerException e) {
			return null;
		}
	}
}
