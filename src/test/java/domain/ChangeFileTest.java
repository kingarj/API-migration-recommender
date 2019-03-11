package domain;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class ChangeFileTest {
	
	@Test
	public void canInstantiateChangeFile() {
		ChangeFile changeFile = new ChangeFile();
		assertNotNull(changeFile);
	}
	
}
