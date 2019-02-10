package brm.script;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import brm.Script;
import brm.dump.Charset1;
import brm.dump.Charset2;
import brm.dump.Ctrl;
import brm.dump.EnglishConf;
import common.Util;

public interface Exporter {
	
	public interface Callback{
		void sentenceStart(String file, int sentenceIndex, long sentenceAddr, String english);
		void everyWord(byte[] wordBytes, String word);
		
		/**
		 * @param file
		 * @param sentenceIndex  index in this script
		 * @param sentenceAddr	address in this script
		 * @param sentenceLen
		 */
		void sentenceEnd(String file, int sentenceIndex, long sentenceAddr, int sentenceLen);
	}
	void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException;

}

class CommonExporter implements Exporter{
	
	protected Script script;
	protected String splitDir;
	protected TextAddrGetter textAddrGetter;
	protected ScriptAddrGetter scriptStartAddrGetter;
	
	public CommonExporter(Script script, String splitDir, TextAddrGetter addrGetter,
			ScriptAddrGetter scriptAddrGetter) {
		this.script = script;
		this.splitDir = splitDir;
		this.textAddrGetter = addrGetter;
		this.scriptStartAddrGetter = scriptAddrGetter;
	}

	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException{
		int[] textAddr1=textAddrGetter.getText1Addr();
		RandomAccessFile file = null;
		Charset2 charset2 = new Charset2();//every script has its own charset
		
		EnglishConf english = new EnglishConf(englishTexts, script.englishFile);
		try {
			file = new RandomAccessFile(this.splitDir+script.file, "r");
			file.seek(textAddrGetter.getFontPointerOffset());
			int subfontOffset = Util.hilo(file.readInt()) - scriptStartAddrGetter.getStartAddr();
			if(subfontOffset<0){
				throw new RuntimeException("config error!!!  "+script.file);
			}
			int nextSentenceIndex = new ScriptReader(charset2, english).readTextArea(file, script.file, ctrl, charset1,cb, 
										textAddr1[0], textAddr1[1], subfontOffset, 0);
			
			int[] textAddr2=textAddrGetter.getText2Addr();
			if(textAddr2!=null){
				new ScriptReader(charset2, english).readTextArea(file, script.file, ctrl, charset1, cb, 
										textAddr2[0], textAddr2[1], subfontOffset, nextSentenceIndex);
			}
		} finally{
			file.close();
		}
	}
}
