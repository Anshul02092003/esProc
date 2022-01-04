package com.scudata.dw.compress;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dw.BufferReader;

public class NullColumn extends Column {

	public void addData(Object data) {
		throw new RQException("never run to here!");

	}
	
	// ȡ��row�е�����
	public Object getData(int row) {
		return null;
	}
	
	public Column clone() {
		return new NullColumn();
	}

	public void appendData(BufferReader br) throws IOException {
		throw new RQException("never run to here!");
	}
}
