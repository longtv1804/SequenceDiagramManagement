package SQDManagement;

public class Utils {
	
	// check null or empty for a string
	public static boolean isEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static String trim(String str) {
		if (str == null) {
			return "";
		}
		return str.trim();
	}
}
