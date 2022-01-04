package com.scudata.common;

/**
 *	ByteMap --- ʹ��byte��Ϊkey��������Map
 */
import java.io.*;

public interface IByteMap
	extends ICloneable,Externalizable {
	/**
	 * ȷ���������ٵ���ָ��ֵ
	 * @param minCapacity ָ������С����
	 */
	public void ensureCapacity(int minCapacity);

	/*
	 * ����Ԫ�ظ���
	 */
	public short size();

	/*
	 * ����Ƿ�Ϊ��
	 */
	public boolean isEmpty();

	/**
	 * ������������ʵ�ʴ�С
	 */
	public void trimToSize();

	/*
	 * ���Map���Ƿ���ָ��value
	 * @param value ��Ҫ���ҵ�value
	 * @see ByteMap#containsKey
	 */
	public boolean contains(Object value);

	/*
	 * ���Map���Ƿ���ָ����key
	 * @param key Ҫ���ҵ�key
	 * @see ByteMap#contains
	 */
	public boolean containsKey(byte key);

	/*
	 * ȡMap����ָ��key��Ӧ��value
	 * @param key ָ����key
	 * @see ByteMap#put
	 */
	public Object get(byte key);

	/*
	 * ��ָ����key��ָ����value����Map
	 * @param key ָ����key
	 * @see ByteMap#get
	 */
	public Object put(byte key, Object value);

	/**
	 * ������һ��ByteMap�е���������뱾ByteMap��key���ظ��򸲸�
	 * @param bam ��һ��ByteMap
	 */
	public void putAll(IByteMap bm);

	/*
	 * ���߶�Ӧ��ָ��key��Ԫ�أ���ָ����key�����ڣ���ֱ�ӷ���
	 * @param key ָ����key
	 * @return ָ��key��Ӧ��value����key�����ڣ��򷵻�null
	 */
	public Object remove(byte key);

	/**
	 * ��ָ����key��value׷�ӵ���ByteMap�У�ע��˷�����������ͬkey����
	 * @param key ָ����key
	 * @param value ָ����value
	 */
	public void add(byte key, Object value);

	/**
	 * ����һ��ByteMap�е���׷�ӵ���ByteMap��;ע��˷�����������ͬkey����
	 * @param bm ��һ��ByteMap
	 */
	public void addAll(IByteMap bm);

	/*
	 * ����λ��ɾ����
	 * @param index λ��
	 * @return ����ָ��λ�õ�value
	 */
	public Object removeEntry(int index);

	/*
	 * ����λ��ȡ�ö�Ӧ��key
	 * @param index λ��
	 */
	public byte getKey(int index);

	/*
	 * ����λ��ȡ�ö�Ӧ��value
	 * @param index λ��
	 */
	public Object getValue(int index);

	/*
	 * ȡMap����ָ��key��Ӧ��index,����Ҳ���ָ����key�򷵻�-1
	 * @param key ָ����key
	 */
	public int getIndex(byte key);

	/*
	 * ��ָ����index��Ӧ��value����Map
	 * @param index ָ����index
	 * @see ByteMap#setValue
	 */
	public void setValue(int index,Object value);

	/**
	 * ������ظ����ֻ�������һ��
	 */
	public void purgeDupKeys();

	/**
	 * ���ֵΪnull����
	 */
	public void purgeNullValues();

	/*
	 * ���Map
	 */
	public void clear();

	/*************************���¼̳���Externalizable************************/
	/**
	 * д���ݵ���
	 *@param out �����
	 */
	public void writeExternal(ObjectOutput out) throws IOException;

	/**
	 * �����ж�����
	 *@param in ������
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;

	/*************************���¼̳���ICloneable************************/
	/**
	 * ��ȿ�¡
	 *@return ��¡���Ķ���
	 */
	public Object deepClone();

}
