package brm.script;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import brm.Script;
import brm.dump.Charset1;
import brm.dump.Ctrl;
import brm.hack.Encoding1;
import brm.hack.Sentence;
import common.Util;
	
public class ScriptHandlerSC04_020_04 implements Importer,Exporter{
	
	String splitDir;
	Script script;
	
	protected TextAddrGetter textAddrGetter;
	protected ScriptAddrGetter scriptAddrGetter = new Script2Addr();
	
	public ScriptHandlerSC04_020_04(String splitDir, Script script) {
		this.splitDir = splitDir;
		this.script = script;
		textAddrGetter = new AddrManual(script.addr);
	}

	@Override
	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException {
		new CommonExporter(script, splitDir, textAddrGetter, scriptAddrGetter).export(cb, ctrl, charset1, englishTexts);
	}

	@Override
	public List<String> import_(Encoding1 enc1, List<Sentence> sentences, List<String> parentChars)
			throws IOException {
		List<String> ret=new ImporterScript2WithChild(textAddrGetter.getFontPointerOffset(), scriptAddrGetter, splitDir, script)
				.import_(enc1, sentences, parentChars);
		Util.copyFile(splitDir+script.file, splitDir+"SC04/021/0.4");
		return ret;
	}
}
