package controllers;

import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

public class UserInterfaceTest {
	
	@Test
	public void canInstantiateUserInterface() {
		Display display = new Display();
		UserInterface ui = new UserInterface(display);
		
		assertNotNull(ui);
		ui.teardown();
	}
	
	@Test
	public void canInit() {
		Display display = new Display();
		UserInterface ui = new UserInterface(display);
		
		ui.init();
		
		assertNotNull(ui);
		
		ui.teardown();
	}

}
