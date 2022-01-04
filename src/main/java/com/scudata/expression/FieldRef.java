package com.scudata.expression;

import java.util.List;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.resources.EngineMessage;
import com.scudata.util.Variant;

/**
 * �ֶ�����
 * A.f r.f ~.f
 * @author WangXiaoJun
 *
 */
public class FieldRef extends Node {
	private String name;
	private Object s2r; // ���л��¼
	private int col; // �ֶ�����
	private DataStruct prevDs;

	public FieldRef(String fieldName) {
		name = fieldName;
	}

	public String getName() {
		return name;
	}
	
	public void getUsedFields(Context ctx, List<String> resultList) {
		resultList.add(name);
	}
	
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}

	public void setDotLeftObject(Object obj) {
		s2r = obj;
	}

	public Object calculate(Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);

			// �����ǰԪ����������ȡ���һ��Ԫ��
			if (obj instanceof Sequence) {
				if (((Sequence)obj).length() == 0) {
					return null;
				} else {
					obj = ((Sequence)obj).get(1);
				}
			}

			if (obj instanceof Record) {
				Record cur = (Record)obj;
				if (prevDs != cur.dataStruct()) {
					prevDs = cur.dataStruct();
					col = prevDs.getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}
				}

				return cur.getNormalFieldValue(col);
			} else if (obj == null) {
				// ���һ���Ƿ��������ֶΣ���ֹT.f(...)д���������ͳ�ȡf�ĳ�Ա��
				if (s2r instanceof Table) {
					col = ((Table)s2r).dataStruct().getFieldIndex(name);
					if (col < 0) {
						MessageManager mm = EngineMessage.get();
						throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
					}					
				}
				
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}
		} else if (s2r instanceof Record) {
			Record cur = (Record)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			return cur.getNormalFieldValue(col);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}
	}

	// '=' ���ֶν��и�ֵ
	public Object assign(Object value, Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);
			if (obj == null) return value;

			if (!(obj instanceof Record)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}

			Record cur = (Record)obj;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			cur.setNormalFieldValue(col, value);
		} else if (s2r instanceof Record) {
			Record cur = (Record)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			cur.setNormalFieldValue(col, value);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}

		return value;
	}

	// '+=' ���ֶν��и�ֵ
	public Object addAssign(Object value, Context ctx) {
		if (s2r instanceof Sequence) {
			ComputeStack stack = ctx.getComputeStack();
			Object obj = stack.getCurrentValue((Sequence)s2r);
			if (obj == null) return value;

			if (!(obj instanceof Record)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}

			Record cur = (Record)obj;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			Object result = Variant.add(cur.getNormalFieldValue(col), value);
			cur.setNormalFieldValue(col, result);
			return result;
		} else if (s2r instanceof Record) {
			Record cur = (Record)s2r;
			if (prevDs != cur.dataStruct()) {
				prevDs = cur.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}
			
			Object result = Variant.add(cur.getNormalFieldValue(col), value);
			cur.setNormalFieldValue(col, result);
			return result;
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.unknownExpression") + name);
		}
	}
	
	public Object move(Move node, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}

		int pos = node.calculateIndex(current, ctx);
		if (pos < 1) return null;

		Object mem = current.get(pos);
		if (mem == null) return null;
		if (mem instanceof Record) {
			Record r = (Record)mem;
			if (prevDs != r.dataStruct()) {
				prevDs = r.dataStruct();
				col = prevDs.getFieldIndex(name);
				if (col < 0) {
					MessageManager mm = EngineMessage.get();
					throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
				}
			}

			return r.getNormalFieldValue(col);
		} else {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
	}

	public Object moveAssign(Move node, Object value, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}
		
		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}
		
		int pos = node.calculateIndex(current, ctx);
		if (pos < 1) return value;
		
		Object mem = current.get(pos);
		if (mem == null) return value;
		if (!(mem instanceof Record)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
		}
		
		Record r = (Record)mem;
		if (prevDs != r.dataStruct()) {
			prevDs = r.dataStruct();
			col = prevDs.getFieldIndex(name);
			if (col < 0) {
				MessageManager mm = EngineMessage.get();
				throw new RQException(name + mm.getMessage("ds.fieldNotExist"));
			}
		}

		r.setNormalFieldValue(col, value);
		return value;
	}

	public Object moves(Move node, Context ctx) {
		if (!(s2r instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)s2r);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("\"[]\"" + mm.getMessage("engine.seriesNotInStack"));
		}

		int []range = node.calculateIndexRange(current, ctx);
		if (range == null) return new Sequence(0);
		return Move.getFieldValues(current, name, range[0], range[1]);
	}
}
