package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.ChangeFile;
import domain.Commit;
import domain.CommitResponse;
import domain.Mapping;
import util.UtilityMethods;

public class CommitService {
	
	Logger logger = LoggerFactory.getLogger(CommitService.class);

	public Commit createNewCommit(HttpResponse response, String source, String target) throws UnsupportedOperationException, IOException {		
		// create CommitResponse from HttpResponse
		HttpEntity entity = response.getEntity();
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = new InputStreamReader(entity.getContent(), charset);
		CommitResponse commitResponse = gson.fromJson(reader, CommitResponse.class);
		
		// attach the change-set to the new Commit
		Commit commit = commitResponse.commit;
		commit.files = commitResponse.files;
		
		// generate the candidate mappings for each file
		for (ChangeFile file : commit.files) {
			setMappings(file);
			sanitiseMappings(file, source, target);
		}
		
		return commit;
	}
	
	
	void sanitiseMappings(ChangeFile file, String source, String target) {
		/**
		 * Given a ChangeFile with mappings, remove the application context and replace with {} characters
		 */
		String[] sourceClasses = new String[] {};
		String[] targetClasses = new String[] {};
		try {
			sourceClasses = UtilityMethods.readFile(source).split(",");
			targetClasses = UtilityMethods.readFile(target).split(",");			
		}
		catch (IOException e) {
			this.logger.debug("Source or target API listing file not found");
		}
		
		String replaceChar = "***";
		if (file.mappings == null) {
			setMappings(file);
		}
		else {
			for (Mapping m : file.mappings) {
				ArrayList<Integer> protectIndices = new ArrayList<Integer>();
				String newString = "";
				// sanitise source
				for (String s : sourceClasses) {
					int i = m.source.indexOf(s);
					if (i > -1) {
						protectIndices.add(i);
						protectIndices.add(i+s.length());						
					}
				}
				for (int i = 0; i < protectIndices.size(); i ++) {
					if (i == protectIndices.size() - 1) {
						if (i < m.source.length()) {
							newString += replaceChar;							
						}
					}
					else if (i == 0 && protectIndices.get(i) == 0) {
						newString += m.source.substring(protectIndices.get(i), protectIndices.get(i+1));
					}
					else if (i == 0 && protectIndices.get(i) != 0) {
						newString += replaceChar;
					}
					else if (i % 2 == 0) {
						newString += m.source.substring(protectIndices.get(i), protectIndices.get(i+1));
					}
					else {
						newString += replaceChar;
					}
				}
				m.source = newString;
			}
		}
	}


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

	public void setMappings(ChangeFile file) {
		/**
		 * create a list of potential mappings by taking the Cartesian product of a text change-set
		 * ChangeFile file : the file containing the change-set
		 * Returns the file with the new candidate mappings
		 */
		// if there is no patch, return early
		if (file.patch != null) {
			
			String encoded_patch = file.patch.replace("\\n", "newline").replace("\n", "newline");
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
			file.mappings = mappings;
		}
	}
	
	public ArrayList<Mapping> mergeTwoMappingLists(ArrayList<Mapping> one, ArrayList<Mapping> two) {
		/**
		 * Given two ArrayLists of Mapping objects, merge the second into the first and return the latter
		 */
		// if one and two are both populated, then merge two into one
		if (one != null && one.size() > 0 && two != null && two.size() > 0) {
			for (Mapping m1 : one) {
				for (Mapping m2 : two) {
					if (m1.source.equals(m2.source)) {
						m2.targets.forEach((k, v) -> m1.targets.merge(k, v, Integer::sum));
					}
				}
			}
		}
		
		// if one is empty then add two's list
		else if (one != null && one.size() == 0 && two != null && two.size() > 0) {
			one.addAll(two);
		}
		
		return one;
					
	}

	public ArrayList<Mapping> mergeFileMappingCandidates(Commit commit) {
		ArrayList<Mapping> mergedList = new ArrayList<Mapping>();
		for (ChangeFile f : commit.files) {
			mergedList = mergeTwoMappingLists(mergedList, (ArrayList<Mapping>) f.mappings);
		}
		return mergedList;
	}

}
