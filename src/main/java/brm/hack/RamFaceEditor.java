package brm.hack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import brm.Conf;
import common.Util;

//修改表情图,腾出位置给主字库
public class RamFaceEditor {
	
	private static int FACE_START = 0x800D7fd4, FACE1_SIZE=0x550, FACE2_SIZE=0x54C;
	
	//去除表情图片后,扩容字库的起始地址以及可用字节数
	//扩容方案1: 把多出来的5个表情全部存放字库
//	public static final int 
//			EXTCHAR_OFFSET = FACE_START+FACE1_SIZE*4-Conf.SCRIPT1_ADDR,
//			EXTCHAR_CAPACITY = (int) (FACE1_SIZE*5);
	
	//扩容方案2: 4.7个表情存放字库,0.3个表情放其它东西
	public static final int 
			EXTCHAR_OFFSET = FACE_START+FACE1_SIZE*4-Conf.SCRIPT1_ADDR,
			EXTCHAR_CAPACITY = (int)(FACE1_SIZE*4.7),
			OTHER_OFFSET = EXTCHAR_OFFSET+EXTCHAR_CAPACITY,		//TODO: 注意:影响asm/uvRouter.asm
			OTHER_CAPACITY = (int)(FACE1_SIZE*0.3);
	
	private static int[] 
//		FACE_CALM1_POINTER=new int[]{},//entry=800D84F4, no need to modify
		FACE_COMPLAIN1_POINTER=new int[]{0x800d7b94,0x800d7c4c,0x800d7c5c,0x800d7c6c},//entry=800D8A44
		FACE_CALM2_POINTER=new int[]{0x800d7b9c,0x800d7c30,0x800d7c38},//entry=800D8F94
		FACE_CLOSE_EYE_POINTER=new int[]{0x800d7ba4,0x800d7c34,0x800d7c64},//entry=800D94E4
		FACE_SAY_POINTER=new int[]{0x800d7bac,0x800d7c74,0x800d7cb0,0x800d7cb4,0x800d7cb8,0x800d7cbc,0x800d7cc0,0x800d7cf0},//800D9A34
		FACE_SURPRISE_POINTER=new int[]{0x800d7bb4,0x800d7c7c},//800D9F84
		FACE_COMPLAIN2_POINTER=new int[]{0x800d7bbc,0x800d7c60,0x800d7c68},//800DA4D4
		FACE_WAKE_UP_POINTER=new int[]{0x800d7c04},//800DAA20
		FACE_DEAD_POINTER=new int[]{0x800d7c0c,0x800d7d70};//800DAF6C
	
	
	
	public static void rebuildScript1(String splitDir) throws IOException{
		RandomAccessFile file=new RandomAccessFile(splitDir+Conf.SCRIPT1, "rw");
		file.seek(FACE_START-Conf.SCRIPT1_ADDR);
		file.skipBytes(FACE1_SIZE*3);//seek  close eye
		byte[] closeEye=new byte[FACE1_SIZE];
		file.read(closeEye);
		byte[] say=new byte[FACE1_SIZE];
		file.read(say);
		byte[] surprise=new byte[FACE1_SIZE];
		file.read(surprise);
		
		file.seek(FACE_START-Conf.SCRIPT1_ADDR);
		file.skipBytes(FACE1_SIZE);//seek 2nd
		int calm1Entry = FACE_START+32+1280;
		int newCloseEntry = move(file, closeEye);
		int newSayEntry = move(file,say);
		int newSurpriseEntry = move(file,surprise);
		
		replacePointer(file, FACE_COMPLAIN1_POINTER, calm1Entry);
		replacePointer(file, FACE_CALM2_POINTER, calm1Entry);
		replacePointer(file, FACE_CLOSE_EYE_POINTER, newCloseEntry);
		replacePointer(file, FACE_SAY_POINTER, newSayEntry);
		replacePointer(file, FACE_SURPRISE_POINTER, newSurpriseEntry);
		replacePointer(file, FACE_COMPLAIN2_POINTER, calm1Entry);
		replacePointer(file, FACE_WAKE_UP_POINTER, newSurpriseEntry);
		replacePointer(file, FACE_DEAD_POINTER, newSurpriseEntry);
		
		file.close();
	}
	
	
	private static void replacePointer(RandomAccessFile file, int[] oldPointers, int replace) throws IOException{
		for(int i:oldPointers){
			file.seek(i-Conf.SCRIPT1_ADDR);
			byte[] newaddr=Util.toBytes(replace);
			file.write(newaddr[3]);
			file.write(newaddr[2]);
			file.write(newaddr[1]);
			file.write(newaddr[0]);
		}
	}
	
	private static int move(RandomAccessFile file, byte[] face) throws IOException{
		int faceStartMemAddr = (int)(file.getFilePointer())+Conf.SCRIPT1_ADDR;
		int faceImgMemAddr = faceStartMemAddr+32;
		ByteBuffer wrapper=ByteBuffer.wrap(face);
		wrapper.order(ByteOrder.LITTLE_ENDIAN);
		wrapper.position(32+1280+12);
		wrapper.putInt(faceStartMemAddr);
		wrapper.position(wrapper.position()+12);
		wrapper.putInt(faceImgMemAddr);
		file.write(face);
		return faceImgMemAddr+1280;
	}
	
}
