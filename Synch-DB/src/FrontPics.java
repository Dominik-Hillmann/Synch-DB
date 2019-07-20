import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class FrontPics extends Information implements DataBaseStorable {
	
	private String graphic_design = null;
	private String illustrationen = null;
	private String drawings = null;
	private String writing = null;
	private String photography = null;
	
	private static final String[] CATEGORY_NAMES = { "graphic_design", "illustrationen", "drawings", "writing", "photography" };
		
	public FrontPics(DbxClientV2 client) throws IOException, DbxException {
		Gson gson = new Gson();		
		String jsonStr = getJSONString("/front_pics/front_pics.json", client);
		
		FrontPics pics;
		try {
			pics = gson.fromJson(jsonStr, FrontPics.class);
		} catch (JsonSyntaxException jse) {
			throw new IOException("JSON string of front_pics could not be converted.");
		}
		
		try {
			this.graphic_design = pics.graphic_design;
			this.illustrationen = pics.illustrationen;
			this.drawings = pics.drawings;
			this.writing = pics.writing;
			this.photography = pics.photography;
		} catch (Exception e) {
			throw new IOException("A value in front_pics has the wrong type or is not used.");
		}		
	}
	
	public FrontPics(/*ResultSet queryResult, */Connection database) throws SQLException {
		ResultSet queryResult = null;
		try {		
			PreparedStatement picQuery = database.prepareStatement("SELECT * FROM db_synchro.front_pics;");
			queryResult = picQuery.executeQuery();
		} catch (SQLException e) {
			Logger.log("The front_pics query could not be finished.");
		}
		
		while (queryResult.next()) {
			String category = queryResult.getString("category_name");
			String picPath = queryResult.getString("pic_filename");
			
			switch (category) {
				case "graphic_design":
					this.graphic_design = picPath;
					break;
				case "illustrationen":
					this.illustrationen = picPath;
					break;
				case "drawings":
					this.drawings = picPath;
					break;
				case "writing":
					this.writing = picPath;
					break;
				case "photography":
					this.photography = picPath;
					break;
			}
		}
	}
	
	
	@Override
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException, DbxException {
		for (String category : CATEGORY_NAMES) {
			String sqlStr = "INSERT INTO db_synchro.front_pics VALUES (" +
					"'" + category + "'," +
				"'" + getPathOfCategory(category) + "');";
			database.prepareStatement(sqlStr).executeUpdate();
		}
		Logger.log("Saved front_pics.");
	}

	@Override
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException, DbxException {
		deleteFromDataBase(database);
		storeInDataBase(database, client);
	}

	@Override
	public void deleteFromDataBase(Connection database) throws SQLException {
		for (String category : CATEGORY_NAMES) {
			String sqlString = "DELETE FROM db_synchro.front_pics WHERE filename='" + category + "';";
			database.prepareStatement(sqlString).executeUpdate();

			Logger.log("Deleted information about front_pic " + category + " from the database.");
		}
	}

	@Override
	public DataChangeMarker containsSameData(DataBaseStorable storable) {
		FrontPics others = (FrontPics) storable;
		for (String category : CATEGORY_NAMES) {
			try {
				
			} catch (Exception e) {
				
			}
		}
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}
	
	private String getPathOfCategory(String category) {
		switch (category) {
			case "graphic_design": return this.graphic_design;
			case "illustrationen": return this.illustrationen;
			case "drawings": return this.drawings;
			case "writing": return this.writing;
			case "photography": return this.photography;
		}
		return null;
	}
	
}
