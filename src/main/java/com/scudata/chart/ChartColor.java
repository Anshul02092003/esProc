package com.scudata.chart;

import java.awt.*;

import com.scudata.dm.*;

/**
 * �������������ɫ����ɫ��
 * @author Joancy
 *
 */
public class ChartColor {
	public static Integer transparentColor = new Integer(0xffffff);
//	�������
	int type = Consts.PATTERN_DEFAULT;
	
//�Ƿ�Ϊ����ɫ
	boolean isGradient = true;

//	����ǽ���ɫ������ɫ1���ǽ���ɫʱֻʹ����ɫ1
	Color color1 = (Color) Para.getDefPalette().get(0);
	
//	����ǽ���ɫ������ɫ2
	Color color2 = color1;

	//����ɫ�ĽǶȷ���	
	int angle = 0;
	// ��color1ΪԲ�ģ����Ƕ�angle��color2�ݶ�
	// ��ɫ����˵����
	// 1: ��isGradient��falseʱ��ͼ��ʹ�ô�ɫcolor1��䣻
	// 2: ��
	// isGradient��trueʱ��color2����color1����ʹ��color1Ϊ������cubeColor
	// ����ʹ�ô�color1��color2�Ľ���ɫ

	/**
	 * ����һ��ȱʡ�������ɫ��
	 */
	public ChartColor() {
	}

	/**
	 * ʹ�õ�һ��ɫc����һ���򵥵������ɫ��
	 */
	public ChartColor(Color c) {
		color1 = c;
		color2 = c;
	}

	/**
	 * ʹ�õ�һ��ɫc(RGBֵ)����һ���򵥵������ɫ��
	 */
	public ChartColor(int c) {
		setColor1(c);
		setColor2(c);
	}
/**
 * �����������
 * @param type ���Ͳο�Consts.PATTERN��ͷ�ĳ�����PATTERN_DEFAULTȱʡģʽ������ɫ��䣬����ģʽ���ںڰ�ͼ��ʱ�ĸ������͡�
 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * ��ȡ�������
	 * @return �������
	 */
	public int getType() {
		return type;
	}

/**
 * ���ý���ɫ��䣬����ɫʱ��color1һ�㲻�ܸ�color2��ͬ������ûЧ��
 * @param gradient �Ƿ񽥱���ɫ
 */
	public void setGradient(boolean gradient) {
		this.isGradient = gradient;
	}

	/**
	 * �Ƿ�Ϊ����ɫ���
	 * @return ʹ�ý���ɫʱ����true������false
	 */
	public boolean isGradient() {
		//������õ�͸��ɫ��û���źͽ���Ч��
		if( color1==null ) return false;
		return isGradient;
	}

	/**
	 * �Ƿ�ʹ������ɫ����ʹ��CubeColor������ɫָ����ͼ�ĸ������棬ʹ�ò�ͬ���Ͷȵ���ɫ������������ʹ���������������Ƚ��š�
	 * @return ʹ������ɫʱ����true������false
	 */
	public boolean isDazzle(){
		if( color1==null ) return false;
		return isGradient && color1.equals(color2);
	}
	
	/**
	 * ������ɫ1
	 * @param color ��ɫֵ
	 */
	public void setColor1(Color color) {
		this.color1 = color;
	}

	/**
	 * ʹ��RGBֵ������ɫ1
	 * @param color ��ɫֵ
	 */
	public void setColor1(int color) {
		if(color==transparentColor){
			setColor1(null);
		}else{
			setColor1(new Color(color));
		}
	}

	/**
	 * ������ɫ2
	 * @param color ��ɫֵ
	 */
	public void setColor2(Color color) {
		this.color2 = color;
	}

	/**
	 * ʹ��RGBֵ������ɫ2
	 * @param color ��ɫֵ
	 */
	public void setColor2(int color) {
		if(color==transparentColor){
			setColor2(null);
		}else{
			setColor2(new Color(color));
		}
	}

	/**
	 * ��ȡ��ɫ1
	 * @return Color��ɫ
	 */
	public Color getColor1() {
		return color1;
	}

	/**
	 * ��ȡ��ɫ2
	 * @return Color��ɫ
	 */
	public Color getColor2() {
		return color2;
	}

	/**
	 * ���ý�����ɫ�Ľ���Ƕ�
	 * @param angle �Ƕ�ֵ
	 */
	public void setAngle(int angle) {
		this.angle = angle;
	}

	/**
	 * ��ȡ������ɫ�Ľ���Ƕ�
	 * @return �Ƕ�ֵ
	 */
	public int getAngle() {
		return angle;
	}

	/**
	 * �������������������plot������������ 
	 * @return ����SPL�﷨�Ĵ�����
	 */
	public String toPlotString() {
		Sequence seq = new Sequence();
		seq.add("ChartColor");
		seq.add(new Integer(type));
		seq.add(new Boolean(isGradient));
		if (color1 == null) {
			seq.add( transparentColor );
		} else {
			seq.add(new Integer(color1.getRGB()));
		}
		if (color2 == null) {
			seq.add(transparentColor);
		} else {
			seq.add(new Integer(color2.getRGB()));
		}
		seq.add(new Integer(angle));
		return seq.toString();
	}

	/**
	 * �����ı���������ͬ����toPlotString
	 */
	public String toString() {
		return toPlotString();
	}

	/**
	 * SPL�﷨�Ĵ��������γ����к���ʵ����Ϊ����
	 * @param seq ��ɫ����������
	 * @return ʵ������ı������
	 */
	public static ChartColor getInstance(Sequence seq) {
		ChartColor cc = new ChartColor();
		cc.setType(((Number) seq.get(2)).intValue());
		cc.setGradient(((Boolean) seq.get(3)).booleanValue());
		Object obj = seq.get(4);
		if (obj != null) {
			cc.setColor1(((Number) obj).intValue());
		}
		obj = seq.get(5);
		if (obj != null) {
			cc.setColor2(((Number) obj).intValue());
		}
		cc.setAngle(((Number) seq.get(6)).intValue());
		return cc;
	}

	/**
	 * ��ȿ�¡һ��ChartColor����
	 * @return ��¡��������ɫ��
	 */
	public ChartColor deepClone() {
		ChartColor cc = new ChartColor();
		cc.setType(type);
		cc.setGradient(isGradient);
		cc.setColor1(color1);
		cc.setColor2(color2);
		cc.setAngle(angle);
		return cc;
	}
}
