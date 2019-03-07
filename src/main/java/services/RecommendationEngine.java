package services;

import java.util.ArrayList;
import java.util.HashMap;

import domain.Commit;
import domain.Mapping;

public class RecommendationEngine {
	
	CommitService commitService = new CommitService();

	public HashMap<String, String> mergeMappings(Commit[] commits) {
		HashMap<String, String> recommendations = new HashMap<String, String>();
		ArrayList<Mapping> multiMap = new ArrayList<Mapping>();
		for (Commit c : commits) {
			multiMap.addAll(commitService.mergeFileMappingCandidates(c));
		}
		return recommendations;
	}

}
