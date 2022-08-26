package com.scudata.dm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.common.StringUtils;
import com.scudata.parallel.UnitClient;
/**
 * "ip:port"
 * ":port"������127.0.0.1
 * ":"�������Լ�����
 * ""�������Լ�����
 *
 */
public class Machines implements Externalizable{
	private String []hosts;
	private int []ports;
	
	public Machines() {
	}
	
	private String parseHost(String str) {
		if(!StringUtils.isValidString(str) || str.length()==1) {//�մ����߹�ð�Ż��߷Ƿ�����ʱ����������
			return null;
		}
		String host=null,defaultHost="127.0.0.1";
		int index = str.lastIndexOf(':');
		if (index == -1 || index==0) {//û��ð�Ż���ð�ſ�ͷʱ����ʾʹ��ȱʡ���طֻ��Ķ˿�
			host = defaultHost;
		}else {
			host = str.substring(0, index);
		}
		return host;
	}
	private int parsePort(String str) {
		if(!StringUtils.isValidString(str) || str.length()==1) {
			return 0;
		}
		int index = str.lastIndexOf(':');
		int port = 0;
		if (index == -1) {//û��ð��ʱ����ʾʹ��ȱʡ���طֻ��Ķ˿�
			port = Integer.parseInt(str);
		}else {
			port = Integer.parseInt(str.substring(index + 1));
		}
		return port;
	}
	
	public boolean set(Object o) {
		if (o == null) {
			//MessageManager mm = EngineMessage.get();
			//throw new RQException(errorMsg + mm.getMessage("function.invalidParam"));
		} else if (o instanceof String) {
			String str = (String)o; // ip:port
			hosts = new String[]{ parseHost(str) };
			ports = new int[]{ parsePort(str) };
		} else if (o instanceof Sequence) {
			Sequence seq = (Sequence)o;
			int count = seq.length();
			if (count == 0) {
				return false;
			}

			hosts = new String[count];
			ports = new int[count];
			for (int i = 0; i < count; ++i) {
				Object obj = seq.get(i + 1);
				if (obj instanceof String || obj==null) {
					String str = (String)obj; // ip:port
					hosts[i] = parseHost(str);
					ports[i] = parsePort(str);
				}else if(obj instanceof UnitClient){
					UnitClient uc = (UnitClient)obj;
					hosts[i] = uc.getHost();
					ports[i] = uc.getPort();
				}else{
					//����ʶ�Ķ���
					return false;
				}

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