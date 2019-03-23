package domain;

import java.util.ArrayList;

public class SearchCommitResponse implements Response {
	public int total_count;
	public ArrayList<SearchCommit> items;
}
