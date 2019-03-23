package services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import com.google.gson.Gson;

import controllers.VersionControlGateway;
import domain.SearchCommit;
import domain.SearchCommitResponse;
import util.UtilityMethods;

public class SearchCommitServiceTest {

	SearchCommitService searchCommitService = new SearchCommitService();
	VersionControlGateway vcg = new VersionControlGateway("test", "test");
	Gson gson = new Gson();

	@Test
	public void canInstantiateSearchCommitService() {
		SearchCommitService searchCommitService = new SearchCommitService();
		assertNotNull(searchCommitService);
	}

	@Test
	public void canFilterOutSearchCommitsWithIrrelevantMessages() throws IOException {
		String responseStr = UtilityMethods.readFile("src/test/resources/examplesearchcommitresponseirrelevantmsg.txt");
		StringEntity entity = new StringEntity(responseStr, ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		Reader reader = vcg.getResponseReader(response);
		SearchCommitResponse searchCommitResponse = gson.fromJson(reader, SearchCommitResponse.class);
		ArrayList<SearchCommit> filteredItems = searchCommitService
				.filterIrrelevantSearchCommits(searchCommitResponse.items);
		assertNotNull(filteredItems);
		assertEquals(filteredItems.size(), 2);
	}

}
