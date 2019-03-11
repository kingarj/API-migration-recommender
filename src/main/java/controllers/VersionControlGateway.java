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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VersionControlGateway {

	CloseableHttpClient httpclient; 
	private static Logger logger = LoggerFactory.getLogger(VersionControlGateway.class);


	public VersionControlGateway() {
		this.httpclient = HttpClients.createDefault();
	}

	public HttpResponse executeHttpRequest(HttpGet request) throws ClientProtocolException, IOException {
		logger.debug("executing HTTP request with {}", request.getRequestLine().toString());
		HttpResponse response = httpclient.execute(request);
		logger.debug("HTTP request sent and response received");
		handleResponse(response);
		return response;
	}

	public HttpGet buildSearchCommitRequestBody(String source, String target) throws URISyntaxException {
		String query = source + " " + target;
		URI uri = new URIBuilder().setScheme("https").setHost("api.github.com").setPath("/search/commits")
				.setParameter("q", query).build();
		HttpGet request = new HttpGet(uri);
		request.setHeader(HttpHeaders.ACCEPT, "application/vnd.github.cloak-preview");
		return request;
	}

	public HttpGet buildCommitRequestBody(String commitUrl) throws URISyntaxException {
		HttpGet request = new HttpGet(commitUrl);
		return request;
	}

	public void handleResponse(HttpResponse response) throws ClientProtocolException {
		StatusLine statusLine = response.getStatusLine();
		HttpEntity entity = response.getEntity();
		if (statusLine.getStatusCode() >= 300) {
			throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
		}
		if (entity == null) {
			throw new ClientProtocolException("Response contains no content");
		}
	}
}
