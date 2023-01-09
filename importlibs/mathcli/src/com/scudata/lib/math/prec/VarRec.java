package com.scudata.lib.math.prec;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;

public class VarRec implements Externalizable {

	private static final long serialVersionUID = 1319081098314395632L;
	//�Ƿ������MissingIndex
	private boolean hasMI = false;
	//��ȥMI֮�⣬�Ƿ����ݲ��������壬�ֶ�������ɾ����
	private boolean onlyMI = true;

	//�Ƿ�Ŀ�������Ŀ�����ֻ���ƫ����ֵ���ͣ�
	private boolean ift = false;
	//SCRec ��ƫ��¼
	private SCRec scRec = null;
	//SertRec �����쳣ֵ��¼
	private SertRec sertRec = null;
	
	//�ֶ����ͣ�����Ԥ����ֻ������MI������MI��δ���ɵ��ֶΣ�������ûʲô�����
	private byte type = Consts.F_TWO_VALUE;
	//2.8 ȱʧֵ���¼������2.10�ϲ���Ƶ���࣬���ǳ�ʼֵ��������
	private FNARec fnaRec = null;
	//2.11�ͻ�����������BI��¼
	private BIRec biRec = null;
	//2.12�߻�������ƽ����
	private SmRec smRec = null;
	//2.13,2.14��ƫ�������쳣ֵ��ͬĿ����ֵ�����ļ�¼
	//2.15��һ��
	private NorRec norRec = null;
	// ��¼��ͳ����Ϣ
	private VarInfo vi = null;
	
	//�Ƿ�ʹ�������
	private boolean impute = false;

	public VarRec() {
	}
	
	public VarRec(boolean hasMI, boolean onlyMI, VarInfo varInfo) {
		this.hasMI = hasMI;
		this.onlyMI = onlyMI;
		this.vi = varInfo;
	}

	/**
	 * �Ƿ���Missing Index��
	 * @return
	 */
	public boolean hasMI() {
		return this.hasMI;
	}

	/**
	 * �����Ƿ���Missing Index��
	 * @param b
	 */
	public void setMI(boolean b) {
		this.hasMI = b;
	}
	
	/**
	 * ��ȥMissing Index�У����������Ƿ���������
	 * @return
	 */
	public boolean onlyHasMI() {
		return onlyMI;
	}

	/**
	 * �������������Ƿ���������
	 * @param onlyMI
	 */
	public void setOnlyMI(boolean onlyMI) {
		this.onlyMI = onlyMI;
	}

	/**
	 * �Ƿ�Ŀ�����
	 * @return the ift
	 */
	public boolean ift() {
		return ift;
	}

	/**
	 * �����Ƿ�Ŀ�����
	 * @param ift the ift to set
	 */
	public void setIft(boolean ift) {
		this.ift = ift;
	}

	/**
	 * �Ƿ�ʹ���������
	 * @return 
	 */
	public boolean ifImpute() {
		return impute;
	}

	/**
	 * �����Ƿ�Ŀ�����
	 * @param 
	 */
	public void setImpute(boolean impute) {
		this.impute = impute;
	}

	/**
	 * ��ȡ��ƫ��¼
	 * @return the scRec
	 */
	public SCRec getSCRec() {
		return scRec;
	}

	/**
	 * ���þ�ƫ��¼
	 * @param scRec the scRec to set
	 */
	public void setSCRec(SCRec scRec) {
		this.scRec = scRec;
	}

	/**
	 * ��ȡ�����쳣ֵ��¼
	 * @return the sertRec
	 */
	public SertRec getSertRec() {
		return sertRec;
	}

	/**
	 * ���������쳣ֵ��¼
	 * @param sertRec the sertRec to set
	 */
	public void setSertRec(SertRec sertRec) {
		this.sertRec = sertRec;
	}

	/**
	 * ��ȡ�ֶ����ͣ�����Ԥ����ֻ������MI������MI��δ���ɵ��ֶΣ�������ûʲô�����
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * �����ֶ�����
	 * @param type the type to set
	 */
	public void setType(byte type) {
		this.type = type;
	}

	/**
	 * ��ȡȱʧֵ���¼
	 * @return the fnaRec
	 */
	public FNARec getFNARec() {
		return fnaRec;
	}

	/**
	 * ����ȱʧֵ���¼
	 * @param fnaRec the fnaRec to set
	 */
	public void setFNARec(FNARec fnaRec) {
		this.fnaRec = fnaRec;
	}

	/**
	 * ��ȡ�ͻ�����������BI��¼
	 * @return the biRec
	 */
	public BIRec getBIRec() {
		return biRec;
	}

	/**
	 * ���õͻ�����������BI��¼
	 * @param biRec the biRec to set
	 */
	public void setBIRec(BIRec biRec) {
		this.biRec = biRec;
	}

	/**
	 * ��ȡ�߻�������ƽ������¼
	 * @return the smRec
	 */
	public SmRec getSmRec() {
		return smRec;
	}

	/**
	 * ���ø߻�������ƽ������¼
	 * @param smRec the smRec to set
	 */
	public void setSmRec(SmRec smRec) {
		this.smRec = smRec;
	}

	/**
	 * ��ȡ��һ����¼
	 * @return the norRec
	 */
	public NorRec getNorRec() {
		return norRec;
	}

	/**
	 * ���ù�һ����¼
	 * @param norRec the norRec to set
	 */
	public void setNorRec(NorRec norRec) {
		this.norRec = norRec;
	}
	
	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(11);
		int i = this.hasMI ? 1 : 0;
		seq.add(i);
		i = this.onlyMI ? 1 : 0;
		seq.add(i);
		i = this.ift ? 1 : 0;
		seq.add(i);
		i = this.impute ? 1 : 0;
		seq.add(i);
		seq.add(type);

		//2.8 ȱʧֵ���¼������2.10�ϲ���Ƶ���࣬���ǳ�ʼֵ��������
		if (this.fnaRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.fnaRec.toSeq());
		}
		//2.11�ͻ�����������BI��¼
		if (this.biRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.biRec.toSeq());
		}
		//2.12�߻�������ƽ����
		if (this.smRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.smRec.toSeq());
		}
		//2.13,2.14��ƫ�������쳣ֵ��ͬĿ����ֵ�����ļ�¼
		//SCRec ��ƫ��¼
		if (this.scRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.scRec.toSeq());
		}
		//SertRec �����쳣ֵ��¼
		if (this.sertRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.sertRec.toSeq());
		}
		//2.15��һ��
		if (this.norRec == null) {
			seq.add(null);
		}
		else {
			seq.add(this.norRec.toSeq());
		}
		
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public void init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 11) {
			throw new RQException("Can't get the Variable Preparing Record, invalid data.");
		}
		this.hasMI = ((Number) seq.get(1)).intValue() == 1;
		this.onlyMI = ((Number) seq.get(2)).intValue() == 1;
		this.ift = ((Number) seq.get(3)).intValue() == 1;
		this.impute = ((Number) seq.get(4)).intValue() == 1;
		this.type = ((Number) seq.get(5)).byteValue();
		
		Sequence rec = (Sequence) seq.get(6);
		//2.8 ȱʧֵ���¼������2.10�ϲ���Ƶ���࣬���ǳ�ʼֵ��������
		if (rec != null) {
			this.fnaRec = new FNARec();
			this.fnaRec.init(rec);
		}
		//2.11�ͻ�����������BI��¼
		rec = (Sequence) seq.get(7);
		if (rec != null) {
			this.biRec = new BIRec(null);
			this.biRec.init(rec);
		}
		//2.12�߻�������ƽ����
		rec = (Sequence) seq.get(8);
		if (rec != null) {
			this.smRec = new SmRec();
			this.smRec.init(rec);
		}
		//2.13,2.14��ƫ�������쳣ֵ��ͬĿ����ֵ�����ļ�¼
		//SCRec ��ƫ��¼
		rec = (Sequence) seq.get(9);
		if (rec != null) {
			this.scRec = new SCRec();
			this.scRec.init(rec);
		}
		//SertRec �����쳣ֵ��¼
		rec = (Sequence) seq.get(10);
		if (rec != null) {
			this.sertRec = new SertRec(0, 0);
			this.sertRec.init(rec);
		}
		//2.15��һ��
		rec = (Sequence) seq.get(11);
		if (rec != null) {
			this.norRec = new NorRec();
			this.norRec.init(rec);
		}
	}

	/**
	 * ��ȡ��ͳ����Ϣ
	 * @return
	 */
	public VarInfo getVi() {
		return vi;
	}

	/**
	 * ������ͳ����Ϣ
	 * @param vi
	 */
	public void setVi(VarInfo vi) {
		this.vi = vi;
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 2;
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);

		out.writeBoolean(this.hasMI);
		out.writeBoolean(this.onlyMI);
		out.writeBoolean(this.ift);
		out.writeBoolean(this.impute);
		out.writeByte(this.type);

		//2.8 ȱʧֵ���¼������2.10�ϲ���Ƶ���࣬���ǳ�ʼֵ��������
		out.writeObject(this.fnaRec);
		//2.11�ͻ�����������BI��¼
		out.writeObject(this.biRec);
		//2.12�߻�������ƽ����
		out.writeObject(this.smRec);
		//2.13,2.14��ƫ�������쳣ֵ��ͬĿ����ֵ�����ļ�¼
		//SCRec ��ƫ��¼
		out.writeObject(this.scRec);
		//SertRec �����쳣ֵ��¼
		out.writeObject(this.sertRec);
		//2.15��һ��
		out.writeObject(this.norRec);
		//ͳ������
		out.writeObject(this.vi);
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		
		this.hasMI = in.readBoolean();
		this.onlyMI = in.readBoolean();
		this.ift = in.readBoolean();
		this.impute = in.readBoolean();
		this.type = in.readByte();
		
		//2.8 ȱʧֵ���¼������2.10�ϲ���Ƶ���࣬���ǳ�ʼֵ��������
		this.fnaRec = (FNARec) in.readObject();
		//2.11�ͻ�����������BI��¼
		this.biRec = (BIRec) in.readObject();
		//2.12�߻�������ƽ����
		this.smRec = (SmRec) in.readObject();
		//2.13,2.14��ƫ�������쳣ֵ��ͬĿ����ֵ�����ļ�¼
		//SCRec ��ƫ��¼
		this.scRec = (SCRec) in.readObject();
		//SertRec �����쳣ֵ��¼
		this.sertRec = (SertRec) in.readObject();
		//2.15��һ��
		this.norRec = (NorRec) in.readObject();
		if (ver > 1) {
			this.vi = (VarInfo) in.readObject();
		}
	}
}
