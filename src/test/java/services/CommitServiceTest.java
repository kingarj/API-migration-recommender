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
import domain.ChangeFile;
import domain.Commit;
import domain.CommitResponse;
import domain.Mapping;
import util.UtilityMethods;

public class CommitServiceTest {

	public CommitService commitService = new CommitService();
	public VersionControlGateway vcg = new VersionControlGateway("test", "test");
	public Gson gson = new Gson();

	@Test
	public void canInstantiateCommitService() {
		CommitService commitService = new CommitService();
		assertNotNull(commitService);
	}

	@Test
	public void canCreateNewCommit() throws UnsupportedOperationException, IOException {
		String responseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity entity = new StringEntity(responseStr, ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		Reader reader = vcg.getResponseReader(response);
		CommitResponse cr = gson.fromJson(reader, CommitResponse.class);
		String message = "add service to map results of searching github commits to an object";
		String url = "/repos/kingarj/API-migration-recommender/git/commits/6e199009fee42f8665923181a2f39adddcb92d5a";
		Commit commit = commitService.createNewCommit(cr, "src/test/resources/source.txt",
				"src/test/resources/target.txt");
		assertNotNull(commit.files);
		assertEquals(commit.files.length, 8);
		assertEquals(message, commit.message);
		assertEquals("https://api.github.com" + url, commit.url);
	}

	@Test
	public void canSetMappingsDeductionsFirst() throws IOException {
		ChangeFile file = new ChangeFile();
		String patch = UtilityMethods.readFile("src/test/resources/examplepatch.txt");
		file.patch = patch;
		commitService.setMappings(file);
		assertNotNull(file.mappings);
		assertEquals(file.mappings.size(), 8);
	}

	@Test
	public void canSetMappingsMixedFile() throws IOException {
		ChangeFile file = new ChangeFile();
		String patch = UtilityMethods.readFile("src/test/resources/examplepatch2.txt");
		file.patch = patch;
		commitService.setMappings(file);
		assertNotNull(file.mappings);
		assertEquals(file.mappings.size(), 10);
	}

	@Test
	public void canMergeFileMappingCandidates() throws IOException {
		String originalLine1 = "Gson gson = new GsonBuilder().create();";
		String originalLine2 = "SearchCommitResponse searchCommitResponse = scs.createNewSearchCommitResponse(response);";
		Mapping mapping = new Mapping(originalLine1, originalLine2);
		ChangeFile file1 = new ChangeFile();
		ChangeFile file2 = new ChangeFile();

		file1.mappings = new ArrayList<Mapping>();
		file2.mappings = new ArrayList<Mapping>();
		file1.mappings.add(mapping);
		file2.mappings.add(mapping);

		Commit commit = new Commit();
		commit.files = new ChangeFile[] { file1, file2 };

		commitService.sanitiseMappings(file1, "src/test/resources/source.txt", "src/test/resources/target.txt");
		commitService.sanitiseMappings(file2, "src/test/resources/source.txt", "src/test/resources/target.txt");

		ArrayList<Mapping> mappings = commitService.mergeFileMappingCandidates(commit);
		String line = "Gson *** = new GsonBuilder().create();";
		String line2 = "SearchCommitResponse *** = ***.***(***);";
		Integer i = 2;
		assertNotNull(mappings);
		assertEquals(mappings.size(), 1);
		assertEquals(mappings.get(0).source, line);
		assertEquals(mappings.get(0).targets.get(line2), i);
	}

	@Test
	public void canSanitiseMappings() {

		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		// test for import statements
		Mapping mapping1 = new Mapping("import x.y.z.One", "import a.b.Six");
		mappings.add(mapping1);
		// test for annotations
		Mapping mapping2 = new Mapping("@Two", "@Four(PROP)");
		mappings.add(mapping2);
		// test for methods on instances of classes with same number of parameters
		// TODO: how do we know if a var is an instance of a class e.g. One
		// unrelated_name = new One();
		Mapping mapping3 = new Mapping("oneInstance.create(var)", "fiveInstance.add(var)");
		mappings.add(mapping3);
		// test for methods on instances of classes with a different number of
		// parameters
		Mapping mapping4 = new Mapping("threeInstance.sum(listVar)", "sixInstance.addTwo(1,2)");
		mappings.add(mapping4);
		// test for multiple methods on the same class
		Mapping mapping5 = new Mapping("threeInstance.sum(listVar)", "sixInstance.addThree(1,2,3)");
		mappings.add(mapping5);

		ChangeFile file = new ChangeFile();
		file.mappings = mappings;

		commitService.sanitiseMappings(file, "src/test/resources/source.txt", "src/test/resources/target.txt");

		String mapping1Source = "import x.y.z.One";
		String mapping1Target = "import a.b.Six";
		String mapping2Source = "@Two";
		String mapping2Target = "@Four(***)";
		String mapping3Source = "***.create(***)";
		String mapping3Target = "***.add(***)";
		String mapping4Source = "***.sum(***)";
		String mapping4Target = "***.addTwo(***,***)";
		String mapping5Source = mapping4Source;
		String mapping5Target = "***.addThree(***,***,***)";

		// assert keywords (rather than application context) have not been affected

		assertEquals(file.mappings.get(0).source, mapping1Source);
		assertEquals(file.mappings.get(0).targets.keySet().toArray()[0], mapping1Target);
		assertEquals(file.mappings.get(1).source, mapping2Source);

		// assert application context has been substituted

		assertEquals(file.mappings.get(1).targets.keySet().toArray()[0], mapping2Target);
		assertEquals(file.mappings.get(2).source, mapping3Source);
		assertEquals(file.mappings.get(2).targets.keySet().toArray()[0], mapping3Target);
		assertEquals(file.mappings.get(3).source, mapping4Source);
		assertEquals(file.mappings.get(3).targets.keySet().toArray()[0], mapping4Target);
		assertEquals(file.mappings.get(4).source, mapping5Source);
		assertEquals(file.mappings.get(4).targets.keySet().toArray()[0], mapping5Target);

	}

}
