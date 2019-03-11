package controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger logger = LoggerFactory.getLogger(Controller.class);

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
		logger.debug("Building search commit request");
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		logger.debug("Search commit request executed with status code {}", response.getStatusLine());
		
		// handle response and map to object
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		logger.debug("Search commit response handled");
		logger.info("Total commits found: {}", searchCommitResponse.total_count);
		
		// loop the commits retrieved
		for (SearchCommit searchCommit : searchCommitResponse.items) {
			
			logger.debug("Building commit request with {} URL", searchCommit.url);
			// build the request body for each commit to retrieve the patch
			HttpGet commitRequest = vcg.buildCommitRequestBody(searchCommit.url);
			logger.debug("commit request built");
			HttpResponse commitResponse = vcg.executeHttpRequest(commitRequest);
			logger.debug("Commit request executed with status code {}", commitResponse.getStatusLine());
			
			// map to a new Commit object including generating Cartesian mappings from the patch
			Commit commit = cs.createNewCommit(commitResponse);
			
			//release the connection from the request object
			commitRequest.releaseConnection();
			
			logger.debug("processing: {}", commit.message);
			
			// add the mappings to the recommendations array
			for (ChangeFile file : commit.files) {
				
				logger.debug("getting mappings for: {}", file.filename);
				
				if (file.mappings != null) {
					logger.debug("mappings are being added");
					recommendations.addAll(file.mappings);
				}
			}
		}
		
		return recommendations;
	}

}
