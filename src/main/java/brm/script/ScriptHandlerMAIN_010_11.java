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
import brm.hack.ErrMsg;
import brm.hack.FontGen;
import brm.hack.SentenceSerializer;
import brm.script.Exporter.Callback;
import common.ExcelParser;
import common.ExcelParser.RowCallback;
import common.Util;
import common.WordsCount;

/**
 * 里面的字符全部为单字节编码+Fxxx编码
 */
public class ScriptHandlerMAIN_010_11{
	
	String splitDir;
	Script script;
	private int fontOffset,start,end;
	public ScriptHandlerMAIN_010_11(String splitDir, Script script) {
		this.splitDir = splitDir;
		this.script = script;
		this.fontOffset = Integer.parseInt(script.addr[0],16);
		this.start = Integer.parseInt(script.addr[1],16);
		this.end = Integer.parseInt(script.addr[2],16);
	}
	
	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException {
		RandomAccessFile file=null;
		try {
			file = new RandomAccessFile(this.splitDir+script.file, "r");
			new ScriptReader(new Charset2(), new EnglishConf(englishTexts, script.englishFile))
						.readTextArea(file,script.file,ctrl, charset1, cb,start, end, fontOffset, 0);
		} finally{
			file.close();
		}
	}
	
	private Integer lastAddr;
	private Integer lastSentenceLen;
//	private List<byte[]> sentence = new ArrayList<>();
	private StringBuilder sentence = new StringBuilder();
	
//	public void validate(File excel, Encoding1 enc1) throws IOException{
//		SentenceSerializer sParser = new SentenceSerializer(enc1); 
//		RandomAccessFile file = new RandomAccessFile(this.splitDir+script.file, "r");
//		new ExcelParser(excel).parse("MAIN", 2, new RowCallback() {
//			@Override
//			public void doInRow(List<String> strs, int rowNum) {
//				String addrCell=strs.get(1);
//				if(Util.isNotEmpty(addrCell)) {
//					if(lastAddr!=null){
//						validateEverySentence(sParser);
//					}
//					sentence = new StringBuilder();
//					lastAddr = Integer.parseInt(addrCell,16);
//				}
//				
//				String ctrls=getCell(strs, 3), chinese=getCell(strs, 5);
//				sentence.append(ctrls).append(chinese);
//				
//				String lenCell=getCell(strs, 2);
//				if(Util.isNotEmpty(lenCell))	
//					lastSentenceLen =Integer.parseInt(lenCell); 
//			}
//		});
//		validateEverySentence(sParser);
//		
//		file.close();
//	}
	
	WordsCount wordsCount = new WordsCount();
	boolean occurErrorSentence=false;
	
	
//	private void validateEverySentence(SentenceSerializer sParser){
//		new SentenceSplitter().splitToWords(sentence.toString(), new SentenceSplitter.Callback() {
//			@Override
//			public void onReadWord(boolean isCtrl, String word) {
//				if(!isCtrl) wordsCount.put(word);
//			}
//		});
//		byte[] bs = sParser.toBytes(sentence.toString());
//		if(bs.length>lastSentenceLen){
//			allok=false;
//			if(!occurErrorSentence){
//				log.info("##### 以下是MAIN工作簿中超出限制长度的句子(有的字符占2个字节,有的占1个字节) ######\n");
//				occurErrorSentence=!occurErrorSentence;
//			}
//			log.info(String.format("起始地址(%X),超出字节(%d),内容:%s\n",lastAddr,bs.length-lastSentenceLen, sentence.toString()));
//		}
//	}
//	
	
	
	
	public void import_(File excel, Encoding1 enc1) throws IOException{
		SentenceSerializer sParser = new SentenceSerializer(enc1); 
		RandomAccessFile file = new RandomAccessFile(this.splitDir+script.file, "rw");
		new ExcelParser(excel).parse("MAIN", 2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				String addrCell=strs.get(1);
				if(Util.isNotEmpty(addrCell)) {
					if(lastAddr!=null){
						rewriteSentence(file,sParser);
					}
					sentence = new StringBuilder();
					lastAddr = Integer.parseInt(addrCell,16);
				}
				
				String ctrls=getCell(strs, 3), chinese=getCell(strs, 5);
				sentence.append(ctrls).append(chinese);
				
				String lenCell=getCell(strs, 2);
				if(Util.isNotEmpty(lenCell))	
					lastSentenceLen =Integer.parseInt(lenCell); 
			}
		});
		
		rewriteSentence(file,sParser);	//handle the last sentence
		
		writeNewFont(file,enc1);
		file.close();
	}
	
	private void rewriteSentence(RandomAccessFile file, SentenceSerializer sParser){
		try {
			byte[] bs=sParser.toBytes(sentence.toString());
			int exceed=bs.length-lastSentenceLen;
			if(exceed<=0){
				file.seek(lastAddr);
				file.write(bs);
			} else {
				ErrMsg.add(String.format("MAIN文本超出%d字节 : %s", exceed, sentence.toString()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
 	private void writeNewFont(RandomAccessFile file, Encoding1 enc1){
		enc1.assertExtendCharsInRange();
		try {
			file.seek(fontOffset);
			file.write(new FontGen().gen(enc1.get611Chars()));
			
			file.seek(enc1.getExtCharOffset());
			List<String> extended = enc1.getExtendChars();
			file.write(new FontGen().gen(extended));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getCell(List<String> strs, int cell){
		String ret="";
		if(strs.size()>cell && strs.get(cell)!=null) {
			ret = strs.get(cell);
		}
		return ret;
	}
	
}
