import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		try {
			this.user = queryResult.getString("name");
			this.pw = queryResult.getString("pw");
		} catch (SQLException e) {
			Logger.log("Konnte Information nicht aus Datenbank retrieven: " + e.getMessage());
		}		
	}
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		String sqlString = "INSERT INTO db_synchro.users VALUES ("
			+ "'" + getUserName() + "'" + "," 
			+ "'" + encrypt(getClearPassword()) + "'" + ");";
		
		database.prepareStatement(sqlString).executeUpdate();
	}

	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		String sqlString = "DELETE FROM db_synchro.users WHERE name='" + getUserName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
	}
	
	public String getUserName() {
		return user;
	}
	
	private String getClearPassword() {
		return pw;
	}
	
	public void printAllPicNames() {
		for (var name : pics) {
			System.out.println(name);
		}
	}
	
	public void printAllWritingNames() {
		for (var writing : writings) {
			System.out.println(writing);
		}
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
		
		if (getUserName().equals(compareInfo.getUserName())) {
			return DataChangeMarker.SAME_FILE_KEPT_SAME;
		} else return DataChangeMarker.DIFFERENT_FILE;
	}
}
