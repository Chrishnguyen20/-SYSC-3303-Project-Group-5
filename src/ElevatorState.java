/*
* @purpose - The states of the elevator 
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
			if (curFloor == elevator.getFirstPassengerFloor()) {
				return PassengersBoarding;
			}
			else if (curFloor == elevator.getFirstDestFloor()
					&& (curFloor <= elevator.getObjectiveFloor() == curFloor <= elevator.getDestFloor())) {
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
			if (elevator.hasRequest()) {
				return MoveToDestination;
			}
			return NoElevatorRequest;
		}

		@Override
		public String getElevatorState() {
			return "HasArrived";
		}
	};

	public abstract ElevatorState nextState(Elevator elevator);

	public abstract String getElevatorState();
}