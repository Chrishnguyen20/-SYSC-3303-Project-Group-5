
/*
 * @param	newFloorRequest  	- a boolean indication whether the system is accepting
 * 								  new floor requests.
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