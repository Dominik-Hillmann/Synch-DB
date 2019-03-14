
public enum DataChangeMarker { 
	DIFFERENT_TYPE, 
	DIFFERENT_FILE, 
	SAME_FILE_CHANGED, 
	SAME_FILE_KEPT_SAME;
	
	public String toString() {
		switch (this) {
			case DIFFERENT_TYPE: return "different type";
			case DIFFERENT_FILE: return "different file";
			case SAME_FILE_CHANGED: return "same file but some data changed";
			default: return "same file, nothing is different";
		}
	}
}
