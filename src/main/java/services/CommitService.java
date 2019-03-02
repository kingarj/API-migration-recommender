package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.Commit;
import domain.CommitResponse;

public class CommitService {

	public Commit createNewCommit(HttpResponse response) throws UnsupportedOperationException, IOException {		
		// create CommitResponse from HttpResponse
		HttpEntity entity = response.getEntity();
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = new InputStreamReader(entity.getContent(), charset);
		CommitResponse commitResponse = gson.fromJson(reader, CommitResponse.class);
		
		// attach the change-set to the new Commit
		Commit commit = commitResponse.commit;
		commit.files = commitResponse.files;
		
		return commit;
	}

}
