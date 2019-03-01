package controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class VersionControlGateway {
	
	CloseableHttpClient httpclient;
		
	public VersionControlGateway() {
		this.httpclient = HttpClients.createDefault();
	}
	
	public HttpResponse executeHttpRequest(HttpGet request) throws ClientProtocolException, IOException {
		HttpResponse response = httpclient.execute(request);
		handleResponse(response);
		return response;
	}
	
	public HttpGet buildSearchCommitRequestBody(String source, String target) throws URISyntaxException {
		String query = source + " " + target;
		URI uri = new URIBuilder().setScheme("https").setHost("api.github.com").setPath("/search/commits").setParameter("q", query).build();
		HttpGet request = new HttpGet(uri);
		request.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.cloak-preview");
		return request;
	}

	public HttpGet buildCommitRequestBody(String owner, String repo, String sha) throws URISyntaxException {
		URI uri = new URIBuilder().setScheme("https").setHost("api.github.com").setPath("/repos/" + owner + "/" + repo + "/commits/" + sha).build();
		HttpGet request = new HttpGet(uri);
		return request;
	}
	
	public void handleResponse(HttpResponse response) throws ClientProtocolException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(
                    statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
	}
}
