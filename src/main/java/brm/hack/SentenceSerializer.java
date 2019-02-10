package brm.hack;

import java.nio.ByteBuffer;
import java.util.Arrays;

import brm.dump.Ctrl;
import brm.dump.SentenceSplitter;
import brm.dump.SentenceSplitter.Callback;

public class SentenceSerializer {
	
	Encoding1 enc1;
	Encoding2 enc2;
	
	public SentenceSerializer(Encoding1 enc1) {
		this.enc1 = enc1;
	}
	public SentenceSerializer(Encoding1 enc1, Encoding2 enc2) {
		this.enc1 = enc1;
		this.enc2 = enc2;
	}

	public byte[] toBytes(String sentence){
		ByteBuffer buf = ByteBuffer.allocate(2048);
		new SentenceSplitter().splitToWords(sentence, new Callback() {
			
			@Override
			public void onReadWord(boolean isCtrl, String word) {
				if(isCtrl){
					buf.put(Ctrl.encode(word));
				} else {
					byte[] code = enc1.getCode(word);
					if(code == null){
						if(enc2==null) {
							code=enc1.put(word);
							buf.put(code);
						} else{
							Integer i=enc2.getCode(word);
							if(i==null) i=enc2.put(word);
							buf.put((byte)(i>>>8&0xff));
							buf.put((byte)(i&0xff));
						}
					} else {
						buf.put(code);
					}
				}
			}
		});
		buf.put((byte)0);//末尾加上结束符
		return Arrays.copyOfRange(buf.array(), 0, buf.position());
	}
}
