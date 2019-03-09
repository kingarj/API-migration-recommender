package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import domain.Commit;
import domain.Mapping;

public class RecommendationEngine {
	
	CommitService commitService = new CommitService();

	public HashMap<String, String> mergeMappings(Commit[] commits) {
		HashMap<String, String> recommendations = new HashMap<String, String>();
		ArrayList<Mapping> multiMap = new ArrayList<Mapping>();
		for (Commit c : commits) {
			ArrayList<Mapping> commitMappings = commitService.mergeFileMappingCandidates(c);
			multiMap = commitService.mergeTwoMappingLists(multiMap, commitMappings);
		}
		for (Mapping m : multiMap) {
			Map.Entry<String, Integer> maxEntry = null;

			for (Map.Entry<String, Integer> entry : m.targets.entrySet())
			{
			    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
			    {
			        maxEntry = entry;
			    }
			}
			recommendations.put(m.source, maxEntry.getKey());
		}
		return recommendations;
	}

}
