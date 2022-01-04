package com.scudata.dw;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

/**
 * LZ4ѹ����ѹ������
 * @author runqian
 *
 */
public class LZ4Util {
	private static ThreadLocal<LZ4Util> local = new ThreadLocal<LZ4Util>() {
		protected synchronized LZ4Util initialValue() {
			return new LZ4Util();
		}
	};
	
	//ѹ��
	private LZ4Compressor compressor = LZ4Factory.fastestInstance().fastCompressor();
	
	//��ѹ
	private LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();
	
	private int count;
	
	private LZ4Util() {
	}
	
	public static LZ4Util instance() {
		return local.get();
	}
	
	/**
	 * ����ѹ����ĳ���
	 * @return
	 */
	public int getCount() {
		return count;
	}
	
	/**
	 * ����ѹ������ֽ����飬�ֽ����鳤�ȿ��ܴ���ʵ�ʳ��ȣ���Ҫ����getCountȡ��ʵ�ʳ���
	 * @param bytes
	 * @return
	 */
	public byte[] compress(byte []bytes) {
		int maxLen = compressor.maxCompressedLength(bytes.length);
		byte []buffer = new byte[maxLen];
		
		count = compressor.compress(bytes, buffer);
		return buffer;
	}
	
	/**
	 * ��ѹ��srcCountΪ��ѹ�󳤶�
	 * @param bytes
	 * @param buffer
	 * @param srcCount
	 */
	public void decompress(byte []bytes, byte []buffer, int srcCount) {
		decompressor.decompress(bytes, buffer, srcCount);
	}
	
}