package controllers;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import domain.Mapping;
import util.UtilityMethods;

public class ControllerTest {
	
	@InjectMocks
	Controller controller = new Controller();
	
	@Mock
	VersionControlGateway vcg;
	
	HttpResponse commitResponse;
	HttpResponse searchCommitResponse;
	String source = "source";
	String target = "target";
	
	@Before
	public void init() throws IOException, URISyntaxException {
		vcg = Mockito.mock(VersionControlGateway.class);
		controller.vcg = vcg;
		VersionControlGateway utilityVCG = new VersionControlGateway();
		
		// set up search commit request
		HttpGet searchCommitRequest = utilityVCG.buildSearchCommitRequestBody(source, target);
		
		String searchCommitResponseStr = UtilityMethods.readFile("src/test/resources/examplesearchcommitresponse.txt");
		StringEntity searchCommitEntity = new StringEntity(searchCommitResponseStr,
				        ContentType.create("application/json", Consts.UTF_8));
		searchCommitResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 
					    HttpStatus.SC_OK, "OK");
		searchCommitResponse.setEntity(searchCommitEntity);
		
		Mockito.when(vcg.buildSearchCommitRequestBody(source, target)).thenReturn(searchCommitRequest);
		Mockito.when(vcg.executeHttpRequest(searchCommitRequest)).thenReturn(searchCommitResponse);
		
		// set up commit request and response
		String url = "https://api.github.com/repos/kingarj/API-migration-recommender/git/commits/6e199009fee42f8665923181a2f39adddcb92d5a";
		HttpGet commitRequest = utilityVCG.buildCommitRequestBody(url);
		String commitResponseStr = UtilityMethods.readFile("src/test/resources/examplecommitresponse.txt");
		StringEntity commitEntity = new StringEntity(commitResponseStr,
		        ContentType.create("application/json", Consts.UTF_8));
		commitResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1,
			    HttpStatus.SC_OK, "OK");
		commitResponse.setEntity(commitEntity);
		
		Mockito.when(vcg.buildCommitRequestBody(url)).thenReturn(commitRequest);
		Mockito.when(vcg.executeHttpRequest(commitRequest)).thenReturn(commitResponse);
	}
	
	@Test
	public void canInstantiateController() {
		Controller controller = new Controller();
		assertNotNull(controller);
	}
	
	@Test
	public void canGenerateRecommendations() throws URISyntaxException, ClientProtocolException, IOException {
		ArrayList<Mapping> recommendations = controller.generateRecommendations(source, target);
		assertNotNull(recommendations);
		assert(recommendations.size() > 0); 	
	}

}
