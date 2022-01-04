package com.scudata.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *	ByteMap --- ʹ��byte��Ϊkey��������Map
 *	�˹�ϣ������������ɵģ�����Ҫ�������¼���Ŀ�ģ�
 *		(1)��ʡ�ڴ�
 *		(2)���ݲ���̫��
 *		(3)put,get,keys�Ȳ���Զ����remove����
 *	ע�⣺
 *		(1)remove�����ĳɱ��Ƚϸ�
 *		(2)��ʹ���±�ѭ�������У��Ա���������put��remove��clear���޸��Բ���ʱ����λ�ñ仯���������ArrayList
 */
public class ByteMap implements IByteMap, ICloneable, Externalizable, IRecord {
	private static final long serialVersionUID = 1l;

	//hash������
	private transient byte[] keys;
	private transient Object[] objs;

	//hash�����������
	private transient short count;

	/*
	 * ��ָ����ʼ��������һ���յ�Map
	 * @param initialCapacity ��ʼ����
	 * @exception IllegalArgumentException �����ʼ����С��0
	 */
	public ByteMap(short initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException();
		}
		keys = new byte[initialCapacity];
		objs = new Object[initialCapacity];
	}

	/*
	 * ����һ����Map����ʼ����Ϊ11
	 */
	public ByteMap() {
		this( (short) 11);
	}

	/**
	 * ȷ���������ٵ���ָ��ֵ
	 * @param minCapacity ָ������С����
	 */
	public void ensureCapacity(int minCapacity) {
		if (minCapacity > keys.length) {
			byte[] oldKeys = this.keys;
			Object[] oldObjs = this.objs;
			this.keys = new byte[minCapacity];
			this.objs = new Object[minCapacity];
			System.arraycopy(oldKeys, 0, this.keys, 0, count);
			System.arraycopy(oldObjs, 0, this.objs, 0, count);
		}
	}

	/*
	 * ����Ԫ�ظ���
	 */
	public short size() {
		return count;
	}

	/*
	 * ����Ƿ�Ϊ��
	 */
	public boolean isEmpty() {
		return count == 0;
	}

	/**
	 * ������������ʵ�ʴ�С
	 */
	public void trimToSize() {
		if (count < keys.length) {
			byte[] oldKeys = this.keys;
			Object[] oldObjs = this.objs;
			this.keys = new byte[count];
			this.objs = new Object[count];
			System.arraycopy(oldKeys, 0, this.keys, 0, count);
			System.arraycopy(oldObjs, 0, this.objs, 0, count);
		}
	}

	/*
	 * ���Map���Ƿ���ָ��value
	 * @param value ��Ҫ���ҵ�value
	 * @see ByteMap#containsKey
	 */
	public boolean contains(Object value) {
		Object[] objs = this.objs;
		if (value != null) {
			for (int i = 0; i < count; i++) {
				if (value.equals(objs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * ���Map���Ƿ���ָ����key
	 * @param key Ҫ���ҵ�key
	 * @see ByteMap#contains
	 */
	public boolean containsKey(byte key) {
		byte[] keys = this.keys;
		for (int i = 0; i < count; i++) {
			if (keys[i] == key) {
				return true;
			}
		}
		return false;
	}

	/*
	 * ȡMap����ָ��key��Ӧ��value
	 * @param key ָ����key
	 * @see ByteMap#put
	 */
	public Object get(byte key) {
		byte[] keys = this.keys;
		for (int i = count - 1; i >= 0; i--) {
			if (keys[i] == key) {
				return objs[i];
			}
		}
		return null;
	}

	/*
	 * ��ָ����key��ָ����value����Map
	 * @param key ָ����key
	 * @param value ָ����value
	 * @see ByteMap#get
	 */
	public Object put(byte key, Object value) {
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		for (int i = 0; i < count; i++) {
			if (keys[i] == key) {
				Object o = objs[i];
				objs[i] = value;
				return o;
			}
		}

		if (count >= keys.length) {
			int len = (int) (count * 1.1) + 1;
			this.keys = new byte[len];
			this.objs = new Object[len];
			System.arraycopy(keys, 0, this.keys, 0, count);
			System.arraycopy(objs, 0, this.objs, 0, count);
		}

		this.keys[count] = key;
		this.objs[count] = value;
		++count;
		return null;
	}

	/**
	 * ������һ��ByteMap�е���������뱾ByteMap��key���ظ��򸲸�
	 * @param bam ��һ��ByteMap
	 */
	public void putAll(IByteMap bm) {
		ensureCapacity(count + bm.size());
		for (int i = 0; i < bm.size(); i++) {
			put(bm.getKey(i), bm.getValue(i));
		}
	}

	/*
	 * ���߶�Ӧ��ָ��key��Ԫ�أ���ָ����key�����ڣ���ֱ�ӷ���
	 * @param key ָ����key
	 * @return ָ��key��Ӧ��value����key�����ڣ��򷵻�null
	 */
	public Object remove(byte key) {
		byte[] keys = this.keys;
		for (int i = 0; i < count; i++) {
			if (keys[i] == key) {
				return removeEntry(i);
			}
		}
		return null;
	}

	/**
	 * ��ָ����key��value׷�ӵ���ByteMap�У�ע��˷�����������ͬkey����
	 * @param key ָ����key
	 * @param value ָ����value
	 */
	public void add(byte key, Object value) {
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		if (count >= keys.length) {
			int len = (int) (count * 1.1) + 1;
			this.keys = new byte[len];
			this.objs = new Object[len];
			System.arraycopy(keys, 0, this.keys, 0, count);
			System.arraycopy(objs, 0, this.objs, 0, count);
		}
		this.keys[count] = key;
		this.objs[count] = value;
		count++;
	}

	/**
	 * ����һ��ByteMap�е���׷�ӵ���ByteMap��;ע��˷�����������ͬkey����
	 * @param bm ��һ��ByteMap
	 */
	public void addAll(IByteMap bm) {
		ensureCapacity(count + bm.size());
		for (int i = 0; i < bm.size(); i++) {
			add(bm.getKey(i), bm.getValue(i));
		}
	}

	/*
	 * ����λ��ɾ����
	 * @param index λ��
	 * @return ����ָ��λ�õ�value
	 */
	public Object removeEntry(int index) {
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		Object o = objs[index];
		System.arraycopy(keys, index + 1, keys, index, count - index - 1);
		System.arraycopy(objs, index + 1, objs, index, count - index - 1);
		count--;
		objs[count] = null; //let gc
		return o;
	}

	/*
	 * ����λ��ȡ�ö�Ӧ��key
	 * @param index λ��
	 */
	public byte getKey(int index) {
		return keys[index];
	}

	/*
	 * ����λ��ȡ�ö�Ӧ��value
	 * @param index λ��
	 */
	public Object getValue(int index) {
		return objs[index];
	}

	/*
	 * ȡMap����ָ��key��Ӧ��index,����Ҳ���ָ����key�򷵻�-1
	 * @param key ָ����key
	 */
	public int getIndex(byte key){
		byte[] keys = this.keys;
		for (int i = count - 1; i >= 0; i--) {
			if (keys[i] == key) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * ��ָ����index��Ӧ��value����Map
	 * @param index ָ����index
	 * @see ByteMap#setValue
	 */
	public void setValue(int index,Object value){
		objs[index]=value;
	}

	/**
	 * ������ظ����ֻ�������һ��
	 */
	public void purgeDupKeys() {
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		short oldCount = this.count, newCount = oldCount;
		int x = oldCount - 2;  //��ǰ�洢λ��
		for( int i = x; i >= 0; i -- ) {
			int j = oldCount - 1;
			for( ; j > x; j -- ) {
				if ( keys[i] == keys[j] ) { //���ظ�ʱ
					newCount --;
					break;
				}
			}//for j
			if ( j == x ) {
				keys[x] = keys[i];
				objs[x] = objs[i];
				x --;
			}
		}//for i
		x = oldCount - newCount;
		if ( x != 0 ) {
			System.arraycopy( keys, x, keys, 0, newCount );
			System.arraycopy( objs, x, objs, 0, newCount );
		}
		this.count = newCount;
	}

	/**
	 * ���ֵΪnull����
	 */
	public void purgeNullValues(){
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		short oldCount = this.count, newCount = oldCount;
		int x = oldCount - 1;   //��ǰ�洢λ��
		for( int i = x; i >= 0; i -- ) {
			if ( objs[i] == null ) { //ֵΪnullʱ
				newCount --;
			} else  {
				if ( x != i ) {
					keys[x] = keys[i];
					objs[x] = objs[i];
				}
				x --;
			}
		}//for i
		x = oldCount - newCount;
		if ( x != 0 ) {
			System.arraycopy( keys, x, keys, 0, newCount );
			System.arraycopy( objs, x, objs, 0, newCount );
		}
		this.count = newCount;
	}

	/*
	 * ���Map
	 */
	public void clear() {
		count = 0;
		for (int i = 0; i < objs.length; i++) {
			objs[i] = null; // let gc
		}
	}

	/*
	 * ��ȿ�¡Map
	 */
	public Object deepClone() {
		short count = this.count;
		ByteMap t = new ByteMap(count);
		t.count = count;
		System.arraycopy(keys, 0, t.keys, 0, count);
		Object[] old1 = this.objs;
		Object[] new1 = t.objs;
		for (short i = 0; i < count; i++) {
			Object o = old1[i];
			if (o instanceof ICloneable) {
				new1[i] = ( (ICloneable) o).deepClone();
			}
			else {
				new1[i] = o;
			}
		}
		return t;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(count);
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		for (int i = 0; i < count; i++) {
			out.writeByte(keys[i]);
			out.writeObject(objs[i]);
		}
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		count = in.readShort();
		byte[] keys = new byte[count];
		Object[] objs = new Object[count];
		for (int i = 0; i < count; i++) {
			keys[i] = in.readByte();
			objs[i] = in.readObject();
		}
		this.keys = keys;
		this.objs = objs;
	}

	public byte[] serialize() throws IOException {
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeShort(count);
		byte[] keys = this.keys;
		Object[] objs = this.objs;
		for (int i = 0; i < count; i++) {
			out.writeByte(keys[i]);
			out.writeObject(objs[i], true);
		}
		return out.toByteArray();
	}

	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);
		count = in.readShort();
		byte[] keys = new byte[count];
		Object[] objs = new Object[count];
		for (int i = 0; i < count; i++) {
			keys[i] = in.readByte();
			objs[i] = in.readObject(true);
		}
		this.keys = keys;
		this.objs = objs;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append('{');

		byte[] keys = this.keys;
		Object[] objs = this.objs;
		for (int i = 0; i < count; i++) {
			buf.append(keys[i]).append('=').append(objs[i]);
			if (i < count - 1) {
				buf.append(", ");
			}
		}

		buf.append('}');
		return buf.toString();
	}
	
	public byte[] getKeys(){
		return keys;
	}
	
	public static void main(String[] args) throws Exception {
		ByteMap ih = new ByteMap();
		ih.add( (byte) 1, "abc");
		ih.add( (byte) 2, null);
		ih.add( (byte) 3, "dfdf");
		ih.add( (byte) 1, null);
		ih.add( (byte) 2, "a bc");
		ih.add( (byte) 3, "ad");
		ih.purgeNullValues();
		System.out.println(ih);
	}
}
