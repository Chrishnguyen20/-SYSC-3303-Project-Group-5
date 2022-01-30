import java.util.ArrayList;

public class Elevator implements Runnable{
	
	private int currentFloor;
	private static int nextCarNum = 0;
	private int carNum;
	private boolean isIdle; 
	private String direction;
	private Scheduler scheduler;
	
	public Elevator (Scheduler scheduler, int floornum, String direction) {
		this.currentFloor = floornum;
		this.carNum = nextCarNum++;
		this.scheduler = scheduler;
		this.isIdle = true;
		this.direction = direction;
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

	public void move() {
 
	}
	
	public void run() {
		while(!this.isIdle()) {
			move();			
		}
	}
	
	

}
