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
	
	public Elevator (Scheduler scheduler, int floornum) {
		this.currentFloor = floornum;
		this.carNum = nextCarNum++;
		this.scheduler = scheduler;
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
	
	public void setIdle() {
		this.isIdle = true;
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
		if(this.currentFloor >= 1 && this.currentFloor <= 7) {
			if(this.currentFloor == 1 && this.direction == "down") {
				System.out.println("Invalid Direction");
			}
			if(this.currentFloor == 7 && this.direction == "up") {
				System.out.println("Invalid Direction");
			}
			else if(this.direction == "up") {
				currentFloor += 1;
			}
			else if(this.direction == "down") {
				currentFloor -= 1;
			}
		}
	}
	
	public void run() {
		while(true) {
			while(!this.isIdle) {
				move();
				scheduler.elevatorUpdate(carNum, currentFloor);
				
			}
			
		}
		
	}
}
