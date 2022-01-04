package com.scudata.util;

import java.util.ArrayList;

import com.scudata.dm.DataStruct;
import com.scudata.dm.Record;
import com.scudata.dm.Sequence;

/**
 * XML��ʽ���Ľڵ�
 * @author RunQian
 *
 */
class XMLNode {
	private String name; // ����
	private XMLNode parent; // ���ڵ�
	private ArrayList<XMLNode> subList; // �ӽڵ��б�
	private String characters; // �ڵ�ֵ�ַ���
	//private Object value;
	
	public XMLNode(String name, XMLNode parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}
	
	public XMLNode getParent() {
		return parent;
	}
	
	/*public void setValue(Object val) {
		this.value = val;
	}*/
	
	public void addCharacters(String str) {
		if (characters == null) {
			characters = str;
		} else {
			characters += str;
		}
	}
	
	public void addSub(XMLNode sub) {
		if (subList == null) {
			subList = new ArrayList<XMLNode>();
		}
		
		subList.add(sub);
	}
	
	public boolean euqalName(String name) {
		return this.name.equals(name);
	}
	
	public Object getResult() {
		ArrayList<XMLNode> subList = this.subList;
		if (subList == null) {
			return Variant.parse(characters, true);
		}
		
		ArrayList<String> nameList = new ArrayList<String>();
		int size = subList.size();
		for (XMLNode sub : subList) {
			if (!nameList.contains(sub.name)) {
				nameList.add(sub.name);
			}
		}
		
		int count = nameList.size();
		String []names = new String[count];
		nameList.toArray(names);
		DataStruct ds = new DataStruct(names);
		Record r = new Record(ds);
		
		if (count == size) {
			for (int i = 0; i < size; ++i) {
				XMLNode sub = subList.get(i);
				r.setNormalFieldValue(i, sub.getResult());
			}
		} else if (count == 1) {
			Sequence seq = new Sequence(size);
			for (int i = 0; i < size; ++i) {
				XMLNode sub = subList.get(i);
				seq.add(sub.getResult());
			}
			
			r.setNormalFieldValue(0, seq);
		} else {
			for (int i = 0; i < count; ++i) {
				String name = names[i];
				Sequence seq = new Sequence();
				for (XMLNode sub : subList) {
					if (sub.euqalName(name)) {
						seq.add(sub.getResult());
					}
				}
				
				if (seq.length() > 1) {
					r.setNormalFieldValue(i, seq);
				} else {
					r.setNormalFieldValue(i, seq.get(1));
				}
			}
		}
		
		return r;
	}
}
