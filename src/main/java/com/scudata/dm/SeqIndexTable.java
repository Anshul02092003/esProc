package com.scudata.dm;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.resources.EngineMessage;

/**
 * ������ż������������ֵ�Ǽ�¼��ά���е����
 * @author RunQian
 *
 */
public class SeqIndexTable extends IndexTable {
	private Sequence code; // ά��
	
	public SeqIndexTable(Sequence code) {
		this.code = code;
	}
	
	/**
	 * �����ź�����
	 * @param code ά��
	 * @param field ����ֶε�����
	 */
	public SeqIndexTable(Sequence code, int field) {
		int len = code.length();
		Sequence result = new Sequence(len);
		
		for (int i = 1; i <= len; ++i) {
			Record r = (Record)code.getMem(i);
			Object v = r.getNormalFieldValue(field);
			if (!(v instanceof Number)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needIntExp"));
			}
			
			int seq = ((Number)v).intValue();
			if (seq <= result.length() && result.getMem(seq) != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(seq + mm.getMessage("engine.dupKeys"));
			}
			
			result.set(seq, r);
		}
		
		this.code = result;
	}

	public Object find(Object key) {
		if (key instanceof Number) {
			int seq = ((Number)key).intValue();
			if (seq > 0 && seq <= code.length()) {
				return code.getMem(seq);
			}
		}
		
		return null;
	}

	public Object find(Object []keys) {
		if (keys.length == 1) {
			return find(keys[0]);
		}
		
		return null;
	}
}
