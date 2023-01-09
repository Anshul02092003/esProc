package com.scudata.lib.math.prec;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;

/**
 * �����쳣ֵ������¼
 * @author bd
 */
public class SmRec implements Externalizable {
	private static final long serialVersionUID = -8790897480565951496L;
	private Sequence X;
	private Sequence X1;
	// ƽ������ӦĿ��ֵ�ļ�¼���Զ�ֵ��������ʵ����1
	private int tv = 1;

	public SmRec() {
	}
	
	public Sequence getX() {
		return X;
	}
	
	public void setX(Sequence x) {
		X = x;
	}
	
	public Sequence getX1() {
		return X1;
	}
	
	public void setX1(Sequence x1) {
		X1 = x1;
	}
	
	public int getTv() {
		return this.tv;
	}
	
	public void setTv(int v) {
		this.tv = v;
	}
	
	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(3);
		seq.add(this.X);
		seq.add(this.X1);
		seq.add(tv);
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public SmRec init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 1) {
			throw new RQException("Can't get the Smothing Record, invalid data.");
		}
		Object o1 = seq.get(1);
		if (o1 instanceof Number) {
			// SmMulRec
			SmMulRec smr = new SmMulRec();
			smr.init(seq);
			return smr;
		}
		if (size < 3) {
			throw new RQException("Can't get the Smothing Record, invalid data.");
		}
		this.X = (Sequence) seq.get(1);
		this.X1 = (Sequence) seq.get(2);
		this.tv = ((Number) seq.get(3)).intValue();
		return this;
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 2;
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		
		this.X = (Sequence) in.readObject();
		this.X1 = (Sequence) in.readObject();
		if (ver > 1) {
			this.tv = in.readInt();
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		
		out.writeObject(this.X);
		out.writeObject(this.X1);
		out.writeInt(this.tv);
	}
}
