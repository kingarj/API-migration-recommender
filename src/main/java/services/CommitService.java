package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
	
	
	void sanitiseMappings(ChangeFile file, String sourceFile, String targetFile) {
		/**
		 * Given a ChangeFile with mappings, remove the application context and replace with {} characters
		 */
		HashMap<String, String[]> sourceLib = new HashMap<String, String[]>();
		HashMap<String, String[]> targetLib = new HashMap<String, String[]>();
		String[] syntaxTokens = new String[] {};
		
		// read library structures and language tokens from file
		try {
			sourceLib = generateLibrary(sourceFile);
			targetLib = generateLibrary(targetFile);
			syntaxTokens = UtilityMethods.readFile("src/main/resources/syntax_tokens.txt").split("\r\n");
		}
		catch (IOException e) {
			this.logger.debug(e.getMessage());
		}
		
		// ensure the mappings of the file are populated
		if (file.mappings == null) {
			setMappings(file);
			// if they are still null, there is nothing to sanitise so return early
			if (file.mappings == null) {
				return;
			}
		}
		
		// loop mappings to sanitise them
		else {
			for (Mapping m : file.mappings) {
				
				// generate the indices of characters to preserve in the source LOC
				ArrayList<Integer> sourceIndices = generateProtectedIndices(sourceLib, m.source, syntaxTokens);
				m.source = replaceContextualSyntax(sourceIndices, m.source);
				for (String target : m.targets.keySet()) {
					
					// generate the indices of characters to preserve in the target LOCs
					ArrayList<Integer> targetIndices = generateProtectedIndices(targetLib, target, syntaxTokens);
					String sanitisedTarget = replaceContextualSyntax(targetIndices, target);
					
					Integer frequency = m.targets.get(target);

					// remove the old target value
					m.targets.remove(target);
					
					// put a new entry in targets with the sanitised target string
					m.targets.put(sanitisedTarget, frequency);
					
				}
			}
		}
	}


	private ArrayList<Integer> generateProtectedIndices(HashMap<String, String[]> library, String loc, String[] syntaxTokens) {
		ArrayList<Integer> protectedIndices = new ArrayList<Integer>();
		
		// identify tokens from the source library
		for (Entry<String, String[]> s : library.entrySet()) {
			int i = loc.indexOf(s.getKey());
			if (i > -1) {
				protectedIndices.add(i);
				protectedIndices.add(i+s.getKey().length());						
			}
			// loop attributes and methods
			for (String ams : s.getValue()) {
				int j = loc.indexOf(ams);
				if (j > -1) {
					protectedIndices.add(j);
					protectedIndices.add(j+ams.length());						
				}
			}
		}
		
		// identify tokens of Java to be ignored
		Boolean clearProtectedIndices = false;
		for (String s : syntaxTokens) {
			int i = loc.indexOf(s);
			if (i > -1) {
				// we want to preserve import statements in their entirety
				if (s.equals("import")) {
					clearProtectedIndices = true;
				}
				else {
					protectedIndices.add(i);
					protectedIndices.add(i+s.length());
				}						
			}
		}
		
		// clear the protected indices if it is flagged to protect the whole statement
		if (clearProtectedIndices) {
			protectedIndices.clear();
		}
		
		return protectedIndices;
	}


	private String replaceContextualSyntax(ArrayList<Integer> protectIndices, String loc) {
		String newString = "";
		String replaceChar = "***";
		Collections.sort(protectIndices);
		
		for (int i = 0; i < protectIndices.size(); i ++) {
			if (i == protectIndices.size() - 1) {
				if (protectIndices.get(i) < loc.length()) {
					newString += replaceChar;							
				}
			}
			else if (i == 0 && protectIndices.get(i) == 0) {
				newString += loc.substring(protectIndices.get(i), protectIndices.get(i+1));
			}
			else if (i == 0 && protectIndices.get(i) != 0) {
				newString += replaceChar;
				newString += loc.substring(protectIndices.get(i), protectIndices.get(i+1));
			}
			else if (i % 2 == 0) {
				newString += loc.substring(protectIndices.get(i), protectIndices.get(i+1));
			}
			else if (protectIndices.get(i) == protectIndices.get(i+1) || protectIndices.get(i) == protectIndices.get(i+1) + 1) {
				continue;
			}
			else {
				newString += replaceChar;
			}
		}
		return newString.length() > 0 ? newString : loc;
	}


	private HashMap<String, String[]> generateLibrary(String file) throws IOException {
		HashMap<String, String[]> newLib = new HashMap<String, String[]>();

		// populate newLib HashMap with classes and their methods and attributes
		String[] classes = UtilityMethods.readFile(file).split("\r\n");
		for (String c : classes) {
			String className = null;
			String attrsMethods = null;
			if (c.indexOf(":") != -1) {
				String[] segmentedClass = c.split(":");
				if (segmentedClass.length != 2) {
					throw new IOException("library file incorrectly configured");
				}
				else {
					className = segmentedClass[0];
					attrsMethods = segmentedClass[1];
				}
			}
			else {
				className = c;
				attrsMethods = "";
			}
			newLib.put(className, attrsMethods.split(","));
		}
		return newLib;
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
