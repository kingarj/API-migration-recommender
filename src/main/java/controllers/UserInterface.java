package controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;

public class UserInterface {

	public static Controller controller;

	public UserInterface() {
	}

	public static void main(String[] args) throws ClientProtocolException, URISyntaxException, IOException {

		Scanner scanner = new Scanner(System.in);

		System.out.println("Please enter your GitHub username");
		String username = scanner.nextLine();
		System.out.println("Please enter your GitHub password");
		String password = scanner.nextLine();
		controller = new Controller("src/main/resources/libraries/", username, password);

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

			for (Entry<String, String> r : recommendations.entrySet()) {
				System.out.println("We recommend that you map:\n " + r.getKey() + "\n" + r.getValue() + "\n");
			}
		}
		scanner.close();
	}
}
