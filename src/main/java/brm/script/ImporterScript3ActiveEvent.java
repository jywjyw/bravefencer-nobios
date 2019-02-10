package brm.script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import brm.Conf;
import brm.Script;
import brm.dump.SentenceSplitter;
import brm.hack.Encoding1;
import brm.hack.Encoding2;
import brm.hack.ErrMsg;
import brm.hack.FontGen;
import brm.hack.Sentence;
import brm.hack.SentenceSerializer;
import common.Util;

/**
 * some script3 event happens village, the font pointer will change after happened.
 * but the font pointer never restore  
 */
public class ImporterScript3ActiveEvent implements Importer{
		
	int fontPointerOffset;
	ScriptAddrGetter scriptAddrGetter;
	String splitDir;
	Script script;
	
	public ImporterScript3ActiveEvent(int fontPointerOffset, ScriptAddrGetter scriptAddrGetter,
			String splitDir, Script script) {
		this.fontPointerOffset = fontPointerOffset;
		this.scriptAddrGetter = scriptAddrGetter;
		this.splitDir = splitDir;
		this.script = script;
	}

	
	/**
	 * 日文版 中, s3的字库中包含了父脚本s2的字库,因为触发s3事件后,字库指针被修改,
	 * 此后再去触发s2对话的话,由于字库指针已被修改,所以要包含整套s2字库才能正常显示, 
	 * s3专用字符放在s2后面
	 */
	@Override
	public List<String> import_(Encoding1 enc1, List<Sentence> sentences, List<String> parentChars)
			throws IOException {
		File scriptFile = new File(splitDir+script.file);
		if(!scriptFile.exists()) throw new RuntimeException();
		RandomAccessFile file = new RandomAccessFile(scriptFile, "rw");
		
		//merge script3 encoding to script2
		Set<String> uniquechars = new LinkedHashSet<>();
		for(Sentence s:sentences){
			new SentenceSplitter().splitToWords(s.sentence, new SentenceSplitter.Callback() {
				@Override
				public void onReadWord(boolean isCtrl, String word) {
					if(!isCtrl && enc1.getCode(word)==null && !parentChars.contains(word)){
						uniquechars.add(word);
					}
				}
			});
		}
		
//			System.out.println(script.file+"'s parent chars : "+Util.join(parentChars, "", ""));

		//new script3 charTable contains script2 charTable
		Encoding2 enc2 = new Encoding2();
		for(String c:parentChars) enc2.put(c);	
		for(String c:uniquechars) enc2.put(c);
		
		//rebuild text
		SentenceSerializer sparser = new SentenceSerializer(enc1, enc2);
		for(Sentence s:sentences){
			byte[] bs=sparser.toBytes(s.sentence);
			int exceed=bs.length-s.len;
			if(exceed<=0){
				file.seek(s.addr);
				file.write(bs);
			} else {
				ErrMsg.add(String.format("SCRIPTS文本超出%d字节 : %s", exceed, s.sentence));
			}
		}
		
		//append new font lib to tail, assert has enough space, don't conflict with script2 font lib. 
		int parentFontTargetAddr = Conf.SCRIPTS_LIMIT_ADDR-parentChars.size()*Conf.CHAR_BYTES; 
		int parentFontOffset = parentFontTargetAddr-scriptAddrGetter.getStartAddr();
		
		byte[] parentfonts=new FontGen().gen(new ArrayList<String>(parentChars));
		byte[] uniquefonts=new FontGen().gen(new ArrayList<String>(uniquechars));
		
		int s3fontOffset=parentFontOffset-uniquefonts.length-parentfonts.length;
		if(s3fontOffset<scriptFile.length()){
			String msg = String.format("[%s]'s new font lib has exceed %s character, please minimize [%s]'s or [%s]'s font lib", 
					script.file, (parentFontOffset-scriptFile.length())/Conf.CHAR_BYTES, script.file, script.parent.file);
			throw new RuntimeException(msg);
		}
		file.seek(s3fontOffset);
		file.write(parentfonts);
		file.write(uniquefonts);
		
		modifyFontPointer(file, s3fontOffset);
		
		file.close();
		return null;
	}
	
	
	public void modifyFontPointer(RandomAccessFile file, int newFontOffset) throws IOException {
		file.seek(this.fontPointerOffset);
		file.writeInt(Util.hilo(scriptAddrGetter.getStartAddr()+newFontOffset));//修改字库指针
	}
	
	
	
}
