package com.scudata.lib.math;

import com.scudata.dm.Sequence;

/**
 * ��ƫ�Ĵ���
 * @author bidalong
 * ԭ��x.pts(p)
 */
public class Pts {
	/**
	 * ��֪x����Сֵm������£�����x.pts(p)
	 * @param x ���У�ͨ����Ŀ���������
	 * @param m Ŀ�����е���Сֵ
	 * @param p ����ptsʱ���õ�ָ��
	 * @return
	 */
	protected static Sequence pts(Sequence x, double m, double p) {
		int n = x.length();
		Sequence result = new Sequence();
		
		for(int i = 1; i <= n; i++){
			Number tmp = (Number) x.get(i);
			result.add(new Double(pts(tmp.doubleValue(), m, p, false)));
		}
		return result;
	}
	
	/**
	 * ��֪x����Сֵm������£�����x.pts(p)����ֱ������x
	 * @param x ���У�ͨ����Ŀ���������
	 * @param m Ŀ�����е���Сֵ
	 * @param p ����ptsʱ���õ�ָ��
	 * @return
	 */
	protected static void ptsSeq(Sequence x, double m, double p, boolean newLn) {
		int n = x.length();		
		for(int i = 1; i <= n; i++){
			Number tmp = (Number) x.get(i);
			x.set(i, Double.valueOf(pts(tmp.doubleValue(), m, p, newLn)));
		}
	}
	
	/**
	 * ��֪Ŀ��������е���Сֵm������£����㵥��Ŀ�����ֵ��pts(p)
	 * @param t ����Ŀ��ֵ
	 * @param m Ŀ�����е���Сֵ
	 * @param p ����ʱ��ʹ�õ�ָ��
	 * @return
	 */
	protected static double pts(double t, double m, double p, boolean newLn) {
		// �������t<m���������t��Ϊ��Сֵ���˴�����Ҫ��Ϊ��reprepareʱ�����ɳ���ֵ��������
		// pΪ0ʱ��ȡ���㷨��sign(x)*ln(abs(x)+1), ������ֵ�޹�
		if (newLn && p == 0) {
			if (t == 0) {
				return 0;
			}
			double pts = Math.log(Math.abs(t) + 1)/Math.log(Math.E);
			if (t < 0) { 
				return -pts;
			}
			return pts;
		}
		if (t < m) {
			t = m;
		}
		if (p == 0) {
			// pΪ0ʱ����ln(abs(x))
			int i = 0;
			if (i > 0) {
				return Math.log(Math.abs(t))/Math.log(Math.E);
			}
			if (m >= 1) {
				return Math.log(t)/Math.log(Math.E);
			}
			else {
				double v = t + Math.abs(m) + 1;
				return Math.log(v)/Math.log(Math.E);
			}
		}
		else {
			// p��Ϊ0ʱ����x��p����
			int i = 0;
			if (i > 0) {
				return Math.pow(t, p);
			}
			if (m >= 1) {
				return Math.pow(t, p);
			}
			else {
				double v = t + Math.abs(m) + 1;
				return Math.pow(v, p);
			}
		}
	}
	
	/**
	 * ��֪x��X.pts(1)������£�����x.pts(p)
	 * @param x ���У�ͨ����Ŀ���������
	 * @param p ����ptsʱ���õ�ָ��
	 * @return
	 */
	protected static Sequence power(Sequence x, double p) {
		int n = x.length();
		Sequence result = new Sequence();
		
		for(int i = 1; i <= n; i++){
			Number tmp = (Number) x.get(i);
			result.add(Double.valueOf(pts(tmp.doubleValue(), 1, p, false)));
		}
		return result;
	}
}
