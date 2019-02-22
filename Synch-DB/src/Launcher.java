import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		
		Logger.log("\n\n");
		
		
		
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
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue;
			} 
		}
		
		Logger.log(picFiles.size());
		for (PictureInformation info : picFiles) {
			info.storeInDataBase();
		}
		
		Logger.log("\n\n");
		
		
		
		var writFiles = new ArrayList<WritingInformation>();
		var writFileNames = new ArrayList<String>();
		try {
			client.files()
				.listFolder(WRIT_DIR)
				.getEntries()
				.forEach(file -> writFileNames.add(file.getName()));					
		} catch (DbxException e) {
			Logger.log("Did not find directory " + WRIT_DIR + ".");
		}
		
		for (String name : writFileNames) {
			try {
				writFiles.add(new WritingInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue;
			} 
		}
		
		Logger.log(writFiles.size());
		for (WritingInformation info : writFiles) {
			info.storeInDataBase();
		}
		
		
		Logger.appendToLogFile();
		
		
	    Path imgsPath = Paths.get("/home/dominik/DB-Synch-imgs/");
	    Logger.log(imgsPath.toAbsolutePath().toString());
		Logger.log(Files.isDirectory(imgsPath.toAbsolutePath()));
		
		if (!Files.isDirectory(imgsPath.toAbsolutePath())) {
			(new File(imgsPath.toAbsolutePath().toString())).mkdirs();
			Logger.log("created");
		} else {
			Logger.log("Dir already exists.");
		}
		
		
		PictureInformation examplePic = picFiles.get(5);
		examplePic.print();
		try {
            // output file for download --> storage location on local system to download file
            InputStream in;
            try {
            	in = client.files().download(imgsPath + examplePic.getFileName()).getInputStream();
             } finally {
             }
         } catch (DbxException e) {
             // error downloading file
             Logger.log("Nicht downloadbar");
        	 // JOptionPane.showMessageDialog(null, "Unable to download file to local system\n Error: " + e);
         //} catch (IOException e) {
             // error downloading file
        	// Logger.log("Kein Stream zu oeffnen.");
        // }
		
		 // suche imgs Ordner weiter oben, vergleiche
		 // wenn nicht da, create und lade Bilder rein
		
		
	}	
	
}

/**
 * IDEEN
 * den Log wieder in die DB laden, um Fehler sehen zu können
 * SQL-Files, um Datenbank wieder auf Raspi herstellen zu können.
 */
