package controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import domain.Commit;
import domain.SearchCommit;
import domain.SearchCommitResponse;
import services.CommitService;
import services.SearchCommitService;

public class Controller {

	VersionControlGateway vcg;
	SearchCommitService scs;
	CommitService cs;

	public Controller() {
		this.vcg = new VersionControlGateway();
		this.scs = new SearchCommitService();
		this.cs = new CommitService();
	}

	public String[] generateRecommendations(String source, String target) throws URISyntaxException, ClientProtocolException, IOException {
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		for (SearchCommit searchCommit : searchCommitResponse.items) {
			HttpGet commitRequest = vcg.buildCommitRequestBody(searchCommit.url);
			HttpResponse commitResponse = vcg.executeHttpRequest(commitRequest);
			Commit commit = cs.createNewCommit(commitResponse);
		}
		return null;
	}

}
