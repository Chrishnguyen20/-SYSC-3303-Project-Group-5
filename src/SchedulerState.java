
public enum SchedulerState {
	WaitRequest {
		public SchedulerState nextState() {
			return NotifyElevator;
		}

		public int Current() {
			return 1;
		}
	},
	NotifyElevator {
		public SchedulerState nextState() {
			return Served;
		}

		public int Current() {
			return 2;
		}
	},
	Served {
		public SchedulerState nextState() {
			return Removed;
		}

		public int Current() {
			return 3;
		}
	},
	Removed {
		public SchedulerState nextState() {
			return WaitRequest;
		}

		public int Current() {
			return 4;
		}
	};

	public abstract SchedulerState nextState();

	public abstract int Current();
}
