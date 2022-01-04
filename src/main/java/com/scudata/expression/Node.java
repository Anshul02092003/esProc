package com.scudata.expression;

import java.util.List;

import com.scudata.cellset.INormalCell;
import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.ComputeStack;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

/**
 * ���ʽ�ڵ����
 * @author WangXiaoJun
 *
 */
public abstract class Node {
	// ���ȼ��������壬��ԽС���ȼ�Խ��
	public static final byte PRI_CMA = (byte) 1; //���������
	public static final byte PRI_EVL = (byte) 2; //��ֵ
	//public static final byte PRI_ASS = (byte) 3;
	//public static final byte PRI_CON = (byte) 4;
	public static final byte PRI_LINK = (byte) 4;
	public static final byte PRI_OR = (byte) 5;
	public static final byte PRI_AND = (byte) 6;
	public static final byte PRI_BOR = (byte) 7;
	public static final byte PRI_BXOR = (byte) 8;
	public static final byte PRI_BAND = (byte) 9;
	public static final byte PRI_IN = (byte) 10;
	public static final byte PRI_EQ = (byte) 10;
	public static final byte PRI_NEQ = (byte) 10;
	public static final byte PRI_GT = (byte) 11;
	public static final byte PRI_SL = (byte) 11;
	public static final byte PRI_NGT = (byte) 11;
	public static final byte PRI_NSL = (byte) 11;
	//public static final byte PRI_SHIFT = (byte) 12;
	public static final byte PRI_ADD = (byte) 13;
	public static final byte PRI_SUB = (byte) 13;
	public static final byte PRI_MUL = (byte) 14;
	public static final byte PRI_DIV = (byte) 14;
	public static final byte PRI_MOD = (byte) 14;
	//public static final byte PRI_NEW = (byte) 15;
	public static final byte PRI_NOT = (byte) 16;
	//public static final byte PRI_ADR = (byte) 16; //address
	//public static final byte PRI_PRF = (byte) 17;
	public static final byte PRI_NEGT = (byte) 17; //-
	public static final byte PRI_PLUS = (byte) 17; //+
	public static final byte PRI_SUF = (byte) 18; // A1(n), A.fn() r.f
	public static final byte PRI_NUM = (byte) 19; // ��ʶ����������
	public static final byte PRI_BRK = (byte) 20; // ����

	protected int priority; // ���ȼ�

	/**
	 * ����ڵ�
	 */
	public Node() {
		priority = PRI_NUM;
	}

	/**
	 * ��ǰ�ڵ��������ڣ����ڵ�����ȼ��������ŵ����ȼ�
	 * @param inBrackets ���Ų���
	 */
	public void setInBrackets(int inBrackets) {
		this.priority += inBrackets * PRI_BRK;
	}

	/**
	 * ȡ�õ�ǰ�ڵ�����ȼ�
	 * @return ���ȼ�
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * ���ýڵ�����ڵ�
	 * @param node �ڵ�
	 */
	public void setLeft(Node node) {
		if (node != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.logicError"));
		}
	}

	/**
	 * ���ýڵ���Ҳ�ڵ�
	 * @param node �ڵ�
	 */
	public void setRight(Node node) {
		if (node != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException(mm.getMessage("Expression.logicError"));
		}
	}

	/**
	 * ȡ�ڵ�����ڵ㣬û�з��ؿ�
	 * @return Node
	 */
	public Node getLeft() {
		return null;
	}

	/**
	 * ȡ�ڵ���Ҳ�ڵ㣬û�з��ؿ�
	 * @return Node
	 */
	public Node getRight() {
		return null;
	}

	/**
	 * �ѵ������������������Ҳ��Ա����
	 * @param obj ������
	 */
	public void setDotLeftObject(Object obj) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.logicError"));
	}

	/**
	 * �жϵ�ǰ�ڵ��Ƿ������к���
	 * �������������Ҳ�ڵ������к��������ڵ�������������Ҫ����ת������
	 * @return
	 */
	public boolean isSequenceFunction() {
		return false;
	}
	
	/**
	 * �����жϵ����������ĺ����Ƿ��������������ƥ��
	 * @param obj �������
	 * @return true������Ľڵ��������������ƥ�䣬�����Ա���Ա������false����ƥ��
	 */
	public boolean isLeftTypeMatch(Object obj) {
		return true;
	}
	
	/**
	 * ȡ�뵱ǰ��Ա����ͬ������һ��ͬ��������û���򷵻ؿ�
	 * @return ��һ��ͬ���ĳ�Ա����
	 */
	public MemberFunction getNextFunction() {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("Expression.logicError"));
	}

	/**
	 * ����ڵ��ֵ
	 * @param ctx ����������
	 * @return Object
	 */
	public abstract Object calculate(Context ctx);

	/**
	 * ��������õĵ�Ԫ�񣬲���ȡ��Ԫ���ֵ��������ʽ���ǵ�Ԫ�������򷵻ؿ�
	 * @param ctx ����������
	 * @return INormalCell
	 */
	public INormalCell calculateCell(Context ctx) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(mm.getMessage("engine.needCellExp"));
	}

	/**
	 * �����Ƿ����ָ������
	 * @param name ������
	 * @return boolean true��������false��������
	 */
	protected boolean containParam(String name) {
		return false;
	}

	/**
	 * ���ұ��ʽ���õ�����
	 * @param ctx ����������
	 * @param resultList ���ֵ���õ��Ĳ�������ӵ�������
	 */
	protected void getUsedParams(Context ctx, ParamList resultList) {
	}
	
	/**
	 * ���ұ��ʽ�п����õ����ֶΣ�����ȡ�ò�׼ȷ���߰���������
	 * @param ctx ����������
	 * @param resultList ���ֵ���õ����ֶ�������ӵ�������
	 */
	public void getUsedFields(Context ctx, List<String> resultList) {
	}
	
	/**
	 * ���ұ��ʽ���õ���Ԫ��
	 * @param resultList ���ֵ���õ��ĵ�Ԫ�����ӵ�������
	 */
	protected void getUsedCells(List<INormalCell> resultList) {
	}
	
	/**
	 * �жϽڵ��Ƿ���޸����еĳ�Աֵ���˷���Ϊ���Ż�[1,2,3].contain(...)���ֱ��ʽ��
	 * ������в��ᱻ������[1,2,3]���Ա������ɳ������У�������ÿ�μ��㶼����һ������
	 * @return true�����޸ģ�false�������޸�
	 */
	public boolean ifModifySequence() {
		return true;
	}
	
	/**
	 * �Խڵ����Ż�
	 * @param ctx ����������
	 * @param optSequence �Ƿ��Ż��������б���[1,2,3].contain(...)��true���Ż�
	 * @return �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx, boolean optSequence) {
		return optimize(ctx);
	}

	/**
	 * �Խڵ����Ż����������ʽ����ɳ���
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		return this;
	}

	/**
	 * �Ե�ǰ�ڵ���и�ֵ
	 * @param value �Ҳ�ֵ
	 * @param ctx ����������
	 * @return �Ҳ�ֵ
	 */
	public Object assign(Object value, Context ctx) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("\"=\"" + mm.getMessage("assign.needVar"));
	}
	
	/**
	 * �Ե�ǰ�ڵ���+=����
	 * @param value �Ҳ�ֵ
	 * @param ctx ����������
	 * @return Object ������
	 */
	public Object addAssign(Object value, Context ctx) {
		MessageManager mm = EngineMessage.get();
		throw new RQException("\"=\"" + mm.getMessage("assign.needVar"));
	}

	/**
	 * ���ؽڵ�ķ���ֵ����
	 * @param ctx ����������
	 * @return byte ���Ͷ�����Expression��
	 */
	public byte calcExpValueType(Context ctx) {
		return Expression.TYPE_OTHER;
	}
	
	/**
	 * ����ȡƫ�ƣ�����A[-1]��F[-1]����ȡ��һ��������
	 * @param node �Ҳ�Move�ڵ�
	 * @param ctx ����������
	 * @return Object
	 */
	public Object move(Move node, Context ctx) {
		Object obj = calculate(ctx);
		if (!(obj instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)obj);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
		}

		int index = node.calculateIndex(current, ctx);
		return index > 0 ? current.get(index) : null;
	}

	/**
	 * ���ڶ�ƫ�ƶ�����и�ֵ������A[-1]=x��F[-1]=x���ָ�ֵ����
	 * @param node �Ҳ�Move�ڵ�
	 * @param value �Ҳ�ֵ
	 * @param ctx ����������
	 * @return �Ҳ�ֵ
	 */
	public Object moveAssign(Move node, Object value, Context ctx) {
		Object obj = calculate(ctx);
		if (!(obj instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)obj);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
		}

		int index = node.calculateIndex(current, ctx);
		if (index > 0) current.assign(index, value);
		return value;
	}

	/**
	 * ����ȡ��Χƫ�ƣ�����A[-1:1]��F[-1:1]����ȡ��һ������һ��֮���Ԫ�ص�����
	 * @param node �Ҳ�Move�ڵ�
	 * @param ctx ����������
	 * @return Object ���������
	 */
	public Object moves(Move node, Context ctx) {
		Object obj = calculate(ctx);
		if (!(obj instanceof Sequence)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("dot.seriesLeft"));
		}

		ComputeStack stack = ctx.getComputeStack();
		Sequence.Current current = stack.getSequenceCurrent((Sequence)obj);
		if (current == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("[]" + mm.getMessage("engine.seriesNotInStack"));
		}

		// ����������Χ
		int []range = node.calculateIndexRange(current, ctx);
		if (range == null) {
			return new Sequence(0);
		}

		int startSeq = range[0];
		int endSeq = range[1];
		Sequence result = new Sequence(endSeq - startSeq + 1);
		for (; startSeq <= endSeq; ++startSeq) {
			result.add(current.get(startSeq));
		}

		return result;
	}
	
	/**
	 * �жϽڵ��Ƿ���ָ������
	 * @param name ������
	 * @return true����ָ��������false������
	 */
	public boolean isFunction(String name) {
		return false;
	}

	/*--------���º���Ϊ��ʵ��groups��Ļ��ܺ��������ǷǾۺϱ��ʽ--------*/
	
	/**
	 * ����������ǰ׼������
	 * @param ctx ����������
	 */
	public void prepare(Context ctx) {
	}
	
	/**
	 * ��������������¼�Ļ���ֵ
	 * @param ctx ����������
	 * @return ����ֵ
	 */
	public Object gather(Context ctx) {
		return calculate(ctx);
	}
	
	/**
	 * ���㵱ǰ��¼��ֵ�����ܵ�֮ǰ�Ļ��ܽ��oldValue��
	 * @param oldValue ֮ǰ�Ļ��ܽ��
	 * @param ctx ����������
	 * @return ����ֵ
	 */
	public Object gather(Object oldValue, Context ctx) {
		return oldValue;
	}
	
	/**
	 * ȡ���λ��ܶ�Ӧ�ı��ʽ
	 * ���̷߳���ʱ��ÿ���߳����һ���������������Ҫ�ڵ�һ�η��������������η���
	 * @param q �����ֶ����
	 * @return Expression
	 */
	public Expression getRegatherExpression(int q) {
		String str = "#" + q;
		return new Expression(str);
	}
	
	/**
	 * ��һ���������ʱ�Ƿ���Ҫ����finish1�Ի���ֵ�����״δ���top��Ҫ����
	 * @return true����Ҫ��false������Ҫ
	 */
	public boolean needFinish1() {
		return false;
	}
	
	/**
	 * �Ե�һ�η���õ��Ļ���ֵ�����״δ���������ֵ��Ҫ�μӶ��η�������
	 * @param val ����ֵ
	 * @return �����Ļ���ֵ
	 */
	public Object finish1(Object val) {
		return val;
	}
	
	/**
	 * �Ƿ���Ҫ�����ջ���ֵ���д���
	 * @return true����Ҫ��false������Ҫ
	 */
	public boolean needFinish() {
		return false;
	}
	
	/**
	 * �Է�������õ��Ļ���ֵ�������մ�����ƽ��ֵ��Ҫ��sum/count����
	 * @param val ����ֵ
	 * @return �����Ļ���ֵ
	 */
	public Object finish(Object val) {
		return val;
	}
}
