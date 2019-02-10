package brm.script;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import brm.Script;
import brm.dump.Charset1;
import brm.dump.Ctrl;
import brm.hack.Encoding1;
import brm.hack.Sentence;

/**
 * 0.4脚本的第1种格式:字库指针要手动查找
 *
 */
public class ScriptHandlerS2 implements Importer,Exporter{
	
	String splitDir;
	Script script;
	
	protected TextAddrGetter textAddrGetter;
	protected ScriptAddrGetter scriptAddrGetter = new Script2Addr();
	
	public ScriptHandlerS2(String splitDir, Script script) {
		this.splitDir = splitDir;
		this.script = script;
		if(script.addr.length==2 || script.addr.length==4){	
			textAddrGetter = new FontPointerNearbyText(splitDir,script);	//字库指针在文本前面20~24字节
		} else {
			textAddrGetter = new AddrManual(script.addr);	//字库指针要手动查找
		}
	}

	@Override
	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException {
		new CommonExporter(script, splitDir, textAddrGetter, scriptAddrGetter).export(cb, ctrl, charset1, englishTexts);
	}

	@Override
	public List<String> import_(Encoding1 enc1, List<Sentence> sentences, List<String> parentChars)
			throws IOException {
		if("tail".equals(script.newfont)){
			return new ImporterScript2WithoutChild(textAddrGetter.getFontPointerOffset(), scriptAddrGetter, splitDir, script)
					.import_(enc1, sentences, parentChars);
		}else if("limit".equals(script.newfont)){
			return new ImporterScript2WithChild(textAddrGetter.getFontPointerOffset(), scriptAddrGetter, splitDir, script)
					.import_(enc1, sentences, parentChars);
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
