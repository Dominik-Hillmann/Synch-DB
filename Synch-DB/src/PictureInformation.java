import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

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
	private String category;
	
	private static final Path PIC_FOLDER_LOCAL = Paths.get("/home/dominik/DB-Synch-imgs/");
	private static final String PIC_STORAGE_LOCAL = "/home/dominik/DB-Synch-imgs/";
	private static final String PIC_STORAGE_DBX = "/img/";
	
	/**
	 * Creates object by getting the information from the JSON file in the DropBox.
	 * @param filename is the name of the JSON file containing the wanted information.
	 * @param client with information to connect to the DropBox.
	 * @throws IOException if the JSON file contains unexpected information or is somehow wrong.
	 * @throws DbxException if the file with this filename does not exist in the DropBox.
	 */
	public PictureInformation(String filename, DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();
		
		String jsonStr = getJSONString("/pic-info/" + filename, client);
		PictureInformation info;
		
		try {
			info = gson.fromJson(jsonStr, PictureInformation.class);
		} catch (JsonSyntaxException jse) {
			throw new IOException("JSON string of file " + filename + " could not be converted.");
		}

		try {
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
			this.category = info.category;
		} catch (Exception e) {
			throw new IOException("A value in " + filename + " has the wrong type or is not used.");
		}
	}
	
	/**
	 * Creates object using the information from the query.
	 * @param queryResult with the cursor on the entry with the information you want to create this object with.
	 * @param database Connection to have access to the MySQL database.
	 * @throws SQLException if a column cannot be found for the entry the cursor points to.
	 */
	public PictureInformation(ResultSet queryResult, Connection database) throws SQLException {
		LocalDate date = LocalDate.parse(queryResult.getString("date"), FORMATTER);
		this.day = date.getDayOfMonth();
		this.month = date.getMonthValue();
		this.year = date.getYear();
			
		this.filename = queryResult.getString("filename");
		this.name = queryResult.getString("name");
		this.description = queryResult.getString("explanation");
			
		this.secret = queryResult.getBoolean("kept_secret");
		this.instagram = queryResult.getBoolean("insta_posted");
		this.twitter = queryResult.getBoolean("twitter_posted");
			
		this.category = queryResult.getString("category");
		
		// Several updates to add the tags to tables associated with this image.
		String tagsQuery = "SELECT tag_name FROM db_synchro.tags_pics WHERE pic_filename = '"
			+ getFileName() + "';";
		ResultSet tagQueryRes = database.prepareStatement(tagsQuery).executeQuery();
		ArrayList<String> tags = new ArrayList<String>();
		while (tagQueryRes.next()) {
			tags.add(tagQueryRes.getString("tag_name"));
		}

		this.tags = Arrays.copyOf(tags.toArray(), tags.toArray().length, String[].class);
	}
	
	/**
	 * Stores the image in the local directory PIC_STORAGE_LOCAL.
	 * @param filename of the JPG/PNG/GIF to be stored in a local directory.
	 * @param client with information to connect to the DropBox.
	 * @throws IOException if there are problems handling the image locally.
	 * @throws DbxException if the image cannot be found in the DropBox.
	 */
	private void savePic(DbxClientV2 client) throws IOException, DbxException {
		// If the necessary directory does not exist, then create it.
		Path localImgsDir = Paths.get(PIC_STORAGE_LOCAL);		
		if (!Files.isDirectory(localImgsDir.toAbsolutePath())) {
			(new File(localImgsDir.toAbsolutePath().toString())).mkdirs();
			Logger.log("Created the directory for images: " + PIC_STORAGE_LOCAL + ".");
		} else {
			Logger.log("Directory " + PIC_STORAGE_LOCAL + " already exists.");
		} 
		
		// Does picture with this name already exist? If not, create the file.
		File picToBeStored = new File(PIC_FOLDER_LOCAL.toAbsolutePath().toString() + "/" + getFileName());						
		if (!picToBeStored.isFile()) {
			picToBeStored.setWritable(true);
			picToBeStored.createNewFile();
		} else {
			Logger.log("File " + picToBeStored.getName() + " already exists");
		}				
		
		// Read the image from the DropBox. If it's not there, throw Exception and let main decide what to do.
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(
				client.files()
					.download(PIC_STORAGE_DBX + getFileName())
		           	.getInputStream()
			);
		} catch (Exception e) {
			throw new DbxException("Could not download " + getFileName() + " from the Dropbox.");
		}
		           	
		// Download seemingly successful. Write it to the local specified directory (PIC_FOLDER_LOCAL).
		String imgName = picToBeStored.getName();
		int extensionStartIndex = imgName.lastIndexOf(".");
		String extension = imgName.substring(extensionStartIndex + 1);
		ImageIO.write(bufferedImage, extension, picToBeStored);
	}
	

	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException {
		// Store basic information with this in the main table.
		String sqlString = "INSERT INTO db_synchro.pic_info VALUES ("
			+ "'" + getFileName() + "'," 
			+ "'" + getName() + "'," 
			+ "'" + getDateStr() + "',"
			+ "'" + getDescription() + "',"
			+ "b'" + (isSecret() ? 1 : 0) + "',"
			+ "b'" + (postedToTwitter() ? 1 : 0) + "',"
			+ "b'" + (postedToInsta() ? 1 : 0) + "'," 
			+ "'" + getCategory() + "');";
		database.prepareStatement(sqlString).executeUpdate();
		
		// Store the tags in separate table.
		for (var tag : this.tags) {
			String newTagSql = "INSERT INTO db_synchro.tags_pics VALUES ("
				+ "'" + tag + "',"
				+ "'" + getFileName() + "');";
			try {
				database.prepareStatement(newTagSql).executeUpdate();
			} catch (SQLException e) {
				// Skip in case of a duplicate entry.
				continue;
			}			
		}
		
		// Now download and store the associated image. If it does not work, delete information again.
		try {
			savePic(client);
		} catch (Exception e) {
			// Information not valid because there is no image associated with it.
			deleteFromDataBase(database); 
			throw new SQLException("Could not find any picture with filename " + getFileName() + ". Information was not inserted into database.");
		}
	}
	
	
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}
	
	
	public void deleteFromDataBase(Connection database) throws SQLException {
		// First, delete main information from picture table, then the tags.
		String sqlString = "DELETE FROM db_synchro.pic_info WHERE filename='" + getFileName() + "';";
		database.prepareStatement(sqlString).executeUpdate();
		String sqlTags = "DELETE FROM db_synchro.tags_pics WHERE "
			+ "pic_filename = '" + getFileName() + "';";
		database.prepareStatement(sqlTags).executeUpdate();
	}
	
	
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		PictureInformation compareInfo;
		try {
			// If the storable is not even a PictureInformation, it will not be the same.
			compareInfo = (PictureInformation) storable;
		} catch (Exception e) {
			return DataChangeMarker.DIFFERENT_TYPE;
		}
		// If both tag lists do not have same length, they cannot be the same.
		var compareTags = compareInfo.getTags();
		boolean sameTags = tags.length == compareTags.size();
		
		// Comparison tag by tag if they are assumed to be the same until here.
		if (sameTags) {
			for (var tag : tags) {
				if (!compareTags.contains(tag)) {
					sameTags = false;
					break;
				}
			}
		}
		
		// If they have the same primary key, they are the same file. Did any other property change?
		if (getFileName().equals(compareInfo.getFileName())) {
			return getName().equals(compareInfo.getName())
				&& getDateStr().equals(compareInfo.getDateStr())
				&& getDescription().equals(compareInfo.getDescription())
				&& isSecret() == compareInfo.isSecret()
				&& postedToTwitter() == compareInfo.postedToTwitter()
				&& postedToInsta() == compareInfo.postedToInsta()
				&& getCategory().equals(compareInfo.getCategory())
				&& sameTags
				? DataChangeMarker.SAME_FILE_KEPT_SAME : DataChangeMarker.SAME_FILE_CHANGED;
		} else {
			// If they do not have the same primary key, it was another file all along.
			return DataChangeMarker.DIFFERENT_FILE;
		}
	}
	
	// Remaining getters, setters and printing methods follow.
	
	public void print() {
		System.out.println("Date: " + String.valueOf(day) + "." + String.valueOf(month) + "." + String.valueOf(year));
		System.out.println("Names: " + name + ", " + filename);
		System.out.println(description + "\n");
	}
	
	public String getFileName() { return filename; }	
	
	public String getName() { return name; }	
	
	public String getDateStr() {
		String dayStr = String.valueOf(day);
		String monthStr = String.valueOf(month);
		String yearStr = String.valueOf(year);
		return yearStr + "-" + monthStr + "-" + dayStr;
	}	
	
	public String getDescription() { return description; }	
	
	public boolean isSecret() { return secret; }	
	
	public boolean postedToTwitter() { return twitter; }
		
	public boolean postedToInsta() { return instagram; }
		
	public String getCategory() { return category; }
		
	public ArrayList<String> getTags() {
		return new ArrayList<String>(Arrays.asList(tags));
	}	
		
}
