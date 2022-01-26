import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Floor implements Runnable{
	private Scheduler scheduler;
	
	public Floor(String name, Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		FileReader fileReader;
		BufferedReader reader = null;
		try {
			fileReader = new FileReader("test.txt");
			reader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String line;
//		try {
//			while ((line = reader.readLine()) != null){
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	

}
