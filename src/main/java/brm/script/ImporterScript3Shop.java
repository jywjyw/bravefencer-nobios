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
 * import some script3 such as shop/church, when musashi goes in or out, the font pointer will change
 */
public class ImporterScript3Shop implements Importer{
	int fontPointerOffset;
	ScriptAddrGetter scriptAddrGetter;
	String splitDir;
	Script script;
	
	public ImporterScript3Shop(int fontPointerOffset, ScriptAddrGetter scriptAddrGetter,
			String splitDir, Script script) {
		this.fontPointerOffset = fontPointerOffset;
		this.scriptAddrGetter = scriptAddrGetter;
		this.splitDir = splitDir;
		this.script = script;
	}

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

		//new script3 charTable contains script2 charTable
		Encoding2 enc2 = new Encoding2();
		for(String c:uniquechars) enc2.put(c);
		for(String c:parentChars) enc2.put(c);	
		
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
		int newFontOffset = Conf.SCRIPTS_LIMIT_ADDR-parentChars.size()*Conf.CHAR_BYTES-scriptAddrGetter.getStartAddr();
		if(uniquechars.size()>0){
			byte[] fonts=new FontGen().gen(new ArrayList<String>(uniquechars));
			newFontOffset -= fonts.length;
			if(newFontOffset<scriptFile.length()){
				String msg = String.format("[%s]'s new font lib has exceed %s character, please minimize [%s]'s or [%s]'s font lib", 
						script.file, (newFontOffset-scriptFile.length())/Conf.CHAR_BYTES, script.file, script.parent.file);
				throw new RuntimeException(msg);
			}
			file.seek(newFontOffset);
			file.write(fonts);
		}
		
		modifyFontPointer(file, newFontOffset);
		
		file.close();
		return null;
	}
	
	//修改字库指针
	public void modifyFontPointer(RandomAccessFile file, int newFontOffset) throws IOException{
		file.seek(this.fontPointerOffset);
		file.writeInt(Util.hilo(scriptAddrGetter.getStartAddr()+newFontOffset));	
	}
}
