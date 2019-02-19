import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;

public class PictureInformation extends Information implements DataBaseStorable {

	private int day;
	private int month;
	private int year;
	private String name;
	private String fileName;
	private boolean secret;
	private String[] tags;
	
	private String description;
	private boolean instagram;
	private boolean twitter;	
	
	/**
	 * 
	 * @param fileName
	 * @param client
	 * @throws IOException
	 * @throws DbxException
	 */
	public PictureInformation(String fileName, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		
		String jsonStr = getJSONString("/pic-info/" + fileName, client);
		PictureInformation info = gson.fromJson(jsonStr, PictureInformation.class);

		
		
	}
	
}
