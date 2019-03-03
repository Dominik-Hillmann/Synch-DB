import java.sql.Connection;

public interface DataBaseStorable {
	public boolean storeInDataBase();
	public boolean isSameAs(DataBaseStorable storable, Connection database);
}
