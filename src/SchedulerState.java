
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
			return Served;
		}

		public String Current() {
			return "NotifyElevator";
		}
	},
	Served {
		public SchedulerState nextState() {
			return Removed;
		}

		public String Current() {
			return "Served";
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
