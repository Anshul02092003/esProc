package com.scudata.dm.op;

import com.scudata.common.MessageManager;
import com.scudata.common.ObjectCache;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Env;
import com.scudata.dm.ListBase1;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.Node;
import com.scudata.resources.EngineMessage;
import com.scudata.util.HashUtil;
import com.scudata.util.Variant;

/**
 * ���ڶ�������������ִ�а����ֶν����ڴ�����������
 * @author RunQian
 *
 */
public class GroupsResult extends IGroupsResult {
	private Expression[] exps; // ������ʽ
	private String []names; // �����ֶ���
	private Expression []calcExps; // ͳ�Ʊ��ʽ
	private String []calcNames; // ͳ���ֶ���
	private String opt; // ����ѡ��
	private Context ctx; // ����������
	
	private HashUtil hashUtil; // �ṩ��ϣ����Ĺ�ϣ��
	private ListBase1 []groups; // ��ϣ��
	private Node[] gathers = null; // ͳ�Ʊ��ʽ�е�ͳ�ƺ���
	
	private DataStruct ds; // ��������ݽṹ
	private int keyCount; // �����ֶ���
	private int valCount; // �����ֶ���
	
	private Table result; // ������ܽ��
	private Record prevRecord; // ��һ���������¼
	
	private SortedGroupsLink link; // ����hѡ��
	
	private boolean oOpt;
	private boolean iOpt;
	private boolean nOpt;
	private boolean hOpt;
	public boolean XOpt;//ʹ�ö�̬�����Ż�����������ʹ��

	/**
	 * ��ʼ������
	 * @param exps		������ʽ
	 * @param names		������ʽ��
	 * @param calcExps	ͳ�Ʊ��ʽ
	 * @param calcNames	ͳ�Ʊ��ʽ��
	 * @param opt		����ѡ��
	 * @param ctx		�����ı���
	 */
	public GroupsResult(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx) {
		this(exps, names, calcExps, calcNames, opt, ctx, Env.getDefaultHashCapacity());
	}
	
	/**
	 * ��ʼ������
	 * @param exps		������ʽ
	 * @param names		������ʽ��
	 * @param calcExps	ͳ�Ʊ��ʽ
	 * @param calcNames	ͳ�Ʊ��ʽ��
	 * @param opt		����ѡ��
	 * @param ctx		�����ı���
	 * @param capacity	�����ϣ���С
	 */
	public GroupsResult(Expression[] exps, String[] names, Expression[] calcExps, 
			String[] calcNames, String opt, Context ctx, int capacity) {
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
		
		// ���ݱ��ʽ���֣����ɸ����е����֡��������������ɽ����ṹ
		// ���ɷ�����ʽ��Ӧ���е�����
		if (exps != null) {
			keyCount = exps.length;
		}

		// ����ͳ�Ʊ��ʽ��Ӧ���е�����, ȡ�þۺϺ���
		if (calcExps != null) {			
			gathers = Sequence.prepareGatherMethods(this.calcExps, ctx);
			valCount = gathers.length;
		}

		// �ϲ�����������ͳ�����������ɽ����������
		String[] colNames = new String[keyCount + valCount];
		if (names != null) {
			System.arraycopy(names, 0, colNames, 0, keyCount);
		}
		
		if (calcNames != null) {
			System.arraycopy(calcNames, 0, colNames, keyCount, valCount);
		}

		// ���ɽ�������ݽṹ
		ds = new DataStruct(colNames);
		ds.setPrimary(names);

		// ���ú���ѡ������ݺ���ѡ��ж��Ƿ���Ҫ��ϣ��
		if (opt != null) {
			if (opt.indexOf('o') != -1) {
				oOpt = true;
			} else if (opt.indexOf('i') != -1) {
				iOpt = true;
				if (keyCount != 1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groups" + mm.getMessage("function.invalidParam"));
				}
			} else if (opt.indexOf('n') != -1) {
				nOpt = true;
				if (keyCount != 1) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groups" + mm.getMessage("function.invalidParam"));
				}
			} else if (opt.indexOf('h') != -1) {
				if (keyCount > 0) {
					hOpt = true;
				}
			} else if (opt.indexOf('X') != -1) {
				XOpt = true;
			}
		}
		
		if (hOpt) {
			link = new SortedGroupsLink();
		} else if (keyCount != 0 && !oOpt && !iOpt && !nOpt) {
			hashUtil = new HashUtil(capacity);
			groups = new ListBase1[hashUtil.getCapacity()];
		}
		
		result = new Table(ds, 1024);
	}
	
	/**
	 * ȡ��������ݽṹ
	 * @return DataStruct
	 */
	public DataStruct getResultDataStruct() {
		return ds;
	}
	
	/**
	 * ȡ������ʽ
	 * @return ���ʽ����
	 */
	public Expression[] getExps() {
		return exps;
	}

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * ȡ���ܱ��ʽ
	 * @return ���ʽ����
	 */
	public Expression[] getCalcExps() {
		return calcExps;
	}

	/**
	 * ȡ�����ֶ���
	 * @return �ֶ�������
	 */
	public String[] getCalcNames() {
		return calcNames;
	}

	public ListBase1[] getGroups() {
		return groups;
	}

	public Node[] getGathers() {
		return gathers;
	}

	public DataStruct getDs() {
		return ds;
	}

	/**
	 * ȡ������ܽ��
	 * @return Table
	 */
	public Table getResult() {
		return result;
	}
	
	/**
	 * ȡѡ��
	 * @return
	 */
	public String getOption() {
		return opt;
	}
	
	/**
	 * ȡ�Ƿ����������
	 * @return true���ǣ����ݰ������ֶ�����false������
	 */
	public boolean isSortedGroup() {
		return oOpt;
	}
	
	/**
	 * ��������ʱ��ȡ��ÿ���̵߳��м������������Ҫ���ж��λ���
	 * @return Table
	 */
	public Table getTempResult() {
		if (hashUtil != null) {
			this.hashUtil = null;
			this.groups = null;
		}  else if (nOpt) {
			result.deleteNullFieldRecord(0);
		} else if (hOpt) {
			link = null;
		}
		
		Table table = result;
		prevRecord = null;
		result = null;

		if (table.length() > 0) {
			if (valCount > 0) {
				table.finishGather1(gathers);
			}
			
			return table;
		} else {
			return null;
		}
	}

	/**
	 * ȡ������ܽ��
	 * @return Table
	 */
	public Table getResultTable() {
		if (hashUtil != null) {	
			if (opt == null || opt.indexOf('u') == -1) {
				int []fields = new int[keyCount];
				for (int i = 0; i < keyCount; ++i) {
					fields[i] = i;
				}
	
				result.sortFields(fields);
			}
	
			this.hashUtil = null;
			this.groups = null;
		} else if (nOpt) {
			if (opt.indexOf('0') != -1) {
				result.deleteNullFieldRecord(0);
			} else {
				int len = result.length();
				ListBase1 mems = result.getMems();
				for (int i = 1; i <= len; ++i) {
					Record r = (Record)mems.get(i);
					if (r.getNormalFieldValue(0) == null) {
						r.setNormalFieldValue(0, ObjectCache.getInteger(i));;
					}
				}				
			}
		} else if (hOpt) {
			link = null;
		}

		Table table = result;
		prevRecord = null;
		result = null;
		
		if (table.length() > 0) {
			if (valCount > 0) {
				table.finishGather(gathers);
			}
			
			if (!nOpt && opt != null && opt.indexOf('0') != -1) {
				table.deleteNullFieldRecord(0);
			}
			
			table.trimToSize();
		} else if (opt == null || opt.indexOf('t') == -1) {
			table = null;
		}
		
		return table;
	}
	
	 /**
	  * �������ͽ�����ȡ���յļ�����
	  * @return
	  */
	public Object result() {
		return getResultTable();
	}
	
	/**
	 * �������͹��������ݣ��ۻ������յĽ����
	 * @param seq ����
	 * @param ctx ����������
	 */
	public void push(Sequence table, Context ctx) {
		if (table == null || table.length() == 0) return;
		
		if (hashUtil != null) {
			addGroups(table, ctx);
		} else if (oOpt) {
			addGroups_o(table, ctx);
		} else if (iOpt) {
			addGroups_i(table, ctx);
		} else if (nOpt) {
			addGroups_n(table, ctx);
		} else if (hOpt) {
			addGroups_h(table, ctx);
		} else {
			addGroups_1(table, ctx);
		}
	}

	/**
	 * �������͹������α����ݣ��ۻ������յĽ����
	 * @param cursor �α�����
	 */
	public void push(ICursor cursor) {
		Context ctx = this.ctx;
		if (hashUtil != null) {
			while (true) {
				// ���α���ȡ��һ�����ݡ�
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				// ��������ӵ��������࣬��ִ��ͳ�ƺ�����
				addGroups(src, ctx);
			}
		} else if (oOpt) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_o(src, ctx);
			}
		} else if (iOpt) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_i(src, ctx);
			}
		} else if (nOpt) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_n(src, ctx);
			}
		} else if (hOpt) {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_h(src, ctx);
			}
		} else {
			while (true) {
				Sequence src = cursor.fuzzyFetch(ICursor.FETCHCOUNT);
				if (src == null || src.length() == 0) break;
				
				addGroups_1(src, ctx);
			}
		}
	}
	
	/**
	 * ������ݣ�ͬʱ�������ͳ�ƺ���
	 * 
	 * @param table	����ӵ�����
	 */
	private void addGroups(Sequence table, Context ctx) {
		final int INIT_GROUPSIZE = HashUtil.getInitGroupSize(); // ��ϣ��Ĵ�С
		HashUtil hashUtil = this.hashUtil; // ��ϣ������
		ListBase1 []groups = this.groups; // ��ϣ��
		Expression[] exps = this.exps;
		int keyCount = this.keyCount;
		int valCount = null == gathers ? 0 : gathers.length;

		Node []gathers = this.gathers; // ͳ�ƺ�������
		Table result = this.result;
		Object []keys = new Object[keyCount];
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				// ���ݼ�¼���ɼ�ֵ
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}

				Record r;
				int hash = hashUtil.hashCode(keys);
				if (groups[hash] == null) {
					groups[hash] = new ListBase1(INIT_GROUPSIZE);
					r = result.newLast(keys);
					groups[hash].add(r);
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					int index = HashUtil.bsearch_r(groups[hash], keys);
					if (index < 1) {
						r = result.newLast(keys);
						groups[hash].add(-index, r);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(ctx);
							r.setNormalFieldValue(f, val);
						}
					} else {
						r = (Record)groups[hash].get(index);
						for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
							Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
							r.setNormalFieldValue(f, val);
						}
					}
				}
			}
		} finally {
			stack.pop();
		}
	}

	private void addGroups_1(Sequence table, Context ctx) {
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);
		int i = 1;
		
		Record r = prevRecord;
		int valCount = this.valCount;
		Node []gathers = this.gathers;

		try {
			if (r == null) {
				r = prevRecord = result.newLast();				
				current.setCurrent(1);
				i++;
				
				for (int v = 0; v < valCount; ++v) {
					Object val = gathers[v].gather(ctx);
					r.setNormalFieldValue(v, val);
				}
			}
			
			for (int len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int v = 0; v < valCount; ++v) {
					Object val = gathers[v].gather(r.getNormalFieldValue(v), ctx);
					r.setNormalFieldValue(v, val);
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	private void addGroups_o(Sequence table, Context ctx) {
		Table result = this.result;
		Record r = prevRecord;
		Expression[] exps = this.exps;
		int keyCount = this.keyCount;
		int valCount = this.valCount;
		Node []gathers = this.gathers;
		
		Object[] keys = new Object[keyCount];
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}

				boolean isSame = true;
				if (r == null) {
					r = result.newLast(keys);
					isSame = false;
				} else {
					for (int k = 0; k < keyCount; ++k) {
						if (Variant.compare(r.getNormalFieldValue(k), keys[k], true) != 0) {
							r = result.newLast(keys);
							isSame = false;
							break;
						}
					}
				}

				if (isSame) {
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				}
			}
		} finally {
			stack.pop();
		}
		
		prevRecord = r;
	}
	
	private void addGroups_i(Sequence table, Context ctx) {
		Table result = this.result;
		Record r = prevRecord;
		Expression exp = exps[0];
		int valCount = this.valCount;
		Node []gathers = this.gathers;

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				Object val = exp.calculate(ctx);

				if (Variant.isTrue(val) || r == null) {
					r = result.newLast();
					r.setNormalFieldValue(0, val);
					
					for (int v = 0, f = 1; v < valCount; ++v, ++f) {
						val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					for (int v = 0, f = 1; v < valCount; ++v, ++f) {
						val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
						r.setNormalFieldValue(f, val);
					}
				}
			}
		} finally {
			stack.pop();
		}
		
		prevRecord = r;
	}

	/**
	 * ���÷�������@nѡ��ʹ��
	 * @param groupCount
	 */
	public void setGroupCount(int groupCount) {
		result.insert(groupCount);
	}

	private void addGroups_n(Sequence table, Context ctx) {
		Table result = this.result;
		Expression exp = exps[0];
		int valCount = this.valCount;
		Node []gathers = this.gathers;

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				Object obj = exp.calculate(ctx);
				if (!(obj instanceof Number)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("groups: " + mm.getMessage("engine.needIntExp"));
				}

				int index = ((Number)obj).intValue();
				if (index < 1) {
					// ����С��1�ķŹ���Ҫ�ˣ����ٱ������ֵ��κ�һ����
					continue;
				}
				
				Record r = result.getRecord(index);
				if (r.getNormalFieldValue(0) == null) {
					r.setNormalFieldValue(0, obj);
					for (int v = 0, f = 1; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					for (int v = 0, f = 1; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
						r.setNormalFieldValue(f, val);
					}
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	private void addGroups_h(Sequence table, Context ctx) {
		Expression[] exps = this.exps;
		int keyCount = this.keyCount;
		int valCount = this.valCount;
		Node []gathers = this.gathers;
		Table result = this.result;
		SortedGroupsLink link = this.link;
		
		Object[] keys = new Object[keyCount];
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = table.new Current();
		stack.push(current);

		try {
			for (int i = 1, len = table.length(); i <= len; ++i) {
				current.setCurrent(i);
				for (int k = 0; k < keyCount; ++k) {
					keys[k] = exps[k].calculate(ctx);
				}

				SortedGroupsLink.Node node = link.put(keys);
				Record r = node.getRecord();
				if (r == null) {
					r = result.newLast(keys);
					node.setReocrd(r);
					
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(ctx);
						r.setNormalFieldValue(f, val);
					}
				} else {
					for (int v = 0, f = keyCount; v < valCount; ++v, ++f) {
						Object val = gathers[v].gather(r.getNormalFieldValue(f), ctx);
						r.setNormalFieldValue(f, val);
					}
				}
			}
		} finally {
			stack.pop();
		}
	}
	
	/**
	 * ��·����ʱ�԰�����·���������ϲ����ж��η�����ܣ��õ����յĻ��ܽ��
	 * @param results ����·�ķ��������ɵ�����
	 * @return ���յĻ��ܽ��
	 */
	public Object combineResult(Object []results) {
		int count = results.length;
		Sequence result = new Sequence();
		for (int i = 0; i < count; ++i) {
			if (results[i] instanceof Sequence) {
				result.addAll((Sequence)results[i]);
			}
		}
		
		// ��·�α갴�����ֶβ�ֵ�
		if (opt != null && opt.indexOf('o') != -1) {
			return result.derive("o");
		}
		
		int dcount = exps == null ? 0 : exps.length;
		int mcount = calcExps == null ? 0 : calcExps.length;
		Expression []exps2 = null;
		if (dcount > 0) {
			exps2 = new Expression[dcount];
			for (int i = 0, q = 1; i < dcount; ++i, ++q) {
				exps2[i] = new Expression(ctx, "#" + q);
			}
		}

		Expression []calcExps2 = null;
		if (mcount > 0) {
			calcExps2 = new Expression[mcount];
			for (int i = 0, q = dcount + 1; i < mcount; ++i, ++q) {
				Gather gather = (Gather)calcExps[i].getHome();
				gather.prepare(ctx);
				calcExps2[i] = gather.getRegatherExpression(q);
			}
		}

		return result.groups(exps2, names, calcExps2, calcNames, opt, ctx);
	}
}
