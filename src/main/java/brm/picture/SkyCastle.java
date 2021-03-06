package brm.picture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import brm.Conf;
import common.Img4bitUtil;
import common.Palette;

public class SkyCastle implements PicHandler {
	
	@Override
	public void export(String splitDir, String exportDir) throws Exception {
		Palette pal = new Palette(16, Conf.getRawFile("368-259"));
		FileInputStream file = new FileInputStream(splitDir+"SC06/006/1.0");
		BufferedImage[] tiles = new BufferedImage[3];
		int i=0;
		file.skip(230*TILE);
		tiles[i++] = Img4bitUtil.readRomToBmp(file, 32, 32, pal);
		file.skip(14*TILE);
		tiles[i++] = Img4bitUtil.readRomToBmp(file, 32, 32, pal);
		file.skip(13*TILE);
		tiles[i++] = Img4bitUtil.readRomToBmp(file, 32, 32, pal);
		ImageIO.write(Img4bitUtil.jointTiles(Arrays.asList(tiles),3), "bmp", new File(exportDir+SkyCastle.class.getSimpleName()+".bmp"));
		file.close();
	}

	@Override
	public void import_(String splitDir) throws IOException {//TODO 图像似乎不正确,要单独导入测试
		Palette pal = new Palette(16, Conf.getRawFile("368-259"));
		BufferedImage img=ImageIO.read(new File(Conf.getRawFile("pic/"+SkyCastle.class.getSimpleName()+".bmp")));
		List<BufferedImage> tiles=Img4bitUtil.splitToTiles(img, 32, 32);
		RandomAccessFile file = new RandomAccessFile(splitDir+"SC06/006/1.0","rw");
		file.skipBytes(230*TILE);
		file.write(Img4bitUtil.toVramImg(tiles.get(0), pal).data);
		file.skipBytes(14*TILE);
		file.write(Img4bitUtil.toVramImg(tiles.get(1), pal).data);
		file.skipBytes(13*TILE);
		file.write(Img4bitUtil.toVramImg(tiles.get(2), pal).data);
		file.close();
	}

}
