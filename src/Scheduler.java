
/*
 * @param	floorRequest    	- the floor request that acts as the shared memory between the floor and scheduler
 * @param   elevatorRequest     - the elevator request that acts as the shared memory between the elevator and scheduler
 */

public class Scheduler implements Runnable {

	private FloorRequest floorRequest;

	private ElevatorRequest elevatorRequest;

	private String states = "Initial";

	public Scheduler(FloorRequest floorRequest, ElevatorRequest elevatorRequest) {
		this.floorRequest = floorRequest;
		this.elevatorRequest = elevatorRequest;
	}

	
	public enum schedulerState {
		WaitRequest {
			public schedulerState nextState() {
				return NotifyElevator;
			}

			public int Current() {
				return 1;
			}
		},
		NotifyElevator {
			public schedulerState nextState() {
				return Served;
			}

			public int Current() {
				return 2;
			}
		},
		Served {
			public schedulerState nextState() {
				return Removed;
			}

			public int Current() {
				return 3;
			}
		},
		Removed {
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
	
	public String getCurrentState() {
		return states;
	}
	
	public void run() {
		schedulerState state = schedulerState.WaitRequest;
		while (true) {
			if (this.floorRequest.hasRequest()) {
				switch (state.Current()) {
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
		}
	}

}