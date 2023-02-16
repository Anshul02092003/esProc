package com.scudata.lib.mongo.function;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.scudata.array.IArray;
import com.scudata.common.*;
import com.scudata.dm.*;
import com.scudata.dm.cursor.ICursor;

//��ȡ����ʱ��ȡdb
public class ImThreadCursor extends ICursor {
	private String m_cmd;
	private String m_dbName;
	
	private MongoDatabase m_db;
	private MongoClient m_client;
	private String[] m_cols;
	private Table m_bufTable;
	private long cursorId = 1; //��Ϊ0�����������ˡ�
	private static Object m_lock = new Object();
	
	public ImThreadCursor(MongoClient client, String dbName, String cmd, Context ctx) {
		m_client = client;
		this.ctx = ctx;
		m_cmd = cmd;
		m_dbName = dbName;
		m_db = null;
		if (ctx != null) {
			ctx.addResource(this);
		}		
	}
	
	@Override
	protected long skipOver(long n) {
		if ( n == 0 ){
			return 0;
		}

		if (m_db == null){
			Document docs = null;
			
			m_db = m_client.getDatabase(m_dbName);
			Document command = Document.parse(m_cmd);
			synchronized (m_lock){
				docs = m_db.runCommand(command);
			}
			double dVal = docs.getDouble("ok");
			if (dVal==0){
				System.out.println("no data");
				return 0;
			}
		
			doCursorData(m_db, docs);
		}
		
		long fetchSize = n;
		long count = 0;
		//1���л������
		if (m_bufTable!=null){
			if (m_bufTable.length()>fetchSize){ //1.1�����㹻����
				for(int i=(int)fetchSize; i>0; i--){
					m_bufTable.delete(i);
				}
				return fetchSize;
			}else{			//1.2. ����������	
				fetchSize -= m_bufTable.length();
				count=m_bufTable.length();
				m_bufTable.clear();		
				m_bufTable = null;				
			}
		}
		//2.��ѯ������
		int cur = 0;
		String cmd = null;
	
		while (m_cmd!=null && fetchSize>0 && cursorId>0) {
			cmd = m_cmd.replace("batchSize:101", "batchSize:"+(fetchSize>1024?1024:fetchSize));
			//System.out.println(Thread.currentThread().getId()+";cmd="+cmd);
			cur = runCommandSkip(m_db, cmd);
			fetchSize -= cur;
			count+=cur;
		}
		
		return count;
	}

	//�������getMore�ر�cursor�����������ǶϿ�����.
	public synchronized void close() {
		m_cols = null;
		m_db = null;
		m_client = null;
		m_bufTable = null;
		super.close();
		
		if (ctx != null) {
			ctx.removeResource(this);	
		}			
		cursorId = 0;
	}
	
	protected  Sequence get(int n) {		
		Table vTbl = null;
		Table[] vTables = new Table[1];
		
		if (m_db == null){
			Document docs = null;
			synchronized (m_lock)
			{
				m_db = m_client.getDatabase(m_dbName);
				Document command = Document.parse(m_cmd);
				docs = m_db.runCommand(command);
			}
			double dVal = docs.getDouble("ok");
			if (dVal==0){
				System.out.println("no data");
				return null;
			}
			
			doCursorData(m_db, docs);
		}
		
		if ( n < 1) {
			if (m_cols!=null){
				return new Table(m_cols);
			}else{
				return null;
			}
		}
		
		//1���л������
		if (m_bufTable!=null){
			vTbl = new Table(m_bufTable.dataStruct());
			if (m_bufTable.length()>n){ //1.1�����㹻����
				for(int i=0; i<n; i++){
					vTbl.add(m_bufTable.get(i+1));					
				}
				for(int i=n; i>0; i--){
					m_bufTable.delete(i);
				}
				return vTbl;
			}else{			//1.2. ����������	
				n -= m_bufTable.length();
				vTbl.addAll(m_bufTable);
				m_bufTable.clear();		
				m_bufTable = null;
			}
		}
		
		//2. ��ѯ�����ݡ�
		int bufSize = 0;
		while (m_cmd!=null && n > 0 && cursorId>0) {
			Table buf = runCommand(m_db, m_cmd);			
			if (buf==null){
				break;
			}
			if (vTbl==null){
				vTbl = new Table(buf.dataStruct());
			}
			bufSize = buf.length();
			vTables[0] = vTbl;
			if(mergeTable(vTables, buf, n)){	
				vTbl = vTables[0];
				n -= bufSize;
				break;
			}
			
			vTbl = vTables[0];
			n -= bufSize;
		}
		
		if (n > 0) {
			close();
		}
		if (vTbl==null && m_cols!=null){
			vTbl = new Table(m_cols);
		}

		return vTbl;
	}
	
	private void doCursorData(MongoDatabase db, Document docs) {
		BaseRecord r = null;
		Object obj = null;
		Document cur = (Document)docs.get("cursor");
				
		if (cur==null){
			r = ImCursor.parse(docs);
		}else{
			r = ImCursor.parse(cur);
		}
		
		if (r.dataStruct().getFieldIndex("firstBatch")>-1){
			obj = r.getFieldValue("firstBatch");
			if (obj instanceof Sequence){
				if ( ((Sequence)obj).length()==0){
					obj = r;
				}
			}
		}
		
		long pid = cur.getLong("id");
		if (obj!=null && pid>0){
			String collectName = cur.getString("ns");
			collectName= collectName.replace(db.getName()+".", "");
			m_cmd = String.format("{'getMore':NumberLong('%d'), 'collection':'%s', batchSize:101 }", pid, collectName);
			//cmd[1] = String.format("{'killCursors':'%s', 'cursors':[ NumberLong('%d')]}", collectName, pid);
		}
		
		if (obj instanceof Table){
			m_bufTable = (Table)obj;
		}
	}
	
	
	//��������ϲ����ֶβ�һ��ʱ�ֶ����ݶ���
	//��bufTbl�ϲ���vTbls��,�Ƚ�����ṹ����Ϊһ�£��ٹ������ݡ�
	private boolean mergeTable(Table[] vTbls, Table bufTbl, int n) {
		boolean bBreak = false;
		Table vTbl = vTbls[0];
		//A.��ͬ�ṹ�ϲ� 
		if (Arrays.equals(vTbl.dataStruct().getFieldNames(), bufTbl.dataStruct().getFieldNames())){
			;//skip
		//B.�����ṹ�ϲ� (vTbl>bufTbl)��ֻ����bufTbl����
		}else if(isColumnContain(vTbl.dataStruct().getFieldNames(), bufTbl.dataStruct().getFieldNames())){
			Table tmpTable = new Table(vTbl.dataStruct());
			modifyTableData(tmpTable, bufTbl);
			bufTbl.clear();
			bufTbl.addAll(tmpTable);
			tmpTable.clear();
		//C. ��ͬ�ṹ�ϲ�
		}else{ 
			String[] newCols = mergeColumns(vTbl.dataStruct().getFieldNames(), 
											bufTbl.dataStruct().getFieldNames());
			Table tmpTable = new Table(newCols);
			IArray mems = vTbl.getMems();
			
			// oldData
			BaseRecord r = null;
			for(int i=0; i<mems.size(); i++){
				r = (BaseRecord)mems.get(i+1);
				tmpTable.newLast(r.getFieldValues());
			}
			vTbl.clear();
			vTbl=new Table(newCols);
			vTbl.addAll(tmpTable);
			tmpTable.clear();
			// newData
			modifyTableData(tmpTable, bufTbl);
			bufTbl.clear();
			bufTbl.addAll(tmpTable);
			tmpTable.clear();
		}
		
		if (bufTbl.length()>n){ //2.1 ���㹻����
			for(int i=0; i<n; i++){
				BaseRecord r = bufTbl.getRecord(i+1);
				vTbl.newLast(r.getFieldValues());
			}
			for(int i=n; i>0; i--){
				bufTbl.delete(i);
			}
			//2.2 ʣ����뻺��.
			m_bufTable = new Table(bufTbl.dataStruct());
			m_bufTable.addAll(bufTbl);
			//return vTbl;
			bBreak = true;
		}else{			//2.3  ����������	
			vTbl.addAll(bufTbl);
			n -= bufTbl.length();
			bufTbl.clear();				
		}
		
		vTbls[0] = vTbl;
		return bBreak;
	}

	protected void finalize() throws Throwable {
		close();
	}
	
	//���ı�ṹ�������ɱ�������䵽�µ��С�
	private void modifyTableData(Table newTbl, Table oldTbl)
	{
		Object[] subLine = null;
		String[] newCols=newTbl.dataStruct().getFieldNames();
		String[] cols = oldTbl.dataStruct().getFieldNames();
		subLine = new Object[newCols.length];
		for(int i=0; i<oldTbl.length(); i++){
			BaseRecord rcd = oldTbl.getRecord(i+1);
			for(int j=0; j<newCols.length; j++){	
				if (ArrayUtils.contains(cols, newCols[j])) {
					 subLine[j] = rcd.getFieldValue(newCols[j]);
				}
			}
			newTbl.newLast(subLine);
		}
	}
	
	private int runCommandSkip(MongoDatabase db, String cmd){
		if (cmd==null) {
			return 0;
		}

		try{
			Document command = null;
			command = Document.parse(cmd);
			//System.out.println("skip thread:"+Thread.currentThread().getId()+"; db="+db+"; cmd="+cmd+"; pid="+cursorId);
			Document docs = null;
			synchronized (m_lock){
				docs = db.runCommand( command);			
			}
			double dVal = docs.getDouble("ok");
			if (dVal==0){
				return 0;
			}
			Document cur = (Document)docs.get("cursor");
			cursorId = cur.getLong("id");
			
			return getDocumentCount(cur);
		}catch(Exception e){
			Logger.error(e.getMessage());
		}
		
		return 0;
	} 
	
	public Table runCommand(MongoDatabase db, String cmd) {
		Table tbl = null;
		try{		
			if (cmd==null) {
				return null;
			}
			
			Object obj = null;
			Document command = null;
			command = Document.parse(cmd);
			
			//System.out.println("work thread:"+Thread.currentThread().getId()+"; db="+db+"; cmd="+cmd+"; pid="+cursorId);
			
			Document docs = null;
			synchronized (m_lock){
				docs = db.runCommand(command);
			}
			double dVal = docs.getDouble("ok");
			if (dVal==0){
				return tbl;
			}
	
			Document cur = (Document)docs.get("cursor");
			cursorId = cur.getLong("id");
			//System.out.println("db="+db+"; cmd="+cmd+"; pid="+cursorId);
			BaseRecord rcd = parse(cur);

			if (rcd.dataStruct().getFieldIndex("firstBatch")>-1){
				obj = rcd.getFieldValue("firstBatch");
			}else if (rcd.dataStruct().getFieldIndex("nextBatch")>-1){
				obj = rcd.getFieldValue("nextBatch");
			}
			if (obj instanceof Table){
				tbl = (Table)obj;
				if (m_cols==null){
					m_cols = tbl.dataStruct().getFieldNames();
				}
			}
		}catch(Exception e){
			Logger.error(e.getMessage());
		}
		
		return tbl;
	}
	
	public static BaseRecord parse(Document doc){
		int idx = 0;
		Set<String>set = doc.keySet();
		String[] curCols = set.toArray(new String[set.size()]);
		DataStruct ds1 = new DataStruct(curCols);
		
		Object[] line = new Object[set.size()];;
		for(String k:set){
			Object val = doc.get(k);			
			if (val instanceof Document){				
				Object subObj = parse((Document)val);
				line[idx++] = subObj;
			}else if(val instanceof List){
				List<?> list = (List<?>)val;
				if (list.size()==0){
					idx++;
					continue;
				}
				Object o = ((List<?>)val).get(0);
				// List<Document>�ṹ
				if (o instanceof Document){
					Table subNode = null;
					BaseRecord subRec = null;
					List<Document> dlist = (List<Document>)val;
					
					for(Document sub:dlist){
						if (subNode == null){
							subRec = parse(sub);
							subNode = new Table(subRec.dataStruct());
							subNode.newLast(subRec.getFieldValues());
						}else{
							subRec = parse(sub);	
							subNode = doRecord(subNode, subRec);
						}
					}
					line[idx++] = subNode;
				}else{ // List<Object>�ṹ
					Object[] objs = list.toArray(new Object[list.size()]);
					for(int i=0;i<objs.length; i++ ){
						if (objs[i] instanceof ObjectId){
							objs[i] = objs[i].toString();
						}
					}
					Sequence seq = new Sequence(objs);					
					line[idx++] = seq;
				}
			}else{
				if (val instanceof ObjectId){
					line[idx++] = val.toString();	
				}else{
					line[idx++] = val;	
				}
			}
		}

		BaseRecord rcd = new Record(ds1,line);
		return rcd;
	}
	
	//doc��Ӧ�ļ�¼��
	private int getDocumentCount(Document doc){
		int idx = 0;
		Object val = null;
		if (doc.containsKey("nextBatch")){
			val = doc.get("nextBatch");	
		}else if (doc.containsKey("firstBatch")){
			val = doc.get("firstBatch");	
		}
				
		if (val instanceof Document){				
			idx++;
		}else if(val instanceof List){
			List<?> list = (List<?>)val;
			idx+=list.size();
		}else{
			idx++;
		}
		
		return idx;
	}
	
	//������ԭ˳��ȥ�غϲ�����
	private static String[] mergeColumns(String[] oldArr, String[] newArr){
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
       
        for (String anOldArr : oldArr) {
           map.put(anOldArr, 1);
        }
 
        for (String aNewArr : newArr) {
        	map.put(aNewArr, 1);
        }
        
        Set<String> set = map.keySet();
        String[] ss = set.toArray(new String[set.size()]);
        return ss;
    }
	
	//����oldDs�Ƿ����newDs
	private static boolean isColumnContain(String[] oldArr, String[] newArr){
		boolean bRet = true;
		if (oldArr.length<newArr.length){
			bRet = false;
		}else{
			for(int i=0; i<newArr.length; i++){
				if (!ArrayUtils.contains(oldArr, newArr[i])){
					bRet = false;
					break;
				}
			}
		}
		
		return bRet;
	}
	
	//������¼�ϲ����ֶβ�һ��ʱ�ֶ����ݶ���
	private static Table doRecord(Table subNode, BaseRecord subRec) {
		Table ret = null;
		// 1.�ṹ��ͬ��
		if (subNode.dataStruct().isCompatible(subRec.dataStruct())){
			subNode.newLast(subRec.getFieldValues());	
			ret = subNode;			
		//2. �ṹ������ϵ, ֱ��׷�����ݲ��ֶζ���
		}else if(isColumnContain(subNode.dataStruct().getFieldNames(), 
				subRec.dataStruct().getFieldNames())){
			// newData
			appendRecord(subNode, subRec);
			ret = subNode;		
		}else{//3. �ṹ��ͬʱ���ع����
			String[] newCols = mergeColumns(subNode.dataStruct().getFieldNames(), 
									 subRec.dataStruct().getFieldNames());
			Table newTable = new Table(newCols);
			IArray mems = subNode.getMems();
			
			// oldData
			BaseRecord r = null;
			for(int i=0; i<mems.size(); i++){
				r = (BaseRecord)mems.get(i+1);
				newTable.newLast(r.getFieldValues());
			}
			// newData
			Object[] subLine = null;
			String[] cols = subRec.getFieldNames();
			subLine = new Object[newCols.length];
			for(int j=0; j<newCols.length; j++){	
				 if (ArrayUtils.contains(cols, newCols[j])) {
					 subLine[j] = subRec.getFieldValue(newCols[j]);
				 }
			}
			newTable.newLast(subLine);

			ret = newTable;
			newTable = null;
		}
		subNode = null;
		return ret;
	}
	
	//��ṹ������¼�ṹ����£�����¼׷�ӵ����У�
	private static void appendRecord(Table vTbl, BaseRecord subRec){
		Object[] subLine = null;
		String[] fullCols = vTbl.dataStruct().getFieldNames();

		String[] cols = subRec.getFieldNames();
		subLine = new Object[fullCols.length];
		for(int j=0; j<fullCols.length; j++){	
			 if (ArrayUtils.contains(cols, fullCols[j])) {
				 subLine[j] = subRec.getFieldValue(fullCols[j]);
			 }
		}
		vTbl.newLast(subLine);
	}
}
