package com.scudata.dw;


//ֻ�Ƿ����ͽ����ļ�¼��
public class TableSeqGather extends TableGather {
	public TableSeqGather() {}
	
	public void setSegment(int startBlock, int endBlock) {}	
	
	void loadData() {}
	
	void skip() {}
	
	Object getNextBySeq(long seq) { return seq;}	
}