import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/*
 * @author Billal Ghadie
 * @purpose 		- The floor class reads floor requests from a file and passes 
 * 					  them to the scheduler. 
 */
public class Floor implements Runnable{

	private FloorRequest floorRequest;
	
	public Floor(FloorRequest fr) {
		this.floorRequest = fr;
	}

	
	@Override
	public void run() {
		FileReader fileReader;
		BufferedReader reader = null;
		try {
			//Read the in-file and store it in a readable buffer
			fileReader = new FileReader("src/FloorRequests.txt");
			reader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			//a read error occurred 
			e.printStackTrace();
		}
		
		String line;
		try {
			while ((line = reader.readLine()) != null){
				String[] arr = line.split("\\t");
				
				//check if the data is in the correct format
				if(arr.length != 4) {
					System.out.println("Read data error!");
				}else {
					//create a new floor request and pass the data to the scheduler. 
					
					floorRequest.add(Integer.parseInt(arr[1]), arr[0].toLowerCase(), arr[2], Integer.parseInt(arr[3]));
					System.out.println("Added Floor Request");
					System.out.print("Buffer: ");
					System.out.println(this.floorRequest.hasRequest());
				}
				
	            try {
					// Sleep for between 0 and 2 seconds
	                Thread.sleep((int)(Math.random() * 2000));
	            } catch (InterruptedException e) {
	            	//an interrupt occurred
	            	e.printStackTrace();
	            }
			}
			
			//close the in-file
			reader.close();
			
		} catch (IOException e) {
			//an I/O error occurred 
			e.printStackTrace();
		}
		
		
		
		
	}
	
	

}
