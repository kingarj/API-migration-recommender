package services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import domain.Commit;
import domain.Mapping;
import util.UtilityMethods;

public class CommitServiceTest {
	
	public CommitService commitService = new CommitService();
	
	@Test
	public void canInstantiateCommitService() {
		CommitService commitService = new CommitService();
		assertNotNull(commitService);
	}
	
	@Test
	public void canCreateNewCommit() throws UnsupportedOperationException, IOException {
		String responseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity entity = new StringEntity(responseStr,
		        ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 
			    HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		String message = "add service to map results of searching github commits to an object";
		String url = "https://api.github.com/repos/kingarj/API-migration-recommender/git/commits/6e199009fee42f8665923181a2f39adddcb92d5a";
		Commit commit = commitService.createNewCommit(response);
		assertNotNull(commit.files);
		assertEquals(commit.files.length, 8);
		assertEquals(commit.files[0].mappings.size(), 8);
		assertEquals(message, commit.message);
		assertEquals(url, commit.url);
	}
	
	@Test
	public void canMergeFileMappingCandidates() throws IOException {
		String responseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponsetwocontrollerfiles.txt");
		StringEntity entity = new StringEntity(responseStr,
		        ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 
			    HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		Commit commit = commitService.createNewCommit(response);
		
		ArrayList<Mapping> mappings = commitService.mergeFileMappingCandidates(commit);		
		String line = "	public String[] generateRecommendations(String source, String target) throws URISyntaxException, ClientProtocolException, IOException {";
		Integer i = 2;
		assertNotNull(mappings);
		assertEquals(mappings.size(), 8);
		assertEquals(mappings.get(0).targets.get(line), i);
		
	}

}
