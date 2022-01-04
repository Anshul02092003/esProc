package com.scudata.dw;

import java.io.IOException;
import java.io.InputStream;

public class BlockLinkReader extends InputStream {
	private BlockLink blockLink;
	private IBlockStorage storage;
	
	private final int blockSize; // ���С
	private final int pointerPos; // ��һ��ָ���ڿ���λ��
	
	private long currentBlockPos = Long.MAX_VALUE;
	private byte []block; // ��ǰ����Ŀ�
	private int caret; // �����block�е�λ��
	
	private byte[] readBuffer = new byte[32];
	private LZ4Util lz4 = LZ4Util.instance();
	private byte []decompressBuffer;
	
	public BlockLinkReader(BlockLink blockLink) {
		this(blockLink, 0);
	}
	
	public BlockLinkReader(BlockLink blockLink, int sbLen) {
		this.blockLink = blockLink;
		storage = blockLink.getBlockStorage();
		
		blockSize = storage.getBlockSize();
		pointerPos = blockSize - IBlockStorage.POS_SIZE;
		block = new byte[blockSize];
	}
	
	public void close() {
		block = null;
	}
	
	void setDecompressBufferSize(int size) {
		decompressBuffer = new byte[size];
	}

	public void loadFirstBlock() throws IOException {
		loadBlock(blockLink.firstBlockPos);
	}
	
	public void loadBlock(long pos) throws IOException {
		currentBlockPos = pos;
		storage.loadBlock(pos, block);
		caret = 0;
	}
	
	public int read() throws IOException {
		if (caret == pointerPos) {
			loadBlock(readPosition(pointerPos));
		}
		
		return block[caret++] & 0xff;
	}
	
	public int read(byte b[], int off, int len) throws IOException {
		int count = len;
		while (true) {
			int freeSize = pointerPos - caret;
			if (len <= freeSize) {
				System.arraycopy(block, caret, b, off, len);
				caret += len;
				return count;
			} else {
				System.arraycopy(block, caret, b, off, freeSize);
				if (currentBlockPos < blockLink.lastBlockPos) {
					off += freeSize;
					len -= freeSize;
					loadBlock(readPosition(pointerPos));
				} else {
					caret = pointerPos;
					return count - len + freeSize;
				}
			}
		}
	}
	
	public void readFully(byte []b, int off, int len) throws IOException {
		while (true) {
			int freeSize = pointerPos - caret;
			if (len <= freeSize) {
				System.arraycopy(block, caret, b, off, len);
				caret += len;
				break;
			} else {
				System.arraycopy(block, caret, b, off, freeSize);
				off += freeSize;
				len -= freeSize;
				loadBlock(readPosition(pointerPos));
			}
		}
	}
	
	public int readInt32() throws IOException {
		if (pointerPos - caret >= 4) {
			byte []data = this.block;
			int index = this.caret;
			this.caret += 4;
			return (data[index] << 24) + ((data[index + 1] & 0xff) << 16) +
				((data[index + 2] & 0xff) << 8) + (data[index + 3] & 0xff);
		} else {
			byte []readBuffer = this.readBuffer;
			readFully(readBuffer, 0, 4);
			return (readBuffer[0] << 24) + ((readBuffer[1] & 0xff) << 16) +
				((readBuffer[2] & 0xff) << 8) + (readBuffer[3] & 0xff);
		}
	}
	
	// ����һ���ݿ鲢��ѹ
	public byte[] readDataBlock() throws IOException {
		int srcCount = readInt32();
		if (storage.isCompress()) {
			int count = readInt32();
			byte []buffer = new byte[count];
			
			readFully(buffer, 0, count);

			if (srcCount > decompressBuffer.length) {
				decompressBuffer = new byte[srcCount];
			}
			
			lz4.decompress(buffer, decompressBuffer, srcCount);
			return decompressBuffer;
		} else {
			byte []buffer = new byte[srcCount];
			readFully(buffer, 0, srcCount);
			return buffer;
		}
	}
	
	private long getBlockPos(long pos) {
		return pos - pos % blockSize;
	}
	
	public void seek(long pos) throws IOException {
		long blockPos = getBlockPos(pos);
		if (blockPos != currentBlockPos) {
			storage.loadBlock(blockPos, block);
			currentBlockPos = blockPos;
		}
		
		caret = (int)(pos - blockPos);
	}
	
	// ��ָ��λ�õ����ݿ鲢��ѹ
	public byte[] readDataBlock(long pos) throws IOException {
		seek(pos);
		return readDataBlock();
	}
	
	// ������������һ���λ��
	private long readPosition(int i) {
		byte []block = this.block;
		return (((long)(block[i] & 0xff) << 32) +
				((long)(block[i + 1] & 0xff) << 24) +
				((block[i + 2] & 0xff) << 16) +
				((block[i + 3] & 0xff) <<  8) +
				(block[i + 4] & 0xff));
	}
	
	// �����������������ֽ����鷵��
	public byte[] readBlocks() throws IOException {
		int blockCount = blockLink.blockCount;
		if (blockCount > 1) {
			byte []bytes = new byte[pointerPos * blockCount];
			long blockPos = blockLink.firstBlockPos;
			for (int i = 0, j = 0; i < blockCount; ++i, j += pointerPos) {
				storage.loadBlock(blockPos, block);
				System.arraycopy(block, 0, bytes, j, pointerPos);
				blockPos = readPosition(pointerPos);
			}
			
			return bytes;
		} else {
			storage.loadBlock(blockLink.firstBlockPos, block);
			return block;
		}
	}
	
	public BufferReader readBlockData() throws IOException {
		return new BufferReader(storage.getStructManager(), readDataBlock());
	}
	
	public BufferReader readBlockData(long pos) throws IOException {
		return new BufferReader(storage.getStructManager(), readDataBlock(pos));

	}
	
	//��ѹ����ȡ
	public RowBufferReader readBlockBuffer() throws IOException {
		int count = readInt32();
		byte []buffer = new byte[count];
		readFully(buffer, 0, count);
		return new RowBufferReader(storage.getStructManager(), buffer);
	}
	
	public BufferReader readBlockBuffer(long pos) throws IOException {
		seek(pos);
		int count = readInt32();
		byte []buffer = new byte[count];
		readFully(buffer, 0, count);
		return new BufferReader(storage.getStructManager(), buffer);
	}
	
	public long position() {
		return currentBlockPos;
	}
	
	public int getCaret() {
		return caret;
	}
}