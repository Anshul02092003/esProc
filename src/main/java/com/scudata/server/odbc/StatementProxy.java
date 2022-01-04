package com.scudata.server.odbc;

import java.util.ArrayList;
import java.util.List;

import com.scudata.app.common.AppUtil;
import com.scudata.common.ArgumentTokenizer;
import com.scudata.common.Escape;
import com.scudata.common.Logger;
import com.scudata.common.StringUtils;
import com.scudata.common.UUID;
import com.scudata.dm.Context;
import com.scudata.dm.Sequence;
import com.scudata.dm.cursor.ICursor;
import com.scudata.expression.fn.Eval;
import com.scudata.parallel.Response;
import com.scudata.parallel.Task;
import com.scudata.server.IProxy;
import com.scudata.server.unit.UnitServer;
import com.scudata.util.DatabaseUtil;

/**
 * Statement������
 * 
 * @author Joancy
 *
 */
public class StatementProxy extends IProxy {
	String cmd = null;
	ArrayList params = null;
	String dfx = null;
	List args = null;
	Task task = null;

	/**
	 * ����Statement������
	 * @param cp ���Ӵ���
	 * @param id ������
	 * @param cmd ��ѯ����
	 * @param params ��������
	 * @throws Exception
	 */
	public StatementProxy(ConnectionProxy cp, int id, String cmd,
			ArrayList<Object> params) throws Exception {
		super(cp, id);
		this.cmd = cmd;
		if (!StringUtils.isValidString(cmd)) {
			throw new Exception("Prepare statement cmd is empty!");
		}
		Logger.debug("StatementProxy cmd:\r\n"+cmd);
		this.params = params;
		if (isDfx()) {
			standardizeDfx();
			String spaceId = UUID.randomUUID().toString();
			task = new Task(dfx, args, id, spaceId);
		} else {//������ʽ
		}
		access();
	}

	/**
	 * ��ȡ���Ӵ�����
	 * @return ���Ӵ�����
	 */
	public ConnectionProxy getConnectionProxy() {
		return (ConnectionProxy) getParent();
	}

	/**
	 * ��ȡ����,������call dfx: {call("a",2,?)}
	 * @return Ҫִ�еĲ�ѯ����
	 */
	public String getCmd() {
		return cmd;
	}

	private boolean isDfx() {
		cmd = cmd.trim();
		if (cmd.startsWith("{") && cmd.endsWith("}"))
			cmd = cmd.substring(1, cmd.length() - 1);
		
		String lower = cmd.toLowerCase();
		return lower.startsWith("call ");
	}

	private void standardizeDfx() {
		if (!isDfx())
			return;
		String tmp = cmd;
		
		int left = tmp.indexOf('(');
		if (left == -1)
			throw new RuntimeException(cmd +" must contain '()'");
		
		if (!tmp.endsWith(")"))
			throw new RuntimeException(cmd+" must end with ')'");

		String name = tmp.substring(5, left).trim();
		
		dfx = standardizeDfx(name);
		String strparams = tmp.substring(left+1,tmp.length()-1);
		args = standardizeArg(strparams);
	}

	private String standardizeDfx(String dfxName) {
		if (!dfxName.toLowerCase().endsWith(".dfx"))
			dfxName += ".dfx";
		return dfxName;
	}
	
//���ݵ���˫����
	private static String adjustQuote(String args){
		ArgumentTokenizer at1 = new ArgumentTokenizer( args );
		StringBuffer buf = new StringBuffer();
		while(at1.hasMoreTokens()){
			String arg = at1.nextToken();
			String tmp = Escape.removeEscAndQuote(arg);
			if(tmp.equals(arg)){
				arg = tmp;
			}else{
				arg = Escape.addEscAndQuote(tmp);
			}
			if(buf.length()>0){
				buf.append(",");
			}
			buf.append(arg);
		}
		return buf.toString();
	}
	
	private List standardizeArg(String strParams) {
		if(params==null) throw new RuntimeException("You didn't bind any parameter for Call dfx()!");
		
		String exp = "["+adjustQuote(strParams)+"]";
		Context ctx = new Context();
		Sequence arg = new Sequence();
		for(Object o:params){
			arg.add(o);
		}
		Sequence o = (Sequence)Eval.calc(exp, arg, null, ctx);
		List cmpArgs = new ArrayList<Object>();
		int len = o.length();
		for(int i=1;i<=len; i++){
			cmpArgs.add(o.get(i));
		}
		return cmpArgs;
	}

	/**
	 * ȡ����ֵ
	 * @return �����б�
	 */
	public List<String> getParams() {
		return params;
	}

	/**
	 * ��ȡ���������
	 * @param id ������
	 * @return �����������
	 * @throws Exception
	 */
	public ResultSetProxy getResultSetProxy(int id) throws Exception{
		ResultSetProxy rsp = (ResultSetProxy)getProxy(id);
		if(rsp==null){
			throw new Exception("ResultSet "+id+" is not exist or out of time!");
		}
		
		return rsp;
	}

	/**
	 * ִ�е�ǰ����
	 * @return ������Ĵ��������
	 * @throws Exception
	 */
	public int[] execute() throws Exception {
		int[] resultIds;
		if (task != null) {
			ICursor[] cursors = task.executeOdbc();
			int size = cursors.length;
			resultIds = new int[size];
			for (int i = 0; i < size; i++) {
				ICursor cursor = cursors[i];
				int resultId = OdbcServer.nextId();
				resultIds[i] = resultId;
				ResultSetProxy rsp = new ResultSetProxy(this, resultId, cursor);
				addProxy(rsp);
			}
		} else {
			Context context = Task.prepareEnv();

			try{
				ICursor cursor;
				Object obj;
				if(params!=null && params.size()>0){
					List<Object> args = new ArrayList<Object>();
					for(Object arg:params){
						args.add(arg);
					}
					obj = AppUtil.executeSql(cmd, args, context);
				}else{
					obj = AppUtil.executeCmd(cmd, context);
				}
				
				if(obj instanceof ICursor){
					cursor = (ICursor)obj;
				}else{
					cursor = Task.toCursor( obj );
				}
				
				resultIds = new int[1];
				int resultId = OdbcServer.nextId();
				resultIds[0] = resultId;
				ResultSetProxy rsp = new ResultSetProxy(this, resultId, cursor);
				addProxy(rsp);
			}finally{
				DatabaseUtil.closeAutoDBs(context);
			}
		}
		return resultIds;
	}

	/**
	 * ȡ����ǰ����
	 * ��dfx��������dfx.interrupt()������ɶҲ����
	 * @return ȡ���ɹ�������true
	 * @throws Exception
	 */
	public boolean cancel() throws Exception {
		Response res = task.cancel();
		if(res.getException()!=null){
			throw res.getException();
		}
		return true;
	}

	/**
	 * ȡ��ʼ����ʱ�䣬δ��ʼ����Ч���ж��򷵻�-1
	 * @return ��ʼ�����ʱ�䣬������ʾ
	 */
	public long getStartTime() {
		return task.getCallTime();
	}

	/**
	 * ȡ�������ʱ�䣬δ��ɻ���Ч���ж��򷵻�-1
	 * @return ����ʱ��
	 */
	public long getEndTime() {
		return task.getFinishTime();
	}

	// closeʱ�ر�����ResultSetProxy������ConnectionProxyɾ���Լ�

	/**
	 * ���������ص��������
	 * @param max
	 */
	public void setMaxRows(int max) {
	}

	/**
	 * �Ƿ�����һ�������
	 * @return false
	 */
	public boolean hasNextResultSet() {
		return false;
	}

	/**
	 * ��һ�������dfx��dql���صķ�ICursor��Ҫ��װ��ICursor
	 * ������ͨ�����ȷ�װ������
	 * �������з�װ��MemoryCursor
	 * ��ǰ̨����ʱ������ͨ���й��ɵ��α��򷵻�����Ϊ_1�Ľ����
	 * @return null
	 */
	public ICursor nextResultSet() {
		return null;
	}

	/**
	 * �رյ�ǰ����
	 */
	public void close() {
	}

	/**
	 * ʵ��toString����
	 */
	public String toString() {
		return "Statement " + getId();
	}

}