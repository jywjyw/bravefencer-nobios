package brm.hack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import common.RscLoader;
import common.Util;

/**
 * main script's encoding, contains 00~ff, f000~f1a2, ff00~ff06
 */
public class Encoding1 {
	
	/**
	 * 
	 * @param extCharOffset 扩展字库的起始offset
	 * @param extCharCapacity 扩展字库可用总字节数
	 */
	public Encoding1(int extCharOffset, int extCharCapacity){
		String file="init_encoding1.gbk";
		RscLoader.load(file, "gbk", new RscLoader.Callback() {
			@Override
			public void doInline(String line) {
				String[] arr=line.split("=");
				if(char_code.containsKey(arr[1]))
					throw new RuntimeException(file+" has duplicate character:" + arr[1]);
				char_code.put(arr[1], Util.decodeHex(arr[0]));
			}
		});
		
		this.extCharOffset = extCharOffset;
		int F1A3offset=0x4c4c+(192+419)*0x16;
		extCharCode = 0xf1a3+(extCharOffset-F1A3offset)/0x16;
		int r = (extCharOffset-F1A3offset)%0x16; 
		if(r>0){
			extCharCode++;
			this.extCharOffset += 0x16-r; 
		}
		extCharMaxCount=extCharCapacity/0x16;
	}
	
	private LinkedHashMap<String,byte[]> char_code = new LinkedHashMap<>();
	int next = 0xF000;
	private int extCharOffset, extCharCode, extCharMaxCount;
	//increment method: f000~f1a2,
	public byte[] put(String key){
		if(char_code.containsKey(key)) throw new RuntimeException();
		char_code.put(key, new byte[]{(byte)(next>>>8&0xff), (byte) (next&0xff)});
		next++;
		if(next==0xf1a3) next=extCharCode;
		return char_code.get(key);
	}
	
	public byte[] getCode(String char_) {
		return char_code.get(char_);
	}
	
	public int size(){
		return char_code.size()-7;	//exclude FF0X character
	}
	
	public List<String> get611Chars(){
		List<String> ret = new ArrayList<>();
		int i=0;
		for(Entry<String,byte[]> e:char_code.entrySet()){
			if(e.getValue().length==2 && e.getValue()[0]==(byte)0xff) 
				continue;//exclude ff0x
			ret.add(e.getKey());
			i++;
			if(i==611) break;
		}
		return ret;
	}
	
	public List<String> getExtendChars() {
		List<String> ret = new ArrayList<>();
		for(Entry<String,byte[]> e:char_code.entrySet()){
			if((e.getValue().length==2		//exclude 20~df 
					&& e.getValue()[0]!=(byte)0xff 	//exclude ff0x
					&& Util.toInt(e.getValue()[0], e.getValue()[1])>0xF1A2)){
				ret.add(e.getKey());
			}
		}
		return ret;
	}
	
	public void assertExtendCharsInRange(){
		int diff=getExtendChars().size()-extCharMaxCount;
		if(diff>0){
			throw new UnsupportedOperationException("main font lib exceed "+diff);
		}else if(diff<0){
			System.out.println("main font lib has "+-diff+" character space remained.");
		}
	}
	
	public void saveAsTbl(String outFile){
		try {
			OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(outFile),"gbk");
			for(Entry<String,byte[]> e:char_code.entrySet()){
				if(e.getValue().length==1){
					fos.write(String.format("%X=%s\n", e.getValue()[0],e.getKey()));
				} else {
					fos.write(Util.hexEncode(e.getValue()).toUpperCase()+"="+e.getKey()+"\n");
				}
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getExtCharOffset() {
		return extCharOffset;
	}
	
}
