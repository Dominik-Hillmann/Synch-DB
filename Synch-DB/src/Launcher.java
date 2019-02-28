import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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
	
	private static final String PIC_STORAGE_LOCAL = "/home/dominik/DB-Synch-imgs/";
	private static final String PIC_STORAGE_DBX = "/img/";
	
	public static void main(String[] args) {
		
		Connection dbc = getConnection();
		
		PreparedStatement statement;
		try {
			statement = dbc.prepareStatement("SHOW TABLES;");
			ResultSet result = statement.executeQuery();
			
			// wenn kein result erwartet, hier speziel für Änderungen in der database
			// statement.executeUpdate();
			
			while (result.next()) {
				Logger.log(result.getString(1));
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
	
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
			// info.storeInDataBase();
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
			// info.storeInDataBase();
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
			// info.storeInDataBase();
		}
		
		
		Logger.appendToLogFile();
		
		
		
		
		
		
		
	    Path localImgsDir = Paths.get(PIC_STORAGE_LOCAL);
	    
	    // Logger.log(localImgsDir.toAbsolutePath().toString());
		
	    // If the local dir does not yet exist, create it.
		if (!Files.isDirectory(localImgsDir.toAbsolutePath())) {
			(new File(localImgsDir.toAbsolutePath().toString())).mkdirs();
			Logger.log("created");
		} else {
			Logger.log("Dir already exists.");
		}
		
		
		List<PictureInformation> examplePics = picFiles.subList(0, picFiles.size());
		
		for (PictureInformation examplePic : examplePics) {
			
			Logger.log();
			
			// Does picture with this name already exist? If not, create the file.
			File picToBeStored = new File(localImgsDir.toAbsolutePath().toString() + "/" + examplePic.getFileName());
			Logger.log("is File " + picToBeStored.isFile());
			if (!picToBeStored.isFile()) {
				try {
					picToBeStored.createNewFile();
				} catch (IOException e) {
					Logger.log("Konnte file " + picToBeStored.getName() + " konnte nicht erstellt.");
					e.printStackTrace();
				}
			} else Logger.log("File " + picToBeStored.getName() + " existiert bereits.");
			
			
			
			try {
				// output file for download --> storage location on local system to download file
				BufferedImage bufferedImage = null;
				Logger.log(PIC_DIR + examplePic.getFileName());
	            bufferedImage = ImageIO.read(
	            	client.files()
	            		.download(PIC_STORAGE_DBX + examplePic.getFileName())
	            		.getInputStream()
	            );
	            	
	            Logger.log(bufferedImage.getWidth());
	            Logger.log(bufferedImage.getHeight());	            
	            
	            // Get extension of the file first:
	            String imgName = picToBeStored.getName();
	            int extensionStartIndex = imgName.lastIndexOf(".");
	            String extension = imgName.substring(extensionStartIndex + 1);
	            Logger.log(extension);
	            
	            // Write the picture into local directory.
	            ImageIO.write(bufferedImage, extension, picToBeStored);          
	            
			} catch (IOException e) {
				Logger.log("Konnte Bild nicht schreiben");
				e.printStackTrace();
				
			} catch (DbxException e) {
				Logger.log("Konnte Bild nicht herunterladen.");
				e.printStackTrace();
			}
		}
	}	
	
	public static Connection getConnection() {		
		try {
			String driverName = "com.mysql.jdbc.Driver";
			String dataBaseUrl = "jdbc:mysql://localhost:3306/db_synchro";
			
			Class.forName(driverName);
			
			Connection conn = DriverManager.getConnection(
				dataBaseUrl,
				DataBaseAccess.username,
				DataBaseAccess.password
			);
			Logger.log("Connected to database.");
			
			return conn;
		} catch (Exception ex) {
			Logger.log("Could not connect to database.");
			return null;
		}
	}
	
	
}

/**
 * IDEEN
 * den Log wieder in die DB laden, um Fehler sehen zu können
 * SQL-Files, um Datenbank wieder auf Raspi herstellen zu können.
 * Fotos auf der Hauptseite über DBX bestimmen.
 */
