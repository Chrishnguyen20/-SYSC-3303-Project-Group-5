
/**     
 * @purpose                  - The Elevator class obtains elevator requests from the scheduler
 * 					           and will move until it gets to the destination floor
 * @param currentFloor       - Represents the current floor that the elevator is on 
 * @param nextCarNum         - Static variable used to assign id's to the elevator
 * @param carNum		     - The elevator id number
 * @param time			     - Amount of time the elevator should sleep for to simulate movement, doors opening, and passengers boarding/disembarking
 * @param receivedPassengers - Whether passengers have boarded the elevator 
 * @param elevatorRequest    - the shared memory between the elevator and scheduler.
 */
public class Elevator implements Runnable {
	
	private static final float time = 9.175f;
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private int receivedPassengers;
	private ElevatorRequest elevatorRequest;
	private ElevatorState state;
	
	public Elevator (ElevatorRequest elevatorRequest, int floornum) {
		this.currentFloor = floornum;
		this.elevatorRequest = elevatorRequest;
		this.carNum = nextCarNum++;
		this.receivedPassengers = 0;
		this.state = ElevatorState.Initial;
	}
	
	
	
	public int getCarNum() { return this.carNum; }
	
	public int getCurrentFloor() { return this.currentFloor; }
	
	public void setCurrentFloor(int cur) { this.currentFloor = cur; }
	
	public float getTime() { return Elevator.time; }
	
	public int getReceivedPassengers() { return receivedPassengers; }
	
	public ElevatorRequest getElevatorRequest() { return elevatorRequest; }
	
	public String getState() { return state.getElevatorState(); }
	
	
	
	public String getDiretion() {
		return getObjectiveFloor() > this.currentFloor ? "up" : "down";
	}
	
	public int getDestFloor() {
		return this.elevatorRequest.getDestFloor();
	}
	
	public int getFloorNum() {
		return this.elevatorRequest.getFloorNum();
	}
	
	public int getObjectiveFloor() {
		if (this.receivedPassengers == 0) {
			return getFloorNum(); 
		}
		return getDestFloor(); 
	}
	
	private void openDoors() {
		// Simulate doors opening
        try {
            Thread.sleep((int) getTime() * 1);
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}
	
	private void simulateFloorMovement() {
		// Simulate movement between floors
        try {
            Thread.sleep((int) getTime() * 1);
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}

	public void move() {
		if (this.currentFloor == getObjectiveFloor()) {
			return;
		}

		if (this.currentFloor >= 1 && this.currentFloor <= 7) {
			if (this.currentFloor == 1 && getDiretion() == "down") {
				this.currentFloor += 1;
			} 
			else if (this.currentFloor == 7 && getDiretion() == "up") {
				this.currentFloor -= 1;
			}

			this.currentFloor = getDiretion() == "up" ? currentFloor + 1 : currentFloor - 1;
		}
	}
	
	public void run() {
		while(true) {
			String currentState = state.getElevatorState();
			
			switch (currentState) {
			case "NoElevatorRequest":
				// Elevator is waiting for ElevatorRequest
				TraceFile.toTrace("Elevator is currently idle and waiting for an ElevatorRequest!\n");
				
				break;
			case "PassengersBoarding":
				// Simulate passengers boarding
				openDoors();
            	
	            this.receivedPassengers++;
            	
            	TraceFile.toTrace("Passengers boarded on floor: " + currentFloor + "\n");
            	TraceFile.toTrace("Passengers currently in elevator: " + receivedPassengers + "\n");
            	
				break;
			case "MoveToDestination":
				// Simulate movement between floors
				move();
				
				simulateFloorMovement();
				
				TraceFile.toTrace("Current Pos of Elevator: "+ currentFloor + "\n");
				
				break;
			case "HasArrived":
				// Simulate doors opening
	            openDoors();
	            
	            this.elevatorRequest.requestServed();
	            
	            this.receivedPassengers--;

				break;
			}
			
			this.elevatorRequest.updatedPosition(this.currentFloor, this.receivedPassengers);
			
			state = state.nextState(this);
		}
	}
}