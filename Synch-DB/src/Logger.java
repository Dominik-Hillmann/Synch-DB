/**
 * Collects everything printed to the console and appends it to a log file.
 * @author dominik
 *
 */
public class Logger {
	
	/**
	 * Logs happenings to the console as well as to the log file.
	 * @param content is the string that is to be logged.
	 */
	public static void log(String content) {
		// vorerst
		System.out.println(content);
	}
	
	/**
	 * Appends everything that was logged during the run of the program to a file accessible in the Dropbox.
	 */
	public static void appendToLogFile() {
		// later
	}
	
}
