package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import domain.CommitResponse;
import util.UtilityMethods;

public class CommitCacheInterfaceTest {

	VersionControlGateway vcg = new VersionControlGateway();
	String cacheLocation = "src/test/resources/cache/";
	CommitCacheInterface cci = new CommitCacheInterface(cacheLocation);
	Gson gson = new Gson();
	String sha;
	CommitResponse commitResponse;

	@Before
	public void init() throws IOException {

		// set up commit request and response
		sha = "6e199009fee42f8665923181a2f39adddcb92d5a";
		String commitResponseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity commitEntity = new StringEntity(commitResponseStr,
				ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response.setEntity(commitEntity);
		commitResponse = vcg.getResponseClass(response);

	}

	@Test
	public void canInstantiateCommitCacheInterface() {
		CommitCacheInterface cci = new CommitCacheInterface("test");
		assertNotNull(cci);
		assertEquals(cci.cacheLocation, "test");
	}

	@Test
	public void canCacheACommit() {

		cci.cacheCommit(sha, commitResponse);

		try {
			String file = UtilityMethods.readFile(cacheLocation + sha + ".txt");
			CommitResponse deserializedFile = gson.fromJson(file, CommitResponse.class);
			assertEquals(commitResponse.sha, deserializedFile.sha);
			assertEquals(commitResponse.url, deserializedFile.url);
		} catch (IOException e) {
			Assert.fail("Exception " + e);
		}

	}

	@Test
	public void canLoadACommitFromCache() throws FileNotFoundException {
		// first cache the commit so it can be loaded
		String currentDateTime = LocalDate.now().toString();
		String fileName = cacheLocation + currentDateTime + ".txt";
		PrintWriter writer = new PrintWriter(fileName);
		writer.print(gson.toJson(commitResponse));
		writer.close();

		try {
			CommitResponse response = cci.loadCommit(currentDateTime);
			assertEquals(commitResponse.sha, response.sha);
			assertEquals(commitResponse.url, response.url);
		} catch (IOException e) {
			Assert.fail("Exception " + e);
		}

	}

	// TODO: refresh cache
	@Test
	public void canRefreshCache() {

	}

}
