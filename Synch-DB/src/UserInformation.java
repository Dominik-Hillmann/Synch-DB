import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UserInformation extends Information implements DataBaseStorable {
	
	private String pw;
	private String user;
	
	// Names of the writings and images someone is supposed to see.
	private String[] pics;
	private String[] writings;
	
	/**
	 * To have the extraction of information out of the JSON all wrapped up in the constructor.
	 * @param fileName name of the JSON that contains the wanted information.
	 * @param client with information to connect to the DropBox.
	 * @throws IOException if the JSON file contains unexpected information or is somehow wrong.
	 * @throws DbxException if the file with this filename does not exist in the DropBox.
	 */
	public UserInformation(String fileName, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		String json = getJSONString("/user-info/" + fileName, client);		
		UserInformation info;
		
		try {
			 info = gson.fromJson(json, UserInformation.class);
		} catch (JsonSyntaxException jse) {
			throw new IOException("JSON string could not be converted.");
		}
		
		try {
			pw = info.pw;
			user = info.user;
			pics = info.pics;
			writings = info.writings;
		} catch (Exception e) {
			throw new IOException("A value in " + fileName + " has the wrong type or is not used.");
		}

	}
	
	/**	
	 * Creates object using the information from the query.
	 * @param set with the cursor on the entry with the information you want to create this object with.
	 * @param database Connection to have access to the MySQL database.
	 * @throws SQLException if a column cannot be found for the entry the cursor points to.
	 */
	public UserInformation(ResultSet set, Connection database) throws SQLException {
		// First, basic information: name and password.
		String username = set.getString("name");
		String password = set.getString("pw");
		this.user = username;
		this.pw = password;
			
		if (username == null || password == null) {
			throw new SQLException("User name " + username + " not found.");
		}
			
		// Get all image names associated with this user out of extra query.
		String picsQuery = "SELECT pic_filename FROM db_synchro.user_pics WHERE user_name='" 
			+ username + "';";
		ResultSet picRes = database.prepareStatement(picsQuery).executeQuery();
		ArrayList<String> picNames = new ArrayList<String>();
		while (picRes.next()) {
			picNames.add(picRes.getString(1));
		}
			
		this.pics = Arrays.copyOf(picNames.toArray(), picNames.toArray().length, String[].class);
			
			
		// Get all writing names for this user out of extra query.
		String writsQuery = "SELECT writ_name FROM db_synchro.user_writs WHERE user_name='"
			+ username + "';";
		ResultSet writRes = database.prepareStatement(writsQuery).executeQuery();
		ArrayList<String> writNames = new ArrayList<String>();
		while (writRes.next()) {
			writNames.add(writRes.getString(1));
		}
			
		this.writings = Arrays.copyOf(writNames.toArray(), writNames.toArray().length, String[].class);
	} 
	
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		// Insert main information first, then associated media names.		
		String sqlString = "INSERT INTO db_synchro.users VALUES ("
			+ "'" + getUserName() + "'," 
			+ "'" + encrypt(getClearPassword()) + "');";
		
		for (var picFileName : this.pics) {
			String additionalPic = "INSERT INTO db_synchro.user_pics VALUES ("
				+ "'" + getUserName() + "',"
				+ "'" + picFileName + "');";
			database.prepareStatement(additionalPic).executeUpdate();
		}
		
		for (var writName : writings) {
			String additionalWriting = "INSERT INTO db_synchro.user_writs VALUES ("
				+ "'" + getUserName() + "',"
				+ "'" + writName + "');";
			database.prepareStatement(additionalWriting).executeUpdate();
		} 
		
		database.prepareStatement(sqlString).executeUpdate();
	}

	
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		// Delete main information first, then associated media names.
		String sqlString = "DELETE FROM db_synchro.users WHERE name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_pics WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_writs WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
	}
	
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		UserInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (UserInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		}
		
		ArrayList<String> comparePics = compareInfo.getPicRessources();
		ArrayList<String> compareWrits = compareInfo.getWritRessources();
		// If they don't have the same size, then they are not the same.
		boolean sameResources = (pics.length == comparePics.size()) && (writings.length == compareWrits.size());

		// If still assumed to be the same, then check tag by tag whether they contain the same ones.
		if (sameResources) {
			for (var picFileName : pics) {
				if (!comparePics.contains(picFileName)) {
					sameResources = false;
					break;
				}
			}
			for (var writName : writings) {
				if (!compareWrits.contains(writName)) {
					sameResources = false;
					break;
				}
			}
		}
		
		// Is it the same file with changed attributes or a different one?
		if (getUserName().equals(compareInfo.getUserName())) {
			if (sameResources) {
				return DataChangeMarker.SAME_FILE_KEPT_SAME;
			} else return DataChangeMarker.SAME_FILE_CHANGED;
		} else {
			return DataChangeMarker.DIFFERENT_FILE;		
		}
	} 
	
	/**
	 * Hashes a string.
	 * @param clearPassword is the non-encrypted password.
	 * @return the encrypted password.
	 */
	private static String encrypt(String clearPassword) { 
		return Console.execute(clearPassword); 
	} 
	
	// Remaining getters, setters and printing methods.
	
	public String getUserName() { return user; }
		
	private String getClearPassword() { return pw; }
	
	public ArrayList<String> getPicRessources() {
		return new ArrayList<String>(Arrays.asList(pics));
	}
	
	public ArrayList<String> getWritRessources() {
		return new ArrayList<String>(Arrays.asList(writings));
	}
		
	public void print() {
		System.out.println("Password: " + pw);
		System.out.println("Username: " + user);
		for (var pic : this.pics) System.out.println("Ass. pic: " + pic);
		for (var writ : this.writings) System.out.println("Ass. writ: " + writ);
		System.out.println();
	} 
	
}
