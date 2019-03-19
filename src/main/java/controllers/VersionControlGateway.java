package controllers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.CommitResponse;

public class VersionControlGateway {

	CloseableHttpClient httpclient;
	HttpGet currentRequest;
	private static Logger logger = LoggerFactory.getLogger(VersionControlGateway.class);
	public CommitCacheInterface cci = new CommitCacheInterface("src/main/resources/cache/");

	public VersionControlGateway() {
		this.httpclient = HttpClients.createDefault();
	}

	public CommitResponse getResponseClass(HttpResponse response) {

		// create CommitResponse from HttpResponse
		HttpEntity entity = response.getEntity();
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = null;
		try {
			reader = new InputStreamReader(entity.getContent(), charset);
		} catch (UnsupportedOperationException | IOException e) {
			logger.debug(e.getMessage());
		}
		return gson.fromJson(reader, CommitResponse.class);
	}

	public CommitResponse getCommit(String commitUrl, String sha) {
		// cache stuff
		try {
			return cci.loadCommit(sha);
		} catch (IOException e1) {
			logger.debug("Commit does not exist in the cache");
			logger.debug("Building commit request with {} URL", commitUrl);
			try {
				// build the request body for each commit to retrieve the patch
				HttpGet commitRequest = buildCommitRequestBody(commitUrl);
				logger.debug("commit request built");
				HttpResponse response = executeHttpRequest(commitRequest);
				CommitResponse commitResponse = getResponseClass(response);

				// save in the cache for later
				cci.cacheCommit(sha, commitResponse);
				return commitResponse;
			} catch (URISyntaxException | IOException e2) {
				logger.debug(e2.getMessage());
				return null;
			}

		}
	}

	public HttpResponse executeHttpRequest(HttpGet request) throws ClientProtocolException, IOException {
		logger.debug("executing HTTP request with {}", request.getRequestLine().toString());
		currentRequest = request;
		HttpResponse response = httpclient.execute(request);
		logger.debug("Commit request sent and response received with status code {}", response.getStatusLine());

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
