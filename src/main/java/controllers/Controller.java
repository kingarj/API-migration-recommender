package controllers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.SearchCommitResponse;

public class Controller {

	VersionControlGateway vcg;

	public Controller() {
		this.vcg = new VersionControlGateway();
	}

	public String[] generateRecommendations(String source, String target)
			throws URISyntaxException, ClientProtocolException, IOException {
		HttpGet request = vcg.buildSearchCommitRequestBody(source, target);
		HttpResponse response = vcg.executeHttpRequest(request);
		HttpEntity entity = response.getEntity();
		Gson gson = new GsonBuilder().create();
		ContentType contentType = ContentType.getOrDefault(entity);
		Charset charset = contentType.getCharset();
		Reader reader = new InputStreamReader(entity.getContent(), charset);
		SearchCommitResponse searchCommitResponse = gson.fromJson(reader, SearchCommitResponse.class);
		return null;
	}

}
