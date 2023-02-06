package com.scudata.thread;

import com.scudata.array.IntArray;
import com.scudata.dm.Context;
import com.scudata.dm.GroupsSyncReader;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.dm.op.IGroupsResult;
import com.scudata.expression.Expression;

/**
 * ���α������ִ�з������������
 * @author LW
 *
 */
public class GroupsJob2 extends Job {
	private Sequence data;// ����
	private Object key; // ���ݵ�key (data����ʽʱ��������)
	private IntArray hashValue; // ���ݵ�key��hash (data����ʽʱ��������)
	
	private GroupsSyncReader reader; // �����α�
	
	private Expression[] exps; // �����ֶα��ʽ����
	private String[] names; // �����ֶ�������
	private Expression[] calcExps; // �����ֶα��ʽ����
	private String[] calcNames; // �����ֶ�������
	private String opt; // ѡ��
	private Context ctx; // ����������
	
	private IGroupsResult groupsResult; // �������
	private int capacity; // hash���С
	
	private int hashStart;// ����
	private int hashEnd;// ������
	
	public GroupsJob2(GroupsSyncReader reader, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, String opt, Context ctx, int capacity) {
		this.reader = reader;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
		this.capacity = capacity;
	}
	
	public GroupsJob2(Sequence data, Object key, IntArray hashValue, Expression[] exps, String[] names,
			Expression[] calcExps, String[] calcNames, String opt, Context ctx, int capacity) {
		this.data = data;
		this.key = key;
		this.hashValue = hashValue;
		this.exps = exps;
		this.names = names;
		this.calcExps = calcExps;
		this.calcNames = calcNames;
		this.opt = opt;
		this.ctx = ctx;
		this.capacity = capacity;
	}
	
	/**
	 * ȡ�������
	 * @return IGroupsResult
	 */
	public IGroupsResult getGroupsResult() {
		return groupsResult;
	}

	public void run() {
		if (data != null) {
			groupsResult = data.getGroupsResult(exps, names, calcExps, calcNames, opt, ctx);
			groupsResult.setCapacity(capacity);
			groupsResult.push(data, key, hashValue, hashStart, hashEnd);
			return;
		}
		ICursor cursor = reader.getCursor();
		groupsResult = cursor.getGroupsResult(exps, names, calcExps, calcNames, opt, ctx);
		groupsResult.setCapacity(capacity);
		
		groupsResult.push(reader, hashStart, hashEnd);
	}
	
	public void setHashStart(int hashStart) {
		this.hashStart = hashStart;
	}

	public void setHashEnd(int hashEnd) {
		this.hashEnd = hashEnd;
	}
}
