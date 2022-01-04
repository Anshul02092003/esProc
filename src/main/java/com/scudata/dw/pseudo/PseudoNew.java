package com.scudata.dw.pseudo;

import java.util.ArrayList;
import java.util.List;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dw.ColumnTableMetaData;
import com.scudata.dw.ITableMetaData;
import com.scudata.dw.JoinCursor;
import com.scudata.expression.Expression;
import com.scudata.expression.mfn.dw.New;
import com.scudata.parallel.ClusterTableMetaData;

public class PseudoNew extends Pseudo implements Operable, IPseudo {
	private Object ptable;//����cs/A��Ҳ������һ�����
	String option;
	
	/**
	 * ���ݶ���pd����һ��PseudoNew����
	 * @param pd ����
	 * @param ptable new�Ĳ�����������������С��α�
	 * @param option
	 */
	public PseudoNew(PseudoDefination pd, Object ptable, String option) {
		this.pd = pd;
		this.ptable = ptable;
		this.option = option;
		init();
		addPKeyNames();
	}
	
	public PseudoNew() {
	}

	public PseudoNew(PseudoDefination pd, Object ptable, Expression[] exps, String[] names, Expression filter,
			String[] fkNames, Sequence[] codes, String option) {
		this.pd = pd;
		this.ptable = ptable;
		this.exps = exps;
		this.names = names;
		this.filter = filter;
		if (fkNames != null) {
			for (String fkname : fkNames) {
				fkNameList.add(fkname);
			}
		}
		if (codes != null) {
			for (Sequence code : codes) {
				codeList.add(code);
			}
		}
		this.option = option;
		init();
		addPKeyNames();
	}

	private void init() {
		extraNameList = new ArrayList<String>();
		allNameList = new ArrayList<String>();
		String []names = getPd().getAllColNames();
		for (String name : names) {
			allNameList.add(name);
		}
	}
	
	public void addPKeyNames() {
		//addColNames(table.getAllSortedColNames());
		//ptable.addPKeyNames();
	}
	
	public void addColNames(String[] nameArray) {
		for (String name : nameArray) {
			addColName(name);
		}
	}

	public void addColName(String name) {
		if (ptable instanceof IPseudo) {
			((IPseudo)ptable).addColName(name);
		}
		if (name == null) return; 
		if (!extraNameList.contains(name)) {
			extraNameList.add(name);
		}
	}

	/**
	 * �õ�table.new(cursor)���α�
	 * @param table
	 * @param cursor
	 * @param fkNames
	 * @param codes
	 * @return
	 */
	private ICursor getCursor(ITableMetaData table, ICursor cursor, String []fkNames, Sequence []codes) {
		ICursor result;
		if (table instanceof ClusterTableMetaData) {
			result = ((ClusterTableMetaData)table).news(exps, names, cursor, 1, option, filter, fkNames, codes);
		} else if (JoinCursor.isColTable(table)) {
			result = (ICursor) New._new((ColumnTableMetaData)table, cursor, cursor, filter, exps,	names, fkNames, codes, option, ctx);
		} else {
			result = (ICursor) New._new((ITableMetaData)table, cursor, cursor, filter, exps,	names, fkNames, codes, ctx);
		}
		ArrayList<Operation> opList = this.opList;
		if (opList != null) {
			for (Operation op : opList) {
				result.addOperation(op, ctx);
			}
		}
		return result;
	}
	
	/**
	 * ����T.new(cs)���α�
	 */
	public ICursor cursor(Expression[] exps, String[] names) {
		//ȡ��ctx
		if (ctx == null) {
			if (ptable instanceof IPseudo) {
				ctx = ((IPseudo)ptable).getContext();
			} else if (ptable instanceof ICursor) {
				ctx = ((ICursor)ptable).getContext();
			}
		}
		
		//�ѿ��ܵ�ȡ���ֶ���ӵ����T��
		if (exps != null) {
			for (Expression exp : exps) {
				addColName(exp.getIdentifierName());
			}
		}
		
		//�õ����T��ʵ���
		List<ITableMetaData> tables = getPd().getTables();
		int tsize = tables.size();
		
		//����ptable�õ�cs�����ܶ�Ӧ�����
		ICursor cursors[] = new ICursor[tsize];
		if (ptable instanceof PseudoTable) {
			cursors = ((PseudoTable)ptable).getCursors();
		} else if (ptable instanceof ICursor) {
			cursors[0] = (ICursor)ptable;
		} else {
			cursors[0] = new MemoryCursor((Sequence) ptable);
		}
		
		//����ȡ���ֶ�
		setFetchInfo(cursors[0], exps, names);
		exps = this.exps;
		names = this.names;
		
		//��֯F:K����
		String []fkNames = null;
		Sequence []codes = null;
		if (fkNameList != null) {
			int size = fkNameList.size();
			fkNames = new String[size];
			fkNameList.toArray(fkNames);
			
			codes = new Sequence[size];
			codeList.toArray(codes);
		}
		
		if (tsize == 1) {
			return getCursor(tables.get(0), cursors[0], fkNames, codes);
		} else {
			for (int i = 0; i < tsize; i++) {
				cursors[i] = getCursor(tables.get(i), cursors[i], fkNames, codes);
			}
			return PseudoTable.mergeCursor(cursors, null, ctx);
		}
	}
	
	public Object clone(Context ctx) throws CloneNotSupportedException {
		PseudoNew obj = new PseudoNew();
		cloneField(obj);
		obj.ptable = ptable;
		obj.ctx= ctx;
		return obj;
	}
}
