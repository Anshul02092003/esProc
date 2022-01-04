package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.ICloneable;
import com.scudata.common.IRecord;

/**
 * �����б����ڱ������������������
 * @author WangXiaoJun
 *
 */
public class ParamList implements Cloneable, ICloneable, Externalizable, IRecord {
	private static final long serialVersionUID = 0x05000004;

	private List<Param> vList;
	private boolean isUserChangeable; //�Ƿ�ÿ����������ʱ���ò���

	public ParamList() {
	}

	/**
	 * ����һ�������б���Ĳ�����ӵ���ǰ�����б���
	 * @param pl �����б�
	 */
	public void addAll(ParamList pl) {
		if (pl.vList != null && pl.vList.size() > 0) {
			if (vList == null) {
				vList = new ArrayList<Param>(pl.vList);
			} else {
				vList.addAll(pl.vList);
			}
		}
	}

	/**
	 * ��Ӳ���
	 * @param v Param ����
	 */
	public void add(Param v) {
		if (vList == null) {
			vList = new ArrayList<Param>(4);
		}
		
		vList.add(v);
	}

	/**
	 * ��Ӳ�����ָ��λ��
	 * @param index int ָ��λ��
	 * @param v Param ����
	 */
	public void add(int index, Param v) {
		if (isValid(v)) {
			if (vList == null) {
				vList = new ArrayList<Param>(index + 1);
			}
			
			vList.add(index, v);
		}
	}

	/**
	 * ���������ơ��������͡�����ֵ������ޱ������
	 * @param name String ��������
	 * @param kind byte ��������
	 * @param value Object ����ֵ
	 */
	public void add(String name, byte kind, Object value) {
		Param v = new Param(name, kind, value);
		if (isValid(v)) {
			if (vList == null) {
				vList = new ArrayList<Param>(4);
			}
			
			vList.add(v);
		}
	}

	/**
	 * ���������ơ�����ֵ������ޱ������
	 * @param name String ��������
	 * @param value Object ��������ֵ
	 */
	public void addVariable(String name, Object value) {
		add(name, Param.VAR, value);
	}

	/**
	 * ���������ơ����⡢����ֵ����Ӳ���
	 * @param name String ��������
	 * @param value Object ����ֵ
	 */
	public void addArgument(String name, Object value) {
		add(name, Param.ARG, value);
	}

	/**
	 * ���������ơ����⡢����ֵ����ӳ�����
	 * @param name String ����������
	 * @param value Object ������ֵ
	 */
	public void addConstant( String name, Object value ) {
		add(name, Param.CONST, value);
	}

	/**
	 * �Ƴ�ָ��λ�ò���
	 * @param index int ָ��λ��
	 * @return Param �Ƴ�����
	 */
	public Param remove(int index) {
		if (vList == null || vList.size() <= index ) {
			return null;
		}
		
		return vList.remove(index);
	}

	/**
	 * �Ƴ�ָ�����Ʋ���
	 * @param name String ָ������
	 * @return Param �Ƴ�����
	 */
	public Param remove(String name) {
		if (vList == null) {
			return null;
		}
		
		for (int i = 0, iCount = vList.size(); i<iCount; i++) {
			Param p = vList.get(i);
			if (p != null && p.getName().equals(name)) {
				return vList.remove(i);
			}
		}
		
		return null;
	}

	/**
	 * ��ȡָ��λ�ò���
	 * @param index int ָ��λ��
	 * @return Param ����
	 */
	public Param get(int index) {
		if (vList == null || vList.size() <= index) {
			return null;
		}
		
		return vList.get(index);
	}

	/**
	 * ��ȡָ�����Ʋ���
	 * @param name String ָ������
	 * @return Param ����
	 */
	public Param get(String name) {
		if (vList == null) {
			return null;
		}
		
		for (int i = 0, iCount = vList.size(); i < iCount; i++) {
			Param p = vList.get(i);
			if (p != null && p.getName().equals(name)) {
				return p;
			}
		}
		
		return null;
	}

	/**
	 * ����ֵ����value�Ĳ���
	 * @param value Object ����ֵ
	 * @return Param
	 */
	public Param getByValue(Object value) {
		if (vList == null) {
			return null;
		}
		
		for (int i = 0, iCount = vList.size(); i < iCount; i++) {
			Param p = vList.get(i);
			if (p != null && p.getValue() == value) {
				return p;
			}
		}
		
		return null;
	}

	/**
	 * ��ȡ���б���
	 * @param varList ParamList ���������б�
	 */
	public void getAllVarParams(ParamList varList) {
		if (vList == null) {
			return;
		}
		
		for (int i = 0, count = vList.size(); i < count; ++i) {
			Param p = vList.get(i);
			if (p != null && p.getKind() == Param.VAR) {
				varList.add(p);
			}
		}
	}

	/**
	 * ��ȡ���в���
	 * @param expParamList ParamList �����б�
	 */
	public void getAllArguments(ParamList expParamList) {
		if (vList == null) {
			return;
		}
		
		for (int i = 0, count = vList.size(); i < count; ++i) {
			Param p = vList.get(i);
			if (p != null && p.getKind() == Param.ARG) {
				expParamList.add(p);
			}
		}
	}
	
	/**
	 * ��ȡ���г�����
	 * @param varList ParamList �������б�
	 */
	public void getAllConsts(ParamList varList) {
		if (vList == null) {
			return;
		}
		
		for (int i = 0, count = vList.size(); i < count; ++i) {
			Param p = (Param)vList.get(i);
			if (p != null && p.getKind() == Param.CONST) {
				varList.add(p);
			}
		}
	}

	/**
	 * �Ƿ����ָ������
	 * @param p Param ָ������
	 * @return boolean �Ƿ����
	 */
	public boolean contains(Param p) {
		return (vList == null ? false : vList.contains(p));
	}

	/**
	 * ��������
	 * @return int ����
	 */
	public int count() {
		if (vList == null) {
			return 0;
		}
		
		return vList.size();
	}

	/**
	 * ��ղ����б�
	 */
	public void clear() {
		vList = null;
	}

	/**
	 * ��ȸ���
	 * @return Object
	 */
	public Object deepClone() {
		ParamList pl = new ParamList();
		pl.isUserChangeable = isUserChangeable;

		List<Param> vList = this.vList;
		if (vList != null) {
			int size = vList.size();
			pl.vList = new ArrayList<Param>(size);
			for (int i = 0; i < size; i++) {
				Param v = vList.get(i);
				pl.vList.add((Param)v.deepClone());
			}
		}

		return pl;
	}

	public void setUserChangeable(boolean changeable){
	  isUserChangeable = changeable;
	}

	public boolean isUserChangeable(){
	  return isUserChangeable;
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		List<Param> vList = this.vList;
		if(vList == null){
			out.writeShort((short)0);
		} else{
			int size = vList.size();
			out.writeShort((short)size);
			for (int i = 0; i < size; i++) {
				Param v = vList.get(i);
				out.writeRecord(v);
			}
		}
		
		out.writeBoolean(isUserChangeable);
		return out.toByteArray();
	}

	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		int count = in.readShort();
		if (count > 0) {
			vList = new ArrayList<Param>(count);
			for (int i = 0; i < count; i++) {
				Param v = new Param();
				in.readRecord(v);
				vList.add(v);
			}
		}
		
		isUserChangeable = in.readBoolean();
	}

	private boolean isValid(Param o) {
		if (o == null) {
			return false;
		} else {
			String name = o.getName();
			if (name == null) return false;
			if (vList == null) return true;
			
			for (int i = 0, iCount = vList.size(); i < iCount; i++) {
				Param v = (Param)vList.get(i);
				if (v != null && v.getName().equals(name)) return false;
			}
			
			return true;
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1);
		out.writeObject(vList);
		out.writeBoolean(isUserChangeable);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readByte(); // version
		this.vList = (List<Param>)in.readObject();
		isUserChangeable = in.readBoolean();
	}
}
