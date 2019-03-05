package services;

import java.io.IOException;

import domain.SearchCommit;
import util.UtilityMethods;

public class FiltrationService {

	public Boolean checkMessageIsRelevant(SearchCommit searchCommit) {
		String fileText = null;
		try {
			fileText = UtilityMethods.readFile("src/main/resources/relevant.txt");
			fileText = fileText.replace("\r\n", "");
			String[] relevantWords = fileText.split(", ");
			for (String word : relevantWords) {
				if (searchCommit.commit.message.contains(word)) {
					return true;
				}
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
		return false;
	}

}
