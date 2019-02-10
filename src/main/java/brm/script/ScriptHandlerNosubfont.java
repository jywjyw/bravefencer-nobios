package brm.script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;

import brm.Script;
import brm.dump.Charset1;
import brm.dump.Charset2;
import brm.dump.Ctrl;
import brm.dump.EnglishConf;
import brm.hack.Encoding1;
import brm.hack.Encoding2;
import brm.hack.Sentence;
import brm.hack.SentenceSerializer;
import common.Util;
/**
 * 文本太短,仅用到主字库,无副字库,也找不到字库指针
 */
public class ScriptHandlerNosubfont implements Importer,Exporter{
	String splitDir;
	Script script;
	
	public ScriptHandlerNosubfont(String splitDir,Script script) {
		this.splitDir=splitDir;
		this.script=script;
	}

	@Override
	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException {
		int fontOffset=0;//script has not sub font lib; 
		int start1 = Integer.parseInt(script.addr[0],16);
		int end1 = Integer.parseInt(script.addr[1],16);
		RandomAccessFile file=null;
		try {
			file = new RandomAccessFile(this.splitDir+script.file, "r");
			new ScriptReader(new Charset2(),new EnglishConf(englishTexts, script.englishFile))
				.readTextArea(file,script.file,ctrl,charset1,cb,start1, end1, fontOffset, 0);
		} finally{
			file.close();
		}
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
			if(bs.length>s.len){
				throw new RuntimeException("文本超长: "+s.sentence);
			}
			file.seek(s.addr);
			file.write(bs);
		}
		file.close();
		if(enc2.size()>0){
			throw new RuntimeException(script.file+"只能使用主字库的字符,以下不是主字库的字符："+Util.join(enc2.getAllChars(), "", ""));
		}
		return null;
	}

}
