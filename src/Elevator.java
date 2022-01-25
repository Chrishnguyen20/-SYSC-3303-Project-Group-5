import java.util.ArrayList;

public class Elevator extends Thread{
	private int currentFloor;
	private ArrayList<Integer> queue;
	private Scheduler scheduler;
	
	public Elevator (String name, Scheduler scheduler) {
		currentFloor = 0;
		queue = new ArrayList<>();
		this.scheduler = scheduler;
	}
	
	public void move() {
		if (currentFloor < queue.get(0)) {
			currentFloor += 1;
		} else if (currentFloor > queue.get(0)) {
			currentFloor -= 1;
		} else {
			queue.remove(0);
		}
	}
	
	public void run() {
		
	}
	
	

}
