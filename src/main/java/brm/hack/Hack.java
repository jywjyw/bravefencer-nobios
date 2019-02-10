package brm.hack;

import java.io.File;

import brm.Conf;
import brm.ScriptConfigLoader;
import brm.picture.AllPicture;
import brm.script.ScriptHandlerMAIN_010_11;
import common.IsoPatcher;
import common.Util;

public class Hack {
	
	public static void main(String[] args) throws Exception {
		String exe=Conf.outdir+Conf.EXE;
		Util.copyFile(Conf.jpdir+Conf.EXE, exe);
		String splitdir = Conf.desktop+"\\brmjp\\";
//		new CdSplitter(splitdir).split(Conf.jpdir);
		
		RamFaceEditor.rebuildScript1(splitdir);	//操作SCRIPT1,可重复写入
		
		ScriptConfigLoader scriptConfig = new ScriptConfigLoader("jp",splitdir);
		File excel = new File(Conf.getTranslateFile("brm-jp-v5.xlsx"));
		
		Encoding1 enc1 = new Encoding1(RamFaceEditor.EXTCHAR_OFFSET, RamFaceEditor.EXTCHAR_CAPACITY);
		new ScriptHandlerMAIN_010_11(splitdir, scriptConfig.main).import_(excel, enc1);		//import texts and rewrite main font
		new ScriptsImporter().importFrom(excel, splitdir,scriptConfig, enc1);
		enc1.saveAsTbl(Conf.desktop+"新主码表.tbl");
		
		EncodingMenu encMenu=new EncodingMenu();
		new MenuImporter().import_(splitdir, excel, encMenu);
		encMenu.saveAsTbl(Conf.desktop+"新菜单码表.tbl");
		
		if(encMenu.checkErr()!=null) ErrMsg.add(encMenu.checkErr());
		ErrMsg.checkErr();
		
		new MenuFontLibBuilder().rebuild(splitdir, encMenu.chars);
		MenuFontAsmHack.hack(splitdir);
		new VramFaceEditor().edit(splitdir);
		new AllPicture().import_(splitdir);
		new CdReducer().reduce(splitdir);
//		CheatCode.cheat(exe);
		
		CdRebuilder.rebuild(splitdir, Conf.outdir);
		IsoPatcher.patch(Conf.outdir, Conf.outdir+"brave-hack.iso");
		System.out.println("all complete, use ePSXe to run cd : "+Conf.outdir+"brave-hack.iso");
	}
}
