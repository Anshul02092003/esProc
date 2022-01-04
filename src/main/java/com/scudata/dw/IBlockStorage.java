package com.scudata.dw;

import java.io.IOException;

import com.scudata.dm.IResource;

public interface IBlockStorage extends IResource {
	static final int POS_SIZE = 5; // λ����5�ֽڵ�������ʾ
	
	int getBlockSize(); // ȡ�����С
	void loadBlock(long pos, byte []block) throws IOException; // װ������
	void saveBlock(long pos, byte []block) throws IOException; // ��������
	void saveBlock(long pos, byte []block, int off, int len) throws IOException;
	long applyNewBlock() throws IOException; // ����������
	StructManager getStructManager();
	boolean isCompress(); // �Ƿ�ѹ���洢
}