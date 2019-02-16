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

public class Launcher {
	
	private static final String TOKEN = DropboxAccessToken.getToken();
	
	public static void main(String[] args) throws DbxException {
		System.out.println(TOKEN);
		String s = "Das ist ein Test";
		System.out.println(s);
		System.out.println("Ein weiterer Test".length());
		
		// First try to import and use the Dropbox library properly.
		DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        System.out.println(config.getClientIdentifier());
        DbxClientV2 client = new DbxClientV2(config, TOKEN);
        
        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        // System.out.println(account.getName().getDisplayName());
        // System.out.println(client.toString());
        
        // Test
        // try to find out what's in the root folder
        // DbxUserFilesRequests request = new DbxUserFilesRequests("/");
        ListFolderResult result = client.files().listFolder("");
        // System.out.println(result.toStringMultiline());
        ListFolderResult resultUsers = client.files().listFolder("/user-info");
        // System.out.println(resultUsers.toStringMultiline());
        // client.files().createFolderV2("/user-info/test-folder");
        DbxDownloader<FileMetadata> someUserInfo = client
        	.files()
        	.download("/user-info/user2.json");
        InputStream in = someUserInfo.getInputStream();
        try {
        	String str = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        	System.out.println(str);
        } catch (IOException e) {
        	System.out.println("Fehler beim Lesen des Inputstreams.");
        } finally {
        	someUserInfo.close();
        }
        
        // System.out.println(someUserInfo.getResult().toStringMultiline());
	}
	
}
