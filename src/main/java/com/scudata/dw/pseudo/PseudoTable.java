package com.scudata.dw.pseudo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.scudata.common.RQException;
import com.scudata.common.Types;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ConjxCursor;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MemoryCursor;
import com.scudata.dm.cursor.MergeCursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.dm.op.Conj;
import com.scudata.dm.op.Group;
import com.scudata.dm.op.Join;
import com.scudata.dm.op.New;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.Select;
import com.scudata.dm.op.Switch;
import com.scudata.dw.ITableMetaData;
import com.scudata.expression.Constant;
import com.scudata.expression.Expression;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.expression.ParamParser;
import com.scudata.expression.UnknownSymbol;
import com.scudata.expression.mfn.sequence.Contain;
import com.scudata.expression.operator.And;
import com.scudata.expression.operator.DotOperator;
import com.scudata.expression.operator.Equals;
import com.scudata.expression.operator.NotEquals;
import com.scudata.expression.operator.Or;
import com.scudata.util.CursorUtil;

public class PseudoTable extends Pseudo {
	//�����α���Ҫ�Ĳ���
	protected String []fkNames;
	protected Sequence []codes;
	protected int pathCount;
	
	protected ArrayList<Operation> extraOpList = new ArrayList<Operation>();//��������������ӳټ��㣨������������select��ӣ�

	protected PseudoTable mcsTable;
	
	protected boolean hasPseudoColumns = false;//�Ƿ���Ҫ����α�ֶ�ת����ö�١���ֵ�����ʽ
	
	public PseudoTable() {
	}
	
	/**
	 * ����������
	 * @param rec �����¼
	 * @param hs �ֻ�����
	 * @param n ������
	 * @param ctx
	 */
	public PseudoTable(Record rec, int n, Context ctx) {
		pd = new PseudoDefination(rec, ctx);
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}

	public PseudoTable(PseudoDefination pd, int n, Context ctx) {
		this.pd = pd;
		pathCount = n;
		this.ctx = ctx;
		extraNameList = new ArrayList<String>();
		init();
	}
	
	public PseudoTable(Record rec, PseudoTable mcs, Context ctx) {
		this(rec, 0, ctx);
		mcsTable = mcs;
	}
	
	public static PseudoTable create(Record rec, int n, Context ctx) {
		PseudoDefination pd = new PseudoDefination(rec, ctx);
		if (pd.isBFile()) {
			return new PseudoBFile(pd, n, ctx);
		} else {
			return new PseudoTable(pd, n, ctx);
		}
	}
	
	protected void init() {
		if (getPd() != null) {
			allNameList = new ArrayList<String>();
			String []names = getPd().getAllColNames();
			for (String name : names) {
				allNameList.add(name);
			}
			
			if (getPd().getColumns() != null) {
				List<PseudoColumn> columns = getPd().getColumns();
				for (PseudoColumn column : columns) {
					//�������ö��α�ֶκͶ�ֵα�ֶΣ�Ҫ��¼�������ڽ������Ĵ����л��õ�
					if (column.getPseudo() != null) {
						hasPseudoColumns = true;
					}
					if (column.getBits() != null) {
						hasPseudoColumns = true;
					}
					
					if (column.getDim() != null) {
						if (column.getFkey() == null) {
							addColName(column.getName());
						} else {
							for (String key : column.getFkey()) {
								addColName(key);
							}
						}
						if (column.getTime() != null) {
							addColName(column.getTime());
						}
					}
				}
			}
		}
	}

	public void addPKeyNames() {
		addColNames(getPd().getAllSortedColNames());
	}
	
	public void addColNames(String []nameArray) {
		for (String name : nameArray) {
			addColName(name);
		}
	}
	
	public void addColName(String name) {
		if (name == null) return; 
		if (allNameList.contains(name) && !extraNameList.contains(name)) {
			extraNameList.add(name);
		}
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
		
		//set FK codes info
		if (fkNameList != null) {
			int size = fkNameList.size();
			fkNames = new String[size];
			fkNameList.toArray(fkNames);
			
			codes = new Sequence[size];
			codeList.toArray(codes);
		}
		
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
		ArrayList<Expression> tempExpList = new ArrayList<Expression>();
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
						if (col.getExp() != null) {
							//�б��ʽ��α��
							newExps[i] = new Expression(col.getExp());
							name = col.getName();
							exp = new Expression(name);
							needNew = true;
							tempExpList.add(exp);
							tempNameList.add(name);
						} else if (col.get_enum() != null) {
							/**
							 * ö���ֶ���ת��
							 */
							String var = "pseudo_enum_value_" + i;
							ctx.setParamValue(var, col.get_enum());
							name = col.getName();
							newExps[i] = new Expression(var + "(" + name + ")");
							exp = new Expression(name);
							needNew = true;
							tempExpList.add(exp);
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
							tempExpList.add(exp);
							tempNameList.add(name);
						}
					}
				} else {
					tempExpList.add(exp);
					tempNameList.add(name);
				}
				
//			} else if (node instanceof DotOperator) {
//				Node left = node.getLeft();
//				if (left != null && left instanceof UnknownSymbol) {
//					PseudoColumn col = getPd().findColumnByName( ((UnknownSymbol)left).getName());
//					if (col != null) {
//						Derive derive = new Derive(new Expression[] {exp}, new String[] {name}, null);
//						extraOpList.add(derive);
//					}
//				}
			}
		}
		
		for (String name : tempList) {
			tempExpList.add(new Expression(name));
			tempNameList.add(name);
		}
		
		size = tempExpList.size();
		this.exps = new Expression[size];
		tempExpList.toArray(this.exps);
		
		this.names = new String[size];
		tempNameList.toArray(this.names);
	
		
		if (needNew) {
			New _new = new New(newExps, fields, null);
			extraOpList.add(_new);
		}
		return;
	}
	
	public String[] getFetchColNames(String []fields) {
		ArrayList<String> tempList = new ArrayList<String>();
		if (fields != null) {
			for (String name : fields) {
				tempList.add(name);
			}
		}
		for (String name : extraNameList) {
			if (!tempList.contains(name)) {
				tempList.add(name);
			}
		}
		
		int size = tempList.size();
		if (size == 0) {
			return null;
		}
		String []newFields = new String[size];
		tempList.toArray(newFields);
		return newFields;
	}
	
	/**
	 * �õ�����ÿ��ʵ�����α깹�ɵ�����
	 * @return
	 */
	public ICursor[] getCursors() {
		List<ITableMetaData> tables = getPd().getTables();
		int size = tables.size();
		ICursor cursors[] = new ICursor[size];
		
		for (int i = 0; i < size; i++) {
			cursors[i] = getCursor(tables.get(i), null, true);
		}
		return cursors;
	}
	
	/**
	 * �õ�table���α�
	 * @param table
	 * @param mcs
	 * @param addOpt �Ƿ�Ѹ��Ӽ������
	 * @return
	 */
	private ICursor getCursor(ITableMetaData table, ICursor mcs, boolean addOpt) {
		ICursor cursor = null;
		if (fkNames != null) {
			if (mcs != null ) {
				if (mcs instanceof MultipathCursors) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, (MultipathCursors)mcs, null, ctx);
				} else {
					if (exps == null) {
						cursor = table.cursor(null, this.names, filter, fkNames, codes, null, ctx);
					} else {
						cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, ctx);
					}
				}
			} else if (pathCount > 1) {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, pathCount, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, pathCount, ctx);
				}
			} else {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, fkNames, codes, null, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, fkNames, codes, null, ctx);
				}
			}
		} else {
			if (mcs != null ) {
				if (mcs instanceof MultipathCursors) {
					cursor = table.cursor(null, this.names, filter, null, null, null, (MultipathCursors)mcs, null, ctx);
				} else {
					if (exps == null) {
						cursor = table.cursor(this.names, filter, ctx);
					} else {
						cursor = table.cursor(this.exps, this.names, filter, null, null, null, ctx);
					}
				}
			} else if (pathCount > 1) {
				if (exps == null) {
					cursor = table.cursor(null, this.names, filter, null, null, null, pathCount, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, null, null, null, pathCount, ctx);
				}
			} else {
				if (exps == null) {
					cursor = table.cursor(this.names, filter, ctx);
				} else {
					cursor = table.cursor(this.exps, this.names, filter, null, null, null, ctx);
				}
			}
		}
		
		if (getPd() != null && getPd().getColumns() != null) {
			for (PseudoColumn column : getPd().getColumns()) {
				if (column.getDim() != null) {//�����������������һ��switch���ӳټ���
					Sequence dim;
					if (column.getDim() instanceof Sequence) {
						dim = (Sequence) column.getDim();
					} else {
						dim = ((IPseudo) column.getDim()).cursor(null, null).fetch();
					}
					
					String fkey[] = column.getFkey();
					/**
					 * ���fkey����null����name������null���Ҵ���time������֯Ϊһ���µ�fkey={name��time}
					 */
					if (fkey == null && column.getName() != null && column.getTime() != null) {
						fkey = new String[] {column.getName(), column.getTime()};
					}
					
					if (fkey == null) {
						/**
						 * ��ʱname��������ֶ�
						 */
						String[] fkNames = new String[] {column.getName()};
						Sequence[] codes = new Sequence[] {dim};
						Switch s = new Switch(fkNames, codes, null, null);
						cursor.addOperation(s, ctx);
//					} else if (fkey.length == 1) {
//						Sequence[] codes = new Sequence[] {dim};
//						Switch s = new Switch(fkey, codes, null, null);
//						cursor.addOperation(s, ctx);
					} else {
						int size = fkey.length;
						
						/**
						 * ���������ʱ���ֶ�,�Ͱ�ʱ���ֶ�ƴ�ӵ�fkeyĩβ
						 */
						if (column.getTime() != null) {
							size++;
							fkey = new String[size];
							System.arraycopy(column.getFkey(), 0, fkey, 0, size - 1);
							fkey[size - 1] = column.getTime();
						}
						
						Expression[][] exps = new Expression[1][];
						exps[0] = new Expression[size];
						for (int i = 0; i < size; i++) {
							exps[0][i] = new Expression(fkey[i]);
						}
						Expression[][] newExps = new Expression[1][];
						newExps[0] = new Expression[] {new Expression("~")};
						String[][] newNames = new String[1][];
						newNames[0] = new String[] {column.getName()};
						Join join = new Join(null, null, exps, new Sequence[] {dim}, new Expression[1][], newExps, newNames, null);
						cursor.addOperation(join, ctx);
					}
				}
			}
		}
	
		if (addOpt) {
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
		}

		return cursor;
	
	}
	
	/**
	 * �鲢���������α�
	 * @param cursors
	 * @return
	 */
	static ICursor mergeCursor(ICursor cursors[],String user, Context ctx) {
		DataStruct ds = cursors[0].getDataStruct();
		int[] sortFields = null;
		
		/**
		 * �������user���˻����ԣ���ʹ��user�ֶι鲢,����ʹ�������鲢��
		 * û�������Ͳ��鲢��
		 */
		if (user != null) {
			int idx = ds.getFieldIndex(user);
			if (idx >= 0) {
				sortFields = new int[] {idx};
			}
		} else {
			sortFields = ds.getPKIndex();
		}
		if (sortFields != null) {
			return new MergeCursor(cursors, sortFields, null, ctx);//������鲢
		} else {
			return new ConjxCursor(cursors);//����������
		}
	}
	
	private ICursor addOptionToCursor(ICursor cursor) {
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
	
	//���������α�
	public ICursor cursor(Expression []exps, String []names) {
		setFetchInfo(exps, names);//��ȡ���ֶ���ӽ�ȥ��������ܻ��extraOpList��ֵ
		
		//ÿ��ʵ���ļ�����һ���α�
		List<ITableMetaData> tables = getPd().getTables();
		int size = tables.size();
		ICursor cursors[] = new ICursor[size];
		
		/**
		 * �Եõ��α���й鲢����Ϊ���
		 * 1 ֻ��һ���α��򷵻أ�
		 * 2 �ж���α��Ҳ�����ʱ�����й鲢
		 * 3 �ж���α��Ҳ���ʱ���ȶԵ�һ���α�ֶΣ�Ȼ�������α갴��һ��ͬ���ֶΣ�����ÿ���α��ÿ���ν��й鲢
		 */
		if (size == 1) {//ֻ��һ���α�ֱ�ӷ���
			return getCursor(tables.get(0), null, true);
		} else {
			if (pathCount > 1) {//ָ���˲���������ʱ����mcsTable
				cursors[0] = getCursor(tables.get(0), null, false);
				for (int i = 1; i < size; i++) {
					cursors[i] = getCursor(tables.get(i), cursors[0], false);
				}
			} else {//û��ָ��������
				if (mcsTable == null) {//û��ָ���ֶβο����mcsTable
					for (int i = 0; i < size; i++) {
						cursors[i] = getCursor(tables.get(i), null, false);
					}
					return addOptionToCursor(mergeCursor(cursors, pd.getUser(), ctx));
				} else {//ָ���˷ֶβο����mcsTable
					ICursor mcs = null;
					if (mcsTable != null) {
						mcs = mcsTable.cursor();
					}
					for (int i = 0; i < size; i++) {
						cursors[i] = getCursor(tables.get(i), mcs, false);
					}
					mcs.close();
				}
			}
			
			//��cursors���ι鲢������:�������α�ĵ�N·�鲢,�õ�N���α�,�ٰ���N���α����ɶ�·�α귵��
			int mcount = ((MultipathCursors)cursors[0]).getPathCount();//�ֶ���
			ICursor mcursors[] = new ICursor[mcount];//����α�
			for (int m = 0; m < mcount; m++) {
				ICursor cursorArray[] = new ICursor[size];
				for (int i = 0; i < size; i++) {
					cursorArray[i] = ((MultipathCursors)cursors[i]).getCursors()[m];
				}
				mcursors[m] = mergeCursor(cursorArray, pd.getUser(), ctx);
			}
			return addOptionToCursor(new MultipathCursors(mcursors, ctx));
		}
	}
	
	//���ڻ�ȡ��·�α�
	private ICursor cursor() {
		List<ITableMetaData> tables = getPd().getTables();
		return tables.get(0).cursor(null, null, null, null, null, null, pathCount, ctx);
	}

	public Object clone(Context ctx) throws CloneNotSupportedException {
		PseudoTable obj = new PseudoTable();
		obj.hasPseudoColumns = hasPseudoColumns;
		obj.pathCount = pathCount;
		obj.mcsTable = mcsTable;
		obj.fkNames = fkNames == null ? null : fkNames.clone();
		obj.codes = codes == null ? null : codes.clone();
		cloneField(obj);
		obj.ctx = ctx;
		return obj;
	}

	public Pseudo setPathCount(int pathCount) {
		PseudoTable table = null;
		try {
			table = (PseudoTable) clone(ctx);
			table.pathCount = pathCount;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}

	public Pseudo setMcsTable(Pseudo mcsTable) {
		PseudoTable table = null;
		try {
			table = (PseudoTable) clone(ctx);
			table.mcsTable = (PseudoTable) mcsTable;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return table;
	}
	
	/**
	 * ת����ֵα�ֶνڵ�Ϊ���ֶ�
	 * ת����ͨα�ֶνڵ�Ϊ���ֶ�
	 * @param node
	 * @return
	 */
	private Node bitsToBoolean(Node node) {
		String pname = ((UnknownSymbol) node).getName();
		PseudoColumn col = getPd().findColumnByPseudoName(pname);
		
		if (col == null) {
			return null;
		}
		
		if (col.getBits() != null) {
			/**
			 * ��һ��UnknownSymbol�Ķ�ֵ�ڵ�ת��Ϊһ��Boolean�ڵ�
			 */
			Sequence seq;
			seq = col.getBits();
			int idx = seq.firstIndexOf(pname) - 1;
			int bit = 1 << idx;
			String str = "and(" + col.getName() + "," + bit + ")!=0";//��Ϊ���ֶε�λ����
			return new Expression(str).getHome();
		} else if (col.get_enum() != null) {
			return null;//ö�ٵĲ������ﴦ��
		} else {
			return new UnknownSymbol(col.getName());//������ͨα�ֶ�
		}
	}
	
	/**
	 * �����ֵα�ֶ�
	 */
	private void replaceFilter(Node node) {
		if (node == null) {
			return;
		}
		
		if (node.getLeft() instanceof UnknownSymbol) {
			Node left = bitsToBoolean(node.getLeft());
			if (left != null) {
				node.setLeft(left);
			}
		} else {
			replaceFilter(node.getLeft());
		}
		
		if (node.getRight() instanceof UnknownSymbol) {
			Node right = bitsToBoolean(node.getRight());
			if (right != null) {
				node.setRight(right);
			}
		} else {
			replaceFilter(node.getRight());
		}
	}
	
	/**
	 * �ѱ��ʽ���漰α�ֶε�ö���������ת��
	 * @param node
	 */
	private void parseFilter(Node node) {
		if (node instanceof And || node instanceof Or) {
			/**
			 * �߼��롢��ʱ���ݹ鴦��
			 */
			parseFilter(node.getLeft());
			parseFilter(node.getRight());
		} else if (node instanceof Equals || node instanceof NotEquals) {
			/**
			 * ��α�ֶε�==��!=���д���
			 */
			if (node.getLeft() instanceof UnknownSymbol) {
				//�ж��Ƿ���α�ֶ�
				String pname = ((UnknownSymbol) node.getLeft()).getName();
				PseudoColumn col = getPd().findColumnByPseudoName(pname);
				if (col != null) {
					Sequence seq;
					//�ж��Ƿ��Ƕ�ö��α�ֶν�������
					if (col.get_enum() != null) {
						seq = col.get_enum();
						node.setLeft(new UnknownSymbol(col.getName()));//��Ϊ���ֶ�
						Integer obj = seq.firstIndexOf(node.getRight().calculate(ctx));
						node.setRight(new Constant(obj));//��ö��ֵ��Ϊ��Ӧ�����ֵ
					}
				}
			} else if (node.getRight() instanceof UnknownSymbol) {
				//�����ֶ������ұߵ���������ҽ���һ���ٴ����߼�������һ��
				Node right = node.getRight();
				node.setRight(node.getLeft());
				node.setLeft(right);
				parseFilter(node);
			}
		} else if (node instanceof DotOperator) {
			//����ö���б��α�ֶε�contain���д���
			if (node.getRight() instanceof Contain) {
				Contain contain = (Contain)node.getRight();
				IParam param = contain.getParam();
				if (param == null || !param.isLeaf()) {
					return;
				}
				
				//�ж��Ƿ��Ƕ�α�ֶν���contain����
				UnknownSymbol un = (UnknownSymbol) param.getLeafExpression().getHome();
				PseudoColumn col = getPd().findColumnByPseudoName(un.getName());
				if (col != null && col.get_enum() != null) {
					Object val = node.getLeft().calculate(ctx);
					if (val instanceof Sequence) {
						//��contain�ұߵ��ֶ�����Ϊ���ֶ�
						IParam newParam = ParamParser.parse(col.getName(), null, ctx);
						contain.setParam(newParam);
						
						//��contain��ߵ�ö��ֵ���и�Ϊ��Ӧ�����ֵ������
						Sequence value = (Sequence) val;
						Sequence newValue = new Sequence();
						int size = value.length();
						for (int i = 1; i <= size; i++) {
							Integer obj = col.get_enum().firstIndexOf(value.get(i));
							newValue.add(obj);
						}
						node.setLeft(new Constant(newValue));
					}
				}
			}
		}
	}
	
	public Operable addOperation(Operation op, Context ctx) {
		if (op == null) {
			return this;
		}
		if (hasPseudoColumns) {
			/**
			 * ����α�ֶΣ���ֵ�ֶΣ�ö���ֶ�
			 */
			Expression exp = op.getFunction().getParam().getLeafExpression();
			Node node = exp.getHome();
			if (node instanceof UnknownSymbol) {
				/**
				 * node��α�ֶ�
				 */
				Node n = bitsToBoolean(node);
				if (n != null) {
					op = new Select(new Expression(n), null);
				}
			} else {
				/**
				 * node����ͨ�ֶ�
				 */
				replaceFilter(node);
				parseFilter(node);
			}
		}
		
		/**
		 * ������user���ڣ���������group@u(user)������user�������ֶ�ʱ
		 * ��group@u(user)ת��Ϊ.group(���ֶ�).conj(~.group(user))
		 */
		if (pd.getUser() != null && op instanceof Group) {
			String ugrp = pd.getAllColNames()[0];//���ֶ�
			Group group = (Group) op;
			if (!(pd.getUser().equals(ugrp)) && group.getOpt() != null && group.getOpt().indexOf("u") >= 0) {
				Group newGroup = new Group(new Expression[] {new Expression(ugrp)}, null);
				Conj conj = new Conj(new Expression("~.group(" + pd.getUser() + ")"));
				return super.addOperation(newGroup, ctx).addOperation(conj, ctx);
			}
		}
		
		return super.addOperation(op, ctx);
	}
	
	/**
	 * ���α��α��ת��Ϊ���ֶΣ�����update��append
	 * @param cursor
	 * @param columns
	 * @param fields
	 */
	private void convertPseudoColumn(ICursor cursor, List<PseudoColumn> columns, String fields[]) {
		//�ȰѲ���α�ֶεĸ�ֵ����
		DataStruct ds = new DataStruct(fields);
		int size = ds.getFieldCount();
		Expression []exps = new Expression[size];
		String []names = new String[size];
		for (int c = 0; c < size; c++) {
			exps[c] = new Expression(fields[c]);
			names[c] = fields[c];
		}
		
		//ת���α����α�ֶ�
		size = columns.size();
		for (int c = 0; c < size; c++) {
			PseudoColumn column = columns.get(c);
			String pseudoName = column.getPseudo();
			Sequence bitNames = column.getBits();
			int idx = ds.getFieldIndex(column.getName());
			
			if (column.getExp() != null) {
				//�б��ʽ��α��
				exps[idx] = new Expression(column.getExp());
				names[idx] = column.getName();
			} else if (pseudoName != null && column.get_enum() != null) {
				//ö��α��
				String var = "pseudo_enum_value_" + c;
				Context context = cursor.getContext();
				if (context == null) {
					context = new Context();
					cursor.setContext(context);
					context.setParamValue(var, column.get_enum());
				} else {
					context.setParamValue(var, column.get_enum());
				}
				exps[idx] = new Expression(var + ".pos(" + pseudoName + ")");
				names[idx] = column.getName();
			} else if (bitNames != null) {
				//�����ֵα�ֶ�(���α�ֶΰ�λת��Ϊһ�����ֶ�)
				String exp = "0";
				int len = bitNames.length();
				for (int i = 1; i <= len; i++) {
					String field = (String) bitNames.get(i);
					//ת��Ϊbitֵ,���ۼ�
					int bit = 1 << (i - 1);
					exp += "+ if(" + field + "," + bit + ",0)";
				}
				exps[idx] = new Expression(exp);
				names[idx] = column.getName();
			}
		}
		
		New _new = new New(exps, names, null);
		cursor.addOperation(_new, null);
	}
	
	public void append(ICursor cursor, String option) {
		//������׷�ӵ�file������ж��file��ȡ���һ��
		List<ITableMetaData> tables = getPd().getTables();
		int size = tables.size();
		if (size == 0) {
			return;
		}
		ITableMetaData table = tables.get(size - 1);

		List<PseudoColumn> columns = pd.getColumns();
		if (columns != null) {
			String fields[] = table.getAllColNames();
			convertPseudoColumn(cursor, columns, fields);
		}
		
		try {
			table.append(cursor, option);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public Sequence update(Sequence data, String opt) {
		//���µ����һ��file
		List<ITableMetaData> tables = getPd().getTables();
		int size = tables.size();
		if (size == 0) {
			return null;
		}
		ITableMetaData table = tables.get(size - 1);

		List<PseudoColumn> columns = pd.getColumns();
		if (columns != null) {
			String fields[] = table.getAllColNames();
			ICursor cursor = new MemoryCursor(data);
			convertPseudoColumn(cursor, columns, fields);
			data = cursor.fetch();
		}
		
		try {
			return table.update(data, opt);
		} catch (IOException e) {
			throw new RQException(e.getMessage(), e);
		}
	}
	
	public Sequence delete(Sequence data, String opt) {
		List<ITableMetaData> tables = getPd().getTables();
		int size = tables.size();
		if (size == 0) {
			return null;
		}
		
		Sequence result = null;
		for (ITableMetaData table : tables) {
			List<PseudoColumn> columns = pd.getColumns();
			if (columns != null) {
				String fields[] = table.getAllColNames();
				ICursor cursor = new MemoryCursor(data);
				convertPseudoColumn(cursor, columns, fields);
				data = cursor.fetch();
			}
			
			try {
				result = table.delete(data, opt);
			} catch (IOException e) {
				throw new RQException(e.getMessage(), e);
			}
		}
		return result;
	}
	
	/**
	 * ������
	 * @param fkName	�����
	 * @param fieldNames ����ֶ�
	 * @param code	���
	 * @return
	 */
	public Pseudo addForeignKeys(String fkName, String []fieldNames, Pseudo code) {
		PseudoTable table = null;
		try {
			table = (PseudoTable) clone(ctx);
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
	
	// ���ӱ�������������������
	public static ICursor join(PseudoTable masterTable, PseudoTable subTable) {
		String[] keys = masterTable.getPrimaryKey();
		if (keys != null) {
			int size = keys.length;
			Expression[] exps = new Expression[size];
			for (int i = 0; i < size; i++) {
				exps[i] = new Expression(keys[i]);
			}
			Expression [][]joinExps = new Expression [][] {exps, exps};//ʹ�������������join
			
			ICursor cursors[] = new ICursor[]{masterTable.cursor(null, null), subTable.cursor(null, null)};
			ICursor cursor = CursorUtil.joinx(cursors, null, joinExps, null, masterTable.getContext());
			return cursor;
		}
		return null;
	}
	
	/**
	 * �������Ӧ�������ֶ�
	 * @return
	 */
	public String[] getFieldNames() {
		return getPd().getAllColNames();
	}
	
	/**
	 * �������Ӧ������ÿ�е���������
	 * ע�⣺���ص��������Ե�һ����¼Ϊ׼
	 * @return
	 */
	public byte[] getFieldTypes() {
		List<ITableMetaData> tables = getPd().getTables();
		ICursor cursor = tables.get(0).cursor(null, null, null, null, null, null, 1, ctx);
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
	
	/**
	 * ����������������еĶ���
	 * û�����ʱ����NULL
	 * @return
	 */
	public List<PseudoColumn> getDimColumns() {
		List<PseudoColumn> dims = new ArrayList<PseudoColumn>();
		List<PseudoColumn> columns = getPd().getColumns();
		for (PseudoColumn col : columns) {
			if (col.getDim() != null) {
				dims.add(col);
			}
		}
		if (dims.size() == 0) {
			return null;
		}
		return dims;
	}
}


