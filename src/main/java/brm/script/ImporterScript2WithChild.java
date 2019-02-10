package brm.script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import brm.Conf;
import brm.Script;
import brm.hack.Encoding1;
import brm.hack.Encoding2;
import brm.hack.ErrMsg;
import brm.hack.FontGen;
import brm.hack.Sentence;
import brm.hack.SentenceSerializer;
import common.Util;

public class ImporterScript2WithChild implements Importer{
	
	int fontPointerOffset;
	ScriptAddrGetter scriptAddrGetter;
	String splitDir;
	Script script;
	
	public ImporterScript2WithChild(int fontPointerOffset, ScriptAddrGetter scriptAddrGetter,
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
		
		//把新字库追加至内存的临界点Conf.SCRIPTS_LIMIT_ADDR
		if(enc2.size()>0){
			byte[] fonts=new FontGen().gen(enc2.getAllChars());
			int newFontOffset;
			long maxSuffixLen=0;
			for(Script suffix:script.children){
				maxSuffixLen = Math.max(maxSuffixLen, new File(splitDir+suffix.file).length());
			}
			int fontTargetAddr=Conf.SCRIPTS_LIMIT_ADDR-fonts.length;//scriptAddrGetter.getStartAddr()
			fontTargetAddr=Util.align2Bytes(fontTargetAddr);
			System.out.printf("%s,fontsize=%d, new font addr=%X\n",script.file, enc2.size(), fontTargetAddr);
			newFontOffset=fontTargetAddr-scriptAddrGetter.getStartAddr();
			if(newFontOffset<new File(splitDir+script.file).length()+maxSuffixLen){
				throw new RuntimeException();
			}
			file.seek(newFontOffset);
			file.write(fonts);
			
			file.seek(this.fontPointerOffset);
			file.writeInt(Util.hilo(scriptAddrGetter.getStartAddr()+newFontOffset));	//修改字库指针	
		}
		file.close();
		return enc2.getKeys();
	}
	
}