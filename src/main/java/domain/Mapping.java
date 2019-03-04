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
}
