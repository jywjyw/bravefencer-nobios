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

public interface Importer{
	public List<String> import_(Encoding1 enc1, List<Sentence> sentences, List<String> parentChars) throws IOException;
}

