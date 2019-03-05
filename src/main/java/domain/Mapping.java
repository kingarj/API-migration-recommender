package domain;

import java.util.HashMap;

public class Mapping {
	public String source;
	public HashMap<String, Integer> targets;
	
	public Mapping(String source, String target) {
		this.source = source;
		this.targets = new HashMap<String, Integer>();
		this.targets.put(target, 1);
	}

	public void map(String target) {
		if (this.targets.containsKey(target)) {
			targets.put(target, targets.get(target) + 1);
		}
		else {
			targets.put(target, 1);
		}
		
	}

	public String getMostFrequentMapping() {
		String mostFrequent = "";
		Integer highestSeen = 0;
		for (String s : targets.keySet()) {
			if (targets.get(s) > highestSeen) {
				mostFrequent = s;
				highestSeen = targets.get(s);
			}
		}
		return mostFrequent;
	}
}
