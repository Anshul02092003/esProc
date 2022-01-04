package com.scudata.parallel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.scudata.dm.Context;
import com.scudata.dm.JobSpace;
import com.scudata.dm.JobSpaceManager;

/**
 * �ڵ����Ⱥ
 * @author RunQian
 *
 */
public class Cluster implements Externalizable {
	private static final long serialVersionUID = 1L;
	
	private String []hosts; // ip����
	private int []ports; // �˿�����
	private Context ctx; // ����������
	
	// ���л���ʹ��
	public Cluster() {
	}
	
	/**
	 * �����ڵ����Ⱥ
	 * @param hosts ip����
	 * @param ports �˿�����
	 * @param ctx ����������
	 */
	public Cluster(String[] hosts, int[] ports, Context ctx) {
		this.hosts = hosts;
		this.ports = ports;
		this.ctx = ctx;
		for(int i=0;i<hosts.length;i++){
			ctx.getJobSpace().addHosts(hosts[i], ports[i]);
		}
	}
	
	public boolean isEquals(Cluster other) {
		String []h1 = hosts;
		String []h2 = other.hosts;
		int count = h1.length;
		if (count == h2.length) {
			int []p1 = ports;
			int []p2 = other.ports;
			for (int i = 0; i < count; ++i) {
				if (!h1[i].equals(h2[i]) || p1[i] != p2[i]) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ȡ�ڵ������
	 * @return int
	 */
	public int getUnitCount() {
		return hosts.length;
	}
	
	public void setHosts(String[] hosts, int[] ports) {
		this.hosts = hosts;
		this.ports = ports;
	}

	public String[] getHosts() {
		return hosts;
	}
	
	//�������ȡ�ֻ�
	public String getHost(int unit) {
		return hosts[unit];
	}
	
	public int[] getPorts() {
		return ports;
	}
	
	public int getPort(int unit) {
		return ports[unit];
	}
	
	public Context getContext() {
		return ctx;
	}
	
	public String getJobSpaceId() {
		return ctx.getJobSpace().getID();
	}
	
	// ȡ�������̹��ɵļ�Ⱥ�����û�����÷ֽ����򷵻�null
	public static Cluster getHostCluster() {
		return null;
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(hosts);
		out.writeObject(ports);
		out.writeObject(getJobSpaceId());
	}
	
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		hosts = (String [])in.readObject();
		ports = (int [])in.readObject();
		String jobSpaceID = (String)in.readObject();
		JobSpace js = JobSpaceManager.getSpace(jobSpaceID);
		ctx = new Context();
		ctx.setJobSpace(js);
	}
}