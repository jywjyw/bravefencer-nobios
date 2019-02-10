package brm.script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import brm.Script;
import brm.hack.Encoding1;
import brm.hack.Encoding2;
import brm.hack.ErrMsg;
import brm.hack.FontGen;
import brm.hack.Sentence;
import brm.hack.SentenceSerializer;
import common.Util;

public class ImporterScript2WithoutChild implements Importer{
	
	int fontPointerOffset;
	ScriptAddrGetter scriptAddrGetter;
	String splitDir;
	Script script;
	
	public ImporterScript2WithoutChild(int fontPointerOffset, ScriptAddrGetter scriptAddrGetter,
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
		Encoding2 enc2 = new Encoding2();
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
		
		if(enc2.size()>0){
			byte[] fonts=new FontGen().gen(enc2.getAllChars());
			int newFontOffset=Util.align2Bytes((int)scriptFile.length());	
//			newFontOffset=0x801F6768-Conf.SCRIPT2_ADDR;	//801F6766V,801F6767x,801F6768x
			file.seek(newFontOffset);
			file.write(fonts);
			
			file.seek(this.fontPointerOffset);
			file.writeInt(Util.hilo(scriptAddrGetter.getStartAddr()+newFontOffset));	//修改字库指针	
		}
		file.close();
		return enc2.getKeys();
	}
	
}