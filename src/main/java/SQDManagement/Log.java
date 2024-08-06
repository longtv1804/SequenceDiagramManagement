package SQDManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
	private static String mFilePath = null;
	
	private Log () {

	}

	private static void printLog (String fileName, String msg) {
		if (mFilePath != null) {
			try {
				final long curTime = System.currentTimeMillis();
				final Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(curTime);
				final String timeString = new SimpleDateFormat("ddMMYYYY HH:mm:ss:SSS").format(cal.getTime());
				
				String logmsg = timeString + " " + fileName + ": " + msg + "\n";
				Files.write(Paths.get(mFilePath), logmsg.getBytes("UTF-16"), StandardOpenOption.APPEND);
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	public static void d (String fileName, String msg) {
		printLog(fileName, "d    " + msg);
	}
	public static void e (String fileName, String msg) {
		printLog(fileName, "ERR  " + msg);
	}

	public static void setPath(String path)  {
		mFilePath = path;
		Path l_path = Paths.get(mFilePath);
		try {
			Files.createFile(l_path);
		} catch (IOException e) {
			// ignore
		}
	}
}