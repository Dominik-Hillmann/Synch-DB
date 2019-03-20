
public enum DataChangeMarker { 
	DIFFERENT_TYPE, 
	DIFFERENT_FILE, 
	SAME_FILE_CHANGED, 
	SAME_FILE_KEPT_SAME;
	
	public String toString() {
		switch (this) {
			case DIFFERENT_TYPE: return "DIFFERENT_TYPE";
			case DIFFERENT_FILE: return "DIFFERENT_FILE";
			case SAME_FILE_CHANGED: return "SAME_FILE_CHANGED";
			default: return "SAME_FILE_KEPT_SAME";
		}
	}
}
