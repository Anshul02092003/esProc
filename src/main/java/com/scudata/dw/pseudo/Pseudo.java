package com.scudata.dw.pseudo;

import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.IndexTable;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.Operable;
import com.scudata.dm.op.Operation;
import com.scudata.dm.op.Select;
import com.scudata.dm.op.Switch;
import com.scudata.expression.Expression;
import com.scudata.expression.operator.And;
import com.scudata.resources.EngineMessage;

public class Pseudo implements IPseudo{
	protected PseudoDefination pd;//ʵ��Ķ���
	protected Sequence cache = null;//��import�Ľ����cache
	
	//�����α���Ҫ�Ĳ���
	protected String []names;
	protected Expression []exps;
	protected Context ctx;
	protected ArrayList<Operation> opList;
	protected Expression filter;
	protected ArrayList<String> fkNameList;
	protected ArrayList<Sequence> codeList;

	protected ArrayList<String> extraNameList;//��Ϊ���˻�α�ֶζ���Ҫ����ȡ�����ֶ���
	protected ArrayList<String> allNameList;//ʵ��������ֶ�
	
	protected String []deriveNames;//��derive��ӵ�
	protected Expression []deriveExps;
	
	public void addColName(String name) {
		throw new RQException("never run to here");
	}
	
	public void addPKeyNames() {
		throw new RQException("never run to here");
	}
	
	public Operable addOperation(Operation op, Context ctx) {
		IPseudo newObj = null;
		try {
			newObj = (IPseudo) ((IPseudo)this).clone(ctx);
			((Pseudo) newObj).addOpt(op, ctx);
		} catch (CloneNotSupportedException e) {
			throw new RQException(e);
		}
		return (Operable) newObj;
	}
	
	public void addOpt(Operation op, Context ctx) {
		if (opList == null) {
			opList = new ArrayList<Operation>();
		}
		
		if (op != null) {
			ArrayList<String> tempList = new ArrayList<String>();
			new Expression(op.getFunction()).getUsedFields(ctx, tempList);
			
			if (op instanceof Select && op.getFunction() != null) {
				Expression exp = ((Select)op).getFilterExpression();
				boolean flag = true;//true��ʾ���Ǳ�����ֶΡ� ����news��new��derive
				for (String name : tempList) {
					if (!isColumn(name)) {
						flag = false;
						break;
					}
				}
				
				if (flag) {
					if (filter == null) {
						filter = exp;
					} else {
						And and = new And();
						and.setLeft(filter.getHome());
						and.setRight(exp.getHome());
						filter = new Expression(and);
					}
				} else {
					for (String name : tempList) {
						addColName(name);
					}
					opList.add(op);
				}
				
			} else if (op instanceof Switch && ((Switch) op).isIsect()) {
				//��switch@iת��ΪF:K
				//ת�����������ֶΡ������ֶδ�������������code����������codeû��������
				String names[] = ((Switch) op).getFkNames();
				Sequence codes[] = ((Switch) op).getCodes();
				
				//����Ƿ�������������
				boolean flag = true;
				while (true) {
					if (1 != names.length) break;//���ǵ��ֶ�
					String keyName;
					if (((Switch) op).getExps()[0] == null) {
						keyName = codes[0].dataStruct().getPrimary()[0];
					} else {
						keyName = ((Switch) op).getExps()[0].getIdentifierName();
					}
					Object obj = codes[0].ifn();
					
					if (obj instanceof Record) {
						DataStruct ds = ((Record)obj).dataStruct();
						if (-1 == ds.getFieldIndex(keyName)) {
							//code�ﲻ���ڸ��ֶ�
							MessageManager mm = EngineMessage.get();
							throw new RQException(keyName + mm.getMessage("ds.fieldNotExist"));
						}
						int []fields = ds.getPKIndex();
						if (fields == null) {
							break;//����������
						} else {
							if (! ds.getPrimary()[0].equals(keyName)) {
								break;//���ֶβ���code������
							}
						}
					} else {
						MessageManager mm = EngineMessage.get();
						throw new RQException(mm.getMessage("engine.needPmt"));
					}
					
					IndexTable it = codes[0].getIndexTable();
					if (it != null) {
						break;//����������Ҳ�޷�ת��
					}
					
					flag = false;
					if (codeList == null) {
						codeList = new ArrayList<Sequence>();
					}
					if (fkNameList == null) {
						fkNameList = new ArrayList<String>();
					}
					for (String name : names) {
						fkNameList.add(name);
					}
					for (Sequence seq : codes) {
						codeList.add(seq);
					}
					break;
				}
				
				if (flag) {
					//������ȡΪF:T
					for (String name : tempList) {
						addColName(name);
					}
					opList.add(op);
				}
//			} else if (op instanceof Derive) {
//				Derive derive = (Derive) op;
//				Expression []derExps = derive.getExps();
//				String []derNames = derive.getNames();
//				if (deriveExps == null) {
//					deriveExps = derExps;
//					deriveNames = derNames;
//				} else {
//					int oldSize = deriveExps.length;
//					int newSize = derExps.length + oldSize;
//					
//					Expression []newExps = new Expression[newSize];
//					String []newNames = new String[newSize];
//					System.arraycopy(deriveExps, 0, newExps, 0, oldSize);
//					System.arraycopy(derExps, 0, newExps, oldSize, derExps.length);
//					System.arraycopy(deriveNames, 0, newNames, 0, oldSize);
//					System.arraycopy(derNames, 0, newNames, oldSize, derExps.length);
//					
//					deriveExps = newExps;
//					deriveNames = newNames;
//				}
			} else {
				for (String name : tempList) {
					addColName(name);
				}
				opList.add(op);
			}
		}
		this.ctx = ctx;
	}
	
	//����news��new��derive
	protected void setFetchInfo(ICursor cursor, Expression []exps, String []names) {
		if (this.exps != null) return;
		
		if (exps == null && extraNameList.size() == 0) {
			//���û��ָ��ȡ���ֶΣ���������֯
			names = cursor.getDataStruct().getFieldNames();
			for (String name : names) {
				if (!extraNameList.contains(name)) {
					extraNameList.add(name);
				}
			}
			
			int size = extraNameList.size();
			names = new String[size];
			extraNameList.toArray(names);
			exps = new Expression[size];
			for (int i = 0; i < size; i++) {
				exps[i] = new Expression(names[i]);
			}
			this.exps = exps;
			this.names = names;
		} else {
			//���ָ����ȡ���ֶΣ�ҲҪ�Ѷ����õ����ֶμ���
			//���extraNameList���Ƿ����exps����ֶ�
			//����У���ȥ��
			ArrayList<String> tempList = new ArrayList<String>();
			for (String name : extraNameList) {
				if (!tempList.contains(name)) {
					tempList.add(name);
				}
			}
			if (exps != null) {
				for (Expression exp : exps) {
					String expName = exp.getIdentifierName();
					if (tempList.contains(expName)) {
						tempList.remove(expName);
					}
				}
			}
			
			ArrayList<String> tempNameList = new ArrayList<String>();
			ArrayList<Expression> tempExpList = new ArrayList<Expression>();
			if (exps != null) {
				for (Expression exp : exps) {
					tempExpList.add(exp);
				}
				if (names == null) {
					for (Expression exp : exps) {
						tempNameList.add(exp.getIdentifierName());
					}
				} else {
					for (String name : names) {
						tempNameList.add(name);
					}
				}
			}
			for (String name : tempList) {
				tempExpList.add(new Expression(name));
				tempNameList.add(name);
			}
			
			int size = tempExpList.size();
			this.exps = new Expression[size];
			tempExpList.toArray(this.exps);
			
			this.names = new String[size];
			tempNameList.toArray(this.names);
		}
		
//		//�����derive������exps��names
//		if (deriveExps != null) {
//			int oldSize = this.exps.length;
//			int newSize = deriveExps.length + oldSize;
//			
//			Expression []newExps = new Expression[newSize];
//			String []newNames = new String[newSize];
//			System.arraycopy(this.exps, 0, newExps, 0, oldSize);
//			System.arraycopy(deriveExps, 0, newExps, oldSize, deriveExps.length);
//			System.arraycopy(this.names, 0, newNames, 0, oldSize);
//			System.arraycopy(deriveNames, 0, newNames, oldSize, deriveExps.length);
//			
//			this.exps = newExps;
//			this.names = newNames;
//		}
	}
	
	public boolean isColumn(String col) {
		return allNameList.contains(col);
	}
	
	public Context getContext() {
		return ctx;
	}
	
	public void cloneField(Pseudo obj) {
		//obj.table = table;
		obj.pd = getPd();
		obj.ctx = ctx;
		obj.names = names == null ? null : names.clone();
		obj.exps = exps == null ? null : exps.clone();
		obj.filter = filter == null ? null : filter.newExpression(ctx);
		
		if (opList != null) {
			obj.opList = new ArrayList<Operation>();
			for (Operation op : opList) {
				obj.opList.add(op.duplicate(ctx));
			}
		}
		
		if (fkNameList != null) {
			obj.fkNameList = new ArrayList<String>();
			for (String str : fkNameList) {
				obj.fkNameList.add(str);
			}
		}
		if (codeList != null) {
			obj.codeList = new ArrayList<Sequence>();
			for (Sequence seq : codeList) {
				obj.codeList.add(seq);
			}
		}

		if (extraNameList != null) {
			obj.extraNameList = new ArrayList<String>();
			for (String str : extraNameList) {
				obj.extraNameList.add(str);
			}
		}
		if (allNameList != null) {
			obj.allNameList = new ArrayList<String>();
			for (String str : allNameList) {
				obj.allNameList.add(str);
			}
		}
		
		obj.deriveNames = deriveNames == null ? null : deriveNames.clone();
		obj.deriveExps = deriveExps == null ? null : deriveExps.clone();
	}

	public PseudoDefination getPd() {
		return pd;
	}
	
	public void append(ICursor cursor, String option) {
		throw new RQException("never run to here");
	}
	
	public Sequence update(Sequence data, String opt) {
		throw new RQException("never run to here");
	}
	
	public Sequence delete(Sequence data, String opt) {
		throw new RQException("never run to here");
	}

	public void addColNames(String[] nameArray) {
		throw new RQException("never run to here");
	}

	public ICursor cursor(Expression[] exps, String[] names) {
		throw new RQException("never run to here");
	}

	public Object clone(Context ctx) throws CloneNotSupportedException {
		throw new RQException("never run to here");
	}

	public void setCache(Sequence cache) {
		this.cache = cache;
	}
	
	public Sequence getCache() {
		return cache;
	}
}