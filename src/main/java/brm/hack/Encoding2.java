package brm.hack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * script2's encoding
 */
public class Encoding2 {
	
	private Map<String,Integer> char_code = new LinkedHashMap<>();
	
	int next=0xE000;
	
	public Integer getCode(String char_) {
		return char_code.get(char_);
	}
	
//	public void setCode(String char_,int code){
//		this.char_code.put(char_, code);
//	}
	
	public List<String> getAllChars(){
		return new ArrayList<String>(char_code.keySet());
	}
	
	public int size(){
		return char_code.size();
	}
	
	public List<String> getKeys(){
		return new ArrayList<String>(char_code.keySet());
	}
	
	public int put(String key){
		if(char_code.containsKey(key)) 
			throw new UnsupportedOperationException();
		char_code.put(key, next);
		next++;
		return char_code.get(key);
	}
	
}
