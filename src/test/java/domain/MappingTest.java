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
	
	@Test
	public void canMapTargetMultipleTimes() {
		Mapping mapping = new Mapping("1", "2");
		mapping.map("2");
		assert(mapping.targets.get("2") == 2);
	}
	
	@Test
	public void canGetMostFrequentMapping() {
		Mapping mapping = new Mapping("1", "2");
		mapping.map("3");
		mapping.map("2");
		String target = mapping.getMostFrequentMapping();
		assert(target == "2");
	}

}
