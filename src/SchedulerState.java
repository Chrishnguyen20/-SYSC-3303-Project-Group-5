
public enum SchedulerState {
	WaitRequest {
		public SchedulerState nextState() {
			return NotifyElevator;
		}

		public String Current() {
			return "WaitRequest";
		}
	},
	NotifyElevator {
		public SchedulerState nextState() {
			return GetElevatorUpdate;
		}

		public String Current() {
			return "NotifyElevator";
		}
	},
	GetElevatorUpdate {
		public SchedulerState nextState() {
			return Removed;
		}

		public String Current() {
			return "GetElevatorUpdate";
		}
	},
	HandleFault{
		public SchedulerState nextState() {
			return GetElevatorUpdate;
		}
		public String Current() {
			return "HandleFault";
		}
	},
	Removed {
		public SchedulerState nextState() {
			return WaitRequest;
		}

		public String Current() {
			return "Removed";
		}
	};

	public abstract SchedulerState nextState();

	public abstract String Current();
}
