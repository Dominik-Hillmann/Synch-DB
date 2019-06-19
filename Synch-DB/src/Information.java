import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

public abstract class Information {
	
	// For date conversion from MySQL queries in all child classes.
	protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMAN);
	
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
		
		// return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		
		return new String(readFully(in), StandardCharsets.UTF_8);
	}
	
	/**
	 * Converts InputStean to byte[]. Source: https://stackoverflow.com/questions/2163644/in-java-how-can-i-convert-an-inputstream-into-a-byte-array-byte?noredirect=1&lq=1
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private static byte[] readFully(InputStream input) throws IOException
	{
	    byte[] buffer = new byte[8192];
	    int bytesRead;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    while ((bytesRead = input.read(buffer)) != -1)
	    {
	        output.write(buffer, 0, bytesRead);
	    }
	    return output.toByteArray();
	}
	
	/**
	 * Prints object to console.
	 */
	public abstract void print();
	
}
