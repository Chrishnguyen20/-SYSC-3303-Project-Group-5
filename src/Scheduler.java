import java.util.ArrayList;

public class Scheduler{
	
	private ArrayList<String> queue;
	
	public Scheduler() {
		queue = new ArrayList<>();
	}
	
	public synchronized void recieveUpdate(String info) {
		queue.add(info);
	}
	
	public synchronized String updateElevator() {
		try {
			String element = queue.remove(0);
			return element;
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public static void main(String args[]) {
		
		Scheduler scheduler = new Scheduler();
		Elevator elevator = new Elevator("Elevator", scheduler);
<<<<<<< Updated upstream
=======
		Floor floor = new Floor("Floor", scheduler);
>>>>>>> Stashed changes
		
	}

}
