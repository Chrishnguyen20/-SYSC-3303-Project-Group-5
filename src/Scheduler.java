
/*
 * @param	floorRequest    	- the floor request that acts as the shared memory between the floor and scheduler
 * @param   elevatorRequest     - the elevator request that acts as the shared memory between the elevator and scheduler
 */

/*
 * dictionary:
 * 		"a" -> first state
 * 		"b" -> second state	
 * 		... 
 * 
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

	/*
	 * While true:
	 * 	Is it in the "has floor request state"
	 * 		is it in the notify elevator state: 
	 * 			//notify elevator
	 * 			... 
	 * 			//set state to request served
	 * 		is it in the request served state:
	 * 			//request served
	 * 			//set state to remove floor
	 * 		is it in the floor request state
	 * 			//remove floor request 
	 * 			//reset state
	 * 
	 */

}