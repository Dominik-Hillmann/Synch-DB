import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PictureInformation extends Information implements DataBaseStorable {

	private int day;
	private int month;
	private int year;
	private String name;
	private String filename;
	private boolean secret;
	private String[] tags;
	
	private String description;
	private boolean instagram;
	private boolean twitter;	
	
	/**
	 * 
	 * @param filename
	 * @param client
	 * @throws IOException
	 * @throws DbxException
	 */
	public PictureInformation(String filename, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		
		String jsonStr = getJSONString("/pic-info/" + filename, client);
		PictureInformation info;
		
		try {
			info = gson.fromJson(jsonStr, PictureInformation.class);
		} catch (JsonSyntaxException jse) {
			throw new IOException("JSON string could not be converted.");
		}

		this.day = info.day;
		this.month = info.month;
		this.year = info.year;
		this.name = info.name;
		this.filename = info.filename;
		this.secret = info.secret;
		this.tags = info.tags;
		this.description = info.description;
		this.instagram = info.instagram;
		this.twitter = info.twitter;
		
		
	}
	
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name + ", " + filename);
		for (String tag : tags) System.out.println(tag);
		System.out.println("Number of tags: " + String.valueOf(tags.length));
		System.out.println(description + "\n");
	}
	
	
	// placeholder
	public boolean storeInDataBase() { 
		print(); // voerst
		return true;	
	}
	
	/**
	 * 
	 */
	public void writePic() {
		
	}
	
	public String getFileName() {
		return filename;
	}
	
}
