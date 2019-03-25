import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

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
	private boolean twitter;
	private boolean instagram;
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
		this.twitter = info.twitter;
		this.instagram = info.instagram;
		this.tags = info.tags;
		this.text = info.text;
	}
	
	/**
	 * 
	 * @param set
	 * @param database
	 */
	public WritingInformation(ResultSet set, Connection database)  {
		try {
			LocalDate date = LocalDate.parse(set.getString("date"), formatter);
			this.day = date.getDayOfMonth();
			this.month = date.getMonthValue();
			this.year = date.getYear();
			
			this.name = set.getString("name");
			this.secret = set.getBoolean("kept_secret");
			this.twitter = set.getBoolean("twitter_posted");
			this.instagram = set.getBoolean("insta_posted");
			this.text = set.getString("text");
			
			// Tags
			String tagsQuery = "SELECT tag_name FROM db_synchro.tags_writs WHERE writ_name='"
				+ getName() + "';";
			ResultSet tagQueryRes = database.prepareStatement(tagsQuery).executeQuery();
			ArrayList<String> tags = new ArrayList<String>();
			while (tagQueryRes.next()) {
				tags.add(tagQueryRes.getString("tag_name"));
			}			
			this.tags = Arrays.copyOf(tags.toArray(), tags.toArray().length, String[].class);
			
		} catch (Exception e) {
			Logger.log("Could not retrieve value from query: " + e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name);
		for (var tag : tags) System.out.println(tag);
		System.out.println("Number of tags: " + String.valueOf(tags.length));
		System.out.println(text + "\n");
	}

	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		String sqlString = "INSERT INTO db_synchro.writ_info VALUES ("
			+ "'" + getName() + "'," 
			+ "'" + getDateStr() + "',"
			+ "b'" + (isSecret() ? 1 : 0) + "',"
			+ "b'" + (postedToTwitter() ? 1 : 0) + "',"
			+ "b'" + (postedToInstagram() ? 1 : 0) + "',"
			+ "'" + getText() + "');";
		database.prepareStatement(sqlString).executeUpdate();
		
		for (var tag : this.tags) {
			String newTagSql = "INSERT INTO db_synchro.tags_writs VALUES ("
				+ "'" + tag + "',"
				+ "'" + getName() + "');";
			try {
				database.prepareStatement(newTagSql).executeUpdate();
			} catch (SQLException e) {
				continue;
			} // try			
		} // for
	} // storeInDataBase

	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		String sqlMain = "DELETE FROM db_synchro.writ_info WHERE "
			+ "name = '" + getName() + "';";
		database.prepareStatement(sqlMain).executeUpdate();
		String sqlTags = "DELETE FROM db_synchro.tags_writs WHERE "
			+ "writ_name = '" + getName() + "';";
		database.prepareStatement(sqlTags).executeUpdate();
	}
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		WritingInformation compareInfo;
		try {
			// If the storable is not even a WritingInformation, it will not be the same.
			compareInfo = (WritingInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		}
		
		// Do they have the same primary key?
		if (getName().equals(compareInfo.getName())) {
			// Are all other attributes the same, if they have the same primary key?
			if (getDateStr().equals(compareInfo.getDateStr())
				&& isSecret() == compareInfo.isSecret()
				&& postedToTwitter() == compareInfo.postedToTwitter()
				&& postedToInstagram() == compareInfo.postedToInstagram()
				&& getText().equals(compareInfo.getText())) {
				return DataChangeMarker.SAME_FILE_KEPT_SAME;
			} else return DataChangeMarker.SAME_FILE_CHANGED;
		} else return DataChangeMarker.DIFFERENT_FILE;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDateStr() {
		String dayStr = String.valueOf(day);
		String monthStr = String.valueOf(month);
		String yearStr = String.valueOf(year);
		return yearStr + "-" + monthStr + "-" + dayStr;
	}
	
	public boolean isSecret() {
		return this.secret;
	}
	
	public boolean postedToTwitter() {
		return this.twitter;
	}
	
	public boolean postedToInstagram() {
		return this.instagram;
	}
	
	public String getText() {
		return this.text;
	}
	
}
