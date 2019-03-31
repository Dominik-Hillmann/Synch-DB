import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.dropbox.core.DbxException;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v1.DbxWriteMode;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadUploader;

public class Logger {
	
	private static final String LOG_PATH = "/logs/";
	private static final String LOG_FILE = "/logs/log.txt";	
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
		String now = "# End of log at " + LocalDateTime.now().toString() + " #";
		int strLen = now.length();
		String hashes = new String(new char[strLen]).replace("\0", "#");
		
		Logger.log("\n" + hashes);
		Logger.log(now);
		Logger.log(hashes);		
		Logger.log("\n\n\n");
		
		InputStream logFileStream = null;
		try {
			logFileStream = client.files()
				.download(LOG_FILE)
				.getInputStream();
		} catch (DownloadErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		String logDbx = streamToString(logFileStream);
		
		// System.out.println(logDbx);
		
		for (var line : log) {
			logDbx += line + "\n";
		}
		
		System.out.println(logDbx);
		
		UploadUploader test = client.files().upload(LOG_PATH);
		
		try {
			logFileStream.close();
		} catch (IOException e) {
			System.out.println("Cannot close log file stream: " + e.getMessage());
		}
	}
	
	private static String streamToString(InputStream is) {
		// source: https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}
