import java.sql.Connection;
import java.sql.SQLException;

import com.dropbox.core.v2.DbxClientV2;

public interface DataBaseStorable {
	public void storeInDataBase(Connection database, DbxClientV2 client) throws SQLException;
	public DataChangeMarker containsSameData(DataBaseStorable storable, Connection database) throws SQLException;
}
