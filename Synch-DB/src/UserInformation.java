import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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
	}
	
	public UserInformation(ResultSet queryResult) {
		String username = null;
		try {
			username = queryResult.getString("name");
			this.user = username;
			this.pw = queryResult.getString("pw");
		} catch (SQLException e) {
			Logger.log("Konnte Information nicht aus Datenbank retrieven: " + e.getMessage());
		}
		
		String picsQuery = "SELECT pic_filename FROM db_synchro.user_pics WHERE user_name='" 
			+ (username != null ? username : throw new Exception())
			+ "';"; /**********/S
	}
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		String sqlString = "INSERT INTO db_synchro.users VALUES ("
			+ "'" + getUserName() + "'" + "," 
			+ "'" + encrypt(getClearPassword()) + "'" + ");";
		
		for (var picFileName : pics) {
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
		String sqlString = "DELETE FROM db_synchro.users WHERE name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_pics WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		sqlString = "DELETE FROM db_synchro.user_writs WHERE user_name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
	}
	
	public String getUserName() {
		return user;
	}
	
	private String getClearPassword() {
		return pw;
	}
	
	public ArrayList<String> getPicRessources() {
		return (ArrayList<String>) Arrays.asList(pics);
	}
	
	public ArrayList<String> getWritRessources() {
		return (ArrayList<String>) Arrays.asList(writings);
	}
		
	public void print() {
		System.out.println("Password: " + pw);
		System.out.println("Username: " + user);
		System.out.println("Number of pics: " + String.valueOf(pics.length));
		System.out.println("Number of writings: " + String.valueOf(writings.length));
		System.out.println();
	}
	
	private static String encrypt(String clearPassword) { 
		return Console.execute(ENC_SCRIPT, clearPassword); 
	}
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		UserInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (UserInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		}
		
		boolean sameRessources = true;
		var comparePics = compareInfo.getPicRessources();
		var compareWrits = compareInfo.getWritRessources();
		sameRessources = (pics.length != comparePics.size()) || (writings.length != compareWrits.size());
		
		if (sameRessources) {
			for (var picFileName : pics) {
				if (!comparePics.contains(picFileName)) {
					sameRessources = false;
					break;
				}
			}			
			for (var writName : writings) {
				if (!compareWrits.contains(writName)) {
					sameRessources = false;
					break;
				}
			}
		}
		
		if (getUserName().equals(compareInfo.getUserName())) {
			if (sameRessources) {
				return DataChangeMarker.SAME_FILE_KEPT_SAME;
			} else return DataChangeMarker.SAME_FILE_CHANGED;
		} else return DataChangeMarker.DIFFERENT_FILE;		
	}
	
}
