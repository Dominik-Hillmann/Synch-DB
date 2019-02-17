import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.users.FullAccount;
import com.google.gson.Gson;

public class Launcher {
	
	private static final String TOKEN = DropboxAccessToken.getToken();
	
	public static void main(String[] args) throws DbxException, IOException {
	
		// First try to import and use the Dropbox library properly.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
		DbxClientV2 client = new DbxClientV2(config, TOKEN);
        
		// FullAccount account = client.users().getCurrentAccount();
		// ListFolderResult resultUsers = client.files().listFolder("/user-info");
		
		// Weg 1
		String userPath = "/user-info/user1.json";
		InputStream in = client
			.files()
			.download(userPath)
			.getInputStream();
		String jsonStr= new String(in.readAllBytes(), StandardCharsets.UTF_8);
		Gson gson = new Gson();
		UserInformation user1 = gson.fromJson(jsonStr, UserInformation.class);		
		
		// Weg 2
		UserInformation user2 = new UserInformation(userPath, client);
		
		// Vergleich						
		user1.print();
		user2.print();
	}
	
}
