package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ControllerTest {
	
	@InjectMocks
	Controller controller = new Controller();
	
	@Mock
	VersionControlGateway vcg;
	
	@Before
	public void init() {
		vcg = Mockito.mock(VersionControlGateway.class);;
	}
	
	@Test
	public void canInstantiateController() {
		Controller controller = new Controller();
		assertNotNull(controller);
	}
	
//	@Test
//	public void canGenerateRecommendations() throws URISyntaxException, ClientProtocolException, IOException {
//		String source = "source";
//		String target = "target";
//		String[] recommendations = controller.generateRecommendations(source, target);
//		// placeholder
//		assert true;
//	}

}
