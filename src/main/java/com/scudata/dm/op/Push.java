package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.op.Channel;
import com.scudata.expression.Function;

/**
 * �α��ܵ��ĸ��ӵ����͵�ǰ���ݵ�ָ���ܵ����㴦����
 * cs.push(ch)
 * @author RunQian
 *
 */
public class Push extends Operation {
	private Channel channel;

	public Push(Channel channel) {
		this(null, channel);
	}
	
	public Push(Function function, Channel channel) {
		super(function);
		this.channel = channel;
	}
	
	/**
	 * ����ȫ���������ʱ���ã�group������Ҫ֪�����ݽ�����ȷ�����һ�������
	 * @param ctx ����������
	 * @return ���ӵĲ������������
	 */
	public Sequence finish(Context ctx) {
		channel.finish(ctx);
		return null;
	}
	
	/**
	 * �����������ڶ��̼߳��㣬��Ϊ���ʽ���ܶ��̼߳���
	 * @param ctx ����������
	 * @return Operation
	 */
	public Operation duplicate(Context ctx) {
		return new Push(function, channel);
	}
	
	/**
	 * �����α��ܵ���ǰ���͵�����
	 * @param seq ����
	 * @param ctx ����������
	 * @return
	 */
	public Sequence process(Sequence seq, Context ctx) {
		channel.push(seq, ctx);
		return seq;
	}
}
