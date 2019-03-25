import java.sql.Connection;
import java.sql.SQLException;

import com.dropbox.core.v2.DbxClientV2;

public interface DataBaseStorable {
	
	/**
	 * 
	 * @param database
	 * @param client
	 * @throws SQLException
	 */
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException;
	
	/**
	 * 
	 * @param database
	 * @param client
	 * @throws SQLException
	 */
	public void updateDataBase(Connection database, DbxClientV2 client) throws SQLException;
	
	/**
	 * 
	 * @param database
	 * @throws SQLException
	 */
	public void deleteFromDataBase(Connection database) throws SQLException;
	
	/**
	 * 
	 * @param storable
	 * @return
	 */
	public DataChangeMarker containsSameData(DataBaseStorable storable);
}
