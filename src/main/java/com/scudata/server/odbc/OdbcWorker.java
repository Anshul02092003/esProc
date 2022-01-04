package com.scudata.server.odbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.esproc.jdbc.JDBCUtil;
import com.scudata.cellset.datamodel.PgmCellSet;
import com.scudata.common.Logger;
import com.scudata.common.SegmentSet;
import com.scudata.common.StringUtils;
import com.scudata.dm.Context;
import com.scudata.dm.ParamList;
import com.scudata.dm.Sequence;
import com.scudata.dm.Table;
import com.scudata.server.ConnectionProxyManager;
import com.scudata.util.CellSetUtil;

/**
 * ODBC���������߳�
 * @author Joancy
 *
 */
class OdbcWorker extends Thread {
	static final int Buffer_Size = 1024 * 64; // ��������С
	
	private Socket sckt;
	private volatile boolean stop = false;
	OutputStream out;
	InputStream in;

	/**
	 * ����һ����������
	 * @param tg �߳���
	 * @param name ����
	 */
	public OdbcWorker(ThreadGroup tg, String name) {
		super(tg, name);
	}

	/**
	 * ����ͨѶ�׽���
	 * @param socket �׽���
	 * @throws Exception
	 */
	public void setSocket(Socket socket) throws Exception{
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
			socket.setReceiveBufferSize(Buffer_Size);
			socket.setSendBufferSize(Buffer_Size);
			socket.setSoLinger(true, 1);
			socket.setReuseAddress(true);
			socket.setSoTimeout(3000);
			this.sckt = socket;
			holdBufferStream();
	}
	
	private void holdBufferStream() throws IOException{
		OutputStream os = sckt.getOutputStream();
		out = new BufferedOutputStream(os);
		out.flush();
		
		InputStream is = sckt.getInputStream();
		in = new BufferedInputStream(is);
	}

	private void writeOdbcResponse(OutputStream os, int code, String returnMsg)
			throws Exception {
		DataTypes.writeInt(os, code);
		if (returnMsg == null)
			return;
		if (code < 0) {
			DataTypes.writeString(os, returnMsg);
		}
	}

	private ConnectionProxy getConnectionProxy(ConnectionProxyManager cpm,int connId) throws Exception{
		return (ConnectionProxy)cpm.getConnectionProxy(connId);
	}

	/*
	 * Э�� int:high byte first char:unicode big endian ����ֵ�� ��ȷʱ��4byte(>=0
	 * ��Ӧ����Ĵ����)+[������Ϣ] ����ʱ��4byte(-1)+4byte(���󳤶�)+[������Ϣ]
	 */
	private boolean serveODBC(int reqType, InputStream is, OutputStream os) {
		try {
			OdbcServer server = OdbcServer.getInstance();
			OdbcContext context = server.getContext();
			ConnectionProxyManager cpm = ConnectionProxyManager.getInstance();
			switch (reqType) {
			// 1000��odbclogin:
			// 4byte(1000)+4byte(user����) + [user] + 4byte(password����) +
			// [password]
			// return: ��ȷʱ��4byte(���Ӻ�)
			// ����ʱ��4byte(-1)+4byte(���󳤶�)+[������Ϣ]
			case 1000:
				String user = DataTypes.readString(is);
				String password = DataTypes.readString(is);
				if (!context.isUserExist(user)) {
					writeOdbcResponse(os, -1, "Login error: invalid user "
							+ user);
				} else {
					try {
						boolean success = context.checkUser(user, password);
						if (success) {
							int connId = OdbcServer.nextId();
							ConnectionProxy connProxy = new ConnectionProxy(
									cpm, connId, user);
							cpm.addProxy(connProxy);
							writeOdbcResponse(os, connId,
									"Login OK, current odbc user: " + user);
						}
					} catch (Exception x) {
						writeOdbcResponse(os, -1, x.getMessage());
					}
				}
				break;
			// * 1001��prepare statement dfx or dql:
			// * 4byte(1001) + 4byte(���Ӻ�) + 4byte(dfx����) + [dfx] +[ArgRowData]
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte( Statement�� )
			case 1001:
				int connId = DataTypes.readInt(is);
				String dfx = DataTypes.readString(is);
				Object[] args = DataTypes.readRowData(is);
				ArrayList argList = new ArrayList();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						argList.add(args[i]);
					}
				}
				ConnectionProxy connProxy = getConnectionProxy(cpm,connId);
				int stateId = OdbcServer.nextId();
				StatementProxy sp = new StatementProxy(connProxy, stateId, dfx,
						argList);
				connProxy.addProxy(sp);
				writeOdbcResponse(os, stateId, null);
				break;
			// * 1002��execute statement dfx or dql:
			// * 4byte(1002) + 4byte(���Ӻ�) + 4byte(statement��)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte( ��������� )+ 4byte( ������� )...
			case 1002:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				int[] resultIds = sp.execute();
				DataTypes.writeInt(os, resultIds.length);
				for (int i = 0; i < resultIds.length; i++) {
					DataTypes.writeInt(os, resultIds[i]);
				}
				break;
			// * 1003��cancel execute dfx:
			// * 4byte(1003) + 4byte(���Ӻ�) + 4byte(statement��)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte( 0 )
			case 1003:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				sp.cancel();
				DataTypes.writeInt(os, 0);
				break;
			// * 1010����ȡ������ṹ
			// * 4byte(1010) + 4byte(���Ӻ�) + 4byte(statement��) + 4byte(�������ʶ��)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte(�ֶ���Ŀ)+4byte(�ֶ�1����)+[�ֶ�1��Ϣ]...
			// *
			case 1010:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				int resultId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				ResultSetProxy rsp = sp.getResultSetProxy(resultId);
				String[] columns = rsp.getColumns();
				if (columns == null) {
					DataTypes.writeInt(os, 0);
					return true;
				}
				// �ɹ���־
				int size = columns.length;
				DataTypes.writeInt(os, size);
				for (int i = 0; i < size; i++) {
					DataTypes.writeString(os, columns[i]);
				}
				break;
			// * 1011�������ȡ��
			// * 4byte(1011) + 4byte(���Ӻ�)+4byte(statement��)+4byte(�������ʶ��) +
			// 4byte(fetchSize)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte(0)+[���ݱ�]
			// *
			case 1011:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				resultId = DataTypes.readInt(is);
				int n = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				rsp = sp.getResultSetProxy(resultId);
				Sequence data = rsp.fetch(n);
				DataTypes.checkTable(data);
				
				// �ɹ���־
				DataTypes.writeInt(os, 0);
				DataTypes.writeTable(os, data);
				break;
//				����sqlfirst�����õ�cmd=1012(���ĳ�����Ҳ��)
//						����Ϊstring(��:a=1;b=2),ÿ������Ϊk=v��ʽ���÷ֺŸ�������Ҫ�ǿ��ǽ��������ø�������ԡ�
//						��ǰsqlfirst�������ó�sqlfirst=simple��sqlfirst=plus���ݸ�server.
				// * 1012����������
				// * 4byte(1012) + 4byte(�ֽڴ�����)+[�ֽڴ�]
				// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
				// * ���óɹ�, 4byte(0)
				// *
			case 1012://����sql+
				String properties = DataTypes.readString(is);
				// �ɹ���־
				DataTypes.writeInt(os, 0);
				break;
			// * 1018���ر�Statement
			// * 4byte(1018) + 4byte(���Ӻ�)+4byte(statement��)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte(0)
			// *
			case 1018:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				sp.destroy();
				writeOdbcResponse(os, 0, "Statement:" + stateId + " is closed.");
				break;
			// * 1020���رս����
			// * 4byte(1020) + 4byte(���Ӻ�)+4byte(statement��)+4byte(�������ʶ��)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte(0)
			// *
			case 1020:
				connId = DataTypes.readInt(is);
				stateId = DataTypes.readInt(is);
				resultId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				sp = connProxy.getStatementProxy(stateId);
				rsp = sp.getResultSetProxy(resultId);
				rsp.destroy();
				writeOdbcResponse(os, 0, "ResultSet:" + resultId
						+ " is closed.");
				break;
//				 * 1050���г��洢���̣�����ͨ���������dfx����
//				 * 4byte(1050) + 4byte(���Ӻ�) + 4byte(����������) + [������]
//				 * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
//				 *         ��ȷʱ, 4byte(0)+[�̶���ʽ�洢�����б���Ϣ��]
//				 *
			case 1050:
				connId = DataTypes.readInt(is);
				String filter = DataTypes.readString(is);
				Map<String,String> m = com.esproc.jdbc.Server.getSplList(filter);
				String spCols = "PROCEDURE_CAT,PROCEDURE_SCHEM,PROCEDURE_NAME,NUM_INPUT_PARAMS,NUM_OUTPUT_PARAMS,NUM_RESULT_SETS,REMARKS,PROCEDURE_TYPE";
				StringTokenizer st = new StringTokenizer(spCols, ",");
				ArrayList<String> cols = new ArrayList<String>();
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					cols.add(name);
				}

				Table storeInfos = new Table(StringUtils.toStringArray(cols));
				Iterator<String> files = m.keySet().iterator();
				while (files.hasNext()){
					String path = files.next();
					String dfxName = m.get(path);
					int paramCount = getParamCount(path);
					storeInfos.newLast(new Object[]{"","",dfxName,paramCount,-1,-1,"",2});
				}
				// �ɹ���־
				DataTypes.writeInt(os, 0);
				DataTypes.writeTable(os, storeInfos);
				break;
//				 * 1051����ȡ�洢������ϸ��Ϣ��
//				 * 4byte(1051) + 4byte(�洢����������) + [����]
//				 * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
//				 *         ��ȷʱ, 4byte(0)+[�̶���ʽ�洢������Ϣ��]
//				 *
				
				
//				 * 1060���г�����Ϣ��
//				 * 4byte(1060) + 4byte(���Ӻ�) + 4byte(�����Ƴ���) + [������]
//				 * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
//				 *         ��ȷʱ, 4byte(�ֶ���Ŀ)+4byte(�ֶ�1����)+[�ֶ�1��Ϣ]...+[����Ϣ���ݱ�]
			case 1060:
				connId = DataTypes.readInt(is);
				String tableName = DataTypes.readString(is);
				connProxy = getConnectionProxy(cpm,connId);
				Table table = JDBCUtil.getTables(tableName);
				DataTypes.writeDatastructAndData(os, table);
				break;
				
//				 * 1061���г��ֶ���Ϣ��
//				 * 4byte(1061) + 4byte(���Ӻ�) + 4byte(�����Ƴ���) + [������]+ 4byte(�ֶγ���) + [�ֶ�]
//				 * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
//				 *         ��ȷʱ, 4byte(�ֶ���Ŀ)+4byte(�ֶ�1����)+[�ֶ�1��Ϣ]...+[����Ϣ���ݱ�]
			case 1061:
				connId = DataTypes.readInt(is);
				tableName = DataTypes.readString(is);
				String columnName = DataTypes.readString(is);
				connProxy = getConnectionProxy(cpm,connId);
				table = JDBCUtil.getColumns(tableName, columnName, new Context());
				DataTypes.writeDatastructAndData(os, table);
				break;
				
			// * 1111���ر�����
			// * 4byte(1111) + 4byte(���Ӻ�)
			// * return: ����ʱ, 4byte(-1:�������)+4byte(���󳤶�)+[������Ϣ]
			// * ��ȷʱ, 4byte(0)
			// *
			case 1111:
				connId = DataTypes.readInt(is);
				connProxy = getConnectionProxy(cpm,connId);
				writeOdbcResponse(os, 0, null);
				os.flush();
				connProxy.destroy();
				return false;
				// �ر�Socket
			case 2222:
				return false;
			}
		} catch (Throwable x) {
			String msg = x.getMessage();
			while(msg==null && x.getCause()!=null){
				 x = x.getCause();
				 msg = x.getMessage();
			}
			try {
				writeOdbcResponse(os, -1, msg);
			} catch (Exception e) {
			}
			x.printStackTrace();
			Logger.debug("Service exception:"+msg);
		}
		return true;
	}
	
	private int getParamCount(String dfx) throws Exception{
		int c = -1;
		FileInputStream in=null;
		try{
			in = new FileInputStream(dfx);
			PgmCellSet cs = CellSetUtil.readPgmCellSet(in);
			ParamList pl = cs.getParamList();
			if(pl!=null){
				c = pl.count();
			}
		}finally{
			if(in!=null) in.close();
		}
		return c;
	}

	public void shutDown() {
		stop = true;
	}

	/**
	 * ���й�������
	 */
	public void run() {
		try {
			InputStream is = in;
			OutputStream os = out;
			while (!stop) {
				int reqType = 0;
				try{
					reqType = DataTypes.readInt(is);
				}catch (java.net.SocketTimeoutException e) {
					continue;
				}

				if (reqType == -1) {
					// �رշ�����
					OdbcServer.getInstance().shutDown();
					return;
				}

				// �رշ����߳�
				if (reqType == -2) {
					break;
				}

				if (reqType > 0) {
					if (serveODBC(reqType, is, os)) {
						os.flush();
						continue;
					}
					break;
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			in.close();
			out.close();
			sckt.close();
		} catch (IOException e) {
		}
	}
}
