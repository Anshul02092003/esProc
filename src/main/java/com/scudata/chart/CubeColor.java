package com.scudata.chart;

import java.awt.*;
import java.util.ArrayList;

import com.scudata.dm.*;

/**
 * ������ɫ������Ϊĳһ����ɫ����ǰ��front������top������right ��6ɫ��
 * ������ɫ1Ϊǳɫ��2Ϊ��ɫ
 * CubeColor���������򣬴���������T1,F1,T2,F2,R1,R2
 * @author Joancy
 * 
 */
public class CubeColor {
	// 1Ϊǳɫ��2Ϊ��ɫ;ȱʡ����ɫΪ��ɫ
	Color f1 = new Color(204, 213, 194);// ������ɫ
	Color f2 = new Color(116, 126, 104);
	Color t1 = new Color(225, 230, 218);// ������ɫ
	Color t2 = new Color(146, 158, 130);
	Color r1 = new Color(106, 115, 93);// �Ҳ�����ɫ
	Color r2 = new Color(83, 90, 73);
	
//	��ԭʼ��ɫ��������6�����ͶȲ�ͬ����ɫ
	private Color origin=null;
	float f1s = 0.55f;
	float f2s = 0.65f;
	float t1s = 0.35f;
	float t2s = 0.85f;
	float r1s = 0.75f;
	float r2s = 0.35f;
	/**
	 * ����һ��ȱʡ�������������ɫ��
	 */
	public CubeColor() {
	}

	/**
	 * ʹ��ָ������ɫc1��������������ɫ��
	 * @param c1 ����ԭʼ��ɫ
	 */
	public CubeColor(Color c1) {
		origin = c1;
		if(c1!=null){
			f1 = getLight( f1s );
			f2 = getDark( f2s );
			t1 = getLight( t1s );
			t2 = getDark( t2s );
			r1 = getDark( r1s );
			r2 = getDark( r2s );
		}else{
			f1 = null;
			f2 = null;
			t1 = null;
			t2 = null;
			r1 = null;
			r2 = null;
		}
	}

	/**
	 * ��ȡԭʼ��ɫ
	 * @return ��ɫֵ
	 */
	public Color getOrigin(){
		return origin;
	}
	
	/**
	 * ��ȡǰ��ǳɫ
	 * @return ��ɫֵ
	 */
	public Color getF1() {
		return f1;
	}
	
	/**
	 * ��ȡ����ڶ���õ���ɫ��ȡ������������ɫ���Ժ�ͬ
	 * @param relative �����ɫ�����ƣ�(F1,F2, T1,T2, R1,R2)
	 * @param degree����F1��ɫ���ļ���
	 * @return ��ɫֵ
	 */
	public Color getRelativeBrighter(String relative,int degree) {
		float deltaFactor = degree*0.05f;
		if( relative.equalsIgnoreCase("F1")){
			return getLight(f1s - deltaFactor);
		}
		if( relative.equalsIgnoreCase("T1")){
			return getLight(t1s - deltaFactor);
		}
		float tmpFactor;
		if( relative.equalsIgnoreCase("F2")){
			tmpFactor = f2s;
		}else if(relative.equalsIgnoreCase("T2")){
			tmpFactor = t2s;
		}else if(relative.equalsIgnoreCase("R1")){
			tmpFactor = r1s;
		}else{//(relative.equalsIgnoreCase("R2")){
			tmpFactor = r2s;
		}
		return getDark(tmpFactor + deltaFactor);
	}

	/**
	 * �ο�getRelativeBrighter�÷�����ȡ������ɫ
	 * @param relative
	 * @param degree
	 * @return
	 */
	public Color getRelativeDarker(String relative, int degree) {
		float deltaFactor = degree*0.05f;
		if( relative.equalsIgnoreCase("F1")){
			return getLight(f1s + deltaFactor);
		}
		if( relative.equalsIgnoreCase("T1")){
			return getLight(t1s + deltaFactor);
		}
		float tmpFactor;
		if( relative.equalsIgnoreCase("F2")){
			tmpFactor = f2s;
		}else if(relative.equalsIgnoreCase("T2")){
			tmpFactor = t2s;
		}else if(relative.equalsIgnoreCase("R1")){
			tmpFactor = r1s;
		}else{//(relative.equalsIgnoreCase("R2")){
			tmpFactor = r2s;
		}
		return getDark(tmpFactor - deltaFactor);
	}

	/**
	 * ��ȡǰ����ɫ
	 * @return ��ɫֵ
	 */
	public Color getF2() {
		return f2;
	}

	/**
	 * ��ȡ����ǳɫ
	 * @return ��ɫֵ
	 */
	public Color getT1() {
		return t1;
	}

	/**
	 * ��ȡ������ɫ
	 * @return ��ɫֵ
	 */
	public Color getT2() {
		return t2;
	}

	/**
	 * ��ȡ����ǳɫ
	 * @return ��ɫֵ
	 */
	public Color getR1() {
		return r1;
	}

	/**
	 * ��ȡ������ɫ
	 * @return ��ɫֵ
	 */
	public Color getR2() {
		return r2;
	}
	
	//ϵ��ԽС����ɫԽdark
	public Color getDark(float intensity) {
		return getDarkColor(origin,intensity);
	}

	/**
	 * ��ȡԭʼ��ɫ����ɫ�ʣ���Ϊ��ɫҪ�õ��ܶ����ɫ��
	 * �����ɫ��ɫ�ȼ�����ɫ��ʹ����ɫ���ŵ�Ч�����ֲ�����
	 * �ø÷�����ȡ�������ʹ������ɫ����ɫֵ
	 * @param origin ԭʼ��ɫ
	 * @return ���������ʹ������ɫ����ɫֵ
	 */
	public static Color getDazzelColor(Color origin){
		CubeColor cc = new CubeColor(origin);
		if( cc.getT1().equals(origin)) return cc.getT2();
		if( cc.getR2().equals(origin)) return cc.getR1();
		return origin;
	}
	

	public static Color getDarkColor(Color sourceHexColor, float intensity) {
		intensity = (((intensity > 1) || (intensity < 0)) ? 1 : (intensity));
		int _local2 = noAlphaRGB(sourceHexColor.getRGB());
		double _local3 = Math.floor(_local2 / 65536);
		double _local4 = Math.floor((_local2 - (_local3 * 65536)) / 256);
		double _local5 = (_local2 - (_local3 * 65536)) - (_local4 * 256);

		int r = (int) (_local3 * intensity);
		int g = (int) (_local4 * intensity);
		int b = (int) (_local5 * intensity);
		return new Color(r, g, b);
	}

	public static int noAlphaRGB(int rgba) {
		int tmp = 0x00FFFFFF;
		return tmp & rgba;
	}
	//ϵ��ԽС����ɫԽlight
	public Color getLight(float intensity) {
		return getLightColor(origin,intensity);
	}
	
	public static Color getLightColor(Color sourceHexColor, float intensity) {
		intensity = (((intensity > 1) || (intensity < 0)) ? 1 : (intensity));
		int _local2 = noAlphaRGB(sourceHexColor.getRGB());
		double _local3 = Math.floor(_local2 / 65536);
		double _local4 = Math.floor((_local2 - (_local3 * 65536)) / 256);
		double _local5 = (_local2 - (_local3 * 65536)) - (_local4 * 256);
		int r = (int) (256 - (256 - _local3) * intensity);
		int g = (int) (256 - (256 - _local4) * intensity);
		int b = (int) (256 - (256 - _local5) * intensity);
		return new Color(r, g, b);
	}

	public static void main(String[] args) {
		CubeColor cc = new CubeColor(new Color(255, 255, 255));
		
		System.out.println(cc.getF1());
		System.out.println(cc.getF2());
		System.out.println(cc.getT1());
		System.out.println(cc.getT2());
		System.out.println(cc.getR1());
		System.out.println(cc.getR2());
	}
}
