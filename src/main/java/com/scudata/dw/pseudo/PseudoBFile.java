package com.scudata.dw.pseudo;

import java.util.ArrayList;
import com.scudata.common.RQException;
import com.scudata.common.Types;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.BFileCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.op.New;
import com.scudata.dm.op.Operation;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;
import com.scudata.expression.UnknownSymbol;

/**
 * ���ļ������
 * @author LW
 *
 */
public class PseudoBFile extends PseudoTable {
	public PseudoBFile() {
	}
	
	/**
	 * ����������
	 * @param rec �����¼
	 * @param hs �ֻ�����
	 * @param n ������
	 * @param ctx
	 */
	public PseudoBFile(Record rec, int n, Context ctx) {
		pd = new PseudoDefination(rec, ctx);
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}

	public PseudoBFile(Record rec, PseudoTable mcs, Context ctx) {
		this(rec, 0, ctx);
		mcsTable = mcs;
	}
	
	public PseudoBFile(PseudoDefination pd, int n, Context ctx) {
		this.pd = pd;
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}

	/**
	 * �õ�����ÿ��ʵ�����α깹�ɵ�����
	 * @return
	 */
	public ICursor[] getCursors() {
		throw new RQException("Never run to here.");
	}
	
	/**
	 * ����ȡ���ֶ�
	 * @param exps ȡ�����ʽ
	 * @param fields ȡ������
	 */
	protected void setFetchInfo(Expression []exps, String []fields) {
		this.exps = null;
		this.names = null;
		boolean needNew = extraNameList.size() > 0;
		Expression newExps[] = null;
		
		extraOpList.clear();
		
		if (exps == null) {
			if (fields == null) {
				return;
			} else {
				int len = fields.length;
				exps = new Expression[len];
				for (int i = 0; i < len; i++) {
					exps[i] = new Expression(fields[i]);
				}
			}
		}
		
		newExps = exps.clone();//����һ��
		
		/**
		 * ��ȡ�����ʽҲ��ȡ���ֶ�,����extraNameList���Ƿ����exps����ֶ�
		 * ���������ȥ��
		 */
		ArrayList<String> tempList = new ArrayList<String>();
		for (String name : extraNameList) {
			if (!tempList.contains(name)) {
				tempList.add(name);
			}
		}
		for (Expression exp : exps) {
			String expName = exp.getIdentifierName();
			if (tempList.contains(expName)) {
				tempList.remove(expName);
			}
		}
		
		ArrayList<String> tempNameList = new ArrayList<String>();
		int size = exps.length;
		for (int i = 0; i < size; i++) {
			Expression exp = exps[i];
			String name = fields[i];
			Node node = exp.getHome();
			
			if (node instanceof UnknownSymbol) {
				String expName = exp.getIdentifierName();
				if (!allNameList.contains(expName)) {
					/**
					 * �����α�ֶ�����ת��
					 */
					PseudoColumn col = pd.findColumnByPseudoName(expName);
					if (col != null) {
						if (col.get_enum() != null) {
							/**
							 * ö���ֶ���ת��
							 */
							String var = "pseudo_enum_value_" + i;
							ctx.setParamValue(var, col.get_enum());
							name = col.getName();
							newExps[i] = new Expression(var + "(" + name + ")");
							exp = new Expression(name);
							needNew = true;
							tempNameList.add(name);
						} else if (col.getBits() != null) {
							/**
							 * ��ֵ�ֶ���ת��
							 */
							name = col.getName();
							String pname = ((UnknownSymbol) node).getName();
							Sequence seq;
							seq = col.getBits();
							int idx = seq.firstIndexOf(pname) - 1;
							int bit = 1 << idx;
							String str = "and(" + col.getName() + "," + bit + ")!=0";//��Ϊ���ֶε�λ����
							newExps[i] = new Expression(str);
							exp = new Expression(name);
							needNew = true;
							tempNameList.add(name);
						}
					}
				} else {
					tempNameList.add(name);
				}
			}
		}
		
		for (String name : tempList) {
			if (!tempNameList.contains(name)) {
				tempNameList.add(name);
			}
		}
		
		size = tempNameList.size();
		
		this.names = new String[size];
		tempNameList.toArray(this.names);
	
		
		if (needNew) {
			New _new = new New(newExps, fields, null);
			extraOpList.add(_new);
		}
		return;
	}
	
	public ICursor cursor(Expression []exps, String []names) {
		return cursor(exps, names, false);
	}
	
	//���������α�
	public ICursor cursor(Expression []exps, String []names, boolean isColumn) {
		ICursor cursor = null;
		setFetchInfo(exps, names);//��ȡ���ֶ���ӽ�ȥ��������ܻ��extraOpList��ֵ
		if (pathCount > 1) {//ָ���˲�����
			int count = pathCount;
			ICursor cursors[] = new ICursor[count];
			for (int i = 0; i < count; ++i) {
				if (this.exps == null && this.names == null) {
					cursors[i] = new BFileCursor(pd.getFileObject(), null, i + 1, count, null, ctx);
				} else {
					cursors[i] = new BFileCursor(pd.getFileObject(), this.names, i + 1, count, null, ctx);
				}
			}
			cursor = new MultipathCursors(cursors, ctx);
		} else {
			if (this.exps == null && this.names == null) {
				cursor = new BFileCursor(pd.getFileObject(), null, null, ctx);
			} else {
				cursor = new BFileCursor(pd.getFileObject(), this.names, null, ctx);
			}
		}

		if (opList != null) {
			for (Operation op : opList) {
				cursor.addOperation(op, ctx);
			}
		}
		if (extraOpList != null) {
			for (Operation op : extraOpList) {
				cursor.addOperation(op, ctx);
			}
		}
		
		return cursor;
	}
	
	public Object clone(Context ctx) throws CloneNotSupportedException {
		PseudoBFile obj = new PseudoBFile();
		obj.hasPseudoColumns = hasPseudoColumns;
		obj.pathCount = pathCount;
		obj.mcsTable = mcsTable;
		obj.fkNames = fkNames == null ? null : fkNames.clone();
		obj.codes = codes == null ? null : codes.clone();
		cloneField(obj);
		obj.ctx = ctx;
		return obj;
	}
	
	public void append(ICursor cursor, String option) {
		pd.getFileObject().exportCursor(cursor, null, null, "ab", null, ctx);
	}
	
	public Sequence update(Sequence data, String opt) {
		throw new RQException("Never run to here."); 
	}
	
	public Sequence delete(Sequence data, String opt) {
		throw new RQException("Never run to here.");
	}
	
	public Pseudo addForeignKeys(String fkName, String []fieldNames, Pseudo code) {
		PseudoBFile table = null;
		try {
			table = (PseudoBFile) clone(ctx);
			table.getPd().addPseudoColumn(new PseudoColumn(fkName, fieldNames, code));
			if (fieldNames == null) {
				table.addColName(fkName);
			} else {
				for (String key : fieldNames) {
					table.addColName(key);
				}
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * �������Ӧ������ÿ�е���������
	 * ע�⣺���ص��������Ե�һ����¼Ϊ׼
	 * @return
	 */
	public byte[] getFieldTypes() {
		ICursor cursor = new BFileCursor(pd.getFileObject(), null, null, ctx);
		Sequence data = cursor.fetch(1);
		cursor.close();
		
		if (data == null || data.length() == 0) {
			return null;
		}
		
		Record record = (Record) data.getMem(1);
		Object[] objs = record.getFieldValues();
		int len = objs.length;
		byte[] types = new byte[len];
		
		for (int i = 0; i < len; i++) {
			types[i] = Types.getProperDataType(objs[i]);
		}
		return types;
	}
}
