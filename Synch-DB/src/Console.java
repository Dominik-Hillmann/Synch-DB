import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Console {
	
	public static String execute(String scriptPath, String clearPassword) {
		// Not my solution, source: https://stackoverflow.com/questions/26830617/running-bash-commands-in-java
		
		StringBuffer output = new StringBuffer();
	    Process p;
    	
    	String[] commands = { scriptPath, clearPassword };
	    try {
	    	p = Runtime.getRuntime().exec(commands);
	    	p.waitFor();
	    	BufferedReader reader = new BufferedReader(
	    		new InputStreamReader(p.getInputStream())
	    	);

	        String line = "";
	        while ((line = reader.readLine()) != null) {
	        	output.append(line);
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	   
	    return output.toString();
	}
	
}
