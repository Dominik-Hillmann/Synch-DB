
public enum DataChangeMarker { 
	
	DIFFERENT_TYPE, // e.g. PictureInformation vs. Writing Information
	DIFFERENT_FILE, // different primary keys
	SAME_FILE_CHANGED, // same primary keys with changed attributes
	SAME_FILE_KEPT_SAME; // The primary key and all attributes stayed the same.
	
	public String toString() {
		switch (this) {
			case DIFFERENT_TYPE: return "DIFFERENT_TYPE";
			case DIFFERENT_FILE: return "DIFFERENT_FILE";
			case SAME_FILE_CHANGED: return "SAME_FILE_CHANGED";
			default: return "SAME_FILE_KEPT_SAME";
		}
	}
	
}
