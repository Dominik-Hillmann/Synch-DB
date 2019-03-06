import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	private static final Path PIC_FOLDER_LOCAL = Paths.get("/home/dominik/DB-Synch-imgs/");
	
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
		
		// TODO auch noch das Bild an sich speichern
	}
	

	@SuppressWarnings("deprecation")
	public PictureInformation(ResultSet queryResult) throws IOException, DbxException {
		try {/*
			this.filename = queryResult.getString("filename");
			this.name = queryResult.getString("name");
			this.description = queryResult.getString("explanation");
			
			this.secret = queryResult.getBoolean("kept_secret");
			this.instagram = queryResult.getBoolean("insta_posted");
			this.twitter = queryResult.getBoolean("twitter_posted");*/
			
			Logger.log(queryResult.getString("date") + " LALALA");
			// ********************************************************* TODO
			Logger.log(String.valueOf((queryResult.getDate("date").getDay())) + " LALALA");
		} catch (SQLException e) {
			Logger.log("Konnte nicht retrieven: " + e.getMessage());
		}
	}
	
	private void savePic(String filename, DbxClientV2 client) throws IOException, DbxException {
		// TODO Finden des Bildes und einfügen in den Ordner
	}
	
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name + ", " + filename);
		for (String tag : tags) System.out.println(tag);
		System.out.println("Number of tags: " + String.valueOf(tags.length));
		System.out.println(description + "\n");
	}
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException { 
		String sqlString = "INSERT INTO pic_info VALUES ("
			+ "'" + getFileName() + "'" + "," 
			+ "'" + getName() + "'" + "," 
			+ "'" + getDateStr() + "'" + ","
			+ "'" + getDescription() + "'" + ","
			+ "b'" + (isSecret() ? 1 : 0) + "'" + ","
			+ "b'" + (postedToTwitter() ? 1 : 0) + "'" + ","
			+ "b'" + (postedToInsta() ? 1 : 0) + "'" + ");";
		
		try {
			savePic(getFileName(), client);
		} catch (Exception e) {
			throw new SQLException("Could not find any picture with the same filename. Information was not inserted into database.");
		}
		// Not in finally because information is not supposed to be inserted into database.
		database.prepareStatement(sqlString).executeUpdate();
	}
		
	public String getFileName() {
		return filename;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDateStr() {
		String dayStr = String.valueOf(day);
		String monthStr = String.valueOf(month);
		String yearStr = String.valueOf(year);
		return yearStr + "-" + monthStr + "-" + dayStr;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isSecret() {
		return secret;
	}
	
	public boolean postedToTwitter() {
		return twitter;
	}
	
	public boolean postedToInsta() {
		return instagram;
	}
	
	public boolean containsSameData(DataBaseStorable storable, Connection database) throws SQLException {
		PictureInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (PictureInformation) storable;
		} catch (Exception e) {
			return false;
		}
		
		return getFileName().equals(compareInfo.getFileName())
			&& getName().equals(compareInfo.getName())
			&& getDateStr().equals(compareInfo.getName())
			&& getDescription().equals(compareInfo.getName())
			&& isSecret() == compareInfo.isSecret()
			&& postedToTwitter() == compareInfo.postedToTwitter()
			&& postedToInsta() == compareInfo.postedToInsta();
	}
}
