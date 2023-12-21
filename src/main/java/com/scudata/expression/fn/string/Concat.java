package com.scudata.expression.fn.string;

import com.scudata.array.IArray;
import com.scudata.array.ObjectArray;
import com.scudata.array.StringArray;
import com.scudata.common.Escape;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Env;
import com.scudata.dm.Sequence;
import com.scudata.expression.Expression;
import com.scudata.expression.Gather;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * concat(xi,��) ���������ӳ�Ϊ�ַ������Ҵ�ƴ��ʱ�������š�
 * @author runqian
 *
 */
public class Concat extends Gather {
	private Expression exp;
	private String sep = null; // �ָ���
	private boolean addQuotes = false;
	private boolean addSingleQuotes = false;
	
	public void prepare(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("concat" + mm.getMessage("function.missingParam"));
		} else if (param.isLeaf()) {
			exp = param.getLeafExpression();
		} else if (param.getSubSize() == 2) {
			IParam sub = param.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("concat" + mm.getMessage("function.invalidParam"));
			}
			
			exp = sub.getLeafExpression();
			sub = param.getSub(1);
			
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("concat" + mm.getMessage("function.paramTypeError"));
				}

				sep = (String)obj;
			}
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("concat" + mm.getMessage("function.invalidParam"));
		}
		
		if (option != null) {
			if (option.indexOf('c') != -1) sep = ",";
			if (option.indexOf('q') != -1) addQuotes = true;
			if (option.indexOf('i') != -1) addSingleQuotes = true;
		}
	}
	
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("concat" + mm.getMessage("function.missingParam"));
		}
	}

	private static void concat(Object obj, StringBuffer out) {
		if (obj instanceof Sequence) {
			Sequence seq = (Sequence)obj;
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				concat(seq.getMem(i), out);
			}
		} else if (obj != null) {
			out.append(obj.toString());
		}
	}
	
	private void gather(Object obj, StringBuffer out) {
		if (obj instanceof Sequence) {
			Sequence seq = (Sequence)obj;
			for (int i = 1, len = seq.length(); i <= len; ++i) {
				gather(seq.getMem(i), out);
			}
		} else if (obj instanceof StringBuffer) {
			// ���̶߳��λ���
			if (sep != null) {
				out.append(sep);
			}
			
			out.append(obj.toString());
		} else if (obj != null) {
			if (sep != null && out.length() > 0) {
				out.append(sep);
			}
			
			if (addQuotes) {
				if (obj instanceof String) {
					out.append(Escape.addEscAndQuote((String)obj));
				} else {
					out.append(obj.toString());
				}
			} else if (addSingleQuotes) {
				if (obj instanceof String) {
					out.append('\'');
					out.append((String)obj);
					out.append('\'');
				} else {
					out.append(obj.toString());
				}				
			} else {
				out.append(obj.toString());
			}
		} else {
			if (out.length() > 0) {
				out.append(sep);
			}
		}
	}
	
	public Object calculate(Context ctx) {
		StringBuffer sb = new StringBuffer();
		if (param.isLeaf()) {
			Object obj = param.getLeafExpression().calculate(ctx);
			concat(obj, sb);
		} else {
			for (int i = 0, size = param.getSubSize(); i < size; ++i) {
				IParam sub = param.getSub(i);
				if (sub == null || !sub.isLeaf()) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("concat" + mm.getMessage("function.invalidParam"));
				}
				
				Object obj = sub.getLeafExpression().calculate(ctx);
				concat(obj, sb);
			}
		}

		return sb.toString();
	}
	
	public Object gather(Context ctx) {
		Object obj = exp.calculate(ctx);
		if (obj instanceof StringBuffer) {
			return obj;
		}
		
		StringBuffer sb = new StringBuffer();
		gather(obj, sb);
		return sb;
	}

	public Object gather(Object oldValue, Context ctx) {
		Object obj = exp.calculate(ctx);
		gather(obj, (StringBuffer)oldValue);
		return oldValue;
	}

	public Expression getRegatherExpression(int q) {
		if (sep == null) {
			String str = "concat(#" + q + ")";
			return new Expression(str);
		} else {
			String str = "concat(#" + q + ",\"" + sep + "\")";
			return new Expression(str);
		}
	}
	
	public boolean needFinish() {
		return true;
	}
	
	public Object finish(Object val) {
		return val.toString();
	}
	
	/**
	 * �������м�¼��ֵ�����ܵ����������
	 * @param result �������
	 * @param resultSeqs ÿ����¼��Ӧ�Ľ����������
	 * @param ctx ����������
	 * @return IArray �������
	 */
	public IArray gather(IArray result, int []resultSeqs, Context ctx) {
		if (result == null) {
			result = new ObjectArray(Env.INITGROUPSIZE);
		}
		
		IArray array = exp.calculateAll(ctx);
		for (int i = 1, len = array.size(); i <= len; ++i) {
			if (result.size() < resultSeqs[i]) {
				StringBuffer sb = new StringBuffer();
				gather(array.get(i), sb);
				result.add(sb);
			} else {
				StringBuffer sb = (StringBuffer)result.get(resultSeqs[i]);
				gather(array.get(i), sb);
			}
		}
		
		return result;
	}

	/**
	 * ��̷̳���Ķ��λ�������
	 * @param result һ���̵߳ķ�����
	 * @param result2 ��һ���̵߳ķ�����
	 * @param seqs ��һ���̵߳ķ������һ���̷߳���Ķ�Ӧ��ϵ
	 * @param ctx ����������
	 * @return
	 */
	public void gather2(IArray result, IArray result2, int []seqs, Context ctx) {
		for (int i = 1, len = result2.size(); i <= len; ++i) {
			if (seqs[i] != 0) {
				StringBuffer sb1 = (StringBuffer)result.get(seqs[i]);
				StringBuffer sb2 = (StringBuffer)result2.get(i);
				if (sb1 == null) {
					result.set(seqs[i], sb2);
				} else {
					// ���̶߳��λ���
					if (sep != null) {
						sb1.append(sep);
					}
					
					sb1.append(sb2.toString());
				}
			}
		}
	}
	
	/**
	 * �Է�������õ��Ļ����н������մ���
	 * @param array �����е�ֵ
	 * @return IArray
	 */
	public IArray finish(IArray array) {
		int len = array.size();
		StringArray stringArray = new StringArray(len);
		
		for (int i = 1; i <= len; ++i) {
			Object stringBuffer = array.get(i);
			stringArray.push(stringBuffer.toString());
		}
		
		return stringArray;
	}
}