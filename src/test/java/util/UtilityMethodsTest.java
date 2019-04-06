package util;

import org.junit.Test;

public class UtilityMethodsTest {

	@Test
	public void canBuildOutputFileName() {
		String source = "one";
		String target = "two";
		String filename = UtilityMethods.buildOutputFileName(source, target);
		assert filename.substring(0, 7).equals(source + target + "_");

	}

}
