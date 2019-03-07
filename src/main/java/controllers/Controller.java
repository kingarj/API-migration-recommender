package controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import domain.ChangeFile;
import domain.Commit;
import domain.Mapping;
import domain.SearchCommit;
import domain.SearchCommitResponse;
import services.CommitService;
import services.SearchCommitService;

public class Controller {

	VersionControlGateway vcg = new VersionControlGateway();
	SearchCommitService scs = new SearchCommitService();
	CommitService cs = new CommitService();

	public Controller() {

	}

	public ArrayList<Mapping> generateRecommendations(String source, String target) throws URISyntaxException, ClientProtocolException, IOException {
		/**
		 * Given the names of a source and target API, this method retrieves relevant commits that have migrated between the two.
		 * It uses these commits to generate recommendations to migrate the client application between the two.
		 * It returns an ArrayList of recommended mappings.
		 */
		ArrayList<Mapping> recommendations = new ArrayList<Mapping>();
		
		// build the request body to get search commits from the name of the source and target APIs
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		
		// handle response and map to object
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		
		// loop the commits retrieved
		for (SearchCommit searchCommit : searchCommitResponse.items) {
			
			// build the request body for each commit to retrieve the patch
			HttpGet commitRequest = vcg.buildCommitRequestBody(searchCommit.url);
			HttpResponse commitResponse = vcg.executeHttpRequest(commitRequest);
			
			// map to a new Commit object including generating Cartesian mappings from the patch
			Commit commit = cs.createNewCommit(commitResponse);
			
			// add the mappings to the recommendations array
			for (ChangeFile file : commit.files) {
				
				if (file.mappings != null) {
					recommendations.addAll(file.mappings);
				}
			}
		}
		
		return recommendations;
	}

}
