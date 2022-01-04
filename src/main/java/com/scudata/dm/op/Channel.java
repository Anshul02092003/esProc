package com.scudata.dm.op;

import java.util.ArrayList;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;

/**
 * �ܵ����󣬹ܵ����Ը��Ӷ������㣬��ֻ�ܶ���һ�ֽ��������
 * @author WangXiaoJun
 *
 */
public class Channel implements Operable, IPipe {
	protected Context ctx; // �ö��߳��α�ȡ��ʱ��Ҫ���������Ĳ����½������ʽ
	private ArrayList<Operation> opList; // ���Ӳ����б�
	protected IResult result; // �ܵ����յĽ��������
	
	// ������ʽ�������sum(...)+sum(...)�����Ļ��������groups(...).new(...)�����ڴ�ź����new
	protected New resultNew;
	
	/**
	 * �����ܵ�
	 * @param ctx ����������
	 */
	public Channel(Context ctx) {
		this.ctx = ctx;
	}
	
	/**
	 * ���α깹���ܵ����α�����ݽ����Ƹ��˹ܵ�
	 * @param ctx ����������
	 * @param cs �α�
	 */
	public Channel(Context ctx, ICursor cs) {
		this.ctx = ctx;
		Push push = new Push(this);
		cs.addOperation(push, ctx);
	}
	
	/**
	 * Ϊ�ܵ���������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable addOperation(Operation op, Context ctx) {
		checkResultChannel();
		
		this.ctx = ctx;
		if (opList == null) {
			opList = new ArrayList<Operation>();
		}
		
		opList.add(op);
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
		if (opList != null) {
			for (Operation op : opList) {
				if (seq == null || seq.length() == 0) {
					return;
				}
				
				seq = op.process(seq, ctx);
			}
		}
		
		if (result != null && seq != null) {
			result.push(seq, ctx);
		}
	}
	
	/**
	 * �������ͽ���ʱ���ã���Щ���ӵĲ����Ỻ�����ݣ���Ҫ����finish�������Ĵ���
	 * @param ctx ����������
	 */
	public void finish(Context ctx) {
		if (opList == null) {
			return;
		}
		
		Sequence seq = null;
		for (Operation op : opList) {
			if (seq == null) {
				seq = op.finish(ctx);
			} else {
				seq = op.process(seq, ctx);
				Sequence tmp = op.finish(ctx);
				if (tmp != null) {
					if (seq != null) {
						seq = ICursor.append(seq, tmp);
					} else {
						seq = tmp;
					}
				}
			}
		}
		
		if (result != null && seq != null) {
			result.push(seq, ctx);
		}
	}
	
	/**
	 * ���عܵ��ļ�����
	 * @return
	 */
	public Object result() {
		if (result != null) {
			Object val = result.result();
			result = null;
			if (resultNew != null) {
				if (val instanceof Sequence) {
					return resultNew.process((Sequence)val, ctx);
				} else {
					return val;
				}
			} else {
				return val;
			}
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
		return this;
	}
	
	/**
	 * ȡ�ܵ��Ľ������
	 * @return IResult
	 */
	public IResult getResult() {
		return result;
	}
	
	/**
	 * groups������������ֶβ��ǵ����ľۺϱ������Ҫ��newһ��
	 * @param op new����
	 */
	public void setResultNew(New op) {
		resultNew = op;
	}
	
	/**
	 * ȡ����������
	 * @return Context
	 */
	public Context getContext() {
		return ctx;
	}
}