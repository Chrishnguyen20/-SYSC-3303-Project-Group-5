import java.util.ArrayList;
/**
 * @author Leo Xu
 * @purpose          - The Elevator class obtains elevator requests from the scheduler
 * 					   and will move until it gets to the destination floor
 * @param floorunm   -Represents the current floor that the elevator is on 
 * @param er		 -Represents the elevator requests that are passed through 
 */
public class Elevator implements Runnable{
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private boolean isIdle; 
	private float time;
	private int destFloor;
	private ElevatorRequest elevatorRequest;
	
	public Elevator (ElevatorRequest er, int floornum) {
		this.currentFloor = floornum;
		this.elevatorRequest = er;
		this.carNum = nextCarNum++;
		this.isIdle = true;
		this.time = (float) 9.175;
	}
	
	public void startCar() {
		this.isIdle = false;
	}
	
	public void stopCar() {
		this.isIdle = true;
	}
	
	public boolean isIdle() {
		return this.isIdle;
	}
	
	public String getDiretion() {
		if(getDestFloor() > this.currentFloor) {
			return "up";
		}
		else{
			return "down";
		}
	}
	
	public int getCarNum() {
		return this.carNum;
	}
	
	public int getCurrentFloor() {
		return this.currentFloor;	
	}
	
	public void setCurrentFloor(int curr) {
		this.currentFloor = curr;
	}
	
	public float getTime() {
		return this.time;
	}
	
	public int getDestFloor() {
		return this.elevatorRequest.getDestFloor();
	}

	public void move() {
		if(this.currentFloor >= 1 && this.currentFloor <= 7) {
			if(this.currentFloor == 1 && getDiretion() == "down") {
				this.currentFloor += 1;
			}
			if(this.currentFloor == 7 && getDiretion() == "up") {
				this.currentFloor -= 1;
			}
			else if(getDiretion() == "up") {
				this.currentFloor += 1;
			}
			else if(getDiretion() == "down") {
				this.currentFloor -= 1;
			}
		}
	}
	
	public void run() {
		while(true) {
			if(this.elevatorRequest.hasRequest()) {
				System.out.println("Direction: "+getDiretion());
				System.out.println("Destination Floor: "+getDestFloor());
				move();
				
	            // Simulate movement between floors
	            try {
	                Thread.sleep((int)this.time*1);
	            } catch (InterruptedException e) {}
	            this.elevatorRequest.updatedPosition(this.currentFloor);
	            
	            if(this.elevatorRequest.hasArrived()) {
		            // Simulate doors opening
		            try {
		                Thread.sleep((int)this.time*1);
		            } catch (InterruptedException e) {}
		            this.elevatorRequest.requestServed();
	            }
			}	
		}
	}
}
