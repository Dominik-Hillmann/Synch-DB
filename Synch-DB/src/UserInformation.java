import java.io.IOException;

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
	
	public UserInformation(String path, DbxClientV2 client) throws DbxException, IOException {
		Gson gson = new Gson();
		
		// To have the extraction of information out of the JSON all wrapped up in the constructor.
		String json = getJSONString(path, client);
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
	
	public boolean storeInDataBase() { return true; }
}
