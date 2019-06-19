import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadUploader;

public class Logger {
	
	private static final String LOG_PATH = "/logs/";
	private static ArrayList<String> log = new ArrayList<String>();
	
	/**
	 * Logs happenings to the console as well as to the log file.
	 * @param content is the string that is to be logged.
	 */		
	public static void log(String content) {
		System.out.println(content);
		log.add(content);
	}
	
	public static void log(int content) {
		System.out.println(content);
		log.add(String.valueOf(content));
	}
	
	public static void log(boolean content) {
		System.out.println(content);
		Logger.log(String.valueOf(content));
	}
	
	public static void log() {
		System.out.println();
	}
	
	public static void startLogging() {
		String now = "# Log from " + LocalDateTime.now().toString() + " #";
		int strLen = now.length();
		String hashes = new String(new char[strLen]).replace("\0", "#");
		
		Logger.log(hashes);
		Logger.log(now);
		Logger.log(hashes + "\n");
	}
	
	public static void appendToLogFile(DbxClientV2 client) {
		String now = LocalDateTime.now().toString();
		String nowFormatted = "# End of log at " + now + " #";
		int strLen = nowFormatted.length();
		String hashes = new String(new char[strLen]).replace("\0", "#");
		
		Logger.log("\n" + hashes);
		Logger.log(nowFormatted);
		Logger.log(hashes);		
		Logger.log("\n\n");
		
		
		String logDbx = "";		
		for (String line : log) {
			logDbx += line + "\n";
		}
				
		UploadUploader upload = null;
		try {
			upload = client
				.files()
				.upload(LOG_PATH + "Log_" + now + ".txt");
		} catch (DbxException e) {
			System.out.println(e.getMessage());
		}
		
		
		try {
			upload.uploadAndFinish(new ByteArrayInputStream(logDbx.getBytes()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
		
	}
}
