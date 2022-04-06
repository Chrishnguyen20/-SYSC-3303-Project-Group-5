/**
  The states of the elevator 
*/

public enum ElevatorState {

	Initial {
		@Override
		public ElevatorState nextState(Elevator elevator) {
			return NoElevatorRequest;
		}

		@Override
		public String getElevatorState() {
			return "Initial";
		}
	},
	NoElevatorRequest {
		@Override
		public ElevatorState nextState(Elevator elevator) {
			if (elevator.hasRequest()) {
				return MoveToDestination;
			}
			return NoElevatorRequest;
		}

		@Override
		public String getElevatorState() {
			return "NoElevatorRequest";
		}
	},
	MoveToDestination {
		@Override
		public ElevatorState nextState(Elevator elevator) { 			
			int curFloor = elevator.getCurrentFloor();
			if(elevator.checkFaulted()) {
				return handleFaults;
			}
			else if (curFloor == elevator.getFirstPassengerFloor()) {
				return PassengersBoarding;
			}
			else if (curFloor == elevator.getFirstDestFloor()
					&& elevator.getReceivedPassengers() != 0) {
				return HasArrived;
			}
			
			return MoveToDestination;
		}

		@Override
		public String getElevatorState() {
			return "MoveToDestination";
		}
	},
	PassengersBoarding {
		@Override
		public ElevatorState nextState(Elevator elevator) {
			if(elevator.checkFaulted()) {
				return handleFaults;
			}
			else if (elevator.getCurrentFloor() == elevator.getFirstDestFloor()
					&& elevator.getReceivedPassengers() != 0) {
				return HasArrived;
			}
			return MoveToDestination;
		}

		@Override
		public String getElevatorState() {
			return "PassengersBoarding";
		}
	},
	HasArrived {
		@Override
		public ElevatorState nextState(Elevator elevator) {
			if(elevator.checkFaulted()) {
				return handleFaults;
			}
			else if (elevator.hasRequest()) {
				return MoveToDestination;
			}
			return NoElevatorRequest;
		}

		@Override
		public String getElevatorState() {
			return "HasArrived";
		}
	},
	handleFaults{
		public ElevatorState nextState(Elevator elevator) {
			int curFloor = elevator.getCurrentFloor();
			if (curFloor == elevator.getFirstPassengerFloor()) {
				return PassengersBoarding;
			}
			else if (curFloor == elevator.getFirstDestFloor()
					&& elevator.getReceivedPassengers() != 0) {
				return HasArrived;
			}
			return MoveToDestination;
		}

		@Override
		public String getElevatorState() {
			return "handleFaults";
		}
		
	};

	/**
	 *  Returns the next state of the Elevator 
	 */
	public abstract ElevatorState nextState(Elevator elevator);

	/**
	 * Gets the current state of the Elevator
	 * @return string - representing the current state of the Elevator
	 */
	public abstract String getElevatorState();
}