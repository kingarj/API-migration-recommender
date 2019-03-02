package domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CommitResponseTest {
	
	@Test
	public void canInstantiateCommitResponse() {
		CommitResponse commitResponse = new CommitResponse();
		assertNotNull(commitResponse);
	}

}
