package com.scudata.expression.mfn.channel;

import com.scudata.dm.Context;
import com.scudata.expression.ChannelFunction;

/**
 * Ϊ�ܵ����屣���ܵ���ǰ������Ϊ�����������
 * ch.fetch()
 * @author RunQian
 *
 */
public class Fetch extends ChannelFunction {
	public Object calculate(Context ctx) {
		return channel.fetch();
	}
}