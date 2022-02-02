import java.util.ArrayList;

public class Elevator implements Runnable{
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private boolean isIdle; 
	private String direction;
	private Scheduler scheduler;
	private float time;
	private int destFloor;
	
	public Elevator (Scheduler scheduler, int floornum, String direction) {
		this.currentFloor = floornum;
		this.carNum = nextCarNum++;
		this.scheduler = scheduler;
		this.isIdle = true;
		this.direction = direction;
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
	
	public void setDirection(String dir) {
		this.direction = dir;
	}
	
	public String getDiretion() {
		return this.direction;
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
		return this.destFloor;
	}
	
	public void setDestFloor(int d) {
		this.destFloor = d;
	}

	public void move() {
		if(currentFloor >= 1 && currentFloor <= 7) {
			if(currentFloor == 1 && direction == "down") {
				System.out.println("Invalid Direction");
			}
			if(currentFloor == 7 && direction == "up") {
				System.out.println("Invalid Direction");
			}
			else if(direction == "up") {
				currentFloor += 1;
			}
			else if(direction == "down") {
				currentFloor -= 1;
			}
		}
	}
	
	public void run() {
		while(!this.isIdle()) {
			move();
			scheduler.elevatorUpdate(carNum, currentFloor);
		}
		
	}
}
