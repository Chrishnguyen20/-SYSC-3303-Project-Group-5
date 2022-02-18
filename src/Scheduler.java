
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
	
	public enum schedulerState{
		Initial{
			public schedulerState nextState() {
				return CreateRequest;
			}
			public boolean Approved() {
				return true;
			}
		},
		CreateRequest{
			public schedulerState nextState() {
				return NotifyElevator;
			}
			public boolean Approved() {
				return true;
			}
		},
		NotifyElevator{
			public schedulerState nextState() {
				return Request;
			}
			public boolean Approved() {
				return true;
			}
		},
		Request{
			public schedulerState nextState() {
				return Removed;
			}
			public boolean Approved() {
				return true;
			}
		},
		Removed{
			public schedulerState nextState() {
				return this;
			}
			public boolean Approved() {
				return true;
			}
		};	
		
		public abstract schedulerState nextState(); 
	    public abstract boolean Approved();
	}
	
	public Scheduler(FloorRequest floorRequest, ElevatorRequest elevatorRequest) {
		this.floorRequest = floorRequest;
		this.elevatorRequest = elevatorRequest;
	}

	
	
	
	public void run() {
		while(true) {
			schedulerState state = schedulerState.Initial;
			if(this.floorRequest.hasRequest()) {
				state = state.nextState();
				//assertEquals(schedulerState.CreateRequest, state);
				elevatorRequest.notifyElevatorRequest(floorRequest.getFloorNum(), floorRequest.getCarBut());
				state = state.nextState();
				elevatorRequest.requestServed();
				state = state.nextState();
				floorRequest.remove();
				state = state.nextState();
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