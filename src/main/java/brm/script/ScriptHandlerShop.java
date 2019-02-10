package brm.script;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import brm.Script;
import brm.dump.Charset1;
import brm.dump.Ctrl;
import brm.hack.Encoding1;
import brm.hack.Sentence;

public class ScriptHandlerShop implements Exporter,Importer{
	
	String splitDir;
	Script script;
	
	protected TextAddrGetter addrGetter;
	protected ScriptAddrGetter scriptAddrGetter;
	
	public ScriptHandlerShop(String splitDir, Script script) {
		this.splitDir = splitDir;
		this.script = script;
		scriptAddrGetter = new Script3Addr(script);
		if(script.addr.length==2 || script.addr.length==4){	
			addrGetter = new FontPointerNearbyText(splitDir,script);	//字库指针在文本前面20~24字节
		} else {
			addrGetter = new AddrManual(script.addr);	//字库指针要手动查找
		}
	}

	@Override
	public void export(Callback cb, Ctrl ctrl, Charset1 charset1, Map<String,String> englishTexts) throws IOException {
		new CommonExporter(script, splitDir, addrGetter, scriptAddrGetter).export(cb, ctrl, charset1,englishTexts);
	}

	//进出商店时,字库指针会改变和还原
	@Override
	public List<String> import_(Encoding1 enc1, List<Sentence> sentences, List<String> parentChars) throws IOException {
		return new ImporterScript3Shop(addrGetter.getFontPointerOffset(), scriptAddrGetter, splitDir, script)
				.import_(enc1, sentences, parentChars);
	}
}
