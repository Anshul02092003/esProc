package com.scudata.lib.math.prec;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.RQException;
import com.scudata.dm.Sequence;

/**
 * �ȱʧֵ������¼
 * @author bd
 *
 */
public class FNARec implements Externalizable {
	private static final long serialVersionUID = -8581578093941419454L;
	//�������ֵ
	Object missing = null;
	//�������Ƶֵ
	Object setting = null;
	//�������ķ���ֵ
	Sequence keepValues = new Sequence();
	//���ϲ���ʧ�ĵ�Ƶ����ֵ
	Sequence otherValues = new Sequence();
	
	public FNARec() {
	}
	
	/**
	 * ��ȡ�������ֵ������
	 * @return the missing
	 */
	public Object getMissing() {
		return missing;
	}
	
	/**
	 * �����������ֵ������
	 * @param missing the missing to set
	 */
	public void setMissing(Object missing) {
		this.missing = missing;
	}
	
	/**
	 * ��ȡ�������Ƶ���������
	 * @return the setting
	 */
	public Object getSetting() {
		return setting;
	}
	
	/**
	 * �����������Ƶ���������
	 * @param setting the setting to set
	 */
	public void setSetting(Object setting) {
		this.setting = setting;
	}
	
	/**
	 * ��ȡ�������ĸ�Ƶ����ֵ
	 * @return the keepValues
	 */
	public Sequence getKeepValues() {
		return keepValues;
	}
	
	/**
	 * ���ñ������ĸ�Ƶ����ֵ
	 * @param keepValues the keepValues to set
	 */
	public void setKeepValues(Sequence keepValues) {
		this.keepValues = keepValues;
	}
	
	/**
	 * ��ȡ���ϲ���ʧ�ĵ�Ƶ����ֵ
	 * @return the otherValues
	 */
	public Sequence getOtherValues() {
		return otherValues;
	}
	
	/**
	 * ���ñ��ϲ���ʧ�ĵ�Ƶ����ֵ
	 * @param otherValues the otherValues to set
	 */
	public void setOtherValues(Sequence otherValues) {
		this.otherValues = otherValues;
	}

	/****************************************************/
	/**
	 * �洢ʱ��������
	 * @return
	 */
	public Sequence toSeq() {
		Sequence seq = new Sequence(4);
		seq.add(this.missing);
		seq.add(this.setting);
		if (this.keepValues == null || this.keepValues.length() < 1) {
			seq.add(null);
		}
		else {
			seq.add(this.keepValues);
		}
		if (this.otherValues == null || this.otherValues.length() < 1) {
			seq.add(null);
		}
		else {
			seq.add(this.otherValues);
		}
		return seq;
	}
	
	/**
	 * ��ȡʱ����Sequence��ʼ������
	 */
	public void init(Sequence seq) {
		int size = seq == null ? 0 : seq.length();
		if (size < 4) {
			throw new RQException("Can't get the Fill NA Value Record, invalid data.");
		}
		this.missing = seq.get(1);
		this.setting = seq.get(2);
		this.keepValues = (Sequence) seq.get(3);
		if (this.keepValues == null ) {
			this.keepValues = new Sequence();
		}
		this.otherValues = (Sequence) seq.get(4);
		if (this.otherValues == null ) {
			this.otherValues = new Sequence();
		}
	}

	/************************* ����ʵ��Externalizable ************************/
	private byte version = (byte) 1;
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		
		this.missing = in.readObject();
		this.setting = in.readObject();
		this.keepValues = (Sequence) in.readObject();
		this.otherValues = (Sequence) in.readObject();
		if (ver > 1) {
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(this.version);
		
		out.writeObject(this.missing);
		out.writeObject(this.setting);
		out.writeObject(this.keepValues);
		out.writeObject(this.otherValues);
	}
}
