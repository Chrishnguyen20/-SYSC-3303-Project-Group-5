
public class Floor extends Thread{
	private Scheduler scheduler;
	
	public Floor(String name, Scheduler scheduler) {
		super(name);
		this.scheduler = scheduler;
	}

}
