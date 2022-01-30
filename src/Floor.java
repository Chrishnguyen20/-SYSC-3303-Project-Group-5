import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Floor implements Runnable{
	private Scheduler scheduler;
	
	public Floor(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		FileReader fileReader;
		BufferedReader reader = null;
		try {
			fileReader = new FileReader("src/FloorRequests.txt");
			reader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String line;
		try {
			while ((line = reader.readLine()) != null){
				String[] arr = line.split("\\t");
				if(arr.length != 4) {
					System.out.println("Read data error!");
				}else {
					//create a new floor request and pass the data to the scheduler. 
					this.scheduler.floorRequest(new FloorRequest(Integer.parseInt(arr[1]), arr[0].toLowerCase(), arr[2]));
				}
				// Sleep for between 0 and 2 seconds
	            try {
	                Thread.sleep((int)(Math.random() * 2000));
	            } catch (InterruptedException e) {}
			}
			reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	

}
