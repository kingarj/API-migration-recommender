package domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CommitTest {
	
	@Test
	public void canInstantiateCommit() {
		Commit commit = new Commit();
		assertNotNull(commit);
	}

}
