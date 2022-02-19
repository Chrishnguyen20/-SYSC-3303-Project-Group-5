
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
public class Elevator implements Runnable{
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private float time;
	private int receivedPassengers;
	private ElevatorRequest elevatorRequest;
	private ElevatorState state;
	
	public Elevator (ElevatorRequest elevatorRequest, int floornum) {
		this.currentFloor = floornum;
		this.elevatorRequest = elevatorRequest;
		this.carNum = nextCarNum++;
		this.receivedPassengers = 0;
		this.time = (float) 9.175;
		this.state = ElevatorState.Initial;
	}
	
	
	
	public int getCarNum() { return this.carNum; }
	
	public int getCurrentFloor() { return this.currentFloor; }
	
	public void setCurrentFloor(int curr) { this.currentFloor = curr; }
	
	public float getTime() { return this.time; }
	
	public int getReceivedPassengers() { return receivedPassengers; }
	
	public ElevatorRequest getElevatorRequest() { return elevatorRequest; }
	
	public String getState() { return state.getElevatorState(); }
	
	
	
	public String getDiretion() {
		if(getObjectiveFloor() > this.currentFloor) {
			return "up";
		}else{
			return "down";
		}
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
            Thread.sleep((int)this.time*1);
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}
	
	private void simulateFloorMovement() {
		// Simulate movement between floors
        try {
            Thread.sleep((int)this.time*1);
        } catch (InterruptedException e) {
        	System.err.println(e);
        }
	}

	public void move() {
		if (this.currentFloor == getObjectiveFloor()) {
			return;
		}
		
		if(this.currentFloor >= 1 && this.currentFloor <= 7) {
			if(this.currentFloor == 1 && getDiretion() == "down") {
				this.currentFloor += 1;
			}
			else if(this.currentFloor == 7 && getDiretion() == "up") {
				this.currentFloor -= 1;
			}
			
			if(getDiretion() == "up") {
				this.currentFloor += 1;
			}
			else if(getDiretion() == "down") {
				this.currentFloor -= 1;
			}
		}
	}
	
	public void run() {
		while(true) {
			String currentState = state.getElevatorState();
			
			switch (currentState) {
			case "NoElevatorRequest":
				// Elevator is waiting for ElevatorRequest
				this.elevatorRequest.updatedPosition(this.currentFloor, this.receivedPassengers);
				
				state = state.nextState(this);
				
				TraceFile.toTrace("Elevator is currently idle waiting for ElevatorRequest!\n");
				
				continue;
			case "PassengersBoarding":
				// Simulate passengers boarding
				openDoors();
            	
	            this.receivedPassengers++;
            	
            	TraceFile.toTrace("Passengers boarded on floor: " + currentFloor + "\n");
            	TraceFile.toTrace("Passengers currently in elevator: " + receivedPassengers + "\n");
            	
				break;
			case "MoveToDestination":
				move();
				
	            // Simulate movement between floors
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