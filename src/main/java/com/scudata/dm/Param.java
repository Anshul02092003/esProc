package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.ICloneable;
import com.scudata.common.IRecord;

/**
 * ���ڶ��������������ʱ����
 * @author WangXiaoJun
 *
 */
public class Param implements Cloneable, ICloneable, Externalizable, IRecord {
	private static final long serialVersionUID = 0x05000003;

	public final static byte VAR = (byte)0; // ����
	public final static byte ARG = (byte)1; // ��������Ҫ���룬���ܸ�ֵ
	public final static byte CONST = (byte)3; // ���������ܸ�ֵ
	
	private String name; // ������
	private byte kind = VAR; // ����
	private Object value; // ����ֵ

	private String remark; // ��ע���������û������д���������������ṩ�����ı༭���֮��
	private Object editValue; // �༭������α����ʱ����ʹ��

	public Param() {
	}

	/**
	 * ����һ���µĲ���
	 * @param name String ������
	 * @param kind byte �������ͣ�VAR��ATTR��EXP��CONST
	 * @param value Object ����ֵ
	 */
	public Param(String name,  byte kind, Object value ) {
		this.name = name;
		this.kind = kind;
		this.value = value;
	}

	/**
	 * �ɲ�������һ��������ͬ���²���
	 * @param other Param ��һ������
	 */
	public Param(Param other) {
		if (other != null) {
			this.name = other.name;
			this.kind = other.kind;
			this.value = other.value;
			this.editValue = other.editValue;
		}
	}

	/**
	 * ���ز�����
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * ���ò�����
	 * @param name String ������
	 */
	public void setName( String name ) {
		this.name = name;
	}

	/**
	 * ���ز�������
	 * @return byte
	 */
	public byte getKind() {
		return kind;
	}

	/**
	 * ���ò�������
	 * @param kind byte
	 */
	public void setKind( byte kind ) {
		this.kind = kind;
	}

	/**
	 * ���ز���ֵ
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * ���ò���ֵ
	 * @param defValue Object
	 */
	public void setValue( Object defValue ) {
		this.value = defValue;
	}

	/**
	 * ȡ��ע
	 * @return String
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * �豸ע
	 * @param str String
	 */
	public void setRemark(String str) {
		this.remark = str;
	}

	public void setEditValue(Object val) {
		this.editValue = val;
	}

	public Object getEditValue() {
		return this.editValue;
	}

	public Object deepClone() {
		return new Param(this);
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeByte( kind );
		out.writeString( name );
		out.writeObject( value, true );
		out.writeObject(editValue, true);

		out.writeString(remark);
		return out.toByteArray();
	}

	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		kind = in.readByte();
		name = in.readString();
		value = in.readObject(true);
		editValue = in.readObject(true);

		if (in.available() > 0) {
			remark = in.readString();
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(2);
		out.writeByte(kind);
		out.writeObject(name);
		out.writeObject(value);
		out.writeObject(editValue);

		out.writeObject(remark); // �汾2���
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		byte ver = in.readByte();
		kind = in.readByte();
		name = (String) in.readObject();
		value = in.readObject();
		editValue = in.readObject();

		if (ver > 1) {
			remark = (String)in.readObject();
		}
	}
}
