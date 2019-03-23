package controllers;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import domain.CommitResponse;
import domain.SearchCommitResponse;
import util.UtilityMethods;

public class ControllerTest {

	@InjectMocks
	Controller controller = new Controller("src/test/resources/", "test", "test");

	@Mock
	VersionControlGateway vcg;

	@Mock
	LoadingCache<String, CommitResponse> cache;

	HttpResponse commitResponse;
	HttpResponse searchCommitResponse;
	Gson gson = new Gson();
	String source = "source";
	String target = "target";

	@Before
	public void init() throws IOException, URISyntaxException {
		vcg = Mockito.mock(VersionControlGateway.class);
		controller.vcg = vcg;
		VersionControlGateway utilityVCG = new VersionControlGateway("test", "test");

		String searchCommitResponseStr = UtilityMethods.readFile("src/test/resources/examplesearchcommitresponse.txt");
		StringEntity searchCommitEntity = new StringEntity(searchCommitResponseStr,
				ContentType.create("application/json", Consts.UTF_8));
		searchCommitResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		searchCommitResponse.setEntity(searchCommitEntity);
		SearchCommitResponse scr = gson.fromJson(utilityVCG.getResponseReader(searchCommitResponse),
				SearchCommitResponse.class);

		Mockito.when(vcg.getSearchCommit(source, target)).thenReturn(scr);

		// set up commit request and response
		String url = "https://api.github.com/repos/kingarj/API-migration-recommender/git/commits/6e199009fee42f8665923181a2f39adddcb92d5a";
		String sha = "6e199009fee42f8665923181a2f39adddcb92d5a";
		String commitResponseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity commitEntity = new StringEntity(commitResponseStr,
				ContentType.create("application/json", Consts.UTF_8));
		commitResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		commitResponse.setEntity(commitEntity);
		Reader reader = utilityVCG.getResponseReader(commitResponse);
		CommitResponse commit = gson.fromJson(reader, CommitResponse.class);

		Mockito.when(vcg.getCommit(url, sha)).thenReturn(commit);
	}

	@Test
	public void canInstantiateController() {
		Controller controller = new Controller("src/test/resources/", "test", "test");
		assertNotNull(controller);
	}

	@Test
	public void canGenerateRecommendations() throws URISyntaxException, ClientProtocolException, IOException {
		HashMap<String, String> recommendations = controller.generateRecommendations(source, target);
		assertNotNull(recommendations);
		assert (recommendations.size() > 0);
		assert (recommendations.get("***Gson *** = new GsonBuilder().create();")
				.equals("***SearchCommitResponse *** = ***.***(***);"));
	}

}
