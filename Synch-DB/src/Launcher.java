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
	
		// First try to import and use the Dropbox library properly.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("Synch-DB").build();
		// Groupings of parameters of how to contact the Dropbox API.
		DbxClientV2 client = new DbxClientV2(config, TOKEN);
        
		// Vergleich der Picture-Information:
		// Schritt 1.1: alle Information aus pic_info-Ordner in ArrayList.
		var picFilesDbx = new ArrayList<PictureInformation>();
		var picFileNames = new ArrayList<String>();
		// Zuerst die Names der Dateien, dann in den Konstruktoren die eigentlichen Dateien herunterladen.
		try {
			client.files()
				.listFolder(PIC_DIR)
				.getEntries()
				.forEach(file -> picFileNames.add(file.getName()));					
		} catch (DbxException e) {
			Logger.log("Did not find directory " + PIC_DIR + " in the DBX.");
		}
		
		for (String name : picFileNames) {
			try {
				picFilesDbx.add(new PictureInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue; // Try the next one.
			} 
		}
		
		var ltest1 = picFilesDbx.get(5);
		var ltest2 = picFilesDbx.get(5);
		var ltest3 = picFilesDbx.get(4);
		try {
			Logger.log("test1 und test2: " + ltest1.containsSameData(ltest2, dbc));
			Logger.log("test1 und test1: " + ltest1.containsSameData(ltest1, dbc));
			Logger.log("test1 und test3: " + ltest1.containsSameData(ltest3, dbc));
		} catch (SQLException e) {
			Logger.log("wut? " + e.getMessage());
		}
		
		// Schritt 1.2: alle Information aus pic_infos in der MySQL-DB in eine ArrayList.
		var picFilesSql = new ArrayList<PictureInformation>();
		ResultSet resPicQuery = null;		
		try {		
			PreparedStatement picQuery = dbc.prepareStatement(
				"SELECT filename, name, date, explanation, kept_secret," 
					+ "insta_posted, twitter_posted FROM db_synchro.pic_info;"
			);
			resPicQuery = picQuery.executeQuery();
		} catch (SQLException e) {
			Logger.log("Die Query für die Bildinformationen konnte nicht ausgeführt werden: " + e.getMessage());
		}
		
		try {
			while (resPicQuery.next()) {
				picFilesSql.add(new PictureInformation(resPicQuery));
			}
		} catch (Exception e) {
			Logger.log("Konnte diesen Wert nicht finden: " + e.getMessage());
		}
		
		for (PictureInformation picInfo : picFilesSql) {
			picInfo.print();
		}
		/*
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
		*/
		
		
	
		
		/*
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
		*/
		
		
		
		
		
		
		
	    Path localImgsDir = Paths.get(PIC_STORAGE_LOCAL);
	    
	    // Logger.log(localImgsDir.toAbsolutePath().toString());
		
	    // If the local dir does not yet exist, create it.
		if (!Files.isDirectory(localImgsDir.toAbsolutePath())) {
			(new File(localImgsDir.toAbsolutePath().toString())).mkdirs();
			Logger.log("created");
		} else {
			Logger.log("Dir already exists.");
		}
		

		List<PictureInformation> examplePics = picFilesDbx.subList(0, picFilesDbx.size());
		
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
		
		PreparedStatement statement;
		PictureInformation insertPic = examplePics.get(2);
			try {
				statement = dbc.prepareStatement("SELECT * FROM pic_info;");
				ResultSet result = statement.executeQuery();
				
				while (result.next()) {
					Logger.log(result.getString(7));
				}
			
			String sqlString2 = "INSERT INTO pic_info VALUES ("
				+ "'" + insertPic.getFileName() + "'" + "," 
				+ "'" + insertPic.getName() + "'" + "," 
				+ "'" + insertPic.getDateStr() + "'" + ","
				+ "'" + insertPic.getDescription() + "'" + ","
				+ "b'" + (insertPic.isSecret() ? 1 : 0) + "'" + ","
				+ "b'" + (insertPic.postedToTwitter() ? 1 : 0) + "'" + ","
				+ "b'" + (insertPic.postedToInsta() ? 1 : 0) + "'" + ");";
			Logger.log(sqlString2);
			dbc.prepareStatement(sqlString2).executeUpdate();
			
			// wenn kein result erwartet, hier speziel für Änderungen in der database
			// statement.executeUpdate();
			
			
			// Algo fuer den Vergleich und der Aenderung der Eigenschaften in der DB, wenn gleicher filename/ID, aber
			// irgendwo anderes andere Eigenschaften
			// wenn DBX-file komplett neuer Filename, dann in die DB.
			
			
		} catch (SQLException sqlEx) {
			Logger.log("Could not write into database.");
			Logger.log(sqlEx.getMessage());
		}		
			
		try {
			dbc.close();
		} catch (Exception e) {
			Logger.log("Could not close database connection.");
		}
		
		PictureInformation test1 = picFilesDbx.get(5);
		var test2 = picFilesDbx.get(5);
		var test3 = picFilesDbx.get(3);
		Logger.log("Is same: " + test1.equals(test2));
		Logger.log("Is same: " + test1.equals(test3));
		
		
		
		Logger.appendToLogFile();
	}	
	
	public static Connection getConnection() {		
		try {
			final String driverName = "com.mysql.jdbc.Driver";
			final String dataBaseUrl = "jdbc:mysql://localhost:3306/db_synchro";
			
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
	
	/**
	 *	 Für jede Ressourcenart:
	 *	 Vorgehen Vergleich DBX-Inhalt und MySQL:
	 *    	Lade alle DBX-Inhalte in eine ArrayList. DONE
	 *    	Lade alle Datenbank-Inhalte in eine Liste --> TODO Konstruktor DONE
	 *    	Gehe durch eine DBX-Liste, durchsuche für jeden die andere Liste --> Methode
	 *    		wenn Eintrag mit anderer Eigenschaft, aber gleichem filename, update die Datenbank
	 *    		Eintrag wurde überhaupt nicht gefunden, dann trage in die Datenbank ein
	 *    			Posten auf Instagram und Twitter
	 *     	Gehe durch die Datenbank-Liste und suche in der DBX-Liste:
	 *     		wenn sich selbst nicht in der DBX-Liste gefunden, dann loesche dich
	 */
	
	
}

/**
 * IDEEN
 * den Log wieder in die DB laden, um Fehler sehen zu können
 * SQL-Files, um Datenbank wieder auf Raspi herstellen zu können.
 * Fotos auf der Hauptseite über DBX bestimmen.
 */
