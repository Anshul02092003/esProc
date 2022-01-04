package com.scudata.expression;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.resources.EngineMessage;

/**
 * ��������Ļ��ܺ����̳д���
 * @author RunQian
 *
 */
abstract public class Gather extends Function {
	/**
	 * �Խڵ����Ż����������ʽ����ɳ���
	 * @param ctx ����������
	 * @param Node �Ż���Ľڵ�
	 */
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}
		
		return this;
	}

	/**
	 * ����������ǰ׼������
	 * @param ctx ����������
	 */
	abstract public void prepare(Context ctx);

	/**
	 * ��������������¼�Ļ���ֵ
	 * @param ctx ����������
	 * @return ����ֵ
	 */
	abstract public Object gather(Context ctx);

	/**
	 * ���㵱ǰ��¼��ֵ�����ܵ�֮ǰ�Ļ��ܽ��oldValue��
	 * @param oldValue ֮ǰ�Ļ��ܽ��
	 * @param ctx ����������
	 * @return ����ֵ
	 */
	abstract public Object gather(Object oldValue, Context ctx);
	
	/**
	 * ȡ���λ��ܶ�Ӧ�ı��ʽ
	 * ���̷߳���ʱ��ÿ���߳����һ���������������Ҫ�ڵ�һ�η��������������η���
	 * @param q �����ֶ����
	 * @return Expression
	 */
	abstract public Expression getRegatherExpression(int q);
	
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
	
	/**
	 * ��Ը��������������ֵ
	 * @param seq ����
	 * @return ����ֵ
	 */
	public Object gather(Sequence seq) {
		MessageManager mm = EngineMessage.get();
		throw new RQException(getFunctionName() + mm.getMessage("engine.unknownGroupsMethod"));
	}
}
