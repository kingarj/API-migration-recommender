package services;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import domain.SearchCommitResponse;

public class SearchCommitServiceTest {
	
	SearchCommitService searchCommitService = new SearchCommitService();
	
	@Test
	public void canInstantiateSearchCommitService() {
		SearchCommitService searchCommitService = new SearchCommitService();
		assertNotNull(searchCommitService);
	}
	
	@Test
	public void canCreateNewSearchCommitResponse() throws UnsupportedOperationException, IOException {
		StringEntity entity = new StringEntity("{\"total_count\":0,\"incomplete_results\":false,\"items\":[]}",
		        ContentType.create("application/json", Consts.UTF_8));
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 
			    HttpStatus.SC_OK, "OK");
		response.setEntity(entity);
		SearchCommitResponse searchCommitResponse = searchCommitService.createNewSearchCommitResponse(response);
		assertNotNull(searchCommitResponse);
		assertEquals(searchCommitResponse.items.length, 0);
		assertEquals(searchCommitResponse.total_count, 0);
	}

}
