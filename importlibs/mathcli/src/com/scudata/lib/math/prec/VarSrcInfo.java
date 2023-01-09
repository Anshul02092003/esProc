package com.scudata.lib.math.prec;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

/**
 * ��������������Ԥ������Ϣ����������������ʼ�����Լ�����������Ҫ����Ԥ����ı���
 * @author bd
 *
 */
public class VarSrcInfo extends VarInfo {
	
	private static final long serialVersionUID = -3288289568049388783L;

	public VarSrcInfo() {
		super();
	}
	
	public VarSrcInfo(String srcName, byte type) {
		super(srcName, type);
	}
	
	// �Ƿ�����MI������MI������ͳ������ֻ��ȱʧ���йأ����ؼ�¼VarInfo
	private boolean hasMI = false;
	// �������ɵ�MVP������Ϣ��δ������Ϊnull
	private VarInfo mvpDerived;
	// MVP�����Ĳ���������ƣ����ձ�����
	private ArrayList<String> mvpCns;
	// BI������Ϣ�����а�������
	private ArrayList<VarInfo> biCols;
	// ƽ��������������Ϣ�����а�������
	private ArrayList<VarInfo> smCols;
	// ����ʱ����Ļ�����������
	private ArrayList<VarSrcInfo> dateCols;
	// ���ڲ�ֵ�����������
	private ArrayList<VarDateInterval> dateIntervals;
	
	/**
	 * ��ȡ���ɵ�MI����Ϣ
	 * @return the miDerived
	 */
	public boolean hasMI() {
		return this.hasMI;
	}
	
	/**
	 * �������ɵ�MI����Ϣ
	 * @param miDerived the miDerived to set
	 */
	public void setMI(boolean b) {
		this.hasMI = b;
	}
	
	/**
	 * ��ȡ���ɵ�MVP����Ϣ
	 * @return the mvpDerived
	 */
	public VarInfo getMvpDerived() {
		return mvpDerived;
	}
	
	/**
	 * �������ɵ�MVP����Ϣ
	 * @param mvpDerived the mvpDerived to set
	 */
	public void setMvpDerived(VarInfo mvpDerived) {
		this.mvpDerived = mvpDerived;
	}
	
	/**
	 * ��ȡ���ɵ�MVPʹ�����ձ������б�
	 * @return	���ձ������б�
	 */
	public ArrayList<String> getMvpVarNames() {
		return this.mvpCns;
	}
	
	/**
	 * �������ɵ�MVPʹ�����ձ������б�
	 * @param ���ձ������б�
	 */
	public void setMvpVarNames(ArrayList<String> vns) {
		this.mvpCns = vns;
	}
	
	/**
	 * ��ȡ��ֳɵ�BI������Ϣ����������
	 * @return the biCols
	 */
	public ArrayList<VarInfo> getBiCols() {
		return biCols;
	}
	
	/**
	 * ���ò�ֳɵ�BI������Ϣ����������
	 * @param biCols the biCols to set
	 */
	public void setBiCols(ArrayList<VarInfo> biCols) {
		this.biCols = biCols;
	}
	
	/**
	 * ��ȡ��ֳɵ�ƽ�����������б�����������
	 * @return the smCols
	 */
	public ArrayList<VarInfo> getSmCols() {
		return smCols;
	}
	
	/**
	 * ���ò�ֳɵ�ƽ�����������б�����������
	 * @param smCols the smCols to set
	 */
	public void setSmCols(ArrayList<VarInfo> smCols) {
		this.smCols = smCols;
	}
	
	/**
	 * ��ȡ���ڻ�����������Ϣ
	 * @return the derivedCols
	 */
	public ArrayList<VarSrcInfo> getDateCols() {
		return this.dateCols;
	}
	
	/**
	 * �������ڻ�����������Ϣ
	 * @param derivedCols the derivedCols to set
	 */
	public void setDateCols(ArrayList<VarSrcInfo> derivedCols) {
		this.dateCols = derivedCols;
	}

	/**
	 * ���һ�����ڻ��������е�ͳ����Ϣ
	 * @param vi
	 */
	public void addDateCol(VarSrcInfo vi) {
		if (this.dateCols == null) {
			this.dateCols = new ArrayList<VarSrcInfo>(4);
		}
		this.dateCols.add(vi);
	}

	/**
	 * ��ȡ���ڲ�ֵ����Ϣ
	 * @return the derivedCols
	 */
	public ArrayList<VarDateInterval> getDateIntervals() {
		return this.dateIntervals;
	}
	
	/**
	 * �������ڲ�ֵ����Ϣ
	 * @param derivedCols the derivedCols to set
	 */
	public void setDateIntervals(ArrayList<VarDateInterval> derivedCols) {
		this.dateIntervals = derivedCols;
	}

	/**
	 * ���һ�����ڲ�ֵ�е�ͳ����Ϣ
	 * @param vi
	 */
	public void addDateInterval(VarDateInterval vi) {
		if (this.dateIntervals == null) {
			this.dateIntervals = new ArrayList<VarDateInterval>(4);
		}
		this.dateIntervals.add(vi);
	}
	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 5;
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeByte(this.version);
		
		out.writeBoolean(this.hasMI);
		out.writeObject(mvpDerived);
		int size = this.mvpCns == null ? 0 : this.mvpCns.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeObject(this.mvpCns.get(i));
		}
		size = biCols == null ? 0 : biCols.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeObject(biCols.get(i));
		}
		size = dateCols == null ? 0 : dateCols.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeObject(dateCols.get(i));
		}
		size = dateIntervals == null ? 0 : dateIntervals.size();
		out.writeInt(size);
		for (int i = 0; i < size; i++) {
			out.writeObject(dateIntervals.get(i));
		}
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		byte version = in.readByte();
		
		this.hasMI = in.readBoolean();
		this.mvpDerived = (VarInfo) in.readObject();
		int size = in.readInt();
		if (size < 1) {
			this.mvpCns = null;
		}
		else {
			this.mvpCns = new ArrayList<String>(size);
			for (int i = 0; i < size; i++) {
				this.mvpCns.add((String)in.readObject());
			}
		}
		
		size = in.readInt();
		if (size < 1) {
			this.biCols = null;
		}
		else {
			this.biCols = new ArrayList<VarInfo>(size);
			for (int i = 0; i < size; i++) {
				this.biCols.add((VarInfo)in.readObject());
			}
		}

		size = in.readInt();
		if (size < 1) {
			this.dateCols = null;
		}
		else {
			this.dateCols = new ArrayList<VarSrcInfo>(size);
			for (int i = 0; i < size; i++) {
				this.dateCols.add((VarSrcInfo)in.readObject());
			}
		}
		size = in.readInt();
		if (size < 1) {
			this.dateIntervals = null;
		}
		else {
			this.dateIntervals = new ArrayList<VarDateInterval>(size);
			for (int i = 0; i < size; i++) {
				this.dateIntervals.add((VarDateInterval)in.readObject());
			}
		}
		if (version > 5) {
		}
	}
}
