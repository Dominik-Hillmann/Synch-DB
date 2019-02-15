import java.io.IOException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.users.FullAccount;

public class Launcher {
	
	private static final String TOKEN = DropboxAccessToken.getToken();
	
	public static void main(String[] args) throws DbxException, IOException {
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
        System.out.println(account.getName().getDisplayName());
        System.out.println(account.getTeamMemberId());
        
        // Test
        // try to find out what's in the root folder
        // DbxUserFilesRequests request = new DbxUserFilesRequests("/");
	}
	
}
