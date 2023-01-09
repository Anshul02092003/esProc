package com.scudata.lib.math.prec;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;

/**
 * ���ڲ�ֵ����������¼
 * @author bd
 *
 */
public class DiMvpRec extends VarRec {
	private static final long serialVersionUID = 8280418163541492457L;
	private ArrayList<VarRec> intervalRecs = new ArrayList<VarRec>();
	private ArrayList<String> interval1 = new ArrayList<String>();
	private ArrayList<String> interval2 = new ArrayList<String>();

	private ArrayList<VarRec> mvpRecs = new ArrayList<VarRec>();
	private ArrayList<ArrayList<String>> mvpCns = new ArrayList<ArrayList<String>>();

	public DiMvpRec() {
		super(false, false, null);
	}
	
	public DiMvpRec(VarInfo vi) {
		super(false, false, vi);
		//�����м�¼�ڴ���ʱӦ�ò�����ʹ�õ�type
	}
	
	/**
	 * ��ȡ���ڲ�ֵ�ֶεļ�¼�б��Ͳ���˳��һһ��Ӧ
	 * @return
	 */
	public ArrayList<VarRec> getIntervalRecs() {
		return intervalRecs;
	}

	/**
	 * ��ȡ����������ڲ�ֵ����ı���������
	 * @return
	 */
	public ArrayList<String> getInterval1() {
		return interval1;
	}

	/**
	 * ��ȡ����������ڲ�ֵ����ļ�������
	 * @return
	 */
	public ArrayList<String> getInterval2() {
		return interval2;
	}
	
	/**
	 * ��ȡMVP���еĴ����¼���Ͳ���˳��һһ��Ӧ
	 * @return
	 */
	public ArrayList<VarRec> getMVPRecs() {
		return this.mvpRecs;
	}

	/**
	 * ��ȡ�������MVP���е��ֶ�����
	 * @return
	 */
	public ArrayList<ArrayList<String>> getMVPCns() {
		return this.mvpCns;
	}
	
	/**
	 * ���һ�������������ֶεļ�¼
	 * @return
	 */
	public void addIntervalRec(VarRec vr, String cn1, String cn2) {
		this.intervalRecs.add(vr);
		this.interval1.add(cn1);
		this.interval2.add(cn2);
	}
	
	/**
	 * ���һ��MVP�ֶεļ�¼
	 * @return
	 */
	public void addMVPRec(VarRec vr, ArrayList<String> cns) {
		this.mvpRecs.add(vr);
		this.mvpCns.add(cns);
	}

	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(4);
		int din = this.intervalRecs.size();
		int mvpn = this.mvpRecs.size();
		seq.add(din);
		seq.add(mvpn);
		
		Sequence sub = new Sequence(din);
		for (int i = 0; i < din; i++) {
			Sequence interRec = new Sequence(3);
			VarRec vr = this.intervalRecs.get(i);
			if (vr == null) {
				interRec.add(null);
			}
			else {
				interRec.add(vr.toSeq());
			}
			interRec.add(interval1.get(i));
			interRec.add(interval2.get(i));
			
			sub.add(interRec);
		}
		seq.add(sub);
		
		sub = new Sequence(mvpn);
		for (int i = 0; i < mvpn; i++) {
			Sequence mvpRec = new Sequence(2);
			VarRec vr = this.mvpRecs.get(i);
			if (vr == null) {
				mvpRec.add(null);
			}
			else {
				mvpRec.add(vr.toSeq());
			}
			ArrayList<String> cns = mvpCns.get(i);
			Sequence cnSeq = new Sequence(cns.size());
			for (String cn : cns) {
				cnSeq.add(cn);
			}
			mvpRec.add(cnSeq);
			
			sub.add(mvpRec);
		}
		seq.add(sub);
		
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public void init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 4) {
			throw new RQException("Can't get the MVP and Date-intervals Record, invalid data.");
		}
		int din = ((Number) seq.get(1)).intValue();
		int mvpn = ((Number) seq.get(2)).intValue();

		Sequence rec = (Sequence) seq.get(3);
		for (int i = 1; i <= din; i++) {
			Sequence diSeq = (Sequence) rec.get(i);
			Sequence diRec = (Sequence) diSeq.get(1);
			if (diRec == null) {
				this.intervalRecs.add(null);
			}
			else {
				VarRec vr = new VarRec(false, false, null);
				vr.init(diRec);
				this.intervalRecs.add(vr);
			}
			this.interval1.add((String) diSeq.get(2));
			this.interval2.add((String) diSeq.get(3));
		}

		rec = (Sequence) seq.get(4);
		for (int i = 1; i <= mvpn; i++) {
			Sequence mvpSeq = (Sequence) rec.get(i);
			Sequence mvpRec = (Sequence) mvpSeq.get(1);
			if (mvpRec == null) {
				this.mvpRecs.add(null);
			}
			else {
				VarRec vr = new VarRec(false, false, null);
				vr.init(mvpRec);
				this.mvpRecs.add(vr);
			}
			
			mvpRec = (Sequence) mvpSeq.get(2);
			int cnn = mvpRec == null ? 0 : mvpRec.length();
			ArrayList<String> cns = new ArrayList<String>(cnn);
			for (int j = 1; j <= cnn; j++) {
				cns.add((String) mvpRec.get(j));
			}
			this.mvpCns.add(cns);
		}
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 2;
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();

		int din = in.readInt();
		int mvpn = in.readInt();

		for (int i = 1; i <= din; i++) {
			this.intervalRecs.add((VarRec) in.readObject());
			this.interval1.add((String) in.readObject());
			this.interval2.add((String) in.readObject());
		}

		for (int i = 1; i <= mvpn; i++) {
			this.mvpRecs.add((VarRec) in.readObject());
			int cnn = in.readInt();
			ArrayList<String> cns = new ArrayList<String>(cnn);
			for (int c = 0; c < cnn; c++) {
				cns.add((String) in.readObject());
			}
			this.mvpCns.add(cns);
		}
		if (ver > 1) {
			this.setVi((VarInfo) in.readObject());
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		
		int din = this.intervalRecs.size();
		int mvpn = this.mvpRecs.size();
		out.writeInt(din);
		out.writeInt(mvpn);
		
		for (int i = 0; i < din; i++) {
			out.writeObject(this.intervalRecs.get(i));
			out.writeObject(this.interval1.get(i));
			out.writeObject(this.interval2.get(i));
		}
		
		for (int i = 0; i < mvpn; i++) {
			out.writeObject(this.mvpRecs.get(i));
			ArrayList<String> cns = mvpCns.get(i);
			int cnn = cns.size();
			out.writeInt(cnn);
			for (int c = 0; c < cnn; c++) {
				out.writeObject(cns.get(c));
			}
		}
		out.writeObject(this.getVi());
	}
}
