import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class TraceFile {
	
	static Writer fileWriter;
	
	public static void init() {
		try {
			fileWriter = new FileWriter("trace.txt", false);
		} catch (IOException e) {
			e.printStackTrace();
		} //overwrites file
	}
	
	public static void toTrace(String s) {
		try {
			System.out.println(s);
			fileWriter.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeTrace() {
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
