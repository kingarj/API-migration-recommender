package controllers;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UserInterfaceTest {

	@Test
	public void canInstantiateUserInterface() {
		UserInterface ui = new UserInterface();
		assertNotNull(ui);
	}

}
