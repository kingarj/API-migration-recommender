package domain;

import java.util.List;

public class ChangeFile {
	public String sha;
	public String filename;
	public String status;
	public Integer additions;
	public Integer deletion;
	public Integer changes;
	public String patch;
	public List<Mapping> mappings; 

}
