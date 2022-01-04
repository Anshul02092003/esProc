package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * ��FileChannel������������
 * @author WangXiaoJun
 *
 */
public class ChannelInputStream extends InputStream {
	private FileChannel channel;

	/**
	 * ����ChannelInputStream
	 * @param channel FileChannel
	 */
	public ChannelInputStream(FileChannel channel) {
		this.channel = channel;
	}

	/**
	 * ֻ֧�ְ����ȡ
	 */
	public int read() throws IOException {
		throw new IOException("read not supported");
	}

	/**
	 * b�ĳ��ȱ���ͻ�����������ͬ
	 * @param b byte[]
	 * @throws IOException
	 * @return int
	 */
	public int read(byte []b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * len����ͻ�����������ͬ
	 * @param b byte[]
	 * @param off int
	 * @param len int
	 * @throws IOException
	 * @return int
	 */
	public int read(byte []b, int off, int len) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(len);
		int n = channel.read(buffer);
		if (n <= 0) {
			return n;
		}
		
		buffer.position(0);
		buffer.get(b, off, n);
		return n;
	}

	/**
	 * ����ָ�����ֽ�
	 * @param n �ֽ���
	 */
	public long skip(long n) throws IOException {
		channel.position(channel.position() + n);
		return n;
	}

	/**
	 * ���ؿ��õ��ֽ���
	 */
	public int available() throws IOException {
		return (int)(channel.size() - channel.position());
	}

	/**
	 * �ر�������
	 */
	public void close() throws IOException {
	}
}
