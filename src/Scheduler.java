import java.util.ArrayList;

/*
 * @param	floorRequest    	- the floor request that acts as the shared memory between the floor and scheduler
 * @param   elevatorRequest     - the elevator request that acts as the shared memory between the elevator and scheduler
 */


public class Scheduler implements Runnable{
	
	private FloorRequest floorRequest;

	private ElevatorRequest elevatorRequest;
	
	public Scheduler(FloorRequest floorRequest, ElevatorRequest elevatorRequest) {
		this.floorRequest = floorRequest;
		this.elevatorRequest = elevatorRequest;
	}

	
	public void run() {
		while(true) {
			if(this.floorRequest.hasRequest()) {
				elevatorRequest.notifyElevatorRequest(floorRequest.getFloorNum(), floorRequest.getCarBut());
				elevatorRequest.requestServed();
				floorRequest.remove();
			}
		}
	}
	
	
}