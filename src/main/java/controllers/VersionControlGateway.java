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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.CommitResponse;
import domain.SearchCommitResponse;

public class VersionControlGateway {

	CloseableHttpClient httpclient;
	HttpGet currentRequest;
	private static Logger logger = LoggerFactory.getLogger(VersionControlGateway.class);
	public CommitCacheInterface cci = new CommitCacheInterface("src/main/resources/cache/");
	Gson gson = new GsonBuilder().create();

	public VersionControlGateway(String username, String password) {
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		provider.setCredentials(AuthScope.ANY, credentials);

		this.httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
	}

	public Reader getResponseReader(HttpResponse response) {

		// create CommitResponse from HttpResponse
		HttpEntity entity = response.getEntity();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = null;
		try {
			reader = new InputStreamReader(entity.getContent(), charset);
		} catch (UnsupportedOperationException | IOException e) {
			logger.debug(e.getMessage());
		}
		return reader;
	}

	public CommitResponse getCommit(String commitUrl, String sha) throws HttpResponseException {
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
				Reader reader = getResponseReader(response);
				CommitResponse commitResponse = gson.fromJson(reader, CommitResponse.class);

				// save in the cache for later
				cci.cacheCommit(sha, commitResponse);
				return commitResponse;
			} catch (URISyntaxException | IOException e2) {
				logger.debug(e2.getMessage());
				if (e2.getMessage().indexOf("403") > -1) {
					throw new HttpResponseException(403, "API limit reached");
				}
				return null;
			}

		}
	}

	public SearchCommitResponse getSearchCommitByPage(String page, String source, String target)
			throws HttpResponseException {
		logger.debug("Building search commit request");
		HttpGet request;
		try {
			request = buildSearchCommitRequestBody(page, source, target);
			HttpResponse response = executeHttpRequest(request);
			logger.debug("Search commit request executed with status code {}", response.getStatusLine());

			Reader reader = getResponseReader(response);
			SearchCommitResponse searchCommitResponse = gson.fromJson(reader, SearchCommitResponse.class);
			request.releaseConnection();

			logger.debug("Checking all commits have been retrieved from search commit");

			return searchCommitResponse;
		} catch (URISyntaxException | IOException e) {
			logger.debug(e.getMessage());
			if (e.getMessage().indexOf("403") > -1) {
				throw new HttpResponseException(403, "API limit reached");
			}
			return null;
		}
	}

	public SearchCommitResponse getSearchCommit(String source, String target) throws HttpResponseException {
		int pageCounter = 1;
		SearchCommitResponse searchCommitResponse = getSearchCommitByPage(Integer.toString(pageCounter), source,
				target);
		int remainingCommits = searchCommitResponse.total_count - 100;
		while (remainingCommits > 0) {
			pageCounter += 1;
			SearchCommitResponse newResponse = getSearchCommitByPage(Integer.toString(pageCounter), source, target);
			searchCommitResponse.items.addAll(newResponse.items);
			remainingCommits -= 100;
		}
		return searchCommitResponse;
	}

	public HttpResponse executeHttpRequest(HttpGet request) throws ClientProtocolException, IOException {
		logger.debug("executing HTTP request with {}", request.getRequestLine().toString());
		currentRequest = request;
		HttpResponse response = httpclient.execute(request);
		logger.debug("Commit request sent and response received with status code {}", response.getStatusLine());

		handleResponse(response);
		return response;
	}

	public HttpGet buildSearchCommitRequestBody(String page, String source, String target) throws URISyntaxException {
		String query = source + " " + target;
		URI uri = new URIBuilder().setScheme("https").setHost("api.github.com").setPath("/search/commits")
				.setParameter("q", query).setParameter("page", page).setParameter("per_page", "100").build();
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
