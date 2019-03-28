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
	
	public static void main(String[] args) throws Exception {
		Connection dbc = getConnection();
		createDatabase(dbc);
	
		// First try to import and use the Dropbox library properly.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("Synch-DB").build();
		// Groupings of parameters of how to contact the Dropbox API.
		DbxClientV2 client = new DbxClientV2(config, TOKEN);
        
		// Vergleich der Picture-Information:
		// Schritt 1.1: alle Information aus pic_info-Ordner in ArrayList.
		ArrayList<PictureInformation> picFilesDbx = new ArrayList<PictureInformation>();
		
		picFilesDbx = getPicListDbx(PIC_DIR, client);
		
		// Schritt 1.2: alle Information aus pic_infos in der MySQL-DB in eine ArrayList.
		ArrayList<PictureInformation> picFilesSql = new ArrayList<PictureInformation>();
		
		picFilesSql = getPicListSql("pic_info", dbc);
		
		
		for (var picFileDbx : picFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (var picFileSql : picFilesSql) {
				markers.add(picFileDbx.containsSameData(picFileSql));
			}
			
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				picFileDbx.storeInDataBase(dbc, client);
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				picFileDbx.updateDataBase(dbc, client);
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {
				
				continue;
				
			} else {
				
				for (var marker : markers) Logger.log(marker.toString());
				throw new Exception("Did not anticipate this marker structure");
				
			}
		}
		
		// picFilesSql neu, da Information nun outdated
		picFilesSql.clear();
		picFilesSql = getPicListSql("pic_info", dbc);
				
		for (var picFileSql : picFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			for (var picFileDbx : picFilesDbx) {
				markers.add(picFileSql.containsSameData(picFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				picFileSql.deleteFromDataBase(dbc);
				Logger.log("Would delete " + picFileSql.getFileName());
			}
		}
		
		
		// Updating all information about the users.
		var userFilesDbx = getUserListDbx(USER_DIR, client);
				
		// Get the user informations from database.
		var userFilesSql = getUserListSql(dbc);
						
		// Vergleich aus Sicht der DBX mit SQL
		for (var userFileDbx : userFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (var userFileSql : userFilesSql) {
				markers.add(userFileDbx.containsSameData(userFileSql));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				userFileDbx.storeInDataBase(dbc, client);
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				userFileDbx.updateDataBase(dbc, client);
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {
				Logger.log("skipped");
				continue;
			} else {
				for (var marker : markers) Logger.log(marker.toString());
				throw new Exception("Did not anticipate this marker structure");
			}
		}
		
		// Vergleich aus Sicht SQL mit DBX
		// Query anew because might be changed.
		userFilesSql.clear();
		userFilesSql = getUserListSql(dbc);
				
		for (var userFileSql : userFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			for (var userFileDbx : userFilesDbx) {
				markers.add(userFileSql.containsSameData(userFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				userFileSql.deleteFromDataBase(dbc);
				Logger.log("Would delete " + userFileSql.getUserName());
			}
		}
		
		
		// Writings
		ArrayList<WritingInformation> writFilesDbx = getWritListDbx(WRIT_DIR, client);
				
		// Get the writing informations from database.
		ArrayList<WritingInformation> writFilesSql = getWritListSql(dbc);
				
		Logger.log("SQL writings: ");
		for (var writ : writFilesSql) writ.print();
		
		// ***** Does comparison of writings work as intended? *****
		// Look at DBX files first.
		for (var writFileDbx : writFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (var writFileSql : writFilesSql) {
				markers.add(writFileDbx.containsSameData(writFileSql));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				writFileDbx.storeInDataBase(dbc, client);
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				writFileDbx.updateDataBase(dbc, client);
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {
				Logger.log("skipped");
				continue;
			} else {
				for (var marker : markers) Logger.log(marker.toString());
				throw new Exception("Did not anticipate this marker structure");
			}
		}
		
		// Look at SQL files first.
		writFilesSql.clear();
		writFilesSql = getWritListSql(dbc);
				
		for (var writFileSql : writFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			for (var writFileDbx : writFilesDbx) {
				markers.add(writFileSql.containsSameData(writFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				writFileSql.deleteFromDataBase(dbc);
				Logger.log("Would delete " + writFileSql.getName());
			}
		}
			
		try {
			dbc.close();
		} catch (Exception e) {
			Logger.log("Could not close database connection.");
		}
		
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
	
	private static ArrayList<String> getNamesListDbx(String dbxDir, DbxClientV2 client) {
		ArrayList<String> fileNames = new ArrayList<String>();
		try {
			client.files()
				.listFolder(dbxDir)
				.getEntries()
				.forEach(file -> fileNames.add(file.getName()));					
		} catch (DbxException e) {
			Logger.log("Did not find directory " + dbxDir + " in the DBX.");
		}
		
		return fileNames;
	}
	
	private static ArrayList<PictureInformation> getPicListDbx(String dbxDir, DbxClientV2 client) {
		var picFilesDbx = new ArrayList<PictureInformation>();
		var picFileNames = getNamesListDbx(dbxDir, client); 
		
		for (var name : picFileNames) {
			try {
				picFilesDbx.add(new PictureInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue; // Try the next one.
			} 
		}
		
		return picFilesDbx;
	}
	
	private static ArrayList<PictureInformation> getPicListSql(String picTableName, Connection database) {
		ArrayList<PictureInformation> picFilesSql = new ArrayList<PictureInformation>();
		ResultSet resPicQuery = null;		
		try {		
			PreparedStatement picQuery = database.prepareStatement(
				"SELECT filename, name, date, explanation, kept_secret," 
					+ "insta_posted, twitter_posted, category "
					+ "FROM db_synchro." + picTableName + ";"
			);
			resPicQuery = picQuery.executeQuery();
		} catch (SQLException e) {
			Logger.log(
				"Die Query für die Bildinformationen aus " 
				+ picTableName 
				+ " konnte nicht ausgeführt werden: " 
				+ e.getMessage()
			);
		}
		
		try {
			while (resPicQuery.next()) {
				picFilesSql.add(new PictureInformation(resPicQuery, database));
			}
		} catch (Exception e) {
			Logger.log("Konnte diesen Wert nicht finden: " + e.getMessage());
		}
		
		return picFilesSql;
	}
	
	private static ArrayList<UserInformation> getUserListDbx(String dbxDir, DbxClientV2 client) {
		ArrayList<UserInformation> userFilesDbx = new ArrayList<UserInformation>();
		ArrayList<String> userFileNames = getNamesListDbx(dbxDir, client);
		
		for (var name : userFileNames) {
			try {
				userFilesDbx.add(new UserInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue; // Try the next one.
			} 
		}
		
		return userFilesDbx;
	}
	
	private static ArrayList<UserInformation> getUserListSql(Connection database) {
		ArrayList<UserInformation> userFilesSql = new ArrayList<UserInformation>();
		ResultSet resUserQuery = null;		
		try {		
			PreparedStatement userQuery = database.prepareStatement("SELECT name, pw FROM db_synchro.users;");
			resUserQuery = userQuery.executeQuery();
			
			while (resUserQuery.next()) {
				userFilesSql.add(new UserInformation(resUserQuery, database));
			}
		} catch (SQLException e) {
			Logger.log("Die Query für die Nutzerinformationen konnte nicht ausgeführt werden: " + e.getMessage());
		}		
		
		return userFilesSql;
	}
	
	private static ArrayList<WritingInformation> getWritListDbx(String dbxDir, DbxClientV2 client) {
		// Writings
		var writFilesDbx = new ArrayList<WritingInformation>();
		var writFileNames = getNamesListDbx(dbxDir, client);
		
		for (var name : writFileNames) {
			try {
				writFilesDbx.add(new WritingInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				e.printStackTrace();
				continue; // Try the next one.
			} 
		}
		
		return writFilesDbx;
	}
	
	private static ArrayList<WritingInformation> getWritListSql(Connection database) {
		ArrayList<WritingInformation> writFilesSql = new ArrayList<WritingInformation>();
		ResultSet resWritQuery = null;		
		try {		
			PreparedStatement writQuery = database.prepareStatement(
				"SELECT name, date, kept_secret, twitter_posted, insta_posted, text, category FROM db_synchro.writ_info;"
			);
			resWritQuery = writQuery.executeQuery();
			
			while (resWritQuery.next()) {
				writFilesSql.add(new WritingInformation(resWritQuery, database));
			}
		} catch (SQLException e) {
			Logger.log("Die Query für die Nutzerinformationen konnte nicht ausgeführt werden: " + e.getMessage());
		}	
		
		return writFilesSql;
	}
	
	private static void createDatabase(Connection database) {
		try {
			PreparedStatement dbCreation = database.prepareStatement("CREATE DATABASE IF NOT EXISTS db_synchro;");
			dbCreation.executeUpdate();
			
			PreparedStatement pic_infoCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS pic_info("
				+ "filename VARCHAR(250) NOT NULL,"
				+ "name VARCHAR(250) NOT NULL,"
				+ "date DATE NOT NULL," 
				+ "explanation TEXT NOT NULL,"
				+ "kept_secret BIT NOT NULL,"
				+ "twitter_posted BIT NOT NULL,"
				+ "insta_posted BIT NOT NULL,"
				+ "category VARCHAR(250) NOT NULL,"
				+ "PRIMARY KEY (filename));"
			);
			pic_infoCreation.executeUpdate();
			
			PreparedStatement writ_infoCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS writ_info(" 
				+ "name VARCHAR(250) NOT NULL," 
				+ "date DATE NOT NULL," 
				+ "kept_secret BIT NOT NULL," 
				+ "twitter_posted BIT NOT NULL," 
				+ "insta_posted BIT NOT NULL," 
				+ "text MEDIUMTEXT NOT NULL," 
				+ "category VARCHAR(250) NOT NULL,"
				+ "PRIMARY KEY (name));"
			);
			writ_infoCreation.executeUpdate();
					
			PreparedStatement usersCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS users(" 
				+ "name VARCHAR(250) NOT NULL," 
				+ "pw VARCHAR(250) NOT NULL,"
				+ "PRIMARY KEY (name));"
			);
			usersCreation.executeUpdate();
			
			PreparedStatement front_picsCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS front_pics("
				+ "category_name VARCHAR(250) NOT NULL," 
				+ "pic_filename VARCHAR(250) NOT NULL);"
			);
			front_picsCreation.executeUpdate();
			
			PreparedStatement user_picsCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS user_pics("
				+ "user_name VARCHAR(250) NOT NULL," 
				+ "pic_filename VARCHAR(250) NOT NULL);"
			);
			user_picsCreation.executeUpdate();
			
			PreparedStatement user_writsCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS user_writs("
				+ "user_name VARCHAR(250) NOT NULL," 
				+ "writ_name VARCHAR(250) NOT NULL);"
			);
			user_writsCreation.executeUpdate();
			
			PreparedStatement tags_picsCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS tags_pics("
				+ "tag_name VARCHAR(250) NOT NULL," 
				+ "pic_filename VARCHAR(250) NOT NULL,"
				+ "UNIQUE (tag_name, pic_filename));"
			);
			tags_picsCreation.executeUpdate();
			
			PreparedStatement tags_writsCreation = database.prepareStatement(
				"CREATE TABLE IF NOT EXISTS tags_writs(tag_name VARCHAR(250) NOT NULL,"
				+ "writ_name VARCHAR(250) NOT NULL, UNIQUE (tag_name, writ_name));"
			);
			tags_writsCreation.executeUpdate();
			
			Logger.log("Erfolg in DB Creation");
		} catch (SQLException e) {
			Logger.log("Konnte Datenbank nicht erstellen.");
		} // try/catch
	} // createDatabase

} // Launcher


/**
 * TODO
 * Logger
 * Alle möglichen Fehler, z.B. fehlendes Bild zum Namen antizipieren und in Logfile eintragen lassen
 */
/**
 * IDEEN
 * den Log wieder in die DB laden, um Fehler sehen zu können
 * SQL-Files, um Datenbank wieder auf Raspi herstellen zu können.
 * Fotos auf der Hauptseite über DBX bestimmen.
 */
