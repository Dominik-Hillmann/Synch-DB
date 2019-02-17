import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

/**
 * 
 */

public abstract class Information {
	
	/**
	 * Gets the JSON file as string as found on the given path.
	 * @param path where the JSON file is to be found in the Dropbox, relative to the folder
	 * where you allowed the API to operate.
	 * @param client to reference the Dropbox account from where to get files.
	 * @return The JSON file as a string.
	 * @throws DbxException the file does not exist or unable to connect.
	 * @throws IOException file was downloaded but stream not opened.
	 */
	static String getJSONString(String path, DbxClientV2 client) throws DbxException, IOException {
		InputStream in = client
			.files()
			.download(path)
			.getInputStream();
		
		return new String(in.readAllBytes(), StandardCharsets.UTF_8);
	}
	
}
