import java.util.ArrayList;

/*
 * @param	newFloorRequest  	- a boolean indication whether the system is accepting
 * 								  new floor requests.
 */


public class Scheduler{
	
	// TO-DO
	
	private ArrayList<FloorRequest> fr;
	private ArrayList<Elevator> elevators;

	private boolean newFloorRequest;
	private boolean pendingFloorRequest;
	
	public Scheduler() {
		this.fr = new ArrayList<>();

		this.newFloorRequest = true;
		this.pendingFloorRequest = false;
	}
	
	public void initElevator(int num) {
		//for(int i = 0; i < )
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
	
	public synchronized void elevatorRequest(Elevator elevator) {
		while(fr.isEmpty()) {
			try {
				wait();
			}catch( InterruptedException e) {
				System.err.println(e);
			}
		}
		
		elevator.setDestFloor(fr.get(0).getCarBut());
		fr.remove(0);
			
		System.out.println(elevator.toString());
		notifyAll();
	}
	
	public synchronized void elevatorUpdate(int carNum, int curFloor) {
		
		int dest = elevators.get(carNum).getDestFloor();
		elevators.get(carNum).setCurrentFloor(curFloor);
		if (curFloor == dest) {
			elevators.get(carNum).setIdle();
		}
	}
}
