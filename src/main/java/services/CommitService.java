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
			file = parsePatchIntoMappings(file);
		}
		
		return commit;
	}

	public ChangeFile parsePatchIntoMappings(ChangeFile file) {
		/**
		 * create a list of potential mappings by taking the Cartesian product of a text change-set
		 * ChangeFile file : the file containing the change-set
		 * Returns the file with the new candidate mappings
		 */
		// if there is no patch, return early
		if (file.patch == null) {
			return file;
		}
		
		String encoded_patch = file.patch.replace("\\n", "newline");
		String[] patch_arr = encoded_patch.split("newline");
		List<Mapping> mappings = new ArrayList<Mapping>();

		Boolean map = false; // flags a potential mapping
		String target = null; // tracks the current target
		ArrayList<String> tbd = new ArrayList<String>(); // tracks any as-yet unmapped deletions
		
		for (String line : patch_arr) {
			String diff = line.substring(0,1);
			if (diff.equals("-")) {
				// this is a deleted line
				map = true;
				Mapping mapping;
				// if we know the target then create a new mapping
				if (target != null) {
					mapping = new Mapping(line.substring(1,line.length()), target);
					mappings.add(mapping);
				}
				// otherwise deal with it once we know the target
				else {
					tbd.add(line);
				}
			}
			else if (diff.equals("+")) {
				// set the target to be the added line
				target = line.substring(1,line.length());
				if (map) {
					// map any substituted lines already identified
					for (String source : tbd) {
						Mapping mapping = new Mapping(source.substring(1,source.length()), target);
						mappings.add(mapping);
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
		return file;
	}

}
