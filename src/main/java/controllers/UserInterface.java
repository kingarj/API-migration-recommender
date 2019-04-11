package controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.UtilityMethods;

public class UserInterface {

	public static Controller controller;
	private static Logger logger = LoggerFactory.getLogger(UserInterface.class);

	public UserInterface() {
	}

	public static void main(String[] args) throws ClientProtocolException, URISyntaxException, IOException {

		Scanner scanner = new Scanner(System.in);
		String root = "src/main/resources/";

		String username;
		String password;

		try {
			String creds = UtilityMethods.readFile("src/main/resources/creds.txt");
			String[] credsArr = creds.split("\r\n");
			username = credsArr[0];
			password = credsArr[1];

		} catch (IOException e) {
			logger.debug(e.getMessage());
			System.out.println("Please enter your GitHub username");
			username = scanner.nextLine();
			System.out.println("Please enter your GitHub password");
			password = scanner.nextLine();
		}

		controller = new Controller(root + "libraries/", username, password);

		while (true) {
			System.out.println("Enter x to exit at any time");
			System.out.println("Please enter the name of the API you wish to migrate from");
			String source = scanner.nextLine();
			if (source.equals("x")) {
				break;
			}

			System.out.println("Please enter the name of the API you wish to migrate to");
			String target = scanner.nextLine();
			if (target.equals("x")) {
				break;
			}

			HashMap<String, String> recommendations = controller.generateRecommendations(source, target);
			if (recommendations.isEmpty()) {

				logger.info("We could not provide any recommendations for these libraries");

			} else {
				String filename = UtilityMethods.buildOutputFileName(source, target);

				try {

					PrintWriter writer = new PrintWriter(root + "output/" + filename + ".txt");

					for (Entry<String, String> r : recommendations.entrySet()) {
						writer.println("We recommend that you map: ");
						writer.println(r.getKey());
						writer.println("to");
						writer.println("\n" + r.getValue());
						writer.println();
						;
					}
					writer.close();
				} catch (FileNotFoundException e) {
					logger.debug(e.getMessage());
				}

			}
		}
		scanner.close();
	}
}
