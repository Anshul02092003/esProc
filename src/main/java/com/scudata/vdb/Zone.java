package com.scudata.vdb;

import java.io.IOException;

import com.scudata.dm.ObjectReader;
import com.scudata.dm.ObjectWriter;

/**
 * ��λ����
 * @author RunQian
 *
 */
class Zone {
	protected int outerSeq; // ���ţ�ÿ���������ݿ��1
	protected long innerSeq; // �ڴ�ţ����������1
	protected int block; // ��λ��Ӧ������ռ�õ�������׿�
	
	public Zone() {
	}
	
	public Zone(int block) {
		this.block = block;
	}

	public boolean isCommitted() {
		return outerSeq > 0;
	}
	
	public int getBlock() {
		return block;
	}
	
	public void setBlock(int block) {
		this.block = block;
	}
	
	public void setTxSeq(int outerSeq, long innerSeq) {
		this.outerSeq = outerSeq;
		this.innerSeq = innerSeq;
	}
	
	public void read(ObjectReader reader) throws IOException {
		outerSeq = reader.readInt();
		innerSeq = reader.readLong();
		block = reader.readInt();
	}
	
	public void write(ObjectWriter writer) throws IOException {
		writer.writeInt(outerSeq);
		writer.writeLong(innerSeq);
		writer.writeInt(block);
	}
	
	// �ж�vdb�������������Ƿ�ʹ���λƥ��
	public boolean match(VDB vdb, boolean isLockVDB) {
		if (outerSeq == 0) {
			// ��λ��δ�ύ�������Ƿ��Ǵ�vdb�����������
			return isLockVDB;
		}
		
		if (outerSeq < vdb.getOuterTxSeq()) {
			return true;
		}
		
		return innerSeq <= vdb.getLoadTxSeq();
	}
	
	public boolean canDelete(int outerSeq, long txSeq) {
		if (this.outerSeq < outerSeq) {
			return true;
		}
		
		return innerSeq < txSeq;
	}
	
	public Object getData(Library library) throws IOException {
		return library.readDataBlock(block);
	}
}
