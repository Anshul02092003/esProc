package com.scudata.expression.fn;

import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.expression.*;
import com.scudata.resources.EngineMessage;

/**
 * ���ɻ������� canvas()
 * �������ж��廭����ֱ���ڵ�Ԫ����ʹ��canvas()������
 * �����Ļ�ͼ�����п���ֱ���õ�Ԫ�����Ƶ��û��������趨��ͼ�������߻�ͼ��
 * @author runqian
 *
 */
public class CreateCanvas extends Function {

	public Object calculate(Context ctx) {
		if (param != null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("canvas" + mm.getMessage("function.invalidParam"));
		}

		return new Canvas();
	}

	public Node optimize(Context ctx) {
		return this;
	}
}
