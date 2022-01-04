package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Comparator;

import com.scudata.common.ByteArrayInputRecord;
import com.scudata.common.ByteArrayOutputRecord;
import com.scudata.common.IRecord;
import com.scudata.thread.MultithreadUtil;
import com.scudata.util.Variant;

/**
 * ������1��ʼ�������б�
 * @author WangXiaoJun
 *
 */
public class ListBase1 implements Externalizable, IRecord {
	private static final long serialVersionUID = 0x02010005;

	protected Object []elementData;
	protected int size;

	/**
	 * ����һָ����ʼ�����Ŀ�list.
	 * @param initialCapacity ��ʼ����.
	 */
	public ListBase1(int initialCapacity) {
		if (initialCapacity > 0) {
			elementData = new Object[initialCapacity + 1];
		} else {
			elementData = new Object[1];
		}
	}

	/**
	 * ����һ��ʼ����Ϊ10�Ŀ��б�.
	 */
	public ListBase1() {
		this(10);
	}

	/**
	 * ����һָ��Ԫ�ص��б�
	 * @param v Object[]
	 */
	public ListBase1(Object []v) {
		size = v.length;
		elementData = new Object[size + 1];
		System.arraycopy(v, 0, elementData, 1, size);
	}

	/**
	 * ���ƶ���
	 * @param src Դ����
	 */
	public ListBase1(ListBase1 src) {
		size = src.size;
		elementData = new Object[size + 1];
		for (int i = 1; i <= size; i++) {
			elementData[i] = src.get(i);
		}
	}

	/**
	 * �����б��������ʹ����Ԫ�������
	 */
	public void trimToSize() {
		int newLen = size + 1;
		if (newLen < elementData.length) {
			Object oldData[] = elementData;
			elementData = new Object[newLen];
			System.arraycopy(oldData, 1, elementData, 1, size);
		}
	}

	/**
	 * ʹ�б��������С��minCapacity
	 * @param minCapacity ��С����
	 */
	public void ensureCapacity(int minCapacity) {
		int oldSize = elementData.length;
		if (oldSize <= minCapacity) {
			Object oldData[] = elementData;
			int newSize = (oldSize * 3) / 2;
			if (newSize <= minCapacity) {
				newSize = minCapacity + 1;
			}

			elementData = new Object[newSize];
			System.arraycopy(oldData, 1, elementData, 1, size);
		}
	}

	/**
	 * �����б��Ԫ����Ŀ
	 * @return int
	 */
	public int size() {
		return size;
	}

	/**
	 * �����б��Ƿ�Ϊ��.
	 * @return boolean
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem) {
		for (int i = 1; i <= size; ++i) {
			if (Variant.isEquals(elem, elementData[i])) return true;
		}
		return false;
	}
	
	/**
	 * �����б����Ƿ����ָ��Ԫ�أ�ʹ�õȺűȽ�
	 * @param elem
	 * @return boolean true��������false��������
	 */
	public boolean objectContains(Object elem) {
		for (int i = 1; i <= size; ++i) {
			if (elem == elementData[i]) return true;
		}
		return false;
	}

	/**
	 * �����б����Ƿ����ָ��Ԫ��
	 * @param elem Object �����ҵ�Ԫ��
	 * @param comparator Comparator<Object> �Ƚ���
	 * @return boolean true��������false��������
	 */
	public boolean contains(Object elem, Comparator<Object> comparator) {
		for (int i = 1; i <= size; ++i) {
			if (comparator.compare(elem, elementData[i]) == 0) return true;
		}
		return false;
	}

	/**
	 * ��list��Ԫ�ؽ�������
	 * @param comparator Comparator<Object>
	 */
	public void sort(Comparator<Object> comparator) {
		MultithreadUtil.sort(elementData, 1, size + 1, comparator);
	}

	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem Object
	 * @param comparator Comparator<Object>
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, Comparator<Object> comparator) {
		return binarySearch(elem, 1, size, comparator);
	}

	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem) {
		Object[] elementData = this.elementData;
		int low = 1, high = size();
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = Variant.compare(elementData[mid], elem, true);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}

	/**
	 * ���ַ�����ָ��Ԫ��
	 * @param elem Object
	 * @param low int ��ʼλ��(����)
	 * @param high int   ����λ��(����)
	 * @param comparator Comparator<Object>
	 * @return int Ԫ�ص�����,��������ڷ��ظ��Ĳ���λ��.
	 */
	public int binarySearch(Object elem, int low, int high, Comparator<Object> comparator) {
		Object[] elementData = this.elementData;
		while (low <= high) {
			int mid = (low + high) >> 1;
			int cmp = comparator.compare(elementData[mid], elem);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}

		return -low; // key not found
	}

	/**
	 * ����Ԫ�س��ֵ�λ��
	 * @param elem Object
	 * @param start int ��ʼλ��(����)
	 * @param end int   ����λ��(����)
	 * @return int ���Ԫ�ز����ڷ���-1
	 */
	public int indexOf(Object elem, int start, int end) {
		for (int i = start; i <= end; ++i) {
			if (Variant.isEquals(elem, elementData[i])) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * ����Ԫ���״γ��ֵ�λ��
	 * @param elem �����ҵ�Ԫ��
	 * @return  ���Ԫ�ز����ڷ���-1
	 */
	public int firstIndexOf(Object elem) {
		for (int i = 1; i <= size; ++i) {
			if (Variant.isEquals(elem, elementData[i])) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * ����Ԫ���״γ��ֵ�λ��
	 * @param elem Object
	 * @param isSorted boolean �б��Ƿ��������
	 * @return int ���Ԫ�ز����ڷ���-1
	 */
	public int firstIndexOf(Object elem, boolean isSorted) {
		if (isSorted) {
			int index = binarySearch(elem);
			if (index < 1) {
				return -1;
			}

			// ��ǰ�Ƚϣ��ҵ���һ����obj��ͬ��Ԫ��
			Object []elementData = this.elementData;
			while (index > 1) {
				if (Variant.isEquals(elem, elementData[index - 1])) {
					index--;
				} else {
					break;
				}
			}

			return index;
		} else {
			for (int i = 1; i <= size; ++i) {
				if (Variant.isEquals(elem, elementData[i])) {
					return i;
				}
			}
			
			return -1;
		}
	}

	/**
	 * ����Ԫ�������ֵ�λ��
	 * @param elem Object
	 * @return ���Ԫ�ز����ڷ���-1
	 */
	public int lastIndexOf(Object elem) {
		for (int i = size; i > 0; --i) {
			if (Variant.isEquals(elem, elementData[i])) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * ����Ԫ�������ֵ�λ��
	 * @param elem Object
	 * @param comparator Comparator<Object>
	 * @param isSorted boolean list�Ƿ��������
	 * @return int ���Ԫ�ز����ڷ���-1
	 */
	public int lastIndexOf(Object elem, Comparator<Object> comparator, boolean isSorted) {
		if (isSorted) {
			int index = binarySearch(elem, comparator);
			if (index < 1) {
				return -1;
			}

			// ��ǰ�Ƚϣ��ҵ���һ����obj��ͬ��Ԫ��
			Object []elementData = this.elementData;
			while (index < size) {
				if (comparator.compare(elem, elementData[index + 1]) == 0) {
					index++;
				} else {
					break;
				}
			}

			return index;
		} else {
			for (int i = size; i > 0; --i) {
				if (comparator.compare(elem, elementData[i]) == 0) {
					return i;
				}
			}
			
			return -1;
		}
	}

	/**
	 * ����list��Ԫ�ع��ɵ�����
	 * @return Object[]
	 */
	public Object[] toArray() {
		Object[] result = new Object[size];
		System.arraycopy(elementData, 1, result, 0, size);
		return result;
	}

	/**
	 * ��Ԫ�����θ���a��������a
	 * @param a Object[]
	 * @return Object[]
	 */
	public Object[] toArray(Object a[]) {
		System.arraycopy(elementData, 1, a, 0, size);
		return a;
	}

	/**
	 * Returns the element at the specified position in this list.
	 * @param  index index of element to return. ��1��ʼ����
	 * @return the element at the specified position in this list.
	 */
	public Object get(int index) {
		return elementData[index];
	}

	/**
	 * Replaces the element at the specified position in this list with
	 * the specified element.
	 * @param index index of element to replace.
	 * @param element element to be stored at the specified position.
	 */
	public void set(int index, Object element) {
		elementData[index] = element;
	}

	/**
	 * Appends the specified element to the end of this list.
	 * @param o element to be appended to this list.
	 */
	public void add(Object o) {
		ensureCapacity(size + 1); // Increments modCount!!
		elementData[++size] = o;
	}

	/**
	 * Inserts the specified element at the specified position in this
	 * list. Shifts the element currently at that position (if any) and
	 * any subsequent elements to the right (adds one to their indices).
	 * @param index index at which the specified element is to be inserted.
	 * @param element element to be inserted.
	 */
	public void add(int index, Object element) {
		ensureCapacity(size + 1); // Increments modCount!!

		System.arraycopy(elementData, index, elementData, index + 1, size - index + 1);
		elementData[index] = element;
		size++;
	}

	/**
	 * Removes the element at the specified position in this list.
	 * Shifts any subsequent elements to the left (subtracts one from their indices).
	 * @param index the index of the element to removed.
	 * @return Object old value
	 */
	public Object remove(int index) {
		Object oldValue = elementData[index];
		System.arraycopy(elementData, index + 1, elementData, index, size - index);
		elementData[size--] = null;
		return oldValue;
	}

	// ɾ�����Ԫ�أ���Ŵ�С��������
	public void remove(int []seqs) {
		int delCount = 0;
		for (int i = 0, len = seqs.length; i < len; ) {
			int cur = seqs[i];
			i++;

			int next = (i < len ? seqs[i] : size() + 1);
			int moveCount = next - cur - 1;

			if (moveCount > 0) {
				System.arraycopy(elementData, cur + 1, elementData, cur - delCount, moveCount);
				delCount++;
			} else if (moveCount == 0) {
				delCount++;
			}
		}

		for (int i = 0; i < delCount; ++i) {
			elementData[size--] = null;
		}
	}

	/**
	 * Removes all of the elements from this list.  The list will
	 * be empty after this call returns.
	 */
	public void clear() {
		Object []elementData = this.elementData;
		int count = size;
		for (int i = 1; i <= count; ++i)
			elementData[i] = null;

		size = 0;
	}

	/**
	 * Appends all of the elements in the specified Collection to the end of
	 * this list, in the order that they are returned by the
	 * specified Collection's Iterator.  The behavior of this operation is
	 * undefined if the specified Collection is modified while the operation
	 * is in progress.  (This implies that the behavior of this call is
	 * undefined if the specified Collection is this list, and this
	 * list is nonempty.)
	 * @param a the elements to be inserted into this list.
	 */
	public void addAll(Object[] a) {
		int numNew = a.length;
		ensureCapacity(size + numNew);  // Increments modCount
		System.arraycopy(a, 0, elementData, size + 1, numNew);
		size += numNew;
	}

	public void addAll(ListBase1 src) {
		int numNew = src.size;
		ensureCapacity(size + numNew);  // Increments modCount
		System.arraycopy(src.elementData, 1, elementData, size + 1, numNew);
		size += numNew;
	}

	public void addAll(ListBase1 src, int count) {
		ensureCapacity(size + count);  // Increments modCount
		System.arraycopy(src.elementData, 1, elementData, size + 1, count);
		size += count;
	}

	public void addSection(ListBase1 src, int srcIndex) {
		int numNew = src.size - srcIndex + 1;
		ensureCapacity(size + numNew);  // Increments modCount
		System.arraycopy(src.elementData, srcIndex, elementData, size + 1, numNew);
		size += numNew;
	}
	
	// srcStartԴ��ʼλ�ã���������srcEndԴ����λ�ã���������
	public void addSection(ListBase1 src, int srcStart, int srcEnd) {
		int numNew = srcEnd - srcStart;
		ensureCapacity(size + numNew);  // Increments modCount
		System.arraycopy(src.elementData, srcStart, elementData, size + 1, numNew);
		size += numNew;
	}

	/**
	 * Inserts all of the elements in the specified Collection into this
	 * list, starting at the specified position.  Shifts the element
	 * currently at that position (if any) and any subsequent elements to
	 * the right (increases their indices).  The new elements will appear
	 * in the list in the order that they are returned by the
	 * specified Collection's iterator.
	 * @param index index at which to insert first element
	 *		    from the specified collection.
	 * @param a elements to be inserted into this list.
	 */
	public void addAll(int index, Object[] a) {
		int numNew = a.length;
		ensureCapacity(size + numNew); // Increments modCount

		System.arraycopy(elementData, index, elementData, index + numNew, size - index + 1);
		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
	}

	public void addAll(int index, ListBase1 src) {
		int numNew = src.size;
		ensureCapacity(size + numNew);  // Increments modCount

		System.arraycopy(elementData, index, elementData, index + numNew, size - index + 1);
		System.arraycopy(src.elementData, 1, elementData, index, numNew);
		size += numNew;
	}

	public void addAll(int index, ListBase1 src, int count) {
		ensureCapacity(size + count);  // Increments modCount

		System.arraycopy(elementData, index, elementData, index + count, size - index + 1);
		System.arraycopy(src.elementData, 1, elementData, index, count);
		size += count;
	}

	/**
	 * Removes from this List all of the elements whose index is between
	 * fromIndex, inclusive and toIndex, inclusive.  Shifts any succeeding
	 * elements to the left (reduces their index).
	 * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
	 * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
	 *
	 * @param fromIndex index of first element to be removed.
	 * @param toIndex index of last element to be removed.
	 */
	public void removeRange(int fromIndex, int toIndex) {
		System.arraycopy(elementData, toIndex + 1, elementData, fromIndex, size - toIndex);

		int newSize = size - (toIndex - fromIndex + 1);
		while (size != newSize) {
			elementData[size--] = null;
		}
	}

	public void reserve(int start, int end) {
		int newSize = end - start + 1;
		System.arraycopy(elementData, start, elementData, 1, newSize);

		while (size != newSize) {
			elementData[size--] = null;
		}
	}

	public byte[] serialize() throws IOException{
		ByteArrayOutputRecord out = new ByteArrayOutputRecord();
		out.writeInt(size);
		out.writeInt(elementData.length);

		for (int i = 1; i <= size; ++i) {
			out.writeObject(elementData[i], true);
		}

		return out.toByteArray();
	}

	public void fillRecord(byte[] buf) throws IOException, ClassNotFoundException {
		ByteArrayInputRecord in = new ByteArrayInputRecord(buf);

		size = in.readInt();
		int length = in.readInt();
		Object elementData[] = new Object[length];
		this.elementData = elementData;

		for (int i = 1; i <= size; ++i) {
			elementData[i] = in.readObject(true);
		}
	}

	/**
	 * д���ݵ���
	 * @param out �����
	 * @throws IOException
	 */
	public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
		out.writeByte(1);
		Object elementData[] = this.elementData;
		out.writeInt(size);
		out.writeInt(elementData.length);

		for (int i = 1; i <= size; ++i) {
			out.writeObject(elementData[i]);
		}
	}

	/**
	 * �����ж�����
	 * @param in ������
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
		in.readByte(); // �汾
		size = in.readInt();
		int length = in.readInt();
		Object elementData[] = new Object[length];
		this.elementData = elementData;

		for (int i = 1; i <= size; ++i) {
			elementData[i] = in.readObject();
		}
	}
}
