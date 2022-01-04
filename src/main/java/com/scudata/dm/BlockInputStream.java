package com.scudata.dm;

import java.io.IOException;
import java.io.InputStream;

/**
 * ���̶����С��ȡ���ݵ�������
 * @author WangXiaoJun
 *
 */
public class BlockInputStream extends InputStream {
	protected InputStream is;
	protected byte []buffer; // ������
	protected volatile int count; // ������ʵ�ʶ�����ֽ���

	protected IOException e;
	protected boolean isClosed;

	/**
	 * ������������������
	 * @param is ������
	 */
	public BlockInputStream(InputStream is) {
		this(is, Env.getFileBufSize());
	}

	/**
	 * ������������������
	 * @param is ������
	 * @param bufSize ����Ŀ��С
	 */
	public BlockInputStream(InputStream is, int bufSize) {
		this.is = is;
		this.count = 0;
		this.buffer = new byte[bufSize];

		InputStreamManager.getInstance().read(this);
	}

	// �������ݵ�������
	void fillBuffers() {
		synchronized(is) {
			if (!isClosed) {
				try {
					do {
						count = is.read(buffer);
					} while (count == 0);
				} catch (Exception e) {
					if (e instanceof IOException) {
						this.e = (IOException)e;
					} else {
						this.e = new IOException(e);
					}
				}
			} else {
				this.e = new IOException("Stream closed");
			}

			is.notify();
		}
	}

	/**
	 * ��֧�ִ˷�����ֻ�ܰ��̶����
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
		if (len != buffer.length) {
			throw new IOException("Invalid buffer size.");
		}

		synchronized(is) {
			if (count == 0) {
				if (e != null) throw e;

				try {
					is.wait();
				} catch (InterruptedException e) {
					throw new IOException(e.toString());
				}
			}

			if (count > 0) {
				int n = count;
				System.arraycopy(buffer, 0, b, off, n);
				count = 0;
				InputStreamManager.getInstance().read(this);
				return n;
			} else if (count < 0) {
				return -1; // EOF
			} else {
				throw e;
			}
		}
	}

	private static long skip(InputStream is, long count) throws IOException {
		long old = count;
		while (count > 0) {
			long num = is.skip(count);
			if (num <= 0) break;

			count -= num;
		}

		return old - count;
	}

	/**
	 * ����ָ���ֽ�
	 * @param n �ֽ���
	 * @return long ʵ���������ֽ���
	 */
	public long skip(long n) throws IOException {
		if (n < 1) return -1;

		synchronized(is) {
			if (count > 0) {
				if (count > n) {
					int rest = count - (int)n;
					System.arraycopy(buffer, (int)n, buffer, 0, rest);
					count = rest;

					return n;
				} else if (count < n) {
					long total = skip(is, n - count) + count;

					count = 0;
					InputStreamManager.getInstance().read(this);

					return total;
				} else {
					count = 0;
					InputStreamManager.getInstance().read(this);
					return n;
				}
			} else if (count < 0) {
				return 0; // EOF
			} else {
				if (e != null) throw e;

				return skip(is, n);
			}
		}
	}

	/**
	 * ���ػ��������ж��ٿ��ã������������������ж��ٿ���
	 * @return int
	 */
	public int available() throws IOException {
		synchronized(is) {
			if (e != null) {
				throw e;
			} else if (count > 0) {
				return buffer.length;
			} else if (count < 0) {
				return 0;
			} else {
				return is.available() > 0 ? buffer.length : 0;
			}
		}
	}

	/**
	 * �ر�������
	 */
	public void close() throws IOException {
		synchronized(is) {
			isClosed = true;
			is.close();
		}
	}
}
