package com.scudata.cellset.graph;

import java.awt.*;
import java.awt.image.*;

import com.scudata.app.common.*;
import com.scudata.cellset.*;
import com.scudata.cellset.graph.config.*;
import com.scudata.cellset.graph.draw.*;
import com.scudata.common.*;
import com.scudata.dm.*;
import java.io.*;
import javax.swing.*;

/**
 * ����ͳ��ͼ�ļ���ʵ��
 * ����ͳ��ͼ�����ͼԪ��ͬ������ͳ��ͼ���ճ���ͳ��ͼ���ú��������ԣ�Ĭ������ϵ
 * ͼԪ�൱�ڻ�ľ�������ò�ͬͼԪ�Լ�����ϳ�������������ͳ��ͼ
 * @author Joancy
 *
 */
public class StatisticGraph {

	/**
	 * ����������ת��Ϊ��ͼǰ����չͼ������
	 * @param prop ��������
	 * @return ��չͼ������
	 */
	public static ExtGraphProperty calc1(PublicProperty prop) {
		ExtGraphProperty catMap = new ExtGraphProperty(prop);
		catMap.setXTitle(prop.getXTitle());
		catMap.setYTitle(prop.getYTitle());
		catMap.setGraphTitle(prop.getGraphTitle());
		catMap.setBarDistance(prop.getBarDistance());
		catMap.setTopData(prop.getTopData());
		catMap.setBackGraphConfig(prop.getBackGraphConfig());

		String str = prop.getDisplayDataFormat();
		if (StringUtils.isValidString(str)) {
			ArgumentTokenizer st = new ArgumentTokenizer(str, ';');
			if (st.hasMoreTokens()) {
				catMap.setDisplayDataFormat1(st.nextToken());
			}
			if (st.hasMoreTokens()) {
				catMap.setDisplayDataFormat2(st.nextToken());
			}
		}

		catMap.setLink(prop.getLink());
		catMap.setLinkTarget(prop.getLinkTarget());

		/** ͳ��ͼ����ɫ������ */
		Palette pltt = Palette.getDefaultPalette();
		str = prop.getColorConfig();
		if (StringUtils.isValidString(str)) {
			Palette pl = Palette.readColor(str);
			if (pl != null) {
				pltt = pl;
			}
		}
		catMap.setPalette(pltt);

		/** ͳ��ֵ��ʼֵ */
		str = prop.getYStartValue();
		if (StringUtils.isValidString(str)) {
			ArgumentTokenizer st = new ArgumentTokenizer(str, ';');
			if (st.hasMoreTokens()) {
				catMap.setYStartValue1(Double.parseDouble(st.nextToken()));
			}
			if (st.hasMoreTokens()) {
				catMap.setYStartValue2(Double.parseDouble(st.nextToken()));
			}
		}

		/** ͳ��ֵ����ֵ */
		str = prop.getYEndValue();
		if (StringUtils.isValidString(str)) {
			ArgumentTokenizer st = new ArgumentTokenizer(str, ';');
			if (st.hasMoreTokens()) {
				catMap.setYEndValue1(Double.parseDouble(st.nextToken()));
			}
			if (st.hasMoreTokens()) {
				catMap.setYEndValue2(Double.parseDouble(st.nextToken()));
			}
		}
		/** ͳ��ֵ��ǩ��� */
		str = prop.getYInterval();
		if (StringUtils.isValidString(str)) {
			ArgumentTokenizer st = new ArgumentTokenizer(str, ';');
			if (st.hasMoreTokens()) {
				catMap.setYInterval1(Double.parseDouble(st.nextToken()));
			}
			if (st.hasMoreTokens()) {
				catMap.setYInterval2(Double.parseDouble(st.nextToken()));
			}
		}

		/** ͳ��ֵ���ٿ̶��� */
		catMap.setYMinMarks(prop.getYMinMarks());
		catMap.setTitleMargin(prop.getTitleMargin());
		catMap.setXInterval(prop.getXInterval());
		return catMap;
	}

	/**
	 * ������ͼ����ָ����ʽתΪͼ������
	 * @param bi ����ͼ��
	 * @param imageFmt ͼƬ��ʽ
	 * @return ͼ������
	 * @throws Exception
	 */
	public static byte[] getImageBytes(BufferedImage bi, byte imageFmt)
			throws Exception {
		byte[] bytes = null;
		switch (imageFmt) {
		case GraphProperty.IMAGE_GIF:
			bytes = ImageUtils.writeGIF(bi);
			break;
		case GraphProperty.IMAGE_JPG:
			bytes = ImageUtils.writeJPEG(bi);
			break;
		case GraphProperty.IMAGE_PNG:
			bytes = ImageUtils.writePNG(bi);
			break;
		}
		return bytes;
	}

	private static byte[] getFileBytes(String picFile) {
		if (picFile == null || picFile.trim().length() == 0) {
			return null;
		}
		InputStream fis = null;
		try {
			File file = new File(picFile);
			if (file.exists()) { // ����·����ʾ���ļ�
				fis = new FileInputStream(picFile);
			} else {
				String paths[] = Env.getPaths();
				if (paths != null && paths.length > 0) {
					file = new File(paths[0], picFile);
					fis = new FileInputStream(file);
				}
			}

			if (fis == null) {
				return null;
			}
			return AppUtil.getStreamBytes(fis);
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ִ�б���ͼ����
	 * @param g ͼ���豸
	 * @param egp ��չͼ������
	 * @param w ���
	 * @param h �߶�
	 */
	public static void drawBackGraph(Graphics2D g, ExtGraphProperty egp,
			int w, int h) {
		BackGraphConfig bgc = egp.getBackGraphConfig();
		if (bgc == null) {
			return;
		}
		byte[] b = getFileBytes(bgc.getValue());
		if (b != null) {
			bgc.setImageBytes(b);
		}

		byte[] backImage = bgc.getImageBytes();
		if (backImage == null) {
			return;
		}
		Image image = new ImageIcon(backImage).getImage();
		g.drawImage(image, 1, 1, w, h, null);
	}

}
