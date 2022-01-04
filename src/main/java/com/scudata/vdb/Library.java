package com.scudata.vdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ObjectReader;
import com.scudata.dm.ObjectWriter;
import com.scudata.dm.Param;
import com.scudata.dm.ParamList;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;


/**
 * �������ݿ⣬�������ļ�ϵͳ�ṹ����һ����Ŀ¼�ͱ�����
 * @author RunQian
 *
 */
public class Library {
	public static final long MAXWAITTIME = 8000; // �������ȴ�ʱ�䣬��λ����
	
	// �����ֳ�Լ����С�����飬�ռ����������Ϊ��λ����
	public static final int BLOCKSIZE = 1024;
	public static final int ENLARGE_BLOCKCOUNT = 1024 * 8; // �ļ�̫Сʱÿ�����ӵĿ���
	
	// ��̨�����Ż��̵߳�˯��ʱ��
	private static final long SLEEPTIME = 2 * 60 * 1000; // 2����
	
	// ɨ�������ʹ������ļ��ʱ��
	private static final long SCANFILEINTERVAL = 60 * 60 * 1000; // 1Сʱ
	
	// ���������Library������vdbʱ�ȵ�����������ݿ��Ƿ��Ѿ�����
	private static ParamList libList = new ParamList();
	
	// �ļ�ͷ��Ϣ
	private long createTime; // ����ʱ��
	private long startTime; // ����ʱ��
	private long stopTime; // ֹͣʱ��
	private int rootHeaderBlock = 1; // ���׿�λ��
	
	// �����ź��ڴ�������������ȷ������һ����
	private int outerTxSeq = 1; // ���ţ�ÿ���������ݿ��1
	private transient long innerTxSeq = 0; // �ڴ��ύ�ţ������ύ������ż�1
	private transient long loadTxSeq = 0; // ������ţ������ǰû���ύ���������innerTxSeq���������innerTxSeq-1
	
	private String pathName; // ��Ӧ�������ļ�·����
	private RandomAccessFile file; // �����ļ�
	private FileChannel channel;
	private boolean isStarted = false; // �Ƿ�������
	
	private BlockManager blockManager; // �տ������
	private LinkedList<VDB> vdbList = new LinkedList<VDB>(); // �������
	private ISection rootSection; // ����
	
	// ����û���¼ʱ��
	private volatile long lastConnectTime = System.currentTimeMillis();
	private OptimizeThread optThread; // �����Ż��߳�
	
	// �����Ż��̣߳������ͷ��ڴ�� ɨ�������ʹ�����
	private class OptimizeThread extends Thread {
		private long scanTime = 0; // �ϴ������ɨ��ʱ��
		private volatile boolean userOn; // ɨ���ļ��������Ƿ������û���¼
		private BlockManager manager = null; // ����������
		
		/**
		 * �������ӵ�¼
		 */
		public void setUserOn() {
			userOn = true;
			
			if (manager != null) {
				// ֹͣɨ�������
				manager.stop();
			}
		}
		
		public void run() {
			while (isStarted) {
				try {
					sleep(SLEEPTIME);
					if (isStarted) {
						doWork();
					}
				} catch (Throwable e){
				}
			}
		}
		
		private void doWork() throws IOException {
			boolean sign = false;
			synchronized(vdbList) {
				// ���û��������ִ�����������ж�
				if (vdbList.size() == 0) {
					long now = System.currentTimeMillis();
					sign = now - lastConnectTime > SLEEPTIME && lastConnectTime - scanTime > SCANFILEINTERVAL;
					if (sign) {
						userOn = false;
						manager = new BlockManager(Library.this);
					} else {
						// �ͷ��ڴ��еĽڵ�
						rootSection.releaseSubSection();
						return;
					}
				}
			}
			
			manager.doThreadScan();
			synchronized(vdbList) {
				if (vdbList.size() == 0) {
					// �ͷ��ڴ��еĽڵ�
					rootSection.releaseSubSection();
					
					// ɨ���ڼ�û���µ�������ɨ��ɹ����
					if (!userOn) {
						scanTime = lastConnectTime;
						blockManager = manager;
					}
				}
				
				manager = null;
			}
		}
	}
	
	/**
	 * �����������ݿ�
	 * @param pathName
	 */
	public Library(String pathName) {
		this.pathName = pathName;
	}
	
	// �������ͱ������õ�·������б�ܡ���б�ܻ����ˣ�ȥ��б��
	private static String getParamName(String path) {
		String paramName = path.replace('\\', '/');
		return paramName.toLowerCase();
	}
	
	public static Library instance(String pathName) {
		String paramName = getParamName(pathName);
		Library library;
		synchronized(libList) {
			Param p = libList.get(paramName);
			if (p != null) {
				library = (Library)p.getValue();
			} else {
				System.out.println("�������ݿ⣺" + pathName);
				library = new Library(pathName);
				library.start();
				libList.add(new Param(paramName, Param.VAR, library));
			}
		}
				
		return library;
	}
	
	public String getPathName() {
		return pathName;
	}
	
	RandomAccessFile getFile() {
		return file;
	}
	
	/**
	 * ���������ļ���ָ����С
	 * @param size ��С
	 */
	void enlargeFile(long size) {
		try {
			synchronized(file) {
				file.setLength(size);
			}
		} catch (IOException e) {
			processIOException(e);
		}
	}
	
	private void processIOException(IOException e) {
		e.printStackTrace();
	}

	int applyHeaderBlock() {
		return blockManager.applyHeaderBlock();
	}
	
	// ����ָ�������
	void recycleBlock(int block) {
		blockManager.recycleBlock(block);
	}
	
	void recycleBlocks(int[] blocks) {
		blockManager.recycleBlocks(blocks);
	}

	// �������ݿ�
	void recycleData(int block) {
		try {
			int []blocks = readOtherBlocks(block);
			if (blocks != null) {
				blockManager.recycleBlocks(blocks);
			}
		} catch (IOException e) {
			processIOException(e);
		}
		
		blockManager.recycleBlock(block);
	}

	/**
	 * �������ݿ�
	 * @return true���ɹ���false��ʧ��
	 */
	public synchronized boolean start() {
		if (file != null) {
			return false;
		}
		
		RandomAccessFile tempFile = null;
		try {
			startTime = System.currentTimeMillis();
			innerTxSeq = 0;
			loadTxSeq = 0;
			File tmp = new File(pathName);
			
			if (!tmp.exists() || tmp.length() == 0) {
				file = new RandomAccessFile(pathName, "rw");
				file.setLength(ENLARGE_BLOCKCOUNT * BLOCKSIZE);
				createTime = startTime;
			} else {
				file = new RandomAccessFile(pathName, "rw");
				readDBHeader(file);
				outerTxSeq++;
				
				try {
					tempFile = new RandomAccessFile(pathName + ".tmp", "r");
					tempFile.seek(0);
					if (tempFile.length() < 16 || tempFile.readInt() == 0 || tempFile.readLong() != stopTime) {
						try {
							tempFile.close();
						} catch (IOException e) {
						} finally {
							tempFile = null;
						}
					}
				} catch (Exception e) {
					try {
						if (tempFile != null) {
							tempFile.close();
						}
					} catch (IOException ie) {
					} finally {
						tempFile = null;
					}
				}
			}
			
			channel = file.getChannel();
			writeDBHeader(file);
			
			rootSection = ISection.read(this, rootHeaderBlock, null);
			blockManager = new BlockManager(this);
			blockManager.start(tempFile);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		} finally {
			try {
				if (tempFile != null) {
					tempFile.close();
				}
			} catch (IOException e) {
			}
		}
		
		isStarted = true;
		
		optThread = new OptimizeThread();
		optThread.start();
		return true;
	}

	private boolean stop(boolean sign) {
		if (!isStarted) return false;
		
		isStarted = false;
		rootSection = null;
		
		// �ѵ�ǰ��ӿ��б���ɾ��
		synchronized(libList) {
			for (int i = 0; i < libList.count(); ++i) {
				Param param = libList.get(i);
				if (param.getValue() == this) {
					libList.remove(i);
					break;
				}
			}
		}
		
		// �ȴ������ύ���������
		synchronized(file) {
			try {
				writeDBHeader(file);
				file.close();
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		
		// ������ʱ��Ϣ
		if (sign) {
			writeTempFile();
		}
		
		blockManager.stop();
		blockManager = null;
		file = null;
		channel = null;
		return true;
	}
	
	/**
	 * �ر����ݿ�
	 * @return true���ɹ���false��ʧ��
	 */
	public synchronized boolean stop() {
		return stop(true);
	}

	// �����ݿ�״̬д����ʱ�ļ���Ϊ���Ż��´ε������ٶȣ����û��д��ʱ�ļ���������ʱ����Ҫɨ�������
	private void writeTempFile() {
		RandomAccessFile tempFile = null;
		try {
			tempFile = new RandomAccessFile(pathName + ".tmp", "rw");
			tempFile.seek(0);
			tempFile.writeInt(0); // ��־���ɹ����ٸĳ�1
			tempFile.writeLong(stopTime);
			blockManager.writeTempFile(tempFile);
			
			tempFile.seek(0);
			tempFile.writeInt(1); // ��־���ɹ����ٸĳ�1
		} catch (IOException e) {
		} finally {
			try {
				if (tempFile != null) {
					tempFile.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * ȡ���ݿ��Ƿ�������
	 * @return true����������false��δ����
	 */
	public synchronized boolean isStarted() {
		return isStarted;
	}
	
	// ȡ��ȡ����ţ�ֻ����λ�������С�ڵ��ڴ˺ŵĲſ���
	synchronized long getLoadTxSeq() {
		return loadTxSeq;
	}

	// �ύ��ɺ������ύ�ź������
	synchronized void addTxSeq() {
		loadTxSeq = ++innerTxSeq;
	}
	
	long getNextInnerTxSeq() {
		return innerTxSeq + 1;
	}
	
	// ȡ����
	int getOuterTxSeq() {
		return outerTxSeq;
	}
	
	// ȡ���ڵ�
	ISection getRootSection() {
		return rootSection;
	}

	// д�ļ�ͷ
	private void writeDBHeader(RandomAccessFile file) throws IOException {
		file.seek(0);
		byte []signs = new byte[]{'r', 'q', 'v', 'd', 'b'};
		file.write(signs);
		file.writeLong(createTime);
		file.writeLong(startTime);
		file.writeInt(rootHeaderBlock);
		file.writeInt(outerTxSeq);
		
		stopTime = System.currentTimeMillis();
		file.writeLong(stopTime);
		
		// ���浽Ӳ��
		channel.force(false);
	}
	
	// ���ļ�ͷ
	private void readDBHeader(RandomAccessFile file)  throws IOException {
		file.seek(0);
		if (file.read() != 'r' || file.read() != 'q' || file.read() != 'v' || 
				file.read() != 'd' || file.read() != 'b') {
			file.close();
			file = null;
			throw new RQException("�Ƿ������ݿ��ļ�");
		}
		
		createTime = file.readLong();
		startTime = file.readLong();
		rootHeaderBlock = file.readInt();
		outerTxSeq = file.readInt();
		stopTime = file.readLong();
	}
	
	/**
	 * ����һ������
	 * @return VDB
	 */
	public VDB createVDB() {
		if (!isStarted()) {
			throw new RQException("���ݿ���δ����");
		}
		
		VDB vdb = new VDB(this);
		synchronized(vdbList) {
			vdbList.addLast(vdb);
			lastConnectTime = System.currentTimeMillis();
			optThread.setUserOn();
		}
		
		return vdb;
	}
	
	/**
	 * ���ӹرգ�ɾ������
	 * @param vdb
	 */
	void deleteVDB(VDB vdb) {
		synchronized(vdbList) {
			//rollback(vdb);
			vdbList.remove(vdb);
		}
	}

	// ���޸��ύ�����ݿ�
	int commit(VDB vdb) {
		ArrayList<ISection> modifySections = vdb.getModifySections();
		if (modifySections.size() == 0) {
			return VDB.S_SUCCESS;
		}
		
		// ɾ���������txSeq��Ķ������λ
		long txSeq = getEarliestTxSeq();
		int outerSeq = getOuterTxSeq();
		
		for (ISection section : modifySections) {
			section.deleteOutdatedZone(this, outerSeq, txSeq);
		}
		
		synchronized(file) {
			try {
				long innerSeq = getNextInnerTxSeq();
				for (ISection section : modifySections) {
					section.commit(this, outerSeq, innerSeq);
				}
				
				// �ύ��ɺ���¶������
				addTxSeq();

				// ���浽Ӳ��
				channel.force(false);
			} catch (Exception e) {
				e.printStackTrace();
				rollback(vdb);
			}
		}

		return VDB.S_SUCCESS;
	}

	/**
	 * �ع������������޸�
	 * @param vdb
	 */
	void rollback(VDB vdb) {
		ArrayList<ISection> modifySections = vdb.getModifySections();
		for (ISection section : modifySections) {
			section.rollBack(this);
		}
	}

	BlockManager getBlockManager() {
		return blockManager;
	}
	
	private int getBlockCount(int dataLen) {
		// �����ָ�����ȵ�������Ҫ��������飬�׿�ͷ��Ҫд�����׿�����������������������...
		// count + block1,...
		int count = dataLen / (BLOCKSIZE - 4);
		int mod = dataLen % (BLOCKSIZE - 4);
		return mod != 0 ? count + 1 : count;
	}
	
	// block��0��ʼ����
	private long getBlockPos(int block) {
		return (long)BLOCKSIZE * block;
	}
	
	private static byte[] toByteArray(Object data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(BLOCKSIZE);
		ObjectWriter writer = new ObjectWriter(bos);
		writer.writeByte(0); // �汾
		
		if (data instanceof Record) {
			Sequence seq = new Sequence(1);
			seq.add(data);
			writer.writeObject(seq);
		} else {
			writer.writeObject(data);
		}
		
		writer.close();
		return bos.toByteArray();
	}
	
	// ������д��ָ�������
	private void writeBlocks(int []blocks, byte []data) throws IOException {
		// ���׿���������������n+������1,...,������n
		int count = blocks.length;
		int headerSize = count * 4;
		RandomAccessFile file = this.file;
				
		synchronized(file) {
			file.seek(getBlockPos(blocks[0]));
			file.writeInt(count - 1);
			
			// ͷ��С���ܳ���һ��
			int b = 0;
			int pos = headerSize;
			
			if (headerSize <= BLOCKSIZE) {
				for (int i = 1; i < count; ++i) {
					file.writeInt(blocks[i]);
				}
			} else {
				pos = 4;
				for (int i = 1; i < count; ++i) {
					if (pos == BLOCKSIZE) {
						b++;
						pos = 0;
						file.seek(getBlockPos(blocks[b]));
					}
					
					file.writeInt(blocks[i]);
					pos += 4;
				}
			}
			
			if (count == 1) {
				file.write(data);
			} else {
				int len = data.length;
				int index = BLOCKSIZE - pos;
				file.write(data, 0, index);
				
				for (++b; b < count; ++b) {
					file.seek(getBlockPos(blocks[b]));
					if (len - index >= BLOCKSIZE) {
						file.write(data, index, BLOCKSIZE);
						index += BLOCKSIZE;
					} else {
						// ���һ��
						file.write(data, index, len - index);
					}
				}
			}
		}
	}
	
	// ������ռ�õ���������ݣ�����ռ�ö������
	// ���׿���������������n+������1,...,������n
	byte[] readBlocks(int block) throws IOException {
		RandomAccessFile file = this.file;
		synchronized(file) {
			file.seek(getBlockPos(block));
			int count = file.readInt();
			
			if (count == 0) {
				byte []bytes = new byte[BLOCKSIZE];
				file.read(bytes, 4, BLOCKSIZE - 4);
				//file.seek(getBlockPos(block));
				//file.read(bytes, 0, BLOCKSIZE);
				return bytes;
			} else {
				count++;
				int []blocks = new int[count];
				blocks[0] = block;
				
				int headerSize = count * 4;
				if (headerSize <= BLOCKSIZE) {
					for (int i = 1; i < count; ++i) {
						blocks[i] = file.readInt();
					}
				} else {
					int b = 0;
					int pos = 4;
					for (int i = 1; i < count; ++i) {
						if (pos == BLOCKSIZE) {
							b++;
							pos = 0;
							file.seek(getBlockPos(blocks[b]));
						}
						
						blocks[i] = file.readInt();
						pos += 4;
					}
				}
				
				byte []bytes = new byte[BLOCKSIZE * count];
				for (int i = 0; i < count; ++i) {
					file.seek(getBlockPos(blocks[i]));
					file.read(bytes, BLOCKSIZE * i, BLOCKSIZE);
				}
				
				return bytes;
			}
		}
	}
		
	// ���߼���ռ�õ����������
	int[] readOtherBlocks(int block) throws IOException {
		RandomAccessFile file = this.file;
		synchronized(file) {
			file.seek(getBlockPos(block));
			int count = file.readInt();
			
			if (count == 0) {
				return null;
			} else {
				int []blocks = new int[count];
				int b = 0;
				int pos = 4;

				for (int i = 0; i < count; ++i) {
					if (pos == BLOCKSIZE) {
						file.seek(getBlockPos(blocks[b]));
						b++;
						pos = 0;
					}

					blocks[i] = file.readInt();
					pos += 4;
				}
				
				return blocks;
			}
		}
	}
	
	// �����׿��
	int writeDataBlock(int pos, Object data) {
		try {
			byte []bytes = toByteArray(data);
			return writeDataBlock(pos, bytes);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	// ������д��ָ�������
	int writeDataBlock(int pos, byte []bytes) throws IOException {
		int blockCount = getBlockCount(bytes.length);
		int []blocks = blockManager.applyDataBlocks(pos, blockCount);
		writeBlocks(blocks, bytes);
		return blocks[0];
	}
	
	// �����ݿ�
	Object readDataBlock(int block) throws IOException {
		byte[] bytes = readBlocks(block);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectReader reader = new ObjectReader(bis);
		
		int blockCount = reader.readInt32();
		for (int i = 0; i < blockCount; ++i) {
			reader.readInt32();
		}
		
		reader.readByte(); // �汾
		Object data = reader.readObject();
		reader.close();
		return data;
	}
	
	static int getDataPos(byte[] bytes) {
		int blockCount = (bytes[0] << 24) + ((bytes[1] & 0xff) << 16) +
				((bytes[2] & 0xff) << 8) + (bytes[3] & 0xff);
		return blockCount * 4 + 4;
	}
	
	int[] writeHeaderBlock(int block, int []otherBlocks, byte []bytes) throws IOException {
		try {
			int oldCount = 1;
			int blockCount = getBlockCount(bytes.length);
			int []blocks;
			
			if (otherBlocks == null) {
				blocks = blockManager.applyHeaderBlocks(block, blockCount);
			} else {
				oldCount += otherBlocks.length;
				if (oldCount == blockCount) {
					blocks = new int[blockCount];
					blocks[0] = block;
					System.arraycopy(otherBlocks, 0, blocks, 1, blockCount - 1);
				} else if (oldCount < blockCount) {
					blockManager.recycleBlocks(otherBlocks);
					blocks = blockManager.applyHeaderBlocks(block, blockCount);
				} else {
					blocks = new int[blockCount];
					blocks[0] = block;
					System.arraycopy(otherBlocks, 0, blocks, 1, blockCount - 1);
					blockManager.recycleBlocks(otherBlocks, blockCount - 1);
				}
			}
			
			writeBlocks(blocks, bytes);
			if (blockCount == 1) {
				return null;
			} else if (oldCount == blockCount) {
				return otherBlocks;
			} else {
				otherBlocks = new int[blockCount - 1];
				System.arraycopy(blocks, 1, otherBlocks, 0, blockCount - 1);
				return otherBlocks;
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	// ȡ����vdb�����������ţ������������ٱ����ʵ���λ
	long getEarliestTxSeq() {
		long seq = getLoadTxSeq();
		VDB []vdbs;
		
		synchronized(vdbList) {
			int size = vdbList.size();
			vdbs = new VDB[size];
			vdbList.toArray(vdbs);
		}
		
		for (VDB vdb : vdbs) {
			long q = vdb.getLoadTxSeq();
			if (q < seq) seq = q;
		}
		
		return seq;
	}
	
	/**
	 * �������⣬�������ù�ϣ�����ҽ�
	 * @param keys ������
	 * @param lens ��ϣ��������
	 */
	public int createKeyLibrary(Object []keys, int []lens) {
		int count = keys.length;
		ISection rootSection = this.rootSection;
		VDB vdb = new VDB(this);
		
		for (int i = 0; i < count; ++i) {
			Object key = keys[i];
			for (int j = i + 1; j < count; ++j) {
				if (Variant.isEquals(key, keys[j])) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(key + mm.getMessage("engine.dupKeys"));
				}
			}
			
			if (rootSection.getSub(vdb, key) != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(key + mm.getMessage("engine.dupKeys"));
			}
			
			int result = rootSection.createSubKeyDir(vdb, key, lens[i]);
			if (result != VDB.S_SUCCESS) {
				rollback(vdb);
				return result;
			}
		}
		
		commit(vdb);
		return VDB.S_SUCCESS;
	}
	
	/**
	 * �������ݿ����ݵ�Ŀ���ļ�
	 * @param destFileName Ŀ���ļ�·����
	 * @return true���ɹ���false��ʧ��
	 */
	public boolean reset(String destFileName) {
		// ����һ��VDB����ֹOptimizeThread����
		VDB vdb = createVDB();
		Library dest = new Library(destFileName);
		
		try {
			dest.start();
			dest.createVDB();
			rootSection.reset(this, dest, dest.rootHeaderBlock);
			dest.stop(false);
			return true;
		} catch (Exception e) {
			if (dest.file != null) {
				try {
					dest.file.close();
				} catch (IOException ie) {
				}
			}
		} finally {
			deleteVDB(vdb);
		}
		
		return false;
	}
}