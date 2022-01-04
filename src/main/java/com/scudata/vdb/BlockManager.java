package com.scudata.vdb;

import java.io.IOException;
import java.io.RandomAccessFile;

//�տ������
/**
 * ���ݿ��ļ���������������������տ�ͻ������ϵĿ�
 * @author RunQian
 *
 */
class BlockManager {
	private static final int ALLUSED = 0xffffffff; // 32λȫ��ռ��
	
	private Library library; // ���ݿ����
	private volatile int totalBlockCount; // �ܿ���
	private int []blockSigns; // ���Ƿ�ռ�ñ�־����ռ��������Ӧ��λΪ1������Ϊ0
	private int signIndex; // ��һ��ɨ�赽�տ��λ��
	private boolean stopSign = false;
	
	
	public BlockManager(Library library) {
		this.library = library;
	}
	
	// ���ÿ�����
	private void setBlockCount(int n) {
		int len = n / 32; // ÿ��int���Ա�ʾ32��
		if (n % 32 != 0) {
			len++;
		}
		
		if (blockSigns == null) {
			// ����ʱ��ʼ��
			blockSigns = new int[len];
		} else {
			// �������
			int []tmp = new int[len];
			System.arraycopy(blockSigns, 0, tmp, 0, blockSigns.length);
			blockSigns = tmp;
		}
	}
	
	// ����ָ�������
	private void setBlockUnused(int block) {
		int m = block / 32;
		int n= block % 32;
		blockSigns[m] &= ~(1 << n);
		
		if (signIndex > m) {
			signIndex = m;
		}
	}
	
	/**
	 * ����ʱ���ã�����ͬ�������ÿ鱻ʹ��
	 * @param block ������
	 */
	public void setBlockUsed(int block) {
		int m = block / 32;
		int n= block % 32;
		blockSigns[m] |= (1 << n);
	}
	
	/**
	 * ����ʱ���ã�����ͬ�������ÿ鱻ʹ��
	 * @param blocks ����������
	 */
	public void setBlocksUsed(int []blocks) {
		for (int block : blocks) {
			int m = block / 32;
			int n = block % 32;
			blockSigns[m] |= (1 << n);
		}
	}
	
	/**
	 * �����������
	 * @param file ��ʱ�ļ������ڼ��ؿ�ʹ����Ϣ�����Ϊ����ɨ�����ݿ��ļ�
	 * @throws IOException
	 */
	public void start(RandomAccessFile file) throws IOException {
		if (file == null) {
			scanUsedBlocks();
		} else {
			totalBlockCount = file.readInt();
			int len = file.readInt();
			int []blockSigns = new int[len];
			this.blockSigns = blockSigns;
			for (int i = 0; i < len; ++i) {
				blockSigns[i] = file.readInt();
			}
		}
	}
	
	boolean getStopSign() {
		return stopSign;
	}
	
	/**
	 * ���ݿ�رգ��رտ������
	 */
	public void stop() {
		//thread.setStop();
		//thread.notify();
		stopSign = true;
	}
	
	// ɨ�������ļ�������λ�õ��Ŀ飬��ʱû�����ӣ�ɾ���������λ
	void doThreadScan() throws IOException {
		int total = (int)(library.getFile().length() / Library.BLOCKSIZE);
		totalBlockCount = total;
		setBlockCount(total);
		
		ISection section = library.getRootSection();
		section.scanUsedBlocks(library, this);
		setBlockUsed(0);
	}
	
	// ɨ�������ļ�������λ�õ��Ŀ飬��ʱû�����ӣ�ɾ���������λ
	private void scanUsedBlocks() throws IOException {
		int total = (int)(library.getFile().length() / Library.BLOCKSIZE);
		totalBlockCount = total;
		setBlockCount(total);
		
		ISection section = library.getRootSection();
		section.scanUsedBlocks(library, this);
		setBlockUsed(0);
				
		/*int usedCount = usedBlocks.length;
		IntArrayList blockList = new IntArrayList(totalBlockCount - usedCount);
		this.blockList = blockList;
		
		int prev = 0;
		int b = 2;
		
		while (b < total) {
			if (b < usedBlocks[prev]) {
				blockList.addInt(b);
				b++;
			} else if (b == usedBlocks[prev]) {
				b++;
				if (++prev == usedCount) {
					break;
				}
			} else {
				if (++prev == usedCount) {
					break;
				}
			}
		}
		
		for (; b < total; ++b) {
			blockList.addInt(b);
		}*/
	}
	
	// �����ļ�
	private void enlargeFile() {
		totalBlockCount += Library.ENLARGE_BLOCKCOUNT;
		library.enlargeFile((long)totalBlockCount * Library.BLOCKSIZE);
		setBlockCount(totalBlockCount);
	}
	
	/**
	 * �����׿�����Ҫ������飬����block��
	 * @param block ԭ�׿��
	 * @param blockCount ��Ҫ���ܿ���
	 * @return �տ��
	 */
	public synchronized int[] applyHeaderBlocks(int block, int blockCount) {
		if (blockCount == 1) {
			return new int[] {block};
		}
		
		int []blocks = new int[blockCount];
		blocks[0] = block;
		
		int m = block / 32;
		if (m < signIndex) m = signIndex;
		
		int []blockSigns = this.blockSigns;
		int count = blockSigns.length;
		
		Next:
		for (int i = 1; i < blockCount; ++i) {
			for (; m < count; ++m) {
				if (blockSigns[m] != ALLUSED) {
					int sign = blockSigns[m];
					for (int n = 0; n < 32; ++n) {
						if ((sign & (1 << n)) == 0) {
							blockSigns[m] |= (1 << n);
							blocks[i] = m * 32 + n;
							continue Next;
						}
					}
				}
			}
			
			blocks[i] = totalBlockCount;
			enlargeFile(); // ��ı�totalBlockCount��С
			setBlockUsed(blocks[i]);
		}
		
		signIndex = m;
		return blocks;
	}
	
	/**
	 * �������ݿ�����Ҫ������飬������block��
	 * @param block �׿��λ�ã����ݿ��ŵ�λ�þ��������׿�
	 * @param blockCount
	 * @return
	 */
	public synchronized int[] applyDataBlocks(int block, int blockCount) {
		int m = block / 32;
		if (m < signIndex) m = signIndex;
		
		int []blockSigns = this.blockSigns;
		int count = blockSigns.length;
		int []blocks = new int[blockCount];
		
		Next:
		for (int i = 0; i < blockCount; ++i) {
			for (; m < count; ++m) {
				if (blockSigns[m] != ALLUSED) {
					int sign = blockSigns[m];
					for (int n = 0; n < 32; ++n) {
						if ((sign & (1 << n)) == 0) {
							blockSigns[m] |= (1 << n);
							blocks[i] = m * 32 + n;
							continue Next;
						}
					}
				}
			}
			
			blocks[i] = totalBlockCount;
			enlargeFile(); // ��ı�totalBlockCount��С
			setBlockUsed(blocks[i]);
		}
		
		signIndex = m;
		return blocks;
	}

	/**
	 * ����һ���׿�
	 * @return �׿��
	 */
	public synchronized int applyHeaderBlock() {
		int []blockSigns = this.blockSigns;
		int count = blockSigns.length;
		for (int m = signIndex; m < count; ++m) {
			if (blockSigns[m] != ALLUSED) {
				int sign = blockSigns[m];
				for (int n = 0; n < 32; ++n) {
					if ((sign & (1 << n)) == 0) {
						signIndex = m;
						blockSigns[m] |= (1 << n);
						return m * 32 + n;
					}
				}
			}
		}
		
		int result = totalBlockCount;
		signIndex = count;
		enlargeFile();
		setBlockUsed(result);
		return result;
	}
	
	// ����ָ�������
	public synchronized void recycleBlock(int block) {
		setBlockUnused(block);
	}
	
	// ����ָ�������
	public synchronized void recycleBlocks(int[] blocks) {
		for (int block : blocks) {
			setBlockUnused(block);
		}
	}
	
	// ����ָ�������
	public synchronized void recycleBlocks(int[] blocks, int pos) {
		for (int len = blocks.length; pos < len; ++pos) {
			setBlockUnused(blocks[pos]);
		}
	}
	
	// �ѿ���Ϣд����ʱ�ļ���Ϊ�´��������ݿ�����
	void writeTempFile(RandomAccessFile file) throws IOException {
		int []blockSigns = this.blockSigns;
		int len = blockSigns.length;
		file.writeInt(totalBlockCount);
		file.writeInt(len);
		for (int sign : blockSigns) {
			file.writeInt(sign);
		}
	}
}
