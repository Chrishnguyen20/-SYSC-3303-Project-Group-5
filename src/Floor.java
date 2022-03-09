import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
/*
 * @purpose 		- The floor class reads floor requests from a file and passes 
 * 					  them to the scheduler. 
 */
public class Floor implements Runnable{

	private InetAddress localAddr;
	private DatagramSocket socket;
	
	public Floor() {
	}
	
	public void writeToTrace(String s) {
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("floor_trace.txt", true));
		    writer.append(s);
		    
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		try {
			this.socket = new DatagramSocket(200);
			this.localAddr = InetAddress.getLocalHost();

		} catch (SocketException | UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
					LocalTime d = LocalTime.now();
					writeToTrace("Floor Subsystem: Read data error! Time Stamp: " + d.toString() + "\n");
				}else {
					//create a new floor request and pass the data to the scheduler. 
					String s = arr[0] + "," + arr[1] + "," + arr[2]+ "," + arr[3];
					byte[] dataArray = s.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, this.localAddr, 201);
					DatagramPacket receivePacket = new DatagramPacket(new byte[dataArray.length], dataArray.length);
					writeToTrace("Floor Subsystem: Queued a request\n");
					this.socket.send(sendPacket);
					this.socket.receive(receivePacket);
					writeToTrace("Floor Subsystem: Received an acknowledgement\n");
				}
				
	            try {
					// Sleep for between 0 and 2 seconds
	                Thread.sleep((int)(Math.random() * 5));
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
