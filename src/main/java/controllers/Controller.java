package controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import domain.SearchCommitResponse;
import services.SearchCommitService;

public class Controller {

	VersionControlGateway vcg;
	SearchCommitService scs;

	public Controller() {
		this.vcg = new VersionControlGateway();
		this.scs = new SearchCommitService();
	}

	public String[] generateRecommendations(String source, String target) throws URISyntaxException, ClientProtocolException, IOException {
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);
		return null;
	}

}
