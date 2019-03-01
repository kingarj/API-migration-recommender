package domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SearchCommitTest {
	
	@Test
	public void canInstantiateSearchCommit() {
		SearchCommit searchCommit = new SearchCommit();
		assertNotNull(searchCommit);
	}
}
