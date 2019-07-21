
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

public class Launcher {
	
	private static final String TOKEN = DropboxAccessToken.getToken();
	
	private static final String USER_DIR = "/user-info/";
	private static final String PIC_DIR = "/pic-info/";
	private static final String WRIT_DIR = "/writing-info/";
	// private static final String FRONT_PIC_DIR = "/front_pics/";
	
	public static void main(String[] args) {
		Logger.startLogging();
		
		Connection dbc = null;
		try {
			dbc = getConnection();
		} catch (SQLException e) {
			Logger.log("Could not connect to the database.");
			Logger.log(e.getMessage());
			System.exit(0);
		}
		createDatabase(dbc);
		
		// Groupings of parameters of how to contact the DropBox API.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("Synch-DB").build();
		DbxClientV2 client = new DbxClientV2(config, TOKEN);        
		
		/* ############
		   ### PICS ###
		   ############ */
		
		ArrayList<PictureInformation> picFilesDbx = getPicListDbx(PIC_DIR, client);		
		ArrayList<PictureInformation> picFilesSql = getPicListSql("pic_info", dbc);		
		
		for (PictureInformation picFileDbx : picFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			// Record each comparison.
			for (PictureInformation picFileSql : picFilesSql) {
				markers.add(picFileDbx.containsSameData(picFileSql));
			}
						
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				// Any problems related to downloading associated image or storing information? Try next one.
				try {
					picFileDbx.storeInDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store information for PictureInformation " + picFileDbx.getName() + " in database.");
					continue;
				} catch (DbxException e) {
					Logger.log("Cannot download image " + picFileDbx.getFileName() + " for " + picFileDbx.getName() + ". " + e.toString());
					continue;
				}
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				// Any problems related to downloading associated image or storing information? Try next one.
				try {
					picFileDbx.updateDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store or delete information for PictureInformation " + picFileDbx.getName() + " in database.");
					continue;
				} catch (DbxException e) {
					Logger.log("Cannot download image " + picFileDbx.getFileName() + " for " + picFileDbx.getName() + ".");
					continue;
				}
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {		
				// Do nothing if it contains exactly the same data.
				continue;	
				
			} else {	
				
				Logger.log("Did not this marker structure when comparing PictureInformations:");
				for (DataChangeMarker marker : markers) Logger.log(marker.toString());
				
			}
		}
		
		// Because the comparison could be followed by altered or new entries into the database,
		// it has to be queried again.
		picFilesSql.clear();
		picFilesSql = getPicListSql("pic_info", dbc);
				
		for (PictureInformation picFileSql : picFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (PictureInformation picFileDbx : picFilesDbx) {
				markers.add(picFileSql.containsSameData(picFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				try {
					picFileSql.deleteFromDataBase(dbc);
				} catch (SQLException e) {
					Logger.log("Cannot delete PictureInformation " + picFileSql.getName() + " from database.");
					continue;
				}
			}
		}
		
		
		/* #############
		   ### USERS ###
		   ############# */
		Logger.log("\n");
		
		ArrayList<UserInformation> userFilesDbx = getUserListDbx(USER_DIR, client);				
		ArrayList<UserInformation> userFilesSql = getUserListSql(dbc);
						
		// Vergleich aus Sicht der DBX mit SQL
		for (UserInformation userFileDbx : userFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (UserInformation userFileSql : userFilesSql) {
				markers.add(userFileDbx.containsSameData(userFileSql));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				try {
					userFileDbx.storeInDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store information for UserInformation " + userFileDbx.getUserName() + " in database.");
					continue;
				}
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				try {
					userFileDbx.updateDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store or delete information for UserInformation " + userFileDbx.getUserName() + " in database.");
					continue;
				}
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {
				
				continue;
				
			} else {
				
				Logger.log("Did not this marker structure when comparing UserInformations:");
				for (DataChangeMarker marker : markers) Logger.log(marker.toString());
			}
		}
		
		// Query anew because might be changed.
		userFilesSql.clear();
		userFilesSql = getUserListSql(dbc);
				
		for (UserInformation userFileSql : userFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			for (UserInformation userFileDbx : userFilesDbx) {
				markers.add(userFileSql.containsSameData(userFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				try {
					userFileSql.deleteFromDataBase(dbc);
				} catch (SQLException e) {
					Logger.log("Cannot delete UserInformation " + userFileSql.getUserName() + " from database.");
					continue;
				}
			}
		}
		
		
		/* ################
		   ### WRITINGS ###
		   ################ */
		Logger.log("\n");
		
		ArrayList<WritingInformation> writFilesDbx = getWritListDbx(WRIT_DIR, client);
		ArrayList<WritingInformation> writFilesSql = getWritListSql(dbc);
		
		// Add new entries in the DropBox to the database or change those that have been changed.		
		for (WritingInformation writFileDbx : writFilesDbx) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			
			for (WritingInformation writFileSql : writFilesSql) {
				markers.add(writFileDbx.containsSameData(writFileSql));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME) 
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				try {
					writFileDbx.storeInDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store information for WritingInformation " + writFileDbx.getName() + " in database.");
					continue;
				} 
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				
				try {
					writFileDbx.updateDataBase(dbc, client);
				} catch (SQLException e) {
					Logger.log("Could not store or delete information for WritingInformation " + writFileDbx.getName() + " in database.");
					continue;
				}
				
			} else if (markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)) {
				
				continue;
				
			} else {
				
				Logger.log("Did not anticipate this marker structure when comparing WritingInformations:");
				for (DataChangeMarker marker : markers) Logger.log(marker.toString());
				
			}
		}
		
		// Possibly new entries in database, now check what entries are missing in DropBox that are in the
		// database and delete those that do not occur in the DropBox.
		writFilesSql.clear();
		writFilesSql = getWritListSql(dbc);
				
		for (WritingInformation writFileSql : writFilesSql) {
			ArrayList<DataChangeMarker> markers = new ArrayList<DataChangeMarker>();
			for (WritingInformation writFileDbx : writFilesDbx) {
				markers.add(writFileSql.containsSameData(writFileDbx));
			}
			
			if (!markers.contains(DataChangeMarker.SAME_FILE_KEPT_SAME)
				&& !markers.contains(DataChangeMarker.SAME_FILE_CHANGED)) {
				try {
					writFileSql.deleteFromDataBase(dbc);
				} catch (SQLException e) {
					Logger.log("Cannot delete WritingInformation " + writFileSql.getName() + " from database.");
					continue;
				}
			}
		}
		
		
		/* ##################
		   ### FRONT PICS ###
		   ################## */
		
		FrontPics frontPicsDbx = null;
		FrontPics frontPicsSql = null;
		
		try {
			frontPicsDbx = new FrontPics(client);
			frontPicsSql = new FrontPics(dbc);
			
			DataChangeMarker marker = frontPicsDbx.containsSameData(frontPicsSql);
			
			if (marker == DataChangeMarker.SAME_FILE_CHANGED) {
				frontPicsDbx.updateDataBase(dbc, client);
			} else if (marker == DataChangeMarker.DIFFERENT_FILE) {
				Logger.log("Different files for front_pics.");
			}			
		} catch (Exception e) {
			Logger.log("Could not open Dropbox or SQL version of front pics:");
			Logger.log(e.toString());
		}
		
		
		// End of program, close database. Append messages to log file in the DropBox.
			
		try {
			dbc.close();
		} catch (Exception e) {
			Logger.log("Could not close database connection.");
		}
		
		Logger.appendToLogFile(client);
	}	
	
	/**
	 * Connects to the MySQL database containing db_synchro.
	 * @return the database connection.
	 * @throws SQLException if the connection can somehow not be established.
	 */	
	public static Connection getConnection() throws SQLException {		
		final String driverName = "com.mysql.jdbc.Driver";
		final String dataBaseUrl = "jdbc:mysql://localhost:3306/";//db_synchro";
			
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Could not connect to database.");
		}
		
		Connection conn = DriverManager.getConnection(
			dataBaseUrl,
			DataBaseAccess.USERNAME,
			DataBaseAccess.PASSWORD
		);
			
		Logger.log("Connected to database.");			
		return conn;
	}
	
	/**
	 * List of all names of files in a DropBox directory.
	 * @param dbxDir Where the JSON files are to be found in the DropBox relative to Apps folder.
	 * @param client To connect to DropBox.
	 * @return A list of the names of the JSON files in the particular directory.
	 * @throws DbxException if the directory could not be found or unexpected files occur.
	 */
	private static ArrayList<String> getNamesListDbx(String dbxDir, DbxClientV2 client) throws DbxException {
		ArrayList<String> fileNames = new ArrayList<String>();
		
		client.files()
			.listFolder(dbxDir)
			.getEntries()
			.forEach(file -> fileNames.add(file.getName()));					
				
		return fileNames;
	}
	
	/**
	 * List of all the image information present in the DropBox.
	 * @param dbxDir Where the JSON files are to be found in the DropBox relative to Apps folder.
	 * @param client To connect to DropBox.
	 * @return A list of the information about images present in the DropBox.
	 */
	private static ArrayList<PictureInformation> getPicListDbx(String dbxDir, DbxClientV2 client) {
		ArrayList<PictureInformation> picFilesDbx = new ArrayList<PictureInformation>();
		ArrayList<String> picFileNames = new ArrayList<String>();
		try {
			picFileNames = getNamesListDbx(dbxDir, client); 
		} catch (DbxException e) {
			Logger.log("Problem with retrieving names for images. Return empty list of images.");
			Logger.log(e.getMessage());
			return picFilesDbx;
		}				
		
		for (String name : picFileNames) {
			try {
				picFilesDbx.add(new PictureInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file or create stream for file named " + name + ".");
				continue; // Try the next one.
			} 
		}
		
		return picFilesDbx;
	}
	
	/**
	 * List of all PictureInformation present in the database.
	 * @param picTableName
	 * @param database Connection to MySQL database.
	 * @return A list of the information about images present in the database.
	 */
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
				"The query for the query of " 
				+ picTableName 
				+ " could not be finished: " 
				+ e.getMessage()
				+ " Returning empty list."
			);
			return new ArrayList<PictureInformation>();
		}
		
		try {
			while (resPicQuery.next()) {
				try {
					picFilesSql.add(new PictureInformation(resPicQuery, database));
				} catch (SQLException e) {
					Logger.log("Creating an PictureInformation object from this entry failed. Trying next one.");
					Logger.log(e.getMessage());
					continue;
				}
			}
		} catch (SQLException e) {
			Logger.log("Could not find next PictureInformation entry. Return all objects added until here.");
			Logger.log(e.getMessage());
			return picFilesSql;
		}
		
		return picFilesSql;
	}
	
	/**
	 * Gives you a list of all of the user information present in the DropBox.
	 * @param dbxDir Directory of where JSON files with user information are to be found in the DropBox.
	 * @param client To connect to the DropBox.
	 * @return A list of all user information in the DropBox.
	 */
	private static ArrayList<UserInformation> getUserListDbx(String dbxDir, DbxClientV2 client) {
		ArrayList<UserInformation> userFilesDbx = new ArrayList<UserInformation>();		
		ArrayList<String> userFileNames = new ArrayList<String>();
		try {
			userFileNames = getNamesListDbx(dbxDir, client); 
		} catch (DbxException e) {
			Logger.log("Problem with retrieving names for users. Return empty list of users.");
			Logger.log(e.getMessage());
			return userFilesDbx;
		}
		
		for (String name : userFileNames) {
			try {
				userFilesDbx.add(new UserInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				Logger.log(e.getMessage());
				continue; // Try the next one.
			} 
		}
		
		return userFilesDbx;
	}
	
	/**
	 * Creates a list of all the user information in the database.
	 * @param database  Connection to MySQL database.
	 * @return A list of all information about users in the database.
	 */
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
	
	/**
	 * Creates list of all writing information in the DropBox.
	 * @param dbxDir Directory of DropBox where the JSON files with necessary information are to be found.
	 * @param client to access the DropBox.
	 * @return A list of all writings present in the DropBox.
	 */
	private static ArrayList<WritingInformation> getWritListDbx(String dbxDir, DbxClientV2 client) {
		ArrayList<WritingInformation> writFilesDbx = new ArrayList<WritingInformation>();
		ArrayList<String> writFileNames = new ArrayList<String>();
		
		try {
			writFileNames = getNamesListDbx(dbxDir, client); 
		} catch (DbxException e) {
			Logger.log("Problem with retrieving names for writings. Return empty list of writings.");
			Logger.log(e.getMessage());
			return writFilesDbx;
		}
		
		for (String name : writFileNames) {
			try {
				writFilesDbx.add(new WritingInformation(name, client));
			} catch (DbxException | IOException e) {
				Logger.log("Could not download file named " + name + ".");
				Logger.log(e.getMessage());
				continue; // Try the next one.
			} 
		}
		
		return writFilesDbx;
	}
	
	/**
	 * Queries the database for all available writings and returns a list of them.
	 * @param database  Connection to MySQL database.
	 * @return The list of all writing information present in the database.
	 */
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
			Logger.log("The query for user information could not be completed: " + e.getMessage());
			Logger.log("Returnig empty writings list.");
			return new ArrayList<WritingInformation>();
		}	
		
		return writFilesSql;
	}
	
		
	/**
	 * If the program is run for the first time, it will create the necessary database and tables.
	 * @param database Connection to MySQL database.
	 */
	private static void createDatabase(Connection database) {
		try {
			if (database == null) {
				throw new SQLException();
			}
			
			PreparedStatement dbCreation = database.prepareStatement(
				// DROP DATABASE IF EXISTS <YOUR_DATABASE_NAME>;
				// CREATE DATABASE <YOUR_DATABAE_NAME> DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
				"CREATE DATABASE IF NOT EXISTS db_synchro;"
			);
			dbCreation.executeUpdate();
			
			PreparedStatement useDatabase = database.prepareStatement("USE db_synchro;");
			useDatabase.executeUpdate();
			
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
			
			Logger.log("Successful database creation...");
			
		} catch (SQLException e) {
			Logger.log("Could not create database.");
			Logger.log(e.getMessage());
			System.exit(0);
		}
	}

} 
/**
 * Logs besser gestalten, z.B.mit Zeiten für einzelne Operationen.
 * 
 */
