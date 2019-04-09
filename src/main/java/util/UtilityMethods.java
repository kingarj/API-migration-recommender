package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class UtilityMethods {

	public static String readFile(String fileName) throws IOException {
		String responseStr;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			responseStr = sb.toString();
		} finally {
			br.close();
		}
		return responseStr;
	}

	public static String buildOutputFileName(String source, String target) {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return source + target + "_" + calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + 1)
				+ calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE)
				+ calendar.get(Calendar.SECOND);
	}

}
