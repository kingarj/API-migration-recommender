package services;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import domain.Commit;
import util.UtilityMethods;

public class RecommendationEngineTest {

	CommitService commitService = new CommitService();
	RecommendationEngine recommendationEngine = new RecommendationEngine();

	@Test
	public void canInstantiateRecommendationEngine() {
		RecommendationEngine recommendationEngine = new RecommendationEngine();
		assertNotNull(recommendationEngine);
	}

	@SuppressWarnings("serial")
	@Test
	public void canMergeMappings() throws IOException {
		// create first commit
		String responseStr1 = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity entity1 = new StringEntity(responseStr1, ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response1 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response1.setEntity(entity1);
		Commit commit1 = commitService.createNewCommit(response1, "src/test/resources/source.txt",
				"src/test/resources/target.txt");

		// create second commit
		String responseStr2 = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity entity2 = new StringEntity(responseStr2, ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response2.setEntity(entity2);
		Commit commit2 = commitService.createNewCommit(response2, "src/test/resources/source.txt",
				"src/test/resources/target.txt");

		ArrayList<Commit> commits = new ArrayList<Commit>() {
			{
				add(commit1);
				add(commit2);
			}
		};

		HashMap<String, String> recommendations = recommendationEngine.mergeMappings(commits);
		assertNotNull(recommendations);
		assert (recommendations.size() > 0);
	}

}
