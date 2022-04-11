import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The JUnit test suit for the floor system 
 */
class FloorSystemTest {
	
	/**
	 * @purpose Checks if an event (string) was captured in the trace file
	 * @param s A string representing some event
	 * @param elevatorTace A flag indicating which trace file to read
	 * @return boolean true if the line exists in the trace file, otherwise false
	 */
	private boolean existsInTrace(String s, boolean elevatorTrace) {
		if(elevatorTrace) {
			boolean flag = true;
			while(flag) {
				FileReader fileReader;
				BufferedReader reader = null;
				try {
					//Read the in-file and store it sin a readable buffer
					fileReader = new FileReader("elevator_trace.txt");
					reader = new BufferedReader(fileReader);
				} catch (FileNotFoundException e) {
					//a read error occurred 
					e.printStackTrace();
				}
				
				String line;
				try {
					while ((line = reader.readLine()) != null){
						if(line.contains(s)) {
							return true;
						}else if(line.contains("EOF")) {
							flag = false;
						}
					}
					reader.close();
				} catch (IOException e) {
					//an I/O error occurred 
					e.printStackTrace();
				}
			}		
			return false;
		}else {
			boolean flag = true;
			while(flag) {
				FileReader fileReader;
				BufferedReader reader = null;
				try {
					//Read the in-file and store it sin a readable buffer
					fileReader = new FileReader("floor_trace.txt");
					reader = new BufferedReader(fileReader);
				} catch (FileNotFoundException e) {
					//a read error occurred 
					e.printStackTrace();
				}
				
				String line;
				try {
					while ((line = reader.readLine()) != null){
						if(line.contains(s)) {
							return true;
						}else if(line.contains("EOF")) {
							flag = false;
						}
					}
					reader.close();
				} catch (IOException e) {
					//an I/O error occurred 
					e.printStackTrace();
				}
			}		
			return false;
		}

	}
	
	/**
	 * @purpose Checks if a subsystem is in its final state (s)
	 * @param s The state (string) being tested 
	 * @return boolean true if the state is found, otherwise false
	 */
	private boolean checkState(String s) {
		boolean flag = true;
		while(flag) {
			FileReader fileReader;
			BufferedReader reader = null;
			try {
				//Read the in-file and store it in a readable buffer
				fileReader = new FileReader("elevator_trace.txt");
				reader = new BufferedReader(fileReader);
			} catch (FileNotFoundException e) {
				//a read error occurred 
				e.printStackTrace();
			}
			
			String line;
			try {
				while ((line = reader.readLine()) != null){
					if(line.contains("EOF")) {
						flag = false;
					}else if(line.contains(s)) {
						return true;
					}
				}
				reader.close();
			} catch (IOException e) {
				//an I/O error occurred 
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * @purpose Test that the program runs successfully
	 * @param event A string representing the event to search for
	 */
	@ParameterizedTest
	@ValueSource(strings = {"EOF", "Floor Subsystem: Queued a request -- request for floor 1 going up",
			 "Floor Subsystem: Received an acknowledgement",
			 "Floor Subsystem: Queued a request -- request for floor 6 going down"})
	void floor_tests(String event) {		
		assert(existsInTrace(event, false));	
	}
	
}
	
