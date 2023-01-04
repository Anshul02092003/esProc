package com.scudata.dm.comparator;

import java.util.Comparator;

import com.scudata.dm.BaseRecord;
import com.scudata.util.Variant;

/**
 * ���ռ�¼�������бȽϵıȽ���
 * @author WangXiaoJun
 *
 */
public class RecordKeyComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		if (o1 == null) {
			return (o2 == null) ? 0 : -1;
		}

		return Variant.compare(((BaseRecord)o1).value(), ((BaseRecord)o2).value());
	}
}
