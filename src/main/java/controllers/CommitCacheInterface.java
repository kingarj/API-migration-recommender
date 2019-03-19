package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import domain.CommitResponse;
import util.UtilityMethods;

public class CommitCacheInterface {

	public String cacheLocation;
	public Gson gson;
	public Logger logger;

	public CommitCacheInterface(String cacheLocation) {
		this.cacheLocation = cacheLocation;
		this.gson = new Gson();
		this.logger = LoggerFactory.getLogger(CommitCacheInterface.class);
	}

	public void cacheCommit(String sha, CommitResponse commitResponse) {
		/**
		 * Caches a commit in the commit location NB: we do not need to check if the
		 * entry already exists as if there is a CommitResponse object then a request to
		 * GitHub has already been made so the cached response may as well be refreshed.
		 */
		String commitJson = gson.toJson(commitResponse);
		String fileName = this.cacheLocation + sha + ".txt";

		// delete file if it already exists

		File file = new File(fileName);
		if (file.delete()) {
			logger.debug("old commit {} being deleted, refreshing cache", sha);
		}

		try {

			PrintWriter writer = new PrintWriter(fileName);
			writer.print(commitJson);
			writer.close();
		} catch (FileNotFoundException e) {
			logger.debug(e.getMessage());
		}
	}

	public CommitResponse loadCommit(String sha) throws IOException {
		String commit = UtilityMethods.readFile(cacheLocation + sha + ".txt");
		logger.debug("commit {} found in cache", sha);
		return gson.fromJson(commit, CommitResponse.class);
	}

}
