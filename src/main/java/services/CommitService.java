package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.ChangeFile;
import domain.Commit;
import domain.CommitResponse;
import domain.Mapping;
import util.UtilityMethods;

public class CommitService {

	Logger logger = LoggerFactory.getLogger(CommitService.class);

	public Commit createNewCommit(CommitResponse commitResponse, String source, String target)
			throws UnsupportedOperationException, IOException {

		// attach the change-set to the new Commit
		Commit commit = commitResponse.commit;
		commit.files = commitResponse.files;

		// generate the candidate mappings for each file
		for (ChangeFile file : commit.files) {
			setMappings(file);
			if (file.mappings != null && !file.mappings.isEmpty()) {
				sanitiseMappings(file, source, target);
			}
		}

		return commit;
	}

	@SuppressWarnings("unused")
	void sanitiseMappings(ChangeFile file, String sourceFile, String targetFile) {
		/**
		 * Given a ChangeFile with mappings, remove the application context and replace
		 * with {} characters
		 */
		HashMap<String, String[]> sourceLib = new HashMap<String, String[]>();
		HashMap<String, String[]> targetLib = new HashMap<String, String[]>();
		String[] syntaxTokens = new String[] {};

		logger.debug("sanitising mappings for {}", file.filename);

		// read library structures and language tokens from file
		try {
			sourceLib = generateLibrary(sourceFile);
			targetLib = generateLibrary(targetFile);
			syntaxTokens = UtilityMethods.readFile("src/main/resources/syntax_tokens.txt").split("\r\n");
		} catch (IOException e) {
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
			// keep a copy of the mappings to be removed
			ArrayList<Mapping> toBeRemoved = new ArrayList<Mapping>();
			for (Mapping m : file.mappings) {
				logger.debug("the old source is: {}", m.source);
				// generate the indices of characters to preserve in the source LOC
				ArrayList<Integer> sourceIndices = generateProtectedIndices(sourceLib, m.source, syntaxTokens);
				logger.debug("the source indices are: {}", sourceIndices != null ? sourceIndices.toString() : "empty");

				if (sourceIndices == null) {
					toBeRemoved.add(m);
				} else {
					m.source = replaceContextualSyntax(sourceIndices, m.source);
					logger.debug("the new source is {}", m.source);

					HashMap<String, Integer> newTargets = new HashMap<String, Integer>();
					for (String target : m.targets.keySet()) {
						logger.debug("the old target is {}", target);
						// generate the indices of characters to preserve in the target LOCs
						ArrayList<Integer> targetIndices = generateProtectedIndices(targetLib, target, syntaxTokens);
						logger.debug("the target indices are {}",
								targetIndices != null ? targetIndices.toString() : "empty");
						if (targetIndices != null) {
							String sanitisedTarget = replaceContextualSyntax(targetIndices, target);
							logger.debug("the new target is {}", sanitisedTarget);

							// put a new entry in targets with the sanitised target string
							Integer frequency = m.targets.get(target);
							newTargets.put(sanitisedTarget, frequency);
						}
					}
					m.targets = newTargets;
				}
			}
			file.mappings.removeAll(toBeRemoved);
		}
	}

	private ArrayList<Integer> generateProtectedIndices(HashMap<String, String[]> library, String loc,
			String[] syntaxTokens) {
		ArrayList<Integer> protectedIndices = new ArrayList<Integer>();

		// identify tokens from the source library
		for (Entry<String, String[]> s : library.entrySet()) {
			// identify any Classes present in the line
			String key = s.getKey();
			int i = loc.indexOf(key);
			while (i >= 0) {
				int classlen = i + key.length();
				String before = i > 0 ? loc.substring(i - 1, i) : "";
				String before2 = i > 1 ? loc.substring(i - 2, i) : "";
				// if the class is at the end of the line then set the after string to be null
				String after = classlen == loc.length() ? null : loc.substring(classlen, classlen + 1);
				logger.debug("char before {} is {}", key, before);
				logger.debug("2 chars before {} is {}", key, before2);
				logger.debug("char after {} is {}", key, after);
				if ((i == 0 || before.equals(" ") || before.equals("(") || before.equals("\t") || before2.equals("\\t")
						|| before2.equals("\t") || before2.equals("\t\t"))
						&& (classlen == loc.length() || after.equals("(") || after.equals(".") || after.equals(" "))
						|| loc.indexOf("@") > -1) {
					protectedIndices.add(i);
					protectedIndices.add(classlen);
					logger.debug("we have found {} in {}", key, loc);
					logger.debug("protected indices are: {}", protectedIndices.toString());
				}
				i = loc.indexOf(s.getKey(), i + 1);
			}
			// loop attributes and methods
			for (String ams : s.getValue()) {
				if (ams.isEmpty()) {
					continue;
				}
				int j = loc.indexOf(ams);
				// if j is -1 then the string isn't present and if it is 0 then it is invalid
				while (j > 0) {
					int amslen = j + ams.length();
					// ensure that this is the full method or attribute by checking surrounding
					// syntax
					if (loc.substring(j - 1, j).equals(".")
							&& (amslen == loc.length() || loc.substring(amslen, amslen + 1).equals("(")
									|| loc.substring(amslen, amslen + 1).equals(";"))) {
						protectedIndices.add(j);
						protectedIndices.add(j + ams.length());
					}
					j = loc.indexOf(ams, j + 1);
				}
			}
		}

		// if we haven't found any matches at this point return null; the LOC is
		// irrelevant
		if (protectedIndices.isEmpty() && loc.indexOf("import") == -1 && loc.indexOf("@") == -1) {
			return null;
		}
		// make sure at this point we don't have any duplicate keys
		else {
			Set<Integer> set = new LinkedHashSet<>();
			set.addAll(protectedIndices);
			protectedIndices.clear();
			protectedIndices.addAll(set);
		}

		// identify tokens of Java to be ignored
		Boolean clearProtectedIndices = false;
		for (String s : syntaxTokens) {
			int i = loc.indexOf(s);
			while (i >= 0) {
				// we want to preserve import statements in their entirety
				if (s.equals("import")) {
					clearProtectedIndices = true;
				} else {
					protectedIndices.add(i);
					protectedIndices.add(i + s.length());
				}
				i = loc.indexOf(s, i + 1);
			}
		}

		// clear the protected indices if it is flagged to protect the whole statement
		if (clearProtectedIndices) {
			protectedIndices.clear();
		}

		return protectedIndices;
	}

	private String replaceContextualSyntax(ArrayList<Integer> protectIndices, String loc) {
		/**
		 * returns a String where the application context has been replaced by a
		 * sequence of three asterisks
		 */
		String newString = "";
		String replaceChar = "***";

		// filter out the repeated integers from the index list using a set
//		Set<Integer> uniqueIndices = new HashSet<>(protectIndices);
//		protectIndices.clear();
//		protectIndices.addAll(uniqueIndices);
		Collections.sort(protectIndices);

		for (int i = 0; i < protectIndices.size(); i++) {
			Integer index = protectIndices.get(i);
			if (i == protectIndices.size() - 1) {
				if (index < loc.length()) {
					newString += replaceChar;
				}
			} else if (i == 0) {
				if (index != 0) {
					newString += replaceChar;
				}
				newString += loc.substring(index, protectIndices.get(i + 1));
			} else if (i % 2 == 0) {
				newString += loc.substring(index, protectIndices.get(i + 1));
			} else if (index == protectIndices.get(i + 1) || index == protectIndices.get(i + 1) + 1) {
				continue;
			} else {
				newString += replaceChar;
			}
		}
		return newString.length() > 0 ? newString : loc;
	}

	private HashMap<String, String[]> generateLibrary(String file) throws IOException {
		/***
		 * Create a HashMap representing a library from a source code where: the class
		 * is on the LHS, separated from its attributes and methods by a colon (:) the
		 * attributes and methods are comma-separated values on the RHS of a colon (:)
		 */
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
				} else {
					className = segmentedClass[0];
					attrsMethods = segmentedClass[1];
				}
			} else {
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
		} else {
			Mapping mapping = new Mapping(source, target);
			mappings.add(mapping);
		}
		return mappings;
	}

	public void setMappings(ChangeFile file) {
		/**
		 * create a list of potential mappings by taking the Cartesian product of a text
		 * change-set ChangeFile file : the file containing the change-set Returns the
		 * file with the new candidate mappings
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
				String diff = "";
				if (line.length() > 0) {
					diff = line.substring(0, 1);
				} else {
					continue;
				}
				if (diff.equals("-")) {
					// this is a deleted line
					map = true;
					String source = line.substring(1, line.length());
					// if we know the target then create a new mapping
					if (target != null) {
						mappings = updateMappings(mapped, mappings, source, target);
						mapped.add(source);
					}
					// otherwise deal with it once we know the target
					else {
						tbd.add(source);
					}
				} else if (diff.equals("+")) {
					// set the target to be the added line
					target = line.substring(1, line.length());
					if (map) {
						// map any substituted lines already identified
						for (String source : tbd) {
							mappings = updateMappings(mapped, mappings, source, target);
							mapped.add(source);
						}
					}
				} else {
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
		 * Given two ArrayLists of Mapping objects, merge the second into the first and
		 * return the latter
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
