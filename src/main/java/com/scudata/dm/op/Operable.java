package com.scudata.dm.op;

import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.Expression;
import com.scudata.expression.Function;

/**
 * ���Ը�������Ľӿ�
 * @author WangXiaoJun
 *
 */
public abstract class Operable {
	/**
	 * ��������
	 * @param op ����
	 * @param ctx ����������
	 * @return Operable
	 */
	public abstract Operable addOperation(Operation op, Context ctx);
	
	/**
	 * ����
	 * @param function ��Ӧ�ĺ���
	 * @param fltExp ��������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable select(Function function, Expression fltExp, String opt, Context ctx) {
		Select op = new Select(function, fltExp, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ����
	 * @param function ��Ӧ�ĺ���
	 * @param fltExp ��������
	 * @param opt ѡ��
	 * @param pipe ���ڴ������������ĳ�Ա
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable select(Function function, Expression fltExp, String opt, IPipe pipe, Context ctx) {
		Select op = new Select(function, fltExp, opt, pipe);
		return addOperation(op, ctx);
	}
	
	/**
	 * �������ӹ��ˣ������ܹ����ϵ�
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable filterJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps, String opt, Context ctx) {
		FilterJoin op = new FilterJoin(function, exps, codes, dataExps, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * �������ӹ��ˣ������ܹ������ϵ�
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable diffJoin(Function function, Expression[][] exps, Sequence[] codes, Expression[][] dataExps, String opt, Context ctx) {
		DiffJoin op = new DiffJoin(function, exps, codes, dataExps, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ������
	 * @param function ��Ӧ�ĺ���
	 * @param fname
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable join(Function function, String fname, Expression[][] exps, Sequence[] codes,
			  Expression[][] dataExps, Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		Join op = new Join(function, fname, exps, codes, dataExps, newExps, newNames, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ��Զ�̱�������
	 * @param function ��Ӧ�ĺ���
	 * @param fname
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param codes ά������
	 * @param dataExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable joinRemote(Function function, String fname, Expression[][] exps, 
			Object[] codes, Expression[][] dataExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		JoinRemote op = new JoinRemote(function, fname, exps, codes, dataExps, newExps, newNames, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * �����ʽ����
	 * @param function
	 * @param dimExps ���ӱ��ʽ����
	 * @param aliasNames ά���¼����
	 * @param newExps �²����ֶα��ʽ����
	 * @param newNames �²����ֶ�������
	 * @param opt ѡ�i����������
	 * @param ctx
	 * @return
	 */
	public Operable fjoin(Function function, Expression[] dimExps, String []aliasNames, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		ForeignJoin op = new ForeignJoin(function, dimExps, aliasNames, newExps, newNames, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * �α갴��������������
	 * @param function
	 * @param srcKeyExps ���ӱ��ʽ����
	 * @param srcNewExps
	 * @param srcNewNames
	 * @param cursors �����α�����
	 * @param options ����ѡ��
	 * @param keyExps ���ӱ��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt
	 * @param ctx
	 * @return
	 */
	public Operable pjoin(Function function, Expression []srcKeyExps, Expression []srcNewExps, String []srcNewNames, 
			ICursor []cursors, String []options, Expression [][]keyExps, 
			Expression [][]newExps, String [][]newNames, String opt, Context ctx) {
		// ����������Ϣ
		if (this instanceof ICursor) {
			ICursor cs = (ICursor)this;
			cs.setSkipBlock(srcKeyExps, cursors, options, keyExps, newExps, opt);
		}
		
		PrimaryJoin op = new PrimaryJoin(function, srcKeyExps, srcNewExps, srcNewNames, 
				cursors, options, keyExps, newExps, newNames, opt, ctx);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���α�������鲢����
	 * @param function ��Ӧ�ĺ���
	 * @param exps ��ǰ������ֶα��ʽ����
	 * @param cursors ά���α�����
	 * @param codeExps ά������ֶα��ʽ����
	 * @param newExps
	 * @param newNames
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable mergeJoinx(Function function, Expression[][] exps, 
			ICursor []cursors, Expression[][] codeExps, 
			Expression[][] newExps, String[][] newNames, String opt, Context ctx) {
		throw new RuntimeException();
	}
	
	/**
	 * ��Ӽ�����
	 * @param function ��Ӧ�ĺ���
	 * @param exps ������ʽ����
	 * @param names �ֶ�������
	 * @param opt ѡ��
	 * @param level
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable derive(Function function, Expression []exps, String []names, String opt, int level, Context ctx) {
		Derive op = new Derive(function, exps, names, opt, level);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���������
	 * @param function ��Ӧ�ĺ���
	 * @param newExps ������ʽ����
	 * @param names �ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable newTable(Function function, Expression []newExps, String []names, String opt, Context ctx) {
		New op = new New(function, newExps, names, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ������ʽ����
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression []exps, String opt, Context ctx) {
		Group op = new Group(function, exps, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param sortExps ��벿������ķ����ֶα��ʽ
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression []exps, Expression []sortExps, String opt, Context ctx) {
		Group op = new Group(function, exps, sortExps, opt);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps �����ֶα��ʽ����
	 * @param names �����ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression[] exps, String []names, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		Groups op = new Groups(function, exps, names, newExps, newNames, opt, ctx);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���������������
	 * @param function ��Ӧ�ĺ���
	 * @param exps ǰ�벿������ķ����ֶα��ʽ
	 * @param names �ֶ�������
	 * @param sortExps ��벿������ķ����ֶα��ʽ
	 * @param sortNames �ֶ�������
	 * @param newExps ���ܱ��ʽ
	 * @param newNames �����ֶ�������
	 * @param opt ѡ��
	 * @param ctx ����������
	 * @return Operable
	 */
	public Operable group(Function function, Expression[] exps, String []names, 
			Expression[] sortExps, String []sortNames, 
			Expression[] newExps, String []newNames, String opt, Context ctx) {
		Groups op = new Groups(function, exps, names, sortExps, sortNames, newExps, newNames, opt, ctx);
		return addOperation(op, ctx);
	}
	
	/**
	 * ���Ӽ���
	 * @param function �����ĺ�������
	 * @param fkNames ����ֶ�������
	 * @param timeFkNames ʱ�����������
	 * @param codes ά������
	 * @param exps ά����������
	 * @param timeExps ά���ʱ����¼�����
	 * @param opt ѡ��
	 */
	public Operable switchFk(Function function, String[] fkNames, String[] timeFkNames, Sequence[] codes, Expression[] exps, Expression[] timeExps, String opt, Context ctx) {
		Switch op = new Switch(function, fkNames, timeFkNames, codes, exps, timeExps, opt);
		return addOperation(op, ctx);
	}
}