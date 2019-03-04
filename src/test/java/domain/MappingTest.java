package domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MappingTest {
	
	@Test
	public void canInstantiateMapping() {
		Mapping mapping = new Mapping("source", "target");
		assertNotNull(mapping);
		assertEquals("source", mapping.source);
		assertEquals(mapping.targets.size(),1);
	}

}
