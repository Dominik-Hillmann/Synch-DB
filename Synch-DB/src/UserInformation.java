import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;

/**
 *
 */
public class UserInformation extends Information implements DataBaseStorable {
	
	private String pw;
	private String user;
	private String[] pics;
	private String[] writings;
	private static final String ENC_SCRIPT = "/home/dominik/Desktop/Encrypt.sh";
	
	/**
	 * To have the extraction of information out of the JSON all wrapped up in the constructor.
	 * @param fileName
	 * @param client
	 * @throws IOException
	 * @throws DbxException 
	 */	
	public UserInformation(String fileName, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		String json = getJSONString("/user-info/" + fileName, client);	
		
		UserInformation userInfo = gson.fromJson(json, UserInformation.class);
		pw = userInfo.pw;
		user = userInfo.user;
		pics = userInfo.pics;
		writings = userInfo.writings;
	} // Constructor
	
	public UserInformation(/*String username, String password*/ ResultSet set, Connection database) throws SQLException {
		// First, basic information: name and password.
		String username = set.getString("name");
		String password = set.getString("pw");
		this.user = username;
		this.pw = password;
			
		if (username == null || password == null) {
			throw new SQLException("User name" + username + "not found.");
		}
			
		// All filenames associated with this user out of extra query.
		String picsQuery = "SELECT pic_filename FROM db_synchro.user_pics WHERE user_name='" 
			+ username + "';";
		ResultSet picRes = database.prepareStatement(picsQuery).executeQuery();
		ArrayList<String> picNames = new ArrayList<String>();
		while (picRes.next()) {
			picNames.add(picRes.getString(1));
		}
			
		this.pics = Arrays.copyOf(picNames.toArray(), picNames.toArray().length, String[].class);
			
			
		// All writing names for this user out of extra query.
		String writsQuery = "SELECT writ_name FROM db_synchro.user_writs WHERE user_name='"
			+ username + "';";
		ResultSet writRes = database.prepareStatement(writsQuery).executeQuery();
		ArrayList<String> writNames = new ArrayList<String>();
		while (writRes.next()) {
			writNames.add(writRes.getString(1));
		}
			
		this.writings = Arrays.copyOf(writNames.toArray(), writNames.toArray().length, String[].class);
	} // Contructor
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		String sqlString = "INSERT INTO db_synchro.users VALUES ("
			+ "'" + getUserName() + "'" + "," 
			+ "'" + encrypt(getClearPassword()) + "'" + ");";
		
		for (var picFileName : pics) {
			String additionalPic = "INSERT INTO db_synchro.user_pics VALUES ("
				+ "'" + getUserName() + "',"
				+ "'" + picFileName + "');";
			database.prepareStatement(additionalPic).executeUpdate();
		} // for
		
		for (var writName : writings) {
			String additionalWriting = "INSERT INTO db_synchro.user_writs VALUES ("
				+ "'" + getUserName() + "',"
				+ "'" + writName + "');";
			database.prepareStatement(additionalWriting).executeUpdate();
		} // for
		
		database.prepareStatement(sqlString).executeUpdate();
	} // storeInDataBase

	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	} // updateFromDataBase
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		String sqlString = "DELETE FROM db_synchro.users WHERE name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_pics WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_writs WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
	} // deleteFromDatabase
	
	public String getUserName() {
		return user;
	} // getUserName
	
	private String getClearPassword() {
		return pw;
	} // getClearPassword
	
	public ArrayList<String> getPicRessources() {
		return new ArrayList<String>(Arrays.asList(pics));
	} // getPicRessources
	
	public ArrayList<String> getWritRessources() {
		return new ArrayList<String>(Arrays.asList(writings));
	} // getWritRessources
		
	public void print() {
		System.out.println("Password: " + pw);
		System.out.println("Username: " + user);
		for (var pic : this.pics) System.out.println("Ass. pic: " + pic);
		for (var writ : this.writings) System.out.println("Ass. writ: " + writ);
		System.out.println();
	} // print
	
	private static String encrypt(String clearPassword) { 
		return Console.execute(ENC_SCRIPT, clearPassword); 
	} // encrypt
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		UserInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (UserInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		} // try
		
		boolean sameRessources = true;
		var comparePics = compareInfo.getPicRessources();
		var compareWrits = compareInfo.getWritRessources();
		sameRessources = (pics.length == comparePics.size()) && (writings.length == compareWrits.size());

		if (sameRessources) {
			for (var picFileName : pics) {
				if (!comparePics.contains(picFileName)) {
					sameRessources = false;
					break;
				} // if
			} // for
			for (var writName : writings) {
				if (!compareWrits.contains(writName)) {
					sameRessources = false;
					break;
				} // if
			} // for
		} // if
		
		Logger.log();
		Logger.log("Vergleich Namen: " + getUserName().equals(compareInfo.getUserName())
			+ " " + getUserName() + " " + compareInfo.getUserName());
		Logger.log("Vergleich Ressis: " + sameRessources);
		
		if (getUserName().equals(compareInfo.getUserName())) {
			if (sameRessources) {
				return DataChangeMarker.SAME_FILE_KEPT_SAME;
			} else return DataChangeMarker.SAME_FILE_CHANGED;
		} else return DataChangeMarker.DIFFERENT_FILE;		
	} // containsSameData
	
} // UserInformation
