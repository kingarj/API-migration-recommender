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
		vcg = new VersionControlGateway("test", "test");
	}

	@Test
	public void canInstantiateVersionControlGateway() {
		VersionControlGateway testGateway = new VersionControlGateway("test", "test");
		assertNotNull(testGateway);
	}

	@Test
	public void canBuildSearchCommitRequestBody() throws ClientProtocolException, URISyntaxException, IOException {
		HttpGet request = vcg.buildSearchCommitRequestBody("1", this.source, this.target);
		String uri = "https://api.github.com/search/commits?q=test1+test2&page=1&per_page=100";
		assertNotNull(request);
		assertEquals(request.getURI().toString(), uri);
	}

	@Test
	public void canHandleResponse() {
		StringEntity entity = new StringEntity("important message", ContentType.create("plain/text", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		try {
			vcg.handleResponse(response);
		} catch (ClientProtocolException e) {
			assertTrue(false);
		}
	}

	@Test
	public void canBuildSoleCommitRequestBody() throws URISyntaxException {
		String uri = "https://api.github.com/repos/owner/repo/commits/sha123";
		HttpGet request = vcg.buildCommitRequestBody(uri);
		assertNotNull(request);
		assertEquals(request.getURI().toString(), uri);
	}

}
