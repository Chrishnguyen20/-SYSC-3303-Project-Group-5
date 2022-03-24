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
 * @purpose 			- the floor class reads floor requests from a file and passes 
 * 						  them to the scheduler via UDP. 
 * @param	localAddr   - is the current scheduler client (floor) facing
 * @param	socket    	- is the current scheduler client (floor) facing
 */
public class Floor implements Runnable{

	private InetAddress localAddr;
	private DatagramSocket socket;
	
	public Floor() {
	}
	
	/*
	 * @purpose - write the string to the appropriate trace file
	 */
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
		System.out.println(s);

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
		LocalTime d = LocalTime.now();
		try {
			while ((line = reader.readLine()) != null){
				String[] arr = line.split("\\t");
				//check if the data is in the correct format
				if(arr.length != 4) {
					writeToTrace(d.toString() + " - Floor Subsystem: Read data error!\n");
					return;
				}else {
					//create a new floor request and pass the data to the scheduler as a comma delimited string. 
					String s = arr[0] + "," + arr[1] + "," + arr[2]+ "," + arr[3];
					byte[] dataArray = s.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(dataArray, dataArray.length, this.localAddr, 201);
					DatagramPacket receivePacket = new DatagramPacket(new byte[dataArray.length], dataArray.length);
					writeToTrace(d.toString() + " - Floor Subsystem: Queued a request -- request for floor " + arr[1] + " going " + arr[2] + " to floor " + arr[3] + "\n");
					//send the packet and receive a acknowledgement
					this.socket.send(sendPacket);
					this.socket.receive(receivePacket);
					writeToTrace(d.toString() + " - Floor Subsystem: Received an acknowledgement.\n");
				}
			}
			writeToTrace("EOF. Time: " + d.toString());
			//close the in-file
			reader.close();
			
		} catch (IOException e) {
			//an I/O error occurred 
			e.printStackTrace();
		}
	}
	public static void main(String args[]) throws SocketException {
		
		Thread floor = new Thread(new Floor(), "Floor Thread");
		
		floor.start();

	}
}
