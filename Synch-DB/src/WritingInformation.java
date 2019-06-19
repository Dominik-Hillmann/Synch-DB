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
	
	private boolean secret;
	private boolean twitter;
	private boolean instagram;
	
	private String name;	
	private String text;

	private String[] tags;
	private String category;
	
	/**
	 * Creates object by finding corresponding JSON file in the DropBox.
	 * @param filename is the name of the JSON file.
	 * @param client with information to connect to the DropBox.
	 * @throws IOException if the JSON file contains unexpected information or is somehow wrong.
	 * @throws DbxException if the file with this filename does not exist in the DropBox.
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

		try {
			this.day = info.day;
			this.month = info.month;
			this.year = info.year;
			this.name = info.name;
			this.secret = info.secret;
			this.twitter = info.twitter;
			this.instagram = info.instagram;
			this.tags = info.tags;
			this.category = info.category;
			this.text = info.text;
		} catch (Exception e) {
			throw new IOException("A value in " + filename + " has the wrong type or is not used.");
		}
	}
	
	/**
	 * Creates object using the information from the query.
	 * @param set with the cursor on the entry with the information you want to create this object with.
	 * @param database Connection to have access to the MySQL database.
	 * @throws SQLException if a column cannot be found for the entry the cursor points to.
	 */
	public WritingInformation(ResultSet set, Connection database) throws SQLException {
		// Assign values first.
		LocalDate date = LocalDate.parse(set.getString("date"), FORMATTER);
		this.day = date.getDayOfMonth();
		this.month = date.getMonthValue();
		this.year = date.getYear();
			
		this.name = set.getString("name");
		this.secret = set.getBoolean("kept_secret");
		this.twitter = set.getBoolean("twitter_posted");
		this.instagram = set.getBoolean("insta_posted");
		this.text = set.getString("text");
		this.category = set.getString("category");
			
		// Then construct tag array from other table.
		String tagsQuery = "SELECT tag_name FROM db_synchro.tags_writs WHERE writ_name='"
			+ getName() + "';";
		ResultSet tagQueryRes = database.prepareStatement(tagsQuery).executeQuery();
		ArrayList<String> tags = new ArrayList<String>();
		while (tagQueryRes.next()) {
			tags.add(tagQueryRes.getString("tag_name"));
		}			
		this.tags = Arrays.copyOf(tags.toArray(), tags.toArray().length, String[].class);
	}
	
	
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		// First store main information in the table.
		String sqlString = "INSERT INTO db_synchro.writ_info VALUES ("
			+ "'" + getName() + "'," 
			+ "'" + getDateStr() + "',"
			+ "b'" + (isSecret() ? 1 : 0) + "',"
			+ "b'" + (postedToTwitter() ? 1 : 0) + "',"
			+ "b'" + (postedToInstagram() ? 1 : 0) + "',"
			+ "'" + getText() +"',"
			+ "'" + getCategory() + "');";
		database.prepareStatement(sqlString).executeUpdate();
		
		// Then store the tags one by one.
		for (String tag : this.tags) {
			String newTagSql = "INSERT INTO db_synchro.tags_writs VALUES ("
				+ "'" + tag + "',"
				+ "'" + getName() + "');";
			try {
				database.prepareStatement(newTagSql).executeUpdate();
			} catch (SQLException e) {
				continue;
			}		
		}
		
		Logger.log("Stored WritingInformation " + getName() + " in database.");
	}

	
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		// Delete first from main table for writings, then delete the tags.
		String sqlMain = "DELETE FROM db_synchro.writ_info WHERE "
			+ "name = '" + getName() + "';";
		database.prepareStatement(sqlMain).executeUpdate();
		String sqlTags = "DELETE FROM db_synchro.tags_writs WHERE "
			+ "writ_name = '" + getName() + "';";
		database.prepareStatement(sqlTags).executeUpdate();
		
		Logger.log("Deleted WritingInformation " + getName() + " from database.");
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
				&& getText().equals(compareInfo.getText())
				&& getCategory().equals(compareInfo.getCategory())) {
				return DataChangeMarker.SAME_FILE_KEPT_SAME;
			} else return DataChangeMarker.SAME_FILE_CHANGED;
		} else return DataChangeMarker.DIFFERENT_FILE;
	}
	
	// Remaining getters, setters and printing methods follow.
	
	public String getDateStr() {
		String dayStr = String.valueOf(day);
		String monthStr = String.valueOf(month);
		String yearStr = String.valueOf(year);
		return yearStr + "-" + monthStr + "-" + dayStr;
	}
	public String getName() { return this.name;	}
	
	public boolean isSecret() {	return this.secret; }
	
	public boolean postedToTwitter() { return this.twitter;	}
	
	public boolean postedToInstagram() { return this.instagram; }
	
	public String getText() { return this.text; }
	
	public String getCategory() { return category; }
	
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name);
		for (String tag : tags) System.out.println(tag);
		System.out.println("Number of tags: " + String.valueOf(tags.length));
		System.out.println(text + "\n");
	} 
	
}
