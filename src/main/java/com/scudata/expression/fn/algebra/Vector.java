package com.scudata.expression.fn.algebra;

import com.scudata.dm.Sequence;

public class Vector {

	private double[] vector;
	
	/**
	 * ��ʼ������
	 * @param value	�����ʾ������ֵ
	 */
	public Vector(double[] value) {
		this.vector = value;
	}
	
	/**
	 * ��ʼ������
	 * @param value	��ά�����ʾ�ľ���ֵ
	 */
	public Vector(Sequence seq) {
		if (seq.length() > 0){
			this.vector = Matrix.getRow(seq, 0);
		}
	}
	
	/**
	 * ��������
	 * @return
	 */
	public int len() {
		if (this.vector == null) return 0;
		return this.vector.length;
	}
	
	/**
	 * ָ����ţ���ȡ������Աֵ����ֵ����0
	 * @param i	��ţ���0��ʼ
	 * @return
	 */
	public double get(int i) {
		if (this.len() > i) {
			return this.vector[i];
		}
		return 0;
	}
	
	/**
	 * ��ȡ����
	 * @return
	 */
	public double[] getValue() {
		return this.vector;
	}
    
    /**
     * �������з���
     * @return
     */
    public Sequence toSequence() {
    	int rows = this.vector.length;
    	Sequence seq = new Sequence(rows);;
        for(int i=0, iSize = this.vector.length; i<iSize; i++){
        	seq.add(Double.valueOf(this.vector[i]));
        }
        return seq;
    }
}
