package com.scudata.dm.query.utils;

import java.util.Comparator;

import com.scudata.common.RQException;

public class NameComprator implements Comparator<String>
{
	public int compare(String NameA, String NameB) 
	{
		if(NameA == null || NameB == null)
		{
			throw new RQException("�ַ���Ϊ��ֵ�޷��Ƚϴ�С");
		}
		//��������ǰ�棬�̵����ں���
        if(NameA.length() < NameB.length())
        {
        	return 1;
        }
        else if(NameA.length() > NameB.length())
        {
        	return -1;
        }
        else
        {
        	return 0;
        }
	}
}
