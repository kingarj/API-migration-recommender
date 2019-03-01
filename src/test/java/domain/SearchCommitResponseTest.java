package domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SearchCommitResponseTest {
	
	@Test
	public void canInstantiateSearchCommitResponse() {
		SearchCommitResponse searchCommitResponse = new SearchCommitResponse();
		assertNotNull(searchCommitResponse);
	}

}
