import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.google.gson.Gson;

public class Launcher {
	
	private static final String TOKEN = DropboxAccessToken.getToken();
	
	private static final String USER_DIR = "/user-info/";
	private static final String PIC_DIR = "/pic-info/";
	private static final String WRIT_DIR = "/writing-info/";
	
	public static void main(String[] args) {
	
		// First try to import and use the Dropbox library properly.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("Synch-DB").build();
		// Groupings of parameters of how to contact the Dropbox API.
		DbxClientV2 client = new DbxClientV2(config, TOKEN);
        
		// FullAccount account = client.users().getCurrentAccount();
		// ListFolderResult resultUsers = client.files().listFolder("/user-info");
		
		/*** <TEST> ***/		
		// PictureInformation pic1 = new PictureInformation("complexity.json", client);
		// pic1.print();		
		// PictureInformation pic2 = new PictureInformation("schlange.json", client);
		// pic2.print();		
		/*** </TEST> ***/
		

		var userFiles = new ArrayList<UserInformation>();
		var userFileNames = new ArrayList<String>();
		try {
			client.files()
				.listFolder(USER_DIR)
				.getEntries()
				.forEach(file -> userFileNames.add(file.getName()));					
		} catch (DbxException e) {
			Logger.log("Did not find directory " + USER_DIR + ".");
		}
		
		for (String name : userFileNames) {
			try {
				userFiles.add(new UserInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name);
				e.printStackTrace();
				continue;
			} 
		}
		
		for (UserInformation info : userFiles) {
			info.storeInDataBase();
		}
		
		
		var picFiles = new ArrayList<PictureInformation>();
		var picFileNames = new ArrayList<String>();
		try {
			client.files()
				.listFolder(PIC_DIR)
				.getEntries()
				.forEach(file -> picFileNames.add(file.getName()));					
		} catch (DbxException e) {
			Logger.log("Did not find directory " + PIC_DIR + ".");
		}
		
		for (String name : picFileNames) {
			try {
				picFiles.add(new PictureInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name);
				e.printStackTrace();
				continue;
			} 
		}
		
		for (PictureInformation info : picFiles) {
			info.storeInDataBase();
		}
		
		/*
		var picFileNames = new ArrayList<String>();
		PictureInformation[] picFiles;
		var writFileNames = new ArrayList<String>();
		WritingInformation[] writFiles;
		
		String [] dirNames = { USER_DIR, PIC_DIR, WRIT_DIR };
		
		for (String dirName : dirNames) {
			
			// Get metadata about files in the directory.
			List<Metadata> dirContent;
			try {
				dirContent = client
					.files()
					.listFolder(dirName)
					.getEntries();
			} catch (DbxException dbxe) {
				System.out.println("Could not download file from Dropbox.");
				continue;
			}
			
		}*/
	
		
		Logger.appendToLogFile();
	}	
	
}

/**
 * IDEEN
 * den Log wieder in die DB laden, um Fehler sehen zu können
 * SQL-Files, um Datenbank wieder auf Raspi herstellen zu können.
 */
