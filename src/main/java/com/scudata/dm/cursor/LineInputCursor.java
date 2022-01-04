package com.scudata.dm.cursor;

import java.io.IOException;

import com.scudata.common.RQException;
import com.scudata.dm.DataStruct;
import com.scudata.dm.ILineInput;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.util.Variant;

/**
 * �ð��ж����ݽӿڹ����α�
 * ������Ϊ�˸��û��ṩ���Զ���������������α�
 * ILineInput.cursor()
 * @author RunQian
 *
 */
public class LineInputCursor extends ICursor {
	private ILineInput importer; // ���ж����ݽṹ
	private DataStruct ds; // ��������ݽṹ
	private boolean isTitle = false; // ��һ���Ƿ��Ǳ���
	private boolean isSingleField = false; // �Ƿ񷵻ص�����ɵ�����
	
	/**
	 * �������ж������α�
	 * @param lineInput ���ж����ݽӿ�
	 * @param opt ѡ��
	 */
	public LineInputCursor(ILineInput lineInput, String opt) {
		this.importer = lineInput;
		if (opt != null) {
			if (opt.indexOf('t') != -1) isTitle = true;
			if (opt.indexOf('i') != -1) isSingleField = true;
		}
	}

	private Sequence fetchAll(ILineInput importer, int n) throws IOException {
		Object []line = importer.readLine();
		if (line == null) {
			return null;
		}

		int fcount;
		if (ds == null) {
			fcount = line.length;
			String []fieldNames = new String[fcount];
			if (isTitle) {
				for (int f = 0; f < fcount; ++f) {
					fieldNames[f] = Variant.toString(line[f]);
				}

				line = importer.readLine();
				if (line == null) {
					return null;
				}
			}

			ds = new DataStruct(fieldNames);
			setDataStruct(ds);
		} else {
			fcount = ds.getFieldCount();
		}

		if (isSingleField && fcount != 1) isSingleField = false;
		
		int curLen = line.length;
		if (curLen > fcount) curLen = fcount;

		int initSize = n > INITSIZE ? INITSIZE : n;
		if (isSingleField) {
			Sequence seq = new Sequence(initSize);
			seq.add(line[0]);

			for (int i = 1; i < n; ++i) {
				line = importer.readLine();
				if (line == null) {
					break;
				}

				seq.add(line[0]);
			}

			//seq.trimToSize();
			return seq;
		} else {
			Table table = new Table(ds, initSize);
			Record r = table.newLast();
			for (int f = 0; f < curLen; ++f) {
				r.setNormalFieldValue(f, line[f]);
			}

			for (int i = 1; i < n; ++i) {
				line = importer.readLine();
				if (line == null) {
					break;
				}

				r = table.newLast();
				curLen = line.length;
				if (curLen > fcount) curLen = fcount;
				for (int f = 0; f < curLen; ++f) {
					r.setNormalFieldValue(f, line[f]);
				}
			}

			//table.trimToSize();
			return table;
		}
	}

	/**
	 * ��ȡָ�����������ݷ���
	 * @param n ����
	 * @return Sequence
	 */
	protected Sequence get(int n) {
		if (n < 1 || importer == null) return null;

		try {
			return fetchAll(importer, n);
		} catch (IOException e) {
			close();
			throw new RQException(e.getMessage(), e);
		}
	}

	/**
	 * ����ָ������������
	 * @param n ����
	 * @return long ʵ������������
	 */
	protected long skipOver(long n) {
		ILineInput importer = this.importer;
		if (n < 1 || importer == null) return 0;

		try {
			if (ds == null && isTitle) {
				Object []line = importer.readLine();
				if (line == null) {
					return 0;
				}
				
				int fcount = line.length;
				String []fieldNames = new String[fcount];
				for (int f = 0; f < fcount; ++f) {
					fieldNames[f] = Variant.toString(line[f]);
				}

				ds = new DataStruct(fieldNames);
			}
			
			for (int i = 0; i < n; ++i) {
				if (!importer.skipLine()) {
					return i;
				}
			}
		} catch (IOException e) {
			close();
			throw new RQException(e.getMessage(), e);
		}

		return n;
	}

	/**
	 * �ر��α�
	 */
	public synchronized void close() {
		super.close();
		if (importer != null) {
			try {
				importer.close();
			} catch (IOException e) {
			}

			importer = null;
			ds = null;
		}
	}

	protected void finalize() throws Throwable {
		close();
	}
}
