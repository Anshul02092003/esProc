package com.scudata.chart.element;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.scudata.chart.*;
import com.scudata.chart.edit.*;
import com.scudata.common.ImageUtils;

/**
 * ����ͼԪ
 * �Ȼ�������ɫ���ٻ�����ͼ���������ͼ��͸���ģ����߲Żᶼ��Ч��
 * ����ͼƬ���ס��ɫ 
 * @author Joancy
 *
 */
public class BackGround extends ObjectElement {
	public ChartColor backColor = new ChartColor(Color.white);
	public float transparent = 1f;
	public boolean visible = true;
	// ͼƬ����
	public Para imageValue = new Para();
	// ͼƬ���ģʽ
	public int imageMode = Consts.MODE_NONE;

	/**
	 * ͼԪ�Ƿ�ɼ�
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * ����ͼ�����Ȼ��ƣ��ò����
	 */
	public void beforeDraw() {
	}

	/**
	 * ����ͼ�����Ȼ��ƣ��ò����
	 */
	public void draw() {
	}
	
	/**
	 * ��ȡͼ���ԭʼ���
	 * @return ��͸ߵ�����ֵ
	 */
	public int[] getOrginalWH(){
		Object imgVal = imageValue.getValue();
		byte[] imageBytes = Utils.getFileBytes(imgVal);

		if (imageBytes == null)
			return null;
		Image image = new ImageIcon(imageBytes).getImage();
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);
		return new int[]{iw,ih};
	}

	/**
	 * ���������
	 */
	public void drawBack() {
		if (!isVisible()) {
			return;
		}
		Graphics2D g = e.getGraphics();
		int w = e.getW();
		int h = e.getH();
		Rectangle rect = new Rectangle(0, 0, w, h);
		if (Utils.setPaint(g, 0, 0, w, h, backColor)) {
			Utils.fillPaint(g, rect, transparent);
		}
		Object imgVal = imageValue.getValue();
		byte[] imageBytes = Utils.getFileBytes(imgVal);

		if (imageBytes == null)
			return;
		Image image = new ImageIcon(imageBytes).getImage();

		switch (imageMode) {
		case Consts.MODE_NONE:
			ImageUtils.drawFixImage(g, image, 0, 0, w, h);
			break;
		case Consts.MODE_FILL:
			g.drawImage(image, 0, 0, w, h, null);
			break;
		case Consts.MODE_TILE:
			int iw = image.getWidth(null);
			int ih = image.getHeight(null);
			int x = 0,
			y = 0;
			while (x < w) {
				y = 0;
				while (y < h) {
					ImageUtils.drawFixImage(g, image, x, y, w, h);
					y += ih;
				}
				x += iw;
			}
			break;
		}

	}

	/**
	 * ǰ������ƣ����Ըò�
	 */
	public void drawFore() {
	}

	/**
	 * ÿ��ͼԪ����Ҫ���ȵĿɱ༭������Ϣ�б�
	 * @return ParamInfoList ��ǰͼԪ�Ĳ�����Ϣ�б�
	 */
	public ParamInfoList getParamInfoList() {
		ParamInfoList paramInfos = new ParamInfoList();
		ParamInfo.setCurrent(BackGround.class, this);

		paramInfos.add(new ParamInfo("backColor", Consts.INPUT_CHARTCOLOR));
		paramInfos.add(new ParamInfo("transparent", Consts.INPUT_DOUBLE));
		paramInfos.add(new ParamInfo("visible", Consts.INPUT_CHECKBOX));

		paramInfos.add(new ParamInfo("imageValue"));
		paramInfos.add(new ParamInfo("imageMode", Consts.INPUT_IMAGEMODE));

		return paramInfos;
	}

	/**
	 * ͼԪ������ɺ󣬲���������shape
	 * ��ǰͼԪ������
	 * @return Shape null
	 */
	public ArrayList<Shape> getShapes() {
		return null;
	}

	/**
	 * ͼԪ������ɺ󣬲����ĳ������б�
	 * ��ǰͼԪ������
	 * @return null
	 */
	public ArrayList<String> getLinks() {
		return null;
	}

	protected transient Engine e;

	/**
	 * ����ͼ������
	 */
	public void setEngine(Engine e) {
		this.e = e;
		Utils.setParamsEngine(this);
	}

	/**
	 * ��ȡ��ǰͼ������
	 * @return ����
	 */
	public Engine getEngine() {
		return e;
	}
}
