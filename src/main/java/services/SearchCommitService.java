package services;

import java.util.ArrayList;
import java.util.stream.Collectors;

import domain.SearchCommit;

public class SearchCommitService {

	FiltrationService filtrationService = new FiltrationService();

	public ArrayList<SearchCommit> filterIrrelevantSearchCommits(ArrayList<SearchCommit> items) {
		return (ArrayList<SearchCommit>) items.stream().filter(x -> filtrationService.checkMessageIsRelevant(x))
				.collect(Collectors.toList());
	}

}
