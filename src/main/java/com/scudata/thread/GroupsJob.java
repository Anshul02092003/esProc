package com.scudata.thread;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.expression.Expression;

/**
 * ���α������ִ�з������������
 * @author WangXiaoJun
 *
 */
public class GroupsJob extends Job {
	private ICursor cursor; // �����α�
	private Sequence seq; // ��������
	
	private Expression[] exps; // �����ֶα��ʽ����
	private String[] names; // �����ֶ�������
	private Expression[] calcExps; // �����ֶα��ʽ����
	private String[] calcNames; // �����ֶ�������
	private String opt; // ѡ��
	private Context ctx; // ����������
	
	private IGroupsResult groupsResult; // �������
	private int groupCount = -1; // @n��ʽ����ʱ�û����õĽ��������
	
	public GroupsJob(ICursor cursor, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, String opt, Context ctx) {
		this.cursor = cursor;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
	}
	
	public GroupsJob(Sequence seq, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, String opt, Context ctx) {
		this.seq = seq;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
	}
	
	/**
	 * ȡ�������
	 * @return IGroupsResult
	 */
	public IGroupsResult getGroupsResult() {
		return groupsResult;
	}
	
	/**
	 * @nѡ��ʹ�ã����õĽ��������
	 * @param groupCount
	 */
	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}

	public void run() {
		if (cursor != null) {
			groupsResult = cursor.getGroupsResult(exps, names, calcExps, calcNames, opt, ctx);
		} else {
			groupsResult = seq.getGroupsResult(exps, names, calcExps, calcNames, opt, ctx);
		}
		
		if (groupCount > 1) {
			groupsResult.setGroupCount(groupCount);
		}
		
		if (seq == null) {
			groupsResult.push(cursor);
		} else {
			groupsResult.push(seq, ctx);
		}
	}
}
