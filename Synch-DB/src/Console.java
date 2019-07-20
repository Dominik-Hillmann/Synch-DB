import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Console {
	
	private static final String ENC_SCRIPT = "/home/pi/DB-Synch-imgs/Encrypt.sh";
	
	/**
	 * Returns the hash of its input string, hashed by PHP function password_hash(...).
	 * @param clearPassword to be hashed.
	 * @return The hashed string.
	 */
	public static String execute(String clearPassword) {
		// Not my solution, source: https://stackoverflow.com/questions/26830617/running-bash-commands-in-java
		
		StringBuffer output = new StringBuffer();
	    Process p;
    	
    	String[] commands = { ENC_SCRIPT, clearPassword };
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
