
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
	
	private String states;
	
	public enum schedulerState{
		WaitRequest{
			public schedulerState nextState() {
				return NotifyElevator;
			}
			public int Current() {
				return 1;
			}
		},
		NotifyElevator{
			public schedulerState nextState() {
				return Served;
			}
			public int Current() {
				return 2;
			}
		},
		Served{
			public schedulerState nextState() {
				return Removed;
			}
			public int Current() {
				return 3;
			}
		},
		Removed{
			public schedulerState nextState() {
				return WaitRequest;
			}
			public int Current() {
				return 4;
			}
		};	
		
		public abstract schedulerState nextState(); 
	    public abstract int Current();
	}
	
	public Scheduler(FloorRequest floorRequest, ElevatorRequest elevatorRequest) {
		this.floorRequest = floorRequest;
		this.elevatorRequest = elevatorRequest;
	}

	public String getCurrentState() {
		return states;
	}
	
	
	public void run() {
		schedulerState state = schedulerState.WaitRequest;
		while(true) {
			if(this.floorRequest.hasRequest()) {
				switch(state.Current()){
					case 1:
						state = state.nextState();
						states = "Has request";
						break;	
					case 2:
						elevatorRequest.notifyElevatorRequest(floorRequest.getFloorNum(), floorRequest.getCarBut());
						state = state.nextState();
						states = "Notified elevator";
						break;
					case 3:
						elevatorRequest.requestServed();
						state = state.nextState();
						states = "Request served";
						break;
					case 4:
						floorRequest.remove();
						state = state.nextState();
						states = "Request removed";
						break;
				}
			}
			/*
			//schedulerState state = schedulerState.WaitRequest;
			if(this.floorRequest.hasRequest()) {
				//state = state.nextState();
				//assertEquals(schedulerState.CreateRequest, state);
				elevatorRequest.notifyElevatorRequest(floorRequest.getFloorNum(), floorRequest.getCarBut());
				//state = state.nextState();
				elevatorRequest.requestServed();
				//state = state.nextState();
				floorRequest.remove();
				//state = state.nextState();
			}
			*/
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