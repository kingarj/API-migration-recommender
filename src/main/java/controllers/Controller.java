package controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import domain.Commit;
import domain.CommitResponse;
import domain.SearchCommit;
import domain.SearchCommitResponse;
import services.CommitService;
import services.RecommendationEngine;
import services.SearchCommitService;
import util.UtilityMethods;

public class Controller {

	VersionControlGateway vcg = new VersionControlGateway();
	SearchCommitService scs = new SearchCommitService();
	CommitService cs = new CommitService();
	RecommendationEngine re = new RecommendationEngine();
	private static Logger logger = LoggerFactory.getLogger(Controller.class);
	private String root;

	public Controller(String root) {
		this.root = root;
	}

	public HashMap<String, String> generateRecommendations(String source, String target)
			throws URISyntaxException, ClientProtocolException, IOException {
		/**
		 * Given the names of a source and target API, this method retrieves relevant
		 * commits that have migrated between the two. It uses these commits to generate
		 * recommendations to migrate the client application between the two. It returns
		 * an ArrayList of recommended mappings.
		 */
		String sourceFile = root + source + ".txt";
		String targetFile = root + target + ".txt";

		// if we have no library files for the given libraries, raise an error output
		try {
			UtilityMethods.readFile(sourceFile);
			UtilityMethods.readFile(targetFile);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.debug("Could not find either the source {} or the target {}", sourceFile, targetFile);
			return null;
		}

		// build the request body to get search commits from the name of the source and
		// target APIs
		logger.debug("Building search commit request");
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		logger.debug("Search commit request executed with status code {}", response.getStatusLine());

		// handle response and map to object
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		request.releaseConnection();
		logger.debug("Search commit response handled");
		logger.info("Total commits found: {}", searchCommitResponse.total_count);

		ArrayList<Commit> commits = new ArrayList<Commit>();

		// loop the commits retrieved
		for (SearchCommit searchCommit : searchCommitResponse.items) {

			// get commit patch from github
			CommitResponse commitResponse = vcg.getCommit(searchCommit.url, searchCommit.sha);

			// map to a new Commit object including generating Cartesian mappings from the
			// patch
			Commit commit = cs.createNewCommit(commitResponse, sourceFile, targetFile);
			logger.debug("processed: {}", commit.message);

			// release the connection from the request object if it exists
			if (vcg.currentRequest != null) {
				vcg.currentRequest.releaseConnection();
			}

			commits.add(commit);
		}

		return re.mergeMappings(commits);
	}

}
