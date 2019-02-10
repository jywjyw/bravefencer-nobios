package brm;

import java.util.ArrayList;
import java.util.List;

public class Script {
	public Script parent;
	public List<Script> children = new ArrayList<>();
	public int pos;	//it's script 2 or script 3
	public String file;
	public String structure;
	public String[] addr;
	public long length;	//original file length
	public String englishFile;	
	public String newfont;
}