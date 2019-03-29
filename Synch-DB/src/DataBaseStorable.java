import java.sql.Connection;
import java.sql.SQLException;

import com.dropbox.core.v2.DbxClientV2;

public interface DataBaseStorable {
	
	/**
	 * Stores an object in the database to all tables needed.
	 * @param database Connection to the MySQL database.
	 * @param client with information to connect to the DropBox.
	 * @throws SQLException if there is a problem with the query caused by the values of this object.
	 */
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException;
	
	/**
	 * Updates all the information about this object in the database.
	 * @param database Connection to the MySQL database.
	 * @param client with information to connect to the DropBox.
	 * @throws SQLException if there is a problem with the query caused by the values of this object.
	 */
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException;
	
	/**
	 * Deletes this information from its main table and all associated tables like tags, categories, etc.
	 * @param database Connection to the MySQL database.
	 * @throws SQLException if there is a problem with the query caused by the values of this object.
	 */
	public void deleteFromDataBase(Connection database) throws SQLException;
	
	/**
	 * Compares two DataBaseStorables. They can have one of four types of relationship (DataChangeMarker).
	 * @param storable to compare this object to.
	 * @return Indicates the relationship of the two DataBaseStorables.
	 */
	public DataChangeMarker containsSameData(DataBaseStorable storable);
	
}
