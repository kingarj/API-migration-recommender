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
		ArrayList<Mapping> recommendations = new ArrayList<Mapping>();
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		for (SearchCommit searchCommit : searchCommitResponse.items) {
			HttpGet commitRequest = vcg.buildCommitRequestBody(searchCommit.url);
			HttpResponse commitResponse = vcg.executeHttpRequest(commitRequest);
			Commit commit = cs.createNewCommit(commitResponse);
			for (ChangeFile file : commit.files) {
				if (file.mappings != null) { 
					recommendations.addAll(file.mappings);
				}
			}
		}
		return recommendations;
	}

}
