package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.common.StringUtils;
import com.scudata.parallel.UnitClient;
import com.scudata.resources.EngineMessage;
/**
 * "ip:port"
 * ":port"������127.0.0.1
 * ":"�������Լ�����
 * ""������
 *
 */
public class Machines implements Externalizable{
	private String []hosts;
	private int []ports;
	
	public Machines() {
	}
	
	private String parseHost(String str) {
		String host=null,defaultHost="127.0.0.1";
		int index = str.lastIndexOf(':');
		if (index == -1) {//û��ð��
			MessageManager mm = EngineMessage.get();
			throw new RQException("HS" + mm.getMessage("function.invalidParam")+": "+str);
		}else if( index==0 ) {//ð�ſ�ͷΪ���طֻ�
			if(str.length()>1) {
				host = defaultHost;
			}//����Ϊ��ð�ű�ʾ�����̣���null��host
		}else {
			host = str.substring(0, index);
		}
		return host;
	}
	private int parsePort(String str) {
		int index = str.lastIndexOf(':');
		if(str.length()==1) {//��ð��ʱ������0�˿�
			return 0;
		}
		int port = Integer.parseInt(str.substring(index + 1));
		return port;
	}
	
	public boolean set(Object o) {
		if (o == null) {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(errorMsg + mm.getMessage("function.invalidParam"));
		} else if (o instanceof String) {
			String str = (String)o; // ip:port
			if(!StringUtils.isValidString(str)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("HS" + mm.getMessage("function.invalidParam"));
			}
			hosts = new String[]{ parseHost(str) };
			ports = new int[]{ parsePort(str) };
		} else if (o instanceof Sequence) {
			Sequence seq = (Sequence)o;
			int count = seq.length();
			if (count == 0) {
				return false;
			}

			ArrayList<String> listH = new ArrayList<String>();
			ArrayList<Integer>listP = new ArrayList<Integer>();
			for (int i = 0; i < count; ++i) {
				Object obj = seq.get(i + 1);
				if (obj==null) {
					continue;
				}else if(obj instanceof String) {
					String str = (String)obj; // ip:port
					if(!StringUtils.isValidString(str)) {
						continue;
					}
					listH.add( parseHost(str) );
					listP.add( parsePort(str) );
				}else if(obj instanceof UnitClient){
					UnitClient uc = (UnitClient)obj;
					listH.add( uc.getHost() );
					listP.add( uc.getPort() );
				}else{
					//����ʶ�Ķ���
					return false;
				}
			}
			hosts = new String[listH.size()];
			ports = new int[listH.size()];
			for(int i=0;i<hosts.length;i++) {
				hosts[i] = listH.get(i);
				ports[i] = listP.get(i);
			}
			
		} else {
			return false;
		}

		return true;
	}
	
	public int size(){
		if(hosts==null) return 0;
		return hosts.length;
	}
	
	public String getHost(int i) {
		return hosts[i];
	}
	
	public String[] getHosts() {
		return hosts;
	}
	
	public int getPort(int i) {
		return ports[i];
	}
	
	public int[] getPorts() {
		return ports;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		hosts = (String[])in.readObject();
		ports = (int[])in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(hosts);
		out.writeObject(ports);
	}
}