import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class WritingInformation extends Information implements DataBaseStorable {

	private int day;
	private int month;
	private int year;
	private String name;
	private boolean secret;
	private String[] tags;
	private String text;
	
	/**
	 * 
	 * @param filename
	 * @param client
	 * @throws IOException
	 * @throws DbxException
	 */
	public WritingInformation(String filename, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		
		String jsonStr = getJSONString("/writing-info/" + filename, client);
		WritingInformation info;
		
		try {
			info = gson.fromJson(jsonStr, WritingInformation.class);
		} catch (JsonSyntaxException jse) {
			throw new IOException("JSON string could not be converted.");
		}

		this.day = info.day;
		this.month = info.month;
		this.year = info.year;
		this.name = info.name;
		this.secret = info.secret;
		this.tags = info.tags;
		this.text = info.text;	
	}
	
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name);
		for (String tag : tags) System.out.println(tag);
		System.out.println("Number of tags: " + String.valueOf(tags.length));
		System.out.println(text + "\n");
	}
	
	// placeholder
	public boolean storeInDataBase() { 
		print();
		return true;
	}
	
}