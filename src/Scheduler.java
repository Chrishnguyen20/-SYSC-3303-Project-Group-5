import java.util.ArrayList;

/*
 * @param	newFloorRequest  	- a boolean indication whether the system is accepting
 * 								  new floor requests.
 */


public class Scheduler{
	
	// TO-DO
	
	private ArrayList<FloorRequest> fr;
	private ArrayList<Elevator> elevators;
	
	private ArrayList<Elevator> activeElevators;
	private ArrayList<Elevator> idleElevators;

	private boolean newFloorRequest;
	//private boolean pendingFloorRequest;
	
	public Scheduler() {
		this.fr = new ArrayList<>();

		this.newFloorRequest     = true;
		//this.pendingFloorRequest = false;
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
		//this.pendingFloorRequest = true;
		notifyAll();
	}
	
	public synchronized void elevatorRequest(Elevator elevator) {
		while(fr.isEmpty()) {
			try {
				wait();
			}catch( InterruptedException e) {
				System.err.println(e);
			}
		}
		
		elevator.setDestFloor(fr.get(0).getFloorNum());
		
			
		System.out.println(elevator.toString());
		notifyAll();
	}
	
	public synchronized void elevatorUpdate(int carNum, int currFloor) {
		elevators.get(carNum).setCurrentFloor(currFloor);
	}
}
