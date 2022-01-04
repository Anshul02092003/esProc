package com.scudata.dm.op;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.cursor.MultipathCursors;
import com.scudata.expression.Expression;
import com.scudata.expression.Node;

/**
 * ��·�ܵ����󣬹ܵ����Ը��Ӷ������㣬��ֻ�ܶ���һ�ֽ��������
 * @author WangXiaoJun
 *
 */
public class MultipathChannel extends Channel {
	private Channel []channels;
	
	/**
	 * �ɶ�·�α깹����·�ܵ�
	 * @param ctx ����������
	 * @param mcs ��·�α�
	 */
	public MultipathChannel(Context ctx, MultipathCursors mcs) {
		super(ctx);
		
		ICursor []cursors = mcs.getCursors();
		int count = cursors.length;
		channels = new Channel[count];
		
		for (int i = 0; i < count; ++i) {
			channels[i] = new Channel(cursors[i].getContext(), cursors[i]);
		}
	}
	
	/**
	 * Ϊ�ܵ���������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable addOperation(Operation op, Context ctx) {
		checkResultChannel();
		for (Channel channel : channels) {
			ctx = channel.getContext();
			channel.addOperation(op.duplicate(ctx), ctx);
		}
		
		return this;
	}
	
	/**
	 * ����Ƿ��Ѿ��н����������
	 */
	protected void checkResultChannel() {
		if (result != null) {
			throw new RQException("���ӽ����֮�����ټ���������������");
		}
	}
	
	/**
	 * ���ܵ��������ݣ����ܻ��ж��Դͬʱ���ܵ���������
	 * @param seq ����
	 * @param ctx ����������
	 */
	public synchronized void push(Sequence seq, Context ctx) {
		if (result != null && seq != null) {
			result.push(seq, ctx);
		}
	}
	
	/**
	 * �������ͽ���ʱ���ã���Щ���ӵĲ����Ỻ�����ݣ���Ҫ����finish�������Ĵ���
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
		for (Channel channel : channels) {
			channel.finish(ctx);
		}
	}
	
	/**
	 * ���عܵ��ļ�����
	 * @return
	 */
	public Object result() {
		if (result instanceof GroupsResult) {
			Table value = null;
			for (Channel channel : channels) {
				GroupsResult groups = (GroupsResult)channel.getResult();
				if (value == null) {
					value = groups.getTempResult();
				} else {
					value.addAll(groups.getTempResult());
				}
			}
			
			GroupsResult gr = (GroupsResult)result;
			result = null;
			if (value == null || gr.isSortedGroup()) {
				return value;
			}
			
			String []names = gr.getNames();
			Expression []calcExps = gr.getCalcExps();
			String []calcNames = gr.getCalcNames();
			String opt = gr.getOption();
			int keyCount = names == null ? 0 : names.length;
			Expression []keyExps = null;
			if (keyCount > 0) {
				keyExps = new Expression[keyCount];
				for (int i = 0, q = 1; i < keyCount; ++i, ++q) {
					keyExps[i] = new Expression(ctx, "#" + q);
				}
			}

			int valCount = calcExps == null ? 0 : calcExps.length;
			Expression []valExps = null;
			if (valCount > 0) {
				valExps = new Expression[valCount];
				for (int i = 0, q = keyCount + 1; i < valCount; ++i, ++q) {
					Node gather = calcExps[i].getHome();
					gather.prepare(ctx);
					valExps[i] = gather.getRegatherExpression(q);
				}
			}

			value = value.groups(keyExps, names, valExps, calcNames, opt, ctx);
			if (resultNew != null) {
				return resultNew.process(value, ctx);
			} else {
				return value;
			}
		} else if (result instanceof TotalResult) {
			TotalResult tr = (TotalResult)result;
			Expression []calcExps = tr.getCalcExps();
			int valCount = calcExps.length;
			int channelCount = channels.length;
			Table value;
			if (valCount == 1) {
				String []fnames = new String[]{"_1"};
				value = new Table(fnames, channelCount);
				for (Channel channel : channels) {
					TotalResult total = (TotalResult)channel.getResult();
					Record r = value.newLast();
					r.setNormalFieldValue(0, total.getTempResult());
				}
			} else {
				String []fnames = new String[valCount];
				for (int i = 1; i < valCount; ++i) {
					fnames[i - 1] = "_" + i;
				}
				
				value = new Table(fnames, channelCount);
				for (Channel channel : channels) {
					TotalResult total = (TotalResult)channel.getResult();
					Sequence seq = (Sequence)total.getTempResult();
					value.newLast(seq.toArray());
				}
			}
			
			Expression []valExps = new Expression[valCount];
			for (int i = 0; i < valCount; ++i) {
				Node gather = calcExps[i].getHome();
				gather.prepare(ctx);
				valExps[i] = gather.getRegatherExpression(i + 1);
			}
			
			TotalResult total = new TotalResult(valExps, ctx);
			total.push(value, ctx);
			return total.result();
		} else if (result != null) {
			Object val = result.result();
			result = null;
			return val;
		} else {
			return null;
		}
	}
	
	/**
	 * �����ܵ���ǰ������Ϊ�����
	 * @return
	 */
	public Channel fetch() {
		checkResultChannel(); 	
		result = new FetchResult();

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}
		
		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��з������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames �����ֶ���
	 * @param opt ѡ��
	 * @return
	 */
	public Channel groups(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt) {
		checkResultChannel();
		result = IGroupsResult.instance(exps, names, calcExps, calcNames, opt, ctx);
		
		for (Channel channel : channels) {
			Context ctx = channel.getContext();
			exps = Operation.dupExpressions(exps, ctx);
			calcExps = Operation.dupExpressions(calcExps, ctx);
			channel.groups(exps, names, calcExps, calcNames, opt);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��л������㲢��Ϊ�����
	 * @param calcExps ���ܱ��ʽ
	 * @return
	 */
	public Channel total(Expression[] calcExps) {
		checkResultChannel();
		result = new TotalResult(calcExps, ctx);
		
		for (Channel channel : channels) {
			Context ctx = channel.getContext();
			calcExps = Operation.dupExpressions(calcExps, ctx);
			channel.total(calcExps);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ������������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param names �����ֶ�������
	 * @param calcExps ���ܱ��ʽ����
	 * @param calcNames �����ֶ���
	 * @param opt ѡ��
	 * @param capacity �ڴ���Դ�ŵķ���������
	 * @return
	 */
	public Channel groupx(Expression[] exps, String[] names,
			   Expression[] calcExps, String[] calcNames, String opt, int capacity) {
		checkResultChannel();
		result = new GroupxResult(exps, names, calcExps, calcNames, opt, ctx, capacity);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ�������������㲢��Ϊ�����
	 * @param exps ������ʽ����
	 * @param capacity �ڴ���Դ�ŵļ�¼����
	 * @param opt ѡ��
	 * @return
	 */
	public Channel sortx(Expression[] exps, int capacity, String opt) {
		checkResultChannel();
		result = new SortxResult(exps, ctx, capacity, opt);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ����������㲢��Ϊ�����
	 * @param fields
	 * @param fileTable
	 * @param keys
	 * @param exps
	 * @param expNames
	 * @param fname
	 * @param ctx
	 * @param option
	 * @param capacity
	 * @return
	 */
	public Channel joinx(Expression [][]fields, Object []fileTable, Expression[][] keys, 
			Expression[][] exps, String[][] expNames, String fname, Context ctx, String option, int capacity) {
		checkResultChannel();
		result = new CsJoinxResult(fields, fileTable, keys, exps, expNames, fname, ctx, option, capacity);

		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ��е������㲢��Ϊ�����
	 * @param exp �������ʽ
	 * @param initVal ��ʼֵ
	 * @param c �������ʽ������������cΪ������ǰ����
	 * @param ctx ����������
	 * @return
	 */
	public Channel iterate(Expression exp, Object initVal, Expression c, Context ctx) {
		checkResultChannel();
		result = new IterateResult(exp, initVal, c, ctx);
		
		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
	
	/**
	 * �Թܵ���ǰ���ݽ���ȥ�����㲢��Ϊ�����
	 * @param exps ȥ�ر��ʽ
	 * @param count
	 * @return
	 */
	public Channel id(Expression []exps, int count) {
		checkResultChannel();
		result = new IDResult(exps, count, ctx);
		
		for (Channel channel : channels) {
			Push push = new Push(this);
			ctx = channel.getContext();
			channel.addOperation(push, ctx);
		}

		return this;
	}
}