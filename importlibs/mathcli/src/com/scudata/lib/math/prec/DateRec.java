package com.scudata.lib.math.prec;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Date;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;

/**
 * ��������������¼
 * @author bd
 *
 */
public class DateRec extends VarRec {
	private static final long serialVersionUID = -7051806699361192538L;
	private byte dateType = Consts.DCT_DATE;
	private ArrayList<VarRec> deriveRecs = new ArrayList<VarRec>();
	private Date now = null;
	
	public DateRec() {
	}

	public DateRec(VarSrcInfo vi) {
		super(false, false, vi);
		super.setType(Consts.F_DATE);
	}
	
	/**
	 * ��ȡ�������ݵ�����
	 * @return ����Consts��������DCT_DATETIME��DCT_DATE��DCT_TIME����DCT_UDATE
	 */
	public byte getDateType() {
		return this.dateType;
	}
	
	/**
	 * �趨�������ݵ����ͣ�����Consts��������DCT_DATETIME��DCT_DATE��DCT_TIME����DCT_UDATE
	 * @param dateType
	 */
	public void setDateType(byte type) {
		this.dateType = type;
	}
	
	/**
	 * ��ȡ�����������ֶεļ�¼�б��Ͳ���˳��һһ��Ӧ�����������ڲ�ֵ��
	 * @return
	 */
	public ArrayList<VarRec> getDeriveRecs() {
		return deriveRecs;
	}
	
	/**
	 * ���һ�������������ֶεļ�¼
	 * @return
	 */
	public void addDeriveRecs(VarRec vr) {
		this.deriveRecs.add(vr);
	}
	
	/**
	 * ��ȡ��ģʱʹ�õ�now
	 * @return
	 */
	public Date getNow() {
		return this.now;
	}
	
	/**
	 * ���ý�ģʱʹ�õ�now
	 * @param now
	 */
	public void setNow(Date now) {
		this.now = now;
	}

	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(3);
		seq.add(this.dateType);
		long time = this.now == null ? 0 : this.now.getTime();
		seq.add(time);
		int len = this.deriveRecs.size();
		Sequence subSeq = new Sequence(len);
		for (int i = 0; i < len; i++) {
			VarRec vr = this.deriveRecs.get(i);
			if (vr == null) {
				subSeq.add(null);
			}
			else {
				subSeq.add(vr.toSeq());
			}
		}
		seq.add(subSeq);
		
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public void init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 3) {
			throw new RQException("Can't get the Fill NA Value Record, invalid data.");
		}
		this.dateType = ((Number) seq.get(1)).byteValue();
		long time = ((Number) seq.get(2)).longValue();
		this.now = new Date(time);
		Sequence subSeq = (Sequence) seq.get(3);
		int len = subSeq == null ? 0 : subSeq.length();
		this.deriveRecs = new ArrayList<VarRec>();
		for (int i = 0; i < len; i++) {
			Sequence rec = (Sequence) subSeq.get(1 + i);
			if (rec == null) {
				this.deriveRecs.add(null);
			}
			else {
				VarRec vr = new VarRec(false, false, null);
				vr.init(rec);
				this.deriveRecs.add(vr);
			}
		}
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 1;
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		
		super.readExternal(in);

		this.dateType = in.readByte();
		long time = in.readLong();
		this.now = new Date(time);
		int len = in.readInt();
		this.deriveRecs = new ArrayList<VarRec>(len);
		for (int i = 0; i < len; i++) {
			this.deriveRecs.add((VarRec) in.readObject());
		}
		if (ver > 1) {
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		
		super.writeExternal(out);
		
		out.writeByte(this.dateType);
		long time = this.now == null ? 0 : this.now.getTime();
		out.writeLong(time);
		int len = this.deriveRecs.size();
		out.writeInt(len);
		for (int i = 0; i < len; i++) {
			out.writeObject(this.deriveRecs.get(i));
		}
	}
}
