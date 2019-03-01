package controllers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ControllerTest {
	
	@Test
	public void canInstantiateController() {
		Controller controller = new Controller();
		assertNotNull(controller);
	}
	
	@Test
	public void canGenerateRecommendations() {
		Controller controller = new Controller();
		assert controller.generateRecommendations() == null;
	}

}
