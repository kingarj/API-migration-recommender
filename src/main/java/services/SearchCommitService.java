package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.SearchCommit;
import domain.SearchCommitResponse;

public class SearchCommitService {
	
	FiltrationService filtrationService = new FiltrationService();

	public SearchCommitResponse createNewSearchCommitResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
		HttpEntity entity = response.getEntity();
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = new InputStreamReader(entity.getContent(), charset);
		SearchCommitResponse searchCommitResponse = gson.fromJson(reader, SearchCommitResponse.class);
		searchCommitResponse.items = filterIrrelevantSearchCommits(searchCommitResponse.items);
		return searchCommitResponse;
	}

	public SearchCommit[] filterIrrelevantSearchCommits(SearchCommit[] items) {
		return Arrays.stream(items).filter(x -> filtrationService.checkMessageIsRelevant(x)).toArray(SearchCommit[]::new);
	}

}
