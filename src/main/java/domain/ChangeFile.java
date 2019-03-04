package domain;

import java.util.ArrayList;
import java.util.List;

public class ChangeFile {
	public String sha;
	public String filename;
	public String status;
	public Integer additions;
	public Integer deletion;
	public Integer changes;
	public String patch;
	public List<Mapping> mappings; 
	
	public List<Mapping> updateMappings(List<String> mapped, List<Mapping> mappings, String source, String target) {
		if (mapped.contains(source)) {
			// loop mappings to find object containing source
			for (Mapping m : mappings) {
				if (m.source.equals(source)) {
					m.map(target);
				}
			}
		}
		else {
			Mapping mapping = new Mapping(source, target);
			mappings.add(mapping);
		}
		return mappings;
	}

	public void setMappings() {
		/**
		 * create a list of potential mappings by taking the Cartesian product of a text change-set
		 * ChangeFile file : the file containing the change-set
		 * Returns the file with the new candidate mappings
		 */
		// if there is no patch, return early
		if (patch != null) {
			
			String encoded_patch = patch.replace("\\n", "newline").replace("\n", "newline");
			String[] patch_arr = encoded_patch.split("newline");
			List<Mapping> mappings = new ArrayList<Mapping>();
			// keep a list of mapped deletions to make tracking multiple mappings cheaper
			List<String> mapped = new ArrayList<String>();
	
			Boolean map = false; // flags a potential mapping
			String target = null; // tracks the current target
			ArrayList<String> tbd = new ArrayList<String>(); // tracks any as-yet unmapped deletions
			
			for (String line : patch_arr) {
				String diff = line.substring(0,1);
				if (diff.equals("-")) {
					// this is a deleted line
					map = true;
					String source = line.substring(1,line.length());
					// if we know the target then create a new mapping
					if (target != null) {
						mappings = updateMappings(mapped, mappings, source, target);
						mapped.add(source);
					}
					// otherwise deal with it once we know the target
					else {
						tbd.add(source);
					}
				}
				else if (diff.equals("+")) {
					// set the target to be the added line
					target = line.substring(1,line.length());
					if (map) {
						// map any substituted lines already identified
						for (String source : tbd) {
							mappings = updateMappings(mapped, mappings, source, target);
							mapped.add(source);
						}
					}
				}
				else {
					// mapping period over, reset variables
					map = false;
					target = null;
					tbd = new ArrayList<String>();
				}
			}
			this.mappings = mappings;
		}
	}
}
