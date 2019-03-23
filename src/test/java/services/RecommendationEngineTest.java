package services;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.google.gson.Gson;

import controllers.VersionControlGateway;
import domain.ChangeFile;
import domain.Commit;
import domain.Mapping;

public class RecommendationEngineTest {

	CommitService commitService = new CommitService();
	RecommendationEngine recommendationEngine = new RecommendationEngine();
	VersionControlGateway vcg = new VersionControlGateway("test", "test");
	Gson gson = new Gson();

	@Test
	public void canInstantiateRecommendationEngine() {
		RecommendationEngine recommendationEngine = new RecommendationEngine();
		assertNotNull(recommendationEngine);
	}

	@SuppressWarnings("serial")
	@Test
	public void canMergeMappings() throws IOException {
		String line1 = "\\t\\tGson *** = new GsonBuilder().create();";
		String line2 = "\\t\\tSearchCommitResponse *** = ***.***(***);";
		String testLine = "this is a test";
		Mapping mapping = new Mapping(line1, line2);
		Mapping mapping2 = new Mapping(line1, testLine);
		ChangeFile file1 = new ChangeFile();
		ChangeFile file2 = new ChangeFile();

		file1.mappings = new ArrayList<Mapping>();
		file2.mappings = new ArrayList<Mapping>();
		file1.mappings.add(mapping);
		file1.mappings.add(mapping2);
		file2.mappings.add(mapping);

		Commit commit1 = new Commit();
		commit1.files = new ChangeFile[] { file1, file2 };
		Commit commit2 = new Commit();
		commit2.files = new ChangeFile[] { file1, file2 };

		ArrayList<Commit> commits = new ArrayList<Commit>() {
			{
				add(commit1);
				add(commit2);
			}
		};

		HashMap<String, String> recommendations = recommendationEngine.mergeMappings(commits);
		assertNotNull(recommendations);
		assert (recommendations.size() > 0);
		assert (recommendations.get(line1).equals(line2));
	}

}
