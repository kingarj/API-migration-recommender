package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.ChangeFile;
import domain.Commit;
import domain.CommitResponse;
import domain.Mapping;

public class CommitService {

	public Commit createNewCommit(HttpResponse response) throws UnsupportedOperationException, IOException {		
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
			file.setMappings();
		}
		
		return commit;
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
