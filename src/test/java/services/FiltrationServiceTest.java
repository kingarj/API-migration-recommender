package services;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import domain.Commit;
import domain.SearchCommit;

public class FiltrationServiceTest {

	String[] relevantMessages = new String[] { "switch from gson to jackson", "transition between jackson and gson",
			"move to jackson from gson" };
	String[] irrelevantMessages = new String[] { "comparison of gson and jackson",
			"performance testing for gson and jackson", "using jackson to support gson" };
	FiltrationService filtrationService = new FiltrationService();
	SearchCommit searchCommit = new SearchCommit();

	@Before
	public void init() {
		searchCommit.commit = new Commit();
	}

	@Test
	public void canInstantiateFiltrationService() {
		FiltrationService filtrationService = new FiltrationService();
		assertNotNull(filtrationService);
	}

	@Test
	public void canFlagCommitMessageAsIrrelevant() throws IOException {
		for (String s : irrelevantMessages) {
			searchCommit.commit.message = s;
			Boolean relevant = filtrationService.checkMessageIsRelevant(searchCommit);
			assert !relevant;
		}
	}

	@Test
	public void canFlagCommitMessageAsRelevant() throws IOException {
		for (String s : relevantMessages) {
			searchCommit.commit.message = s;
			Boolean relevant = filtrationService.checkMessageIsRelevant(searchCommit);
			assert relevant;
		}
	}

}