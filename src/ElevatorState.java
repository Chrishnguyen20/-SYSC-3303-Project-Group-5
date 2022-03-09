
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
			if (elevator.getReceivedPassengers() == 0 && elevator.getCurrentFloor() == elevator.getFloorNum()) {
				return PassengersBoarding;
			} else if (elevator.getCurrentFloor() == elevator.getDestFloor() && !(elevator.getReceivedPassengers() == 0)) {
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