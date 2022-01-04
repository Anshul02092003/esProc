package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * �źţ����8���ֽڹ��ɵ����������ڱ�ʾ��ֵ���ɱȽϣ���������
 * ��һ���ֽڱ�����value1������ֽ���
 * value1					value2
 * b1 b2 b3 b4 b5 b6 b7 b8	b9 b10 b11 b12�ڿڿڿ�
 *
 * @author WangXiaoJun
 */
public class SerialBytes implements Externalizable, Comparable<SerialBytes> {
	private static final long serialVersionUID = 0x02613003;
	private static final long LONGSIGN = 0xFFFFFFFFFFFFFFFFL;
	
	private long value1;
	private long value2;
	private int len;
	
	// �������л�
	public SerialBytes() {
	}
	
	public SerialBytes(byte []bytes, int len) {
		this.len = len;
		int index = 0;
		for (byte b : bytes) {
			++index;
			if (index <= 8) {
				value1 |= (0xFFL & b) << (8 - index) * 8;
			} else {
				value2 |= (0xFFL & b) << (16 - index) * 8;
			}
		}
	}
	
	private SerialBytes(long value1, long value2, int len) {
		this.value1 = value1;
		this.value2 = value2;
		this.len = len;
	}
	
	/**
	 * �����źŶ���
	 * @param num ��
	 * @param len �ֽ�������Χ��[1,16]
	 */
	public SerialBytes(Number num, int len) {
		if (len > 16) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
		}
		
		this.len = len;
		if (len <= 8) {
			value1 = num.longValue() << (8 - len) * 8;
		} else {
			BigInteger bi = Variant.toBigInteger(num);
			byte []bytes = bi.toByteArray();
			int blen = bytes.length;
			if (blen > len) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("serialbytes.biLenMismatch"));
			}
			
			// ���ʵ���ֽ������ڸ������ֽ�������0����
			int index = len - blen;
			for (byte b : bytes) {
				++index;
				if (index <= 8) {
					value1 |= (0xFFL & b) << (8 - index) * 8;
				} else {
					value2 |= (0xFFL & b) << (16 - index) * 8;
				}
			}
		}
	}
	
	/**
	 * �����źŶ���
	 * @param vals ������
	 * @param lens ÿ�������ֽ������飬���ֽ������ܳ���16
	 */
	public SerialBytes(Number []vals, int []lens) {
		int len = 0;
		for (int i = 0; i < vals.length; ++i) {
			int curLen = lens[i];
			if (curLen <= 8) {
				// �������С��8��Ѹ�λ��0���ɵ�����ת�������������Ǹ�ֵ
				long curVal = vals[i].longValue() & (LONGSIGN >>> 8 - curLen);
				
				if (len < 8) {
					len += curLen;
					if (len <= 8) {
						value1 |= curVal << (8 - len) * 8;
					} else {
						value1 |= curVal >>> (len - 8) * 8;
						value2 = curVal << (16 - len) * 8;
					}
				} else {
					len += curLen;
					if (len > 16) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
					}
					
					value2 |= curVal << (16 - len) * 8;
				}
			} else {
				BigInteger bi = Variant.toBigInteger(vals[i]);
				byte []bytes = bi.toByteArray();
				int blen = bytes.length;
				if (blen > curLen) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("serialbytes.biLenMismatch"));
				}
				
				// ���ʵ���ֽ������ڸ������ֽ�������0����
				len += curLen - blen;
				
				for (byte b : bytes) {
					++len;
					if (len <= 8) {
						value1 |= (0xFFL & b) << (8 - len) * 8;
					} else {
						value2 |= (0xFFL & b) << (16 - len) * 8;
					}
				}
				
				if (len > 16) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
				}
			}
		}
		
		this.len = len;
	}
	
	/**
	 * �����źŵĳ���
	 * @return int
	 */
	public int length() {
		return len;
	}
	
	/**
	 * �����źŵĹ�ϣֵ
	 * @return int
	 */
    public int hashCode() {
    	return HashUtil.hashCode(value1 + value2);
    }
	
    /**
     * ת���ַ�����������ʾ
	 * @return String
     */
	public String toString() {
		if (len > 8) {
			return Long.toHexString(value1) + Long.toHexString(value2);
		} else {
			return Long.toHexString(value1);
		}
	}
	
	/**
	 * ���ź�ת���ֽ����飬���ڴ洢
	 * @return �ֽ�����
	 */
	public byte[] toByteArray() {
		byte []bytes = new byte[len];
		int i = len;
		for (; i > 8; --i) {
			bytes[i - 1] = (byte)(value2 >>> (16 - i) * 8);
		}
		
		for (; i > 0; --i) {
			bytes[i - 1] = (byte)(value1 >>> (8 - i) * 8);
		}
		
		return bytes;
	}
	
	/**
	 * ȡ�ź�ָ���ֽڵ�ֵ
	 * @param q �ֽںţ���1��ʼ����
	 * @return long
	 */
	public long getByte(int q) {
		if (q < 1 || q > len) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(q + mm.getMessage("engine.indexOutofBound"));
		} else if (q <= 8) {
			return (value1 >>> (8 - q) * 8) & 0xFFL;
		} else {
			return (value2 >>> (16 - q) * 8) & 0xFFL;
		}
	}
	
	/**
	 * ȡ�ź�ָ���ֽ������ֵ
	 * @param start ��ʼ�ֽںţ���1��ʼ������������
	 * @param end �����ֽںţ���1��ʼ������������
	 * @return long
	 */
	public long getBytes(int start, int end) {
		if (start < 1 || end < start || end > len) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(start + "," + end + mm.getMessage("engine.indexOutofBound"));
		}
		
		if (start <= 8) {
			if (end <= 8) {
				return (value1 << (start - 1) * 8) >>> (7 + start - end) * 8;
			} else {
				long result = (value1 << (start - 1) * 8) >>> (start - 1) * 8;
				return (result << (end - 8) * 8) | (value2 >>> (16 - end) * 8);
			}
		} else {
			return (value2 << (start - 9) * 8) >>> (7 + start - end) * 8;
		}
	}
	
	/**
	 * �Ƚ������źŵĴ�С����������
	 * @param o
	 * @return int
	 */
	public int compareTo(SerialBytes o) {
		if (value1 == o.value1) {
			if (value2 == o.value2) {
				// ���ܳ��Ȳ�ͬ����λ��0
				return len == o.len ? 0 : (len > o.len ? 1 : -1);
			} else if (value2 < 0) {
				if (o.value2 >= 0) {
					return 1;
				} else {
					return value2 > o.value2 ? 1 : -1;
				}
			} else {
				if (o.value2 < 0) {
					return -1;
				} else {
					return value2 > o.value2 ? 1 : -1;
				}
			}
		} else {
			if (value1 < 0) {
				if (o.value1 >= 0) {
					return 1;
				} else {
					return value1 > o.value1 ? 1 : -1;
				}
			} else {
				if (o.value1 < 0) {
					return -1;
				} else {
					return value1 > o.value1 ? 1 : -1;
				}
			}
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SerialBytes) {
			return equals((SerialBytes)obj);
		} else {
			return false;
		}
	}
	
	/**
	 * �ж������ź��Ƿ����
	 * @param other
	 * @return
	 */
	public boolean equals(SerialBytes other) {
		return len == other.len && value1 == other.value1 && value2 == other.value2;
	}
	
	public SerialBytes add(SerialBytes other) {
		int len = this.len;
		int total = len + other.len;
		if (total > 16) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("serialbytes.outOfLimit"));
		}
		
		long value1 = this.value1;
		long value2 = this.value2;
		byte []bytes = other.toByteArray();
		
		for (byte b : bytes) {
			++len;
			if (len <= 8) {
				value1 |= (0xFFL & b) << (8 - len) * 8;
			} else {
				value2 |= (0xFFL & b) << (16 - len) * 8;
			}
		}
		
		return new SerialBytes(value1, value2, total);
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(value1);
		out.writeLong(value2);
		out.writeInt(len);
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value1 = in.readLong();
		value2 = in.readLong();
		len = in.readInt();
	}
}