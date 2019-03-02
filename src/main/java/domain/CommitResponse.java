package domain;

public class CommitResponse implements Response {
	public String sha;
	public Commit commit;
	public String url;
	public ChangeFile[] files;
}
