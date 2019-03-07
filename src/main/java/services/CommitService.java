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

	public ArrayList<Mapping> mergeFileMappingCandidates(Commit commit) {
		ArrayList<Mapping> mergedList = new ArrayList<Mapping>();
		for (ChangeFile f : commit.files) {
			if (mergedList.size() == 0) {
				mergedList.addAll(f.mappings);
			}
			else {
				for (Mapping mL : mergedList) {
					if (f.mappings != null) {
						for (Mapping ma : f.mappings) {
							if (mL.source.equals(ma.source)) {
								mL.targets.forEach((k, v) -> ma.targets.merge(k, v, Integer::sum));
							}
						}						
					}
				}
			}
		}
		return mergedList;
	}

}
