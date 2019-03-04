package services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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

}
