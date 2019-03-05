package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import util.UtilityMethods;

public class ChangeFileTest {
	
	@Test
	public void canInstantiateChangeFile() {
		ChangeFile changeFile = new ChangeFile();
		assertNotNull(changeFile);
	}

	@Test
	public void canSetMappingsDeductionsFirst() throws IOException {
		ChangeFile file = new ChangeFile();
		String patch = UtilityMethods.readFile("src/test/resources/examplepatch.txt");
		file.patch = patch;
		file.setMappings();
		assertNotNull(file.mappings);
		assertEquals(file.mappings.size(), 8);
	}
	
	@Test
	public void canSetMappingsMixedFile() throws IOException {
		ChangeFile file = new ChangeFile();
		String patch = UtilityMethods.readFile("src/test/resources/examplepatch2.txt");
		file.patch = patch;
		file.setMappings();;
		assertNotNull(file.mappings);
		assertEquals(file.mappings.size(), 10);
		
	}
}
