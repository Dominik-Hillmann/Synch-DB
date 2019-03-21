import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

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
	
	private String description;
	private boolean instagram;
	private boolean twitter;
	
	private String[] tags;
	
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
		this.description = info.description;
		this.instagram = info.instagram;
		this.twitter = info.twitter;
		this.tags = info.tags;
		
		// TODO auch noch das Bild an sich speichern
	}
	

	public PictureInformation(ResultSet queryResult, Connection database) {
		try {
			LocalDate date = LocalDate.parse(queryResult.getString("date"), formatter);
			this.day = date.getDayOfMonth();
			this.month = date.getMonthValue();
			this.year = date.getYear();
			
			this.filename = queryResult.getString("filename");
			this.name = queryResult.getString("name");
			this.description = queryResult.getString("explanation");
			
			this.secret = queryResult.getBoolean("kept_secret");
			this.instagram = queryResult.getBoolean("insta_posted");
			this.twitter = queryResult.getBoolean("twitter_posted");
			
			String tagsQuery = "SELECT tag_name FROM db_synchro.tags_pics WHERE pic_filename = '"
					+ getFileName() + "';";
			ResultSet tagQueryRes = database.prepareStatement(tagsQuery).executeQuery();
			ArrayList<String> tags = new ArrayList<String>();
			while (tagQueryRes.next()) {
				tags.add(tagQueryRes.getString("tag_name"));
			}

			this.tags = Arrays.copyOf(tags.toArray(), tags.toArray().length, String[].class);
				
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
		System.out.println(description + "\n");
	}
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException { 
		String sqlString = "INSERT INTO db_synchro.pic_info VALUES ("
			+ "'" + getFileName() + "'" + "," 
			+ "'" + getName() + "'" + "," 
			+ "'" + getDateStr() + "'" + ","
			+ "'" + getDescription() + "'" + ","
			+ "b'" + (isSecret() ? 1 : 0) + "'" + ","
			+ "b'" + (postedToTwitter() ? 1 : 0) + "'" + ","
			+ "b'" + (postedToInsta() ? 1 : 0) + "'" + ");";
		
		for (var tag : this.tags) {
			String newTagSql = "INSERT INTO db_synchro.tags_pics VALUES ("
				+ "'" + tag + "',"
				+ "'" + getFileName() + "');";
			database.prepareStatement(newTagSql).executeUpdate();
		}
		
		try {
			savePic(getFileName(), client);
		} catch (Exception e) {
			throw new SQLException("Could not find any picture with filename " + getFileName() + ". Information was not inserted into database.");
		}
		// Not in finally because information is not supposed to be inserted into database.
		database.prepareStatement(sqlString).executeUpdate();
	}
	
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		String sqlString = "DELETE FROM db_synchro.pic_info WHERE filename='" + getFileName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		String sqlTags = "DELETE FROM db_synchro.pic_info WHERE "
			+ "pic_filename = '" + getFileName() + "';";
		database.prepareStatement(sqlTags).executeUpdate();
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
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		PictureInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (PictureInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		}
		
		if (getFileName().equals(compareInfo.getFileName())) {
			return getName().equals(compareInfo.getName())
				&& getDateStr().equals(compareInfo.getDateStr())
				&& getDescription().equals(compareInfo.getDescription())
				&& isSecret() == compareInfo.isSecret()
				&& postedToTwitter() == compareInfo.postedToTwitter()
				&& postedToInsta() == compareInfo.postedToInsta() 
				? DataChangeMarker.SAME_FILE_KEPT_SAME : DataChangeMarker.SAME_FILE_CHANGED;
		} else return DataChangeMarker.DIFFERENT_FILE;
	}
	/*
	public boolean containsElementWithSameData(ArrayList<DataBaseStorable> arr, Connection database) throws SQLException {
		boolean containsElementWithSameData = false;
		for (DataBaseStorable info : arr) {
			if (containsSameData(info, database)) {
				return true;
			} else continue;
		}
		return false;
	}
	*/
}
