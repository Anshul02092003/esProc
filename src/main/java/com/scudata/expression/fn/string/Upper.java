package com.scudata.expression.fn.string;

import com.scudata.array.ConstArray;
import com.scudata.array.IArray;
import com.scudata.array.StringArray;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.Sentence;
import com.scudata.dm.Context;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;
import com.scudata.resources.EngineMessage;

/**
 * ���ַ���ת�ɴ�д
 * @author runqian
 *
 */
public class Upper extends Function {
	/**
	 * �����ʽ����Ч�ԣ���Ч���׳��쳣
	 */
	public void checkValidity() {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("upper" + mm.getMessage("function.missingParam"));
		} else if (!param.isLeaf()) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("upper" + mm.getMessage("function.invalidParam"));
		}
	}

	public Object calculate(Context ctx) {
		Expression param1 = param.getLeafExpression();
		Object result1 = param1.calculate(ctx);
		
		if (result1 instanceof String) {
			if (option == null || option.indexOf('q') == -1) {
				return ((String)result1).toUpperCase();
			} else {
				return upper((String)result1);
			}
		} else if (result1 == null) {
			return null;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException("upper" + mm.getMessage("function.paramTypeError"));
		}
	}

	// ���������ڵ�
	private String upper(String str) {
		int len = str.length();
		if (len < 3) {
			return str.toUpperCase();
		}
		
		String result = "";
		int i = 0;
		while (true) {
			int index = str.indexOf('"', i);
			if (index < 0) {
				index = str.indexOf('\'', i);
				if (index < 0) {
					if (i == 0) {
						return str.toUpperCase();
					} else {
						return result + str.substring(i).toUpperCase();
					}
				}
			}
			
			int match = Sentence.scanQuotation(str, index);
			if (match > 0) {
				result += str.substring(i, index).toUpperCase();
				result += str.substring(index, match + 1);
				i = match + 1;
			} else {
				if (i == 0) {
					return str.toUpperCase();
				} else {
					return result + str.substring(i).toUpperCase();
				}
			}
		}
	}

	/**
	 * ����signArray��ȡֵΪsign����
	 * @param ctx
	 * @param signArray �б�ʶ����
	 * @param sign ��ʶ
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx, IArray signArray, boolean sign) {
		boolean[] signDatas;
		if (sign) {
			signDatas = signArray.isTrue().getDatas();
		} else {
			signDatas = signArray.isFalse().getDatas();
		}
		
		IArray array = param.getLeafExpression().calculateAll(ctx);
		int size = array.size();
		boolean flag = option == null || option.indexOf('q') == -1;
		
		if (array instanceof StringArray) {
			StringArray stringArray = (StringArray)array;
			StringArray result = new StringArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				if (signDatas[i] == false) {
					result.pushNull();
					continue;
				}
				
				String str = stringArray.getString(i);
				if (str != null) {
					if (flag) {
						result.push(str.toUpperCase());
					} else {
						result.push(upper(str));
					}
				} else {
					result.push(null);
				}
			}
			
			return result;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			String str = null;
			
			if (obj instanceof String) {
				if (flag) {
					str = ((String)obj).toUpperCase();
				} else {
					str = upper((String)obj);
				}
			} else if (obj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("upper" + mm.getMessage("function.paramTypeError"));
			}
			
			return new ConstArray(str, size);
		} else {
			StringArray result = new StringArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				if (signDatas[i] == false) {
					result.pushNull();
					continue;
				}
				
				Object obj = array.get(i);
				String str = null;
				
				if (obj instanceof String) {
					if (flag) {
						str = ((String)obj).toUpperCase();
					} else {
						str = upper((String)obj);
					}
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("upper" + mm.getMessage("function.paramTypeError"));
				}
				
				result.push(str);
			}
			
			return result;
		}
	}

	/**
	 * ����������еĽ��
	 * @param ctx ����������
	 * @return IArray
	 */
	public IArray calculateAll(Context ctx) {
		IArray array = param.getLeafExpression().calculateAll(ctx);
		int size = array.size();
		boolean sign = option == null || option.indexOf('q') == -1;
		
		if (array instanceof StringArray) {
			StringArray stringArray = (StringArray)array;
			StringArray result = new StringArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				String str = stringArray.getString(i);
				if (str != null) {
					if (sign) {
						result.push(str.toUpperCase());
					} else {
						result.push(upper(str));
					}
				} else {
					result.push(null);
				}
			}
			
			return result;
		} else if (array instanceof ConstArray) {
			Object obj = array.get(1);
			String str = null;
			
			if (obj instanceof String) {
				if (sign) {
					str = ((String)obj).toUpperCase();
				} else {
					str = upper((String)obj);
				}
			} else if (obj != null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("upper" + mm.getMessage("function.paramTypeError"));
			}
			
			return new ConstArray(str, size);
		} else {
			StringArray result = new StringArray(size);
			result.setTemporary(true);
			
			for (int i = 1; i <= size; ++i) {
				Object obj = array.get(i);
				String str = null;
				
				if (obj instanceof String) {
					if (sign) {
						str = ((String)obj).toUpperCase();
					} else {
						str = upper((String)obj);
					}
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("upper" + mm.getMessage("function.paramTypeError"));
				}
				
				result.push(str);
			}
			
			return result;
		}
	}
}