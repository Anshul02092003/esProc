package com.scudata.vdb;

import java.io.IOException;
import java.util.ArrayList;

import com.scudata.array.IArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.expression.Expression;
import com.scudata.expression.FieldRef;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.UnknownSymbol;
import com.scudata.resources.EngineMessage;
import com.scudata.util.EnvUtil;

/**
 * ���ݿ�ڻ���
 * @author WangXiaoJun
 *
 */
abstract class ISection {
	static protected int SIGN_ARCHIVE = 0x01;
	static protected int SIGN_ARCHIVE_FILE  = 0x02; // �鵵·������ͬ����
	static protected int SIGN_KEY_SECTION  = 0x80; // �Ƿ����
	
	public ISection() {
	}
	
	/**
	 * ȡ�ڶ�Ӧ��·������
	 * @return
	 */
	abstract public IDir getDir();
	
	/**
	 * ȡ�ڵ���ʾֵ
	 * @return String
	 */
	public String toString() {
		IDir dir = getDir();
		if (dir != null) {
			Object value = dir.getValue();
			return value == null ? null : value.toString();
		} else {
			return "root";
		}
	}
	
	/**
	 * ȡ�ڵĸ���
	 * @return ISection
	 */
	public ISection getParent() {
		IDir dir = getDir();
		if (dir == null) {
			return null;
		} else {
			return dir.getParent();
		}
	}
	
	/**
	 * ȡ�ڵ�ֵ
	 * @return ��ֵ
	 */
	public Object getValue() {
		IDir dir = getDir();
		if (dir != null) {
			return dir.getValue();
		} else {
			// ����
			return null;
		}
	}
	
	/**
	 * ȡ�ڶ�Ӧ���ֶ���
	 * @return String
	 */
	public String getName() {
		IDir dir = getDir();
		if (dir != null) {
			return dir.getName();
		} else {
			// ����
			return null;
		}
	}
	
	/**
	 * ȡ�ڵ�·��
	 * @param opt a������������·����Ĭ�Ϸ��ص�ǰ�ڵģ�f�����ؽ���
	 * @return Object
	 */
	public Object path(String opt) {
		if (opt == null) {
			return getValue();
		} else if (opt.indexOf('a') == -1) {
			if (opt.indexOf('f') == -1) {
				return getValue();
			} else {
				return getName();
			}
		} else {
			Sequence seq = new Sequence();
			ISection section = this;
			ISection parent = getParent();
			if (opt.indexOf('f') == -1) {
				while (parent != null) {
					seq.add(section.getValue());
					section = parent;
					parent = parent.getParent();
				}
			} else {
				while (parent != null) {
					seq.add(section.getName());
					section = parent;
					parent = parent.getParent();
				}
			}
			
			return seq.rvs();
		}
	}
	
	/**
	 * ���ش˽��Ƿ��б�
	 * @return
	 */
	abstract public boolean isFile();
	
	/**
	 * ���ش˽��Ƿ���·�������Ƿ�����
	 * @return
	 */
	abstract public boolean isDir();
	
	/**
	 * ȡ�ӽ�
	 * @param vdb ���ݿ����
	 * @param paths ��·��ֵ����
	 * @return �ӽ�
	 */
	public ISection getSub(VDB vdb, Sequence paths) {
		ISection sub = this;
		for (int i = 1, len = paths.length(); i <= len; ++i) {
			sub = sub.getSub(vdb, paths.getMem(i));
			if (sub == null) return null;
		}
		
		return sub;
	}
	
	/**
	 * ȡ�ӽ�
	 * @param vdb ���ݿ����
	 * @param path ��·��ֵ
	 * @return �ӽ�
	 */
	abstract public ISection getSub(VDB vdb, Object path);
	
	/**
	 * ȡ�ӽ��������ƶ�����
	 * @param vdb ���ݿ����
	 * @param path ��·��ֵ
	 * @return �ӽ�
	 */
	abstract public ISection getSubForMove(VDB vdb, Object path);
	
	/**
	 * ȡ�ӽ��������ƶ�����
	 * @param vdb ���ݿ����
	 * @param paths ��·��ֵ����
	 * @return �ӽ�
	 */
	abstract public ISection getSubForMove(VDB vdb, Sequence paths);
	
	/**
	 * �г���ǰ�������е����ļ���
	 * @param vdb ���ݿ����
	 * @param opt d���г���Ŀ¼�ڣ�w���������ļ���Ŀ¼ȫ���г���l��������ǰ��
	 * @return �ӽ�����
	 */
	abstract public Sequence list(VDB vdb, String opt);

	/**
	 * ��ȡ��ǰ�ڵı�
	 * @param vdb ���ݿ����
	 * @param opt l��������ǰ��
	 * @return ������
	 * @throws IOException
	 */
	abstract public Object load(VDB vdb, String opt) throws IOException ;
	
	/**
	 * ��ȡ�ӽڵı�
	 * @param vdb ���ݿ����
	 * @param path �ӽ�·��
	 * @param opt l��������ǰ��
	 * @return ������
	 * @throws IOException
	 */
	public Object load(VDB vdb, Object path, String opt) throws IOException {
		ISection sub;
		if (path instanceof Sequence) {
			sub = getSub(vdb, (Sequence)path);
		} else {
			sub = getSub(vdb, path);
		}
		
		if (sub != null) {
			return sub.load(vdb, opt);
		} else {
			return null;
		}
	}
	
	public Table importTable(VDB vdb, String []fields, Expression filter, Context ctx) throws IOException {
		if (filter == null) {
			return importTable(vdb, fields);
		}
		
		String []filterFields = getUsedFields(filter, ctx);
		int filterCount = filterFields.length;
		if (filterCount == 0) {
			Object obj = filter.calculate(ctx);
			if (!(obj instanceof Boolean)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(mm.getMessage("engine.needBoolExp"));
			}
			
			if ((Boolean)obj) {
				return importTable(vdb, fields);
			} else {
				return null;
			}
		}
		
		ArrayList<String> list = new ArrayList<String>();
		int []filterIndex = new int[filterCount];
		for (String str : fields) {
			list.add(str);
		}
		
		for (int f = 0; f < filterCount; ++f) {
			int index = list.indexOf(filterFields[f]);
			if (index < 0) {
				filterIndex[f] = list.size();
				list.add(filterFields[f]);
			} else {
				filterIndex[f] = index;
			}
		}
		
		String []totalFields = new String[list.size()];
		list.toArray(totalFields);
		
		int fcount = totalFields.length;
		DataStruct ds = new DataStruct(totalFields);
		Object []values = new Object[fcount];
		boolean []signs = new boolean[fcount];
		
		IDir dir = getDir();
		ISection section = this;
		while (dir != null) {
			int findex = ds.getFieldIndex(dir.getName());
			if (findex != -1) {
				values[findex] = dir.getValue();
				signs[findex] = true;
			}
			
			section = dir.getParent();
			dir = section.getDir();
		}
		
		Table table = new Table(ds);
		importTable(vdb, table, values, signs, filter, filterIndex, ctx);
		
		if (table.length() == 0) {
			return null;
		} else if (fields.length == fcount) {
			return table;
		} else {
			return table.fieldsValues(fields);
		}
	}
	
	public Table importTable(VDB vdb, String []fields, Expression []filters, Context ctx) throws IOException {
		if (filters == null) {
			return importTable(vdb, fields);
		} else if (filters.length == 1) {
			return importTable(vdb, fields, filters[0], ctx);
		}
		
		int expCount = filters.length;
		String []filterFields = getUsedFields(filters[0], ctx);
		int filterCount = filterFields.length;
		ArrayList<String> list = new ArrayList<String>();
		int []filterIndex = new int[filterCount];
		for (String str : fields) {
			list.add(str);
		}
		
		for (int f = 0; f < filterCount; ++f) {
			int index = list.indexOf(filterFields[f]);
			if (index < 0) {
				filterIndex[f] = list.size();
				list.add(filterFields[f]);
			} else {
				filterIndex[f] = index;
			}
		}
		
		for (int i = 1; i < expCount; ++i) {
			filterFields = getUsedFields(filters[i], ctx);
			for (String field : filterFields) {
				if (!list.contains(field)) {
					list.add(field);
				}
			}
		}
		
		String []totalFields = new String[list.size()];
		list.toArray(totalFields);
		
		int fcount = totalFields.length;
		DataStruct ds = new DataStruct(totalFields);
		Object []values = new Object[fcount];
		boolean []signs = new boolean[fcount];
		
		IDir dir = getDir();
		ISection section = this;
		while (dir != null) {
			int findex = ds.getFieldIndex(dir.getName());
			if (findex != -1) {
				values[findex] = dir.getValue();
				signs[findex] = true;
			}
			
			section = dir.getParent();
			dir = section.getDir();
		}
		
		Table table = new Table(ds);
		importTable(vdb, table, values, signs, filters[0], filterIndex, ctx);
		
		for (int i = 1; i < expCount; ++i) {
			table.select(filters[i], "o", ctx);
		}
		
		if (table.length() == 0) {
			return null;
		} else if (fields.length == fcount) {
			return table;
		} else {
			return table.fieldsValues(fields);
		}
	}
	
	static boolean addDataToTable(Table table, Object []values, boolean []signs, Object data) {
		if (!(data instanceof Sequence)) {
			return false;
		}
		
		Sequence seq = (Sequence)data;
		DataStruct srcDs = seq.dataStruct();
		if (srcDs == null) return false;
		
		DataStruct ds = table.dataStruct();
		String []fields = ds.getFieldNames();
		int fcount = fields.length;
		int selCount = 0;

		int []selIndex = new int[fcount];
		int []srcIndex = new int[fcount];
		for (int f = 0; f < fcount; ++f) {
			if (!signs[f]) {
				srcIndex[selCount] = srcDs.getFieldIndex(fields[f]);
				if (srcIndex[selCount] != -1) {
					selIndex[selCount] = f;
					selCount++;
				}
			}
		}
		
		if (selCount == 0) return false;
		
		IArray mems = seq.getMems();
		for (int i = 1, len = seq.length(); i <= len; ++i) {
			Record r = (Record)mems.get(i);
			for (int f = 0; f < selCount; ++f) {
				Object val = r.getFieldValue(srcIndex[f]);
				values[selIndex[f]] = val;
			}
			
			table.newLast(values);
		}
		
		for (int f = 0; f < selCount; ++f) {
			values[selIndex[f]] = null;
		}
		
		return true;
	}
	
	static boolean addDataToTable(Table table, Object []values, boolean []signs, 
			Object data, Expression filter, Context ctx) {
		if (!(data instanceof Sequence)) {
			return false;
		}
		
		Sequence seq = (Sequence)data;
		DataStruct srcDs = seq.dataStruct();
		if (srcDs == null) return false;
		
		DataStruct ds = table.dataStruct();
		String []fields = ds.getFieldNames();
		int fcount = fields.length;
		int selCount = 0;

		int []selIndex = new int[fcount];
		int []srcIndex = new int[fcount];
		for (int f = 0; f < fcount; ++f) {
			if (!signs[f]) {
				srcIndex[selCount] = srcDs.getFieldIndex(fields[f]);
				if (srcIndex[selCount] != -1) {
					selIndex[selCount] = f;
					selCount++;
				}
			}
		}
		
		if (selCount == 0) return false;
		
		IArray mems = seq.getMems();
		Record newRecord = new Record(table.dataStruct());
		ComputeStack stack = ctx.getComputeStack();
		stack.push(newRecord);
		
		try {
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				Record r = (Record)mems.get(i);
				for (int f = 0; f < selCount; ++f) {
					Object val = r.getFieldValue(srcIndex[f]);
					values[selIndex[f]] = val;
				}
				
				newRecord.setStart(0, values);
				Object result = filter.calculate(ctx);
				if (!(result instanceof Boolean)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(mm.getMessage("engine.needBoolExp"));
				}
				
				if ((Boolean)result) {
					table.newLast(values);
				}				
			}
		} finally {
			stack.pop();
		}
		
		for (int f = 0; f < selCount; ++f) {
			values[selIndex[f]] = null;
		}
		
		return true;
	}
	
	abstract protected void importTable(VDB vdb, Table table, Object []values, boolean []signs) throws IOException;
	
	abstract protected void importTable(VDB vdb, Table table, Object []values, boolean []signs, 
			Expression filter, int []filterIndex, Context ctx) throws IOException;

	public Table importTable(VDB vdb, String []fields) throws IOException {
		DataStruct ds = new DataStruct(fields);
		int fcount = fields.length;
		Object []values = new Object[fcount];
		boolean []signs = new boolean[fcount];
		
		IDir dir = getDir();
		ISection section = this;
		while (dir != null) {
			int findex = ds.getFieldIndex(dir.getName());
			if (findex != -1) {
				values[findex] = dir.getValue();
				signs[findex] = true;
			}
			
			section = dir.getParent();
			dir = section.getDir();
		}
		
		Table table = new Table(ds);
		importTable(vdb, table, values, signs);
		
		return table.length() > 0 ? table : null;
	}
	
	// ȡ���ʽ�õ���vdb�Ľ���
	private static String[] getUsedFields(Expression exp, Context ctx) {
		ArrayList<String> fieldList = new ArrayList<String>();
		getUsedFields(exp.getHome(), ctx, fieldList);
		int count = fieldList.size();
		
		if (count > 0) {
			String []fields = new String[count];
			fieldList.toArray(fields);
			return fields;
		} else {
			return new String[0];
		}
	}
	
	private static void getUsedFields(Node node, Context ctx, ArrayList<String> fields) {
		if (node == null) return;
		
		if (node instanceof UnknownSymbol) {
			ComputeStack stack = ctx.getComputeStack();
			if (stack.isStackEmpty()) {
				String name = ((UnknownSymbol)node).getName();
				if (EnvUtil.getParam(name, ctx) == null) {
					fields.add(name);
				}
			} else {
				try {
					node.calculate(ctx);
				} catch (Exception e) {
					String name = ((UnknownSymbol)node).getName();
					fields.add(name);
				}
			}
		} else if (node instanceof FieldRef) {
			FieldRef field = (FieldRef)node;
			fields.add(field.getName());
		} else if (node instanceof Function) {
			IParam param = ((Function)node).getParam();
			if (param != null) {
				ArrayList<Expression> list = new ArrayList<Expression>();
				param.getAllLeafExpression(list);
				for (Expression exp : list) {
					if (exp != null) {
						getUsedFields(exp.getHome(), ctx, fields);
					}
				}
			}
		} else {
			getUsedFields(node.getLeft(), ctx, fields);
			getUsedFields(node.getRight(), ctx, fields);
		}
	}

	/**
	 * ��ȡ�������������ı�
	 * @param vdb ���ݿ����
	 * @param dirNames �������飬ʡ�������������ɴ��ֶ�
	 * @param dirValues ��ֵ���飬ʡ����Դ˽ڲ�������
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fields �����е��ֶ�������
	 * @param exp �������˱��ʽ
	 * @param isRecursion true���ݹ�ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return ���������
	 * @throws IOException
	 */
	public Sequence retrieve(VDB vdb, String []dirNames, Object []dirValues, boolean []valueSigns, 
			String []fields, Expression exp, boolean isRecursion, Context ctx) throws IOException {
		Filter filter = new Filter(dirNames, dirValues, valueSigns, fields, exp, ctx);
		Sequence out = new Sequence(1024);
		retrieve(vdb, filter, isRecursion, out);
		return out;
	}
	
	abstract protected void retrieve(VDB vdb, Filter filter, boolean isRecursion, Sequence out) throws IOException;
	
	/**
	 * ������ǰ������д������
	 * @param vdb ���ݿ����
	 * @return 0���ɹ�
	 */
	abstract public int lockForWrite(VDB vdb);
	
	/**
	 * ������ǰ��
	 * @param vdb ���ݿ����
	 */
	abstract public void unlock(VDB vdb);
	
	/**
	 * �ع���ǰ�����������޸�
	 * @param library ���ݿ����
	 */
	abstract public void rollBack(Library library);
	
	/**
	 * ����ֵ����ǰ����
	 * @param vdb ���ݿ����
	 * @param value ֵ��ͨ��������
	 * @return
	 */
	abstract public int save(VDB vdb, Object value);

	/**
	 * ����ֵ���ӱ���
	 * @param vdb ���ݿ����
	 * @param value ֵ��ͨ��������
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	abstract public int save(VDB vdb, Object value, Object path, Object name);
	
	/**
	 * ������·��
	 * @param vdb ���ݿ����
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	abstract public int makeDir(VDB vdb, Object path, Object name);
	
	/**
	 * ���������
	 * @param vdb ���ݿ����
	 * @param key ��������
	 * @param len ��ϣ����
	 * @return 0���ɹ�
	 */
	abstract public int createSubKeyDir(VDB vdb, Object key, int len);
	
	/**
	 * ���渽����ͨ����ͼƬ
	 * @param vdb ���ݿ����
	 * @param oldValues ��һ�ε��ôκ����ķ���ֵ
	 * @param newValues �޸ĺ��ֵ
	 * @param name ����
	 * @return ֵ���У�������һ�ε��ô˺���
	 */
	abstract public Sequence saveBlob(VDB vdb, Sequence oldValues, Sequence newValues, String name);

	/**
	 * ����ָ��������ֶ�ֵ
	 * @param vdb ���ݿ����
	 * @param dirNames ·��������
	 * @param dirValues ·��ֵ����
	 * @param valueSigns true����Ŀ¼����������ʱ��������Ŀ¼ֵ��null�����ѡֵ��null��Ŀ¼��false��ʡ��Ŀ¼ֵ�������Դ�Ŀ¼������
	 * @param fvals ����Ҫ�޸ĵ��ֶ�ֵ����
	 * @param fields ����Ҫ�޸ĵ��ֶ�������
	 * @param exp �������ʽ
	 * @param isRecursion true����ȥ����·����ȱʡ�������������漰�㼴ֹͣ
	 * @param ctx ����������
	 * @return 0���ɹ�
	 */
	abstract public int update(VDB vdb, String []dirNames, Object []dirValues, boolean []valueSigns, 
			Object []fvals, String []fields, Expression exp, boolean isRecursion, Context ctx);
		
	/**
	 * ɾ����ǰ��
	 * @param vdb ���ݿ����
	 * @return 0���ɹ�
	 */
	abstract public int delete(VDB vdb);
	
	/**
	 * �������ݿ��ά����ִ�д˲���ʱӦ��ֹͣ����
	 * ����˽ڵ�Ϊ����ɾ��������ɾ���˽ڵ��µĿ��ӽڵ�
	 * @param vdb ���ݿ����
	 * @return true���п�Ŀ¼��ɾ����false��û��
	 */
	abstract public boolean deleteNullSection(VDB vdb);
	
	/**
	 * �ƶ���ǰ�ڵ�ָ��Ŀ¼��
	 * @param vdb ���ݿ����
	 * @param dest Ŀ��·��
	 * @param value �½�ֵ��ʡ����ԭ���Ľ�ֵ
	 * @return 0���ɹ�
	 */
	abstract public int move(VDB vdb, Section dest, Object value);
		
	/**
	 * ɾ����ʱ�Ĳ����ٱ����õ�����λ
	 * @param library ���ݿ����
	 * @param outerSeq ����
	 * @param txSeq �ڴ��
	 */
	abstract public void deleteOutdatedZone(Library library, int outerSeq, long txSeq);
	
	/**
	 * �ύ����
	 * @param library ���ݿ����
	 * @param outerSeq ����
	 * @param innerSeq �ڴ��
	 * @throws IOException
	 */
	abstract public void commit(Library library, int outerSeq, long innerSeq) throws IOException;
	
	/**
	 * ȡ��ǰ�ڵ��ύʱ��
	 * @return ʱ��ֵ
	 */
	abstract public long getCommitTime();
	
	/**
	 * ����������
	 * @param vdb ���ݿ����
	 * @param path �ӽ�ֵ���ӽ�ֵ����
	 * @param name �ӽ������ӽ�������
	 * @return 0���ɹ�
	 */
	abstract public int rename(VDB vdb, Object path, String name);
	
	/**
	 * ɨ���Ѿ�������飬���ڴ洢����
	 * @param library ���ݿ����
	 * @param manager �������
	 * @throws IOException
	 */
	abstract public void scanUsedBlocks(Library library, BlockManager manager) throws IOException;
	
	/**
	 * �ͷŻ���Ľڶ��������ͷ��ڴ�
	 */
	abstract public void releaseSubSection();
	
	/**
	 * �������ݿ����ݵ��¿�
	 * @param srcLib Դ��
	 * @param destLib Ŀ���
	 * @param destHeader Ŀ����׿�
	 * @throws IOException
	 */
	abstract public void reset(Library srcLib, Library destLib, int destHeader) throws IOException;
	
	/**
	 * ����ڶ���
	 * @param library Դ��
	 * @param header ���׿�λ��
	 * @param dir ·��
	 * @return �ڶ���
	 */
	public static ISection read(Library library, int header, Dir dir) {
		try {
			byte []bytes = library.readBlocks(header);
			int dataPos = Library.getDataPos(bytes);
			if ((bytes[dataPos] & ISection.SIGN_ARCHIVE) == 0) {
				return new Section(dir, header, bytes);
			} else {
				return new ArchiveSection(dir, header, bytes);
			}
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
}
