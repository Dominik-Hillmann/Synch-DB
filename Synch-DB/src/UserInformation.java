import java.io.IOException;
import java.sql.Connection;
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
	
	public String getUserName() {
		return user;
	}
	
	public void printAllPicNames() {
		for (String name : pics) {
			System.out.println(name);
		}
	}
	
	public void printAllWritingNames() {
		for (String writing : writings) {
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
	
	public void storeInDataBase(Connection database) throws SQLException {
		print(); // vorerst
	}
	
	private String encrypt(String inStr) { return "later"; }
	
	public boolean containsSameData(DataBaseStorable storable, Connection database) throws SQLException { return true; }
}
