package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;

public class VersionControlGatewayTest {
	
	String source;
	String target;
	VersionControlGateway vcg;
	
	@Before
	public void init() {
		source = "test1";
		target = "test2";
		vcg = new VersionControlGateway();
	}
	
	@Test
	public void canInstantiateVersionControlGateway() {
		VersionControlGateway testGateway = new VersionControlGateway();
		assertNotNull(testGateway);
	}
	
	@Test
	public void canBuildSearchCommitRequestBody() throws ClientProtocolException, URISyntaxException, IOException {
		HttpGet request = vcg.buildSearchCommitRequestBody(this.source, this.target);
		String uri = "https://api.github.com/search/commits?q=test1+test2";
		assertNotNull(request);
		assertEquals(request.getURI().toString(), uri);
	}
	
	@Test
	public void canHandleResponse() {
		StringEntity entity = new StringEntity("important message",
		        ContentType.create("plain/text", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 
			    HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		try {
			vcg.handleResponse(response);	
		} catch (ClientProtocolException e) {
			assertTrue(false);
		}
	}
	
	@Test
	public void canBuildSoleCommitRequestBody() throws URISyntaxException {
		String owner = "owner";
		String repo = "repo";
		String sha = "sha123";
		String uri = "https://api.github.com/repos/owner/repo/commits/sha123";
		HttpGet request = vcg.buildCommitRequestBody(owner, repo, sha );
		assertNotNull(request);
		assertEquals(request.getURI().toString(), uri);
	}

}
